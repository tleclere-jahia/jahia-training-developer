package org.foo.modules.jahia.actions;

import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Component(service = Action.class)
public class AjaxFormAction extends Action {
    private static final Logger logger = LoggerFactory.getLogger(AjaxFormAction.class);

    public AjaxFormAction() {
        setName("ajaxForm");
        setRequireAuthenticatedUser(false);
    }

    @Override
    public ActionResult doExecute(HttpServletRequest httpServletRequest, RenderContext renderContext, Resource resource, JCRSessionWrapper jcrSessionWrapper, Map<String, List<String>> parameters, URLResolver urlResolver) {
        logger.info("Ajax form action: {}", parameters);
        return ActionResult.OK_JSON;
    }
}
