package org.foo.modules.jahia.actions;

import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@Component(service = Action.class, immediate = true)
public class HelloAction extends Action {
    public static final Logger logger = LoggerFactory.getLogger(HelloAction.class);

    public HelloAction() {
        setName("hi");
        setRequireAuthenticatedUser(false);
        setRequiredMethods("GET,POST");
        setRequiredPermission("sayHi");
    }

    @Override
    public ActionResult doExecute(HttpServletRequest httpServletRequest, RenderContext renderContext, Resource resource, JCRSessionWrapper jcrSessionWrapper, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        String firstname = resource.getNode().getPropertyAsString("firstname");
        String lastname = resource.getNode().getPropertyAsString("lastname");
        logger.info("Hi {} {}!", firstname, lastname);
        if (parameters.containsKey("ajax")) {
            JSONObject jsonData = new JSONObject();
            jsonData.put("firstname", firstname);
            jsonData.put("lastname", lastname);
            return new ActionResult(HttpServletResponse.SC_OK, null, jsonData);
        }
        return new ActionResult(HttpServletResponse.SC_OK, renderContext.getSite().getHome().getPath());
    }
}
