package org.foo.modules.jahia.authentication;

import org.jahia.bin.Logout;
import org.jahia.exceptions.JahiaException;
import org.jahia.modules.jahiaauth.service.ConnectorConfig;
import org.jahia.modules.jahiaauth.service.SettingsService;
import org.jahia.params.valves.LoginUrlProvider;
import org.jahia.params.valves.LogoutUrlProvider;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

@Component(service = {LoginUrlProvider.class, LogoutUrlProvider.class})
public class SamlLoginLogoutUrlProvider implements LoginUrlProvider, LogoutUrlProvider {
    private static final Logger logger = LoggerFactory.getLogger(SamlLoginLogoutUrlProvider.class);

    @Reference
    private JahiaSitesService jahiaSitesService;
    @Reference
    private SettingsService settingsService;

    @Override
    public boolean hasCustomLoginUrl() {
        return true;
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

        JahiaSite site = jahiaSitesService.getDefaultSite();
        if (site == null) {
            return null;
        }
        return site.getSiteKey();
    }

    @Override
    public String getLoginUrl(HttpServletRequest httpServletRequest) {
        String siteKey = getSiteKey(httpServletRequest);
        if (siteKey == null) {
            return null;
        }
        ConnectorConfig connectorConfig = settingsService.getConnectorConfig(siteKey, "Saml");
        if (connectorConfig == null) {
            return null;
        }
        try {
            return jahiaSitesService.getSiteByKey(siteKey).getJCRLocalPath() + "/home.connectToSaml.do";
        } catch (JahiaException e) {
            logger.error("", e);
            return null;
        }
    }

    @Override
    public boolean hasCustomLogoutUrl() {
        return true;
    }

    @Override
    public String getLogoutUrl(HttpServletRequest request) {
        return Logout.getLogoutServletPath();
    }
}
