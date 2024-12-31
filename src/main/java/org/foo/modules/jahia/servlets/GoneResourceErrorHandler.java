package org.foo.modules.jahia.servlets;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.errors.ErrorHandler;
import org.jahia.services.render.URLResolver;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

@Component(service = ErrorHandler.class)
public class GoneResourceErrorHandler implements ErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(GoneResourceErrorHandler.class);

    private String[] vanityUrls;
    private String[] pathes;

    @Activate
    private void onActivate(Map<String, ?> params) {
        pathes = StringUtils.split((String) params.get("pathes"), ";");
        if (pathes == null) {
            pathes = new String[0];
        }
        vanityUrls = StringUtils.split((String) params.get("vanityUrls"), ";");
        if (vanityUrls == null) {
            vanityUrls = new String[0];
        }
    }

    @Override
    public boolean handle(Throwable e, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        URLResolver urlResolver = (URLResolver) request.getAttribute("urlResolver");
        if (urlResolver == null) {
            logger.warn("URLResolver not found: {}", request.getRequestURI());
            return false;
        }

        String path = urlResolver.getPath();
        if (Arrays.asList(vanityUrls).contains(path) || Arrays.asList(pathes).contains(path)) {
            response.sendError(HttpServletResponse.SC_GONE, e.getMessage());
            return true;
        }
        return false;
    }
}
