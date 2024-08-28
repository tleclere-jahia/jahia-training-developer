package org.foo.modules.jahia.cache;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.cache.CacheKeyPartGenerator;
import org.jahia.utils.Patterns;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import java.util.Arrays;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component(service = CacheKeyPartGenerator.class)
public class CookieCacheKeyPartGenerator implements CacheKeyPartGenerator {
    private static final Logger logger = LoggerFactory.getLogger(CookieCacheKeyPartGenerator.class);

    private static final String KEY = "_cookie_";
    private static final Pattern COOKIE_NAMES_REGEXP = Pattern.compile("(.*)(" + KEY + "\\[([^\\]]+)\\]_)(.*)");

    @Override
    public String getKey() {
        return "cookie";
    }

    @Override
    public String getValue(Resource resource, RenderContext renderContext, Properties properties) {
        String cookieNames = properties.getProperty("cache.cookie");
        if (!StringUtils.isEmpty(cookieNames)) {
            return KEY + Arrays.toString(Patterns.COMMA.split(cookieNames)) + "_";
        }
        return StringUtils.EMPTY;
    }

    @Override
    public String replacePlaceholders(RenderContext renderContext, String keyPart) {
        Cookie[] cookies = renderContext.getRequest().getCookies();
        SortedMap<String, Object> qs = new TreeMap<>();
        if (cookies == null || cookies.length == 0) {
            return StringUtils.EMPTY;
        }
        Matcher m = COOKIE_NAMES_REGEXP.matcher(keyPart);
        if (m.matches()) {
            String qsString = m.group(2);
            String[] cookieNames = Patterns.COMMA.split(m.group(3));

            for (String cookieName : cookieNames) {
                cookieName = cookieName.trim();
                if (cookieName.endsWith("*")) {
                    cookieName = cookieName.substring(0, cookieName.length() - 1);
                    for (Cookie cookie : cookies) {
                        if (StringUtils.startsWith(cookie.getName(), cookieName)) {
                            qs.put(cookie.getName(), cookie.getValue());
                        }
                    }
                } else {
                    final String cName = cookieName;
                    Arrays.stream(cookies).filter(c -> StringUtils.startsWith(c.getName(), cName)).findFirst()
                            .ifPresent(value -> qs.put(value.getName(), value.getValue()));
                }
            }
            keyPart = keyPart.replace(qsString, qs.isEmpty() ? StringUtils.EMPTY : qs.toString());
        }
        return keyPart;
    }
}
