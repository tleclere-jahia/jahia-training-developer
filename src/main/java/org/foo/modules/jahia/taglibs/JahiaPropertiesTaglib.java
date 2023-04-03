package org.foo.modules.jahia.taglibs;

import org.jahia.api.settings.SettingsBean;
import org.jahia.osgi.BundleUtils;
import org.jahia.taglibs.ValueJahiaTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public final class JahiaPropertiesTaglib extends ValueJahiaTag {
    private static final Logger logger = LoggerFactory.getLogger(OsgiConfigurationTaglib.class);
    private static final long serialVersionUID = -7003920708028255020L;

    private String property;

    public void setProperty(String property) {
        this.property = property;
    }

    @Override
    public int doStartTag() {
        String valueToSet = BundleUtils.getOsgiService(SettingsBean.class, null).getPropertyValue(property);
        if (getVar() != null) {
            pageContext.setAttribute(getVar(), valueToSet);
        } else {
            try {
                pageContext.getOut().print(valueToSet);
            } catch (IOException e) {
                logger.error("", e);
            }
        }

        return SKIP_BODY;
    }
}
