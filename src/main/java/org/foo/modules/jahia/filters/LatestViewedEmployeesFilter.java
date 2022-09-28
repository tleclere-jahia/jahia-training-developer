package org.foo.modules.jahia.filters;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.services.render.filter.RenderFilter;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;

@Component(service = RenderFilter.class)
public class LatestViewedEmployeesFilter extends AbstractFilter {
    private static final Logger logger = LoggerFactory.getLogger(LatestViewedEmployeesFilter.class);

    private static final String SESSION_ATTRIBUTE = "latestViewedEmployees";
    private static final long MAX_ELEMENTS = 10;

    public LatestViewedEmployeesFilter() {
        setApplyOnNodeTypes("foont:employee");
        setApplyOnMainResource(true);
        setApplyOnConfigurations(Resource.CONFIGURATION_PAGE);
        setApplyOnModes("preview,live");
        setPriority(10);
    }

    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        LinkedList<String> latestViewedEmployees =
                (LinkedList<String>) renderContext.getRequest().getSession().getAttribute(SESSION_ATTRIBUTE);
        if (latestViewedEmployees == null) {
            latestViewedEmployees = new LinkedList<>();
        }
        String nodePath = resource.getNodePath();
        int i = latestViewedEmployees.indexOf(nodePath);
        if (i >= 0) {
            latestViewedEmployees.remove(i);
        }
        latestViewedEmployees.addFirst(nodePath);
        if (latestViewedEmployees.size() > MAX_ELEMENTS) {
            latestViewedEmployees.removeLast();
        }
        renderContext.getRequest().getSession().setAttribute(SESSION_ATTRIBUTE, latestViewedEmployees);
        return null;
    }
}
