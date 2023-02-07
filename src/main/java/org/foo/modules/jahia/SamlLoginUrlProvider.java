package org.foo.modules.jahia;

import org.jahia.exceptions.JahiaException;
import org.jahia.modules.jahiaauth.service.ConnectorConfig;
import org.jahia.modules.jahiaauth.service.SettingsService;
import org.jahia.params.valves.LoginUrlProvider;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

@Component(service = LoginUrlProvider.class)
public class SamlLoginUrlProvider implements LoginUrlProvider {
    private static final Logger logger = LoggerFactory.getLogger(SamlLoginUrlProvider.class);

    private JahiaSitesService jahiaSitesService;
    private SettingsService settingsService;

    @Reference
    private void setJahiaSitesService(JahiaSitesService jahiaSitesService) {
        this.jahiaSitesService = jahiaSitesService;
    }

    @Reference
    private void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

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
}
