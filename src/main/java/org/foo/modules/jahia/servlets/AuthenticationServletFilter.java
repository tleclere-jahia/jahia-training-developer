package org.foo.modules.jahia.servlets;

import org.apache.commons.lang3.StringUtils;
import org.jahia.bin.filters.AbstractServletFilter;
import org.jahia.exceptions.JahiaException;
import org.jahia.modules.jahiaauth.service.ConnectorConfig;
import org.jahia.modules.jahiaauth.service.JahiaAuthConstants;
import org.jahia.modules.jahiaauth.service.SettingsService;
import org.jahia.modules.jahiaoauth.service.JahiaOAuthService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component(service = AbstractServletFilter.class)
public class AuthenticationServletFilter extends AbstractServletFilter {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationServletFilter.class);
    private static final Pattern PATTERN_SITEKEY = Pattern.compile("/sites/([^/]+)/");

    @Reference
    private JahiaSitesService jahiaSitesService;
    @Reference
    private SettingsService settingsService;
    @Reference
    private JahiaOAuthService jahiaOAuthService;

    public AuthenticationServletFilter() {
        setMatchAllUrls(true);
        setOrder(99f);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Do nothing
    }

    private String getSiteKey(HttpServletRequest httpServletRequest) {
        try {
            JahiaSite jahiaSite = jahiaSitesService.getSiteByServerName(httpServletRequest.getServerName());
            if (jahiaSite != null) {
                return jahiaSite.getSiteKey();
            }
        } catch (JahiaException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("", e);
            }
        }

        Matcher pattern = PATTERN_SITEKEY.matcher(httpServletRequest.getRequestURI());
        if (pattern.matches()) {
            return pattern.group(0);
        }
        logger.debug("No site found in httpServletRequest {}", httpServletRequest.getRequestURL());
        return null;
    }

    private String getAuthorizationUrl(String siteKey, String sessionId) {
        if (siteKey == null) {
            return null;
        }
        ConnectorConfig connectorConfig = settingsService.getConnectorConfig(siteKey, "KeycloakApi");
        if (connectorConfig == null) {
            logger.debug("No connector config for site {}", siteKey);
            // fallback to systemsite
            connectorConfig = settingsService.getConnectorConfig(JahiaSitesService.SYSTEM_SITE_KEY, "KeycloakApi");
            if (connectorConfig == null) {
                logger.debug("No connector config for systemsite");
                // no configuration found
                return null;
            }
        }
        if (!connectorConfig.getBooleanProperty(JahiaAuthConstants.PROPERTY_IS_ENABLED)) {
            logger.debug("Connector config is not enabled");
            return null;
        }
        return jahiaOAuthService.getAuthorizationUrl(connectorConfig, sessionId, null);
    }

    private boolean isEnabled(HttpServletRequest httpServletRequest, String siteKey) {
        return !StringUtils.endsWith(httpServletRequest.getRequestURI(), "robots.txt") &&
                !StringUtils.endsWith(httpServletRequest.getRequestURI(), ".css") &&
                !StringUtils.contains(httpServletRequest.getRequestURI(), "/css/") &&
                !StringUtils.endsWith(httpServletRequest.getRequestURI(), ".ico") &&
                !StringUtils.contains(httpServletRequest.getRequestURI(), "error") &&
                !StringUtils.endsWith(httpServletRequest.getRequestURI(), ".keycloakOAuthCallbackAction.do") &&
                "Connect".equals(siteKey);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        String siteKey = getSiteKey(httpServletRequest);
        if (siteKey == null) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            if (JahiaUserManagerService.isGuest(JCRSessionFactory.getInstance().getCurrentUser()) && isEnabled(httpServletRequest, siteKey)) {
                String authorizationUrl = getAuthorizationUrl(siteKey, httpServletRequest.getRequestedSessionId());
                if (authorizationUrl == null) {
                    filterChain.doFilter(servletRequest, servletResponse);
                } else {
                    httpServletResponse.sendRedirect(authorizationUrl);
                }
            } else {
                filterChain.doFilter(servletRequest, servletResponse);
            }
        }
    }

    @Override
    public void destroy() {
        // Do nothing
    }
}
