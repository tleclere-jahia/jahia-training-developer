package org.foo.modules.jahia.webflow;

import org.foo.modules.jahia.taglibs.OrbeonTaglib;
import org.jahia.utils.ClassLoaderUtils;
import org.orbeon.oxf.fr.embedding.servlet.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Collections;

public class FlowHandler implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(FlowHandler.class);

    private static final long serialVersionUID = 1821008438718532468L;

    public void init(RequestContext requestContext) {
        logger.info("Init webflow {}", requestContext);
    }

    public String isReady(RequestContext requestContext) {
        logger.info("Is ready ?");
        return ClassLoaderUtils.executeWith(getClass().getClassLoader(), () -> {
            HttpServletRequest httpServletRequest = (HttpServletRequest) requestContext.getExternalContext().getNativeRequest();
            try (StringWriter writer = new StringWriter()) {
                API.embedPageJava(httpServletRequest, writer, "", Collections.emptyMap());
                return "ok";
            } catch (ClassCastException | IOException e) {
                httpServletRequest.getSession().removeAttribute(OrbeonTaglib.ORBEON_FORM_ATTRIBUTE);
                return "ko";
            }
        });
    }

    public String index() {
        logger.info("Webflow index");
        return "FAKE_DATA";
    }
}
