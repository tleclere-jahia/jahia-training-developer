package org.foo.modules.jahia.filters;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.services.render.filter.RenderFilter;
import org.osgi.service.component.annotations.Component;

@Component(service = RenderFilter.class)
public class EmployeeNameFilter extends AbstractFilter {

    public EmployeeNameFilter() {
        setApplyOnNodeTypes("foont:employee");
        setApplyOnModes("preview,live");
        setApplyOnTemplates("default");
        setApplyOnTemplateTypes("html");
        setPriority(17f);
    }

    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) {
        renderContext.getRequest().setAttribute("fullname",
                resource.getNode().getPropertyAsString("firstname") + " " + resource.getNode().getPropertyAsString("lastname"));
        return null;
    }
}
