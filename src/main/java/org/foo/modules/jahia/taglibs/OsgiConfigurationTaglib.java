package org.foo.modules.jahia.taglibs;

import org.jahia.osgi.BundleUtils;
import org.jahia.taglibs.ValueJahiaTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public final class OsgiConfigurationTaglib extends ValueJahiaTag {
    private static final Logger logger = LoggerFactory.getLogger(OsgiConfigurationTaglib.class);
    private static final long serialVersionUID = 620377504883338760L;

    private String factoryPid;
    private String property;
    private String varIsMultivalued;

    public void setFactoryPid(String factoryPid) {
        this.factoryPid = factoryPid;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public void setVarIsMultivalued(String varIsMultivalued) {
        this.varIsMultivalued = varIsMultivalued;
    }

    @Override
    public int doStartTag() {
        List<Object> values = BundleUtils.getOsgiService(OsgiConfigurationService.class, null)
                .getPropertyFromConfiguration(factoryPid, property);

        Object valueToSet;
        if (values.isEmpty()) {
            valueToSet = null;
        } else if (values.size() == 1) {
            valueToSet = values.get(0);
        } else {
            valueToSet = values;
        }

        if (varIsMultivalued != null) {
            pageContext.setAttribute(varIsMultivalued, !values.isEmpty() && values.size() > 1);
        }

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
