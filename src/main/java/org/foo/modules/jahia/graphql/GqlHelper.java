package org.foo.modules.jahia.graphql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jahia.osgi.BundleUtils;
import org.jahia.services.securityfilter.PermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class GqlHelper {
    private static final Logger logger = LoggerFactory.getLogger(GqlHelper.class);

    private GqlHelper() {
        // Nothing to do
    }

    public static void execute(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String query, Map<String, ?> variables) throws IOException, ServletException {
        BundleUtils.getOsgiService(PermissionService.class, null).addScopes(Collections.singleton("graphql"), httpServletRequest);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> jsonObject = new HashMap<>();
        jsonObject.put("query", query);
        if (variables != null && !variables.isEmpty()) {
            jsonObject.put("variables", objectMapper.writeValueAsString(variables));
        }

        BundleUtils.getOsgiService(HttpServlet.class, "(component.name=graphql.kickstart.servlet.OsgiGraphQLHttpServlet)")
                .service(new HttpServletRequestWrapper(httpServletRequest) {
                    @Override
                    public BufferedReader getReader() throws IOException {
                        return new BufferedReader(new StringReader(objectMapper.writeValueAsString(jsonObject)));
                    }

                    @Override
                    public String getParameter(String name) {
                        if ("query".equals(name)) {
                            return query;
                        }
                        if ("variables".equals(name) && variables != null && !variables.isEmpty()) {
                            try {
                                return objectMapper.writeValueAsString(variables);
                            } catch (JsonProcessingException e) {
                                logger.error("", e);
                            }
                        }
                        return super.getParameter(name);
                    }
                }, httpServletResponse);
    }
}
