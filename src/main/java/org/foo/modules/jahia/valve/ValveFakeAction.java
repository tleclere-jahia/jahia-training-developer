package org.foo.modules.jahia.valve;

import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.notification.HttpClientService;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Component(service = Action.class, immediate = true)
public class ValveFakeAction extends Action {
    private HttpClientService httpClientService;

    @Reference
    private void setHttpClientService(HttpClientService httpClientService) {
        this.httpClientService = httpClientService;
    }

    public ValveFakeAction() {
        setName("valveFake");
        setRequiredMethods("GET,POST");
        setRequireAuthenticatedUser(false);
    }

    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) {
        Cookie[] cookies = req.getCookies();
        Map<String, String> headers;
        if (cookies != null) {
            headers = Collections.singletonMap("Cookie", Arrays.stream(cookies)
                    .map(cookie -> cookie.getName() + "=" + cookie.getValue()).collect(Collectors.joining(";")));
        } else {
            headers = Collections.emptyMap();
        }
        httpClientService.executeGet("http://docker.for.mac.localhost:3000/valve", headers);
        return ActionResult.OK;
    }
}
