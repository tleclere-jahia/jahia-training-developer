package org.foo.modules.jahia.filters;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.EndTag;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Source;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.services.render.filter.RenderFilter;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Component(service = RenderFilter.class)
public class HtmlGenerationInfoFilter extends AbstractFilter {
    private static final Logger logger = LoggerFactory.getLogger(HtmlGenerationInfoFilter.class);

    private static final String ATTRIBUTE_NAME = "startime";

    public HtmlGenerationInfoFilter() {
        setApplyOnConfigurations(Resource.CONFIGURATION_PAGE);
        setPriority(10.1f);
    }

    @Activate
    private void onActivate(BundleContext bundleContext) {
        addCondition((renderContext, resource) -> renderContext.getSite().getInstalledModules().contains(bundleContext.getBundle().getSymbolicName()));
    }

    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) {
        long startTime = System.currentTimeMillis();
        renderContext.getRequest().setAttribute(ATTRIBUTE_NAME, startTime);
        return null;
    }

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) {
        long startTime = (long) renderContext.getRequest().getAttribute(ATTRIBUTE_NAME);
        double timeEllapsed = (System.currentTimeMillis() - startTime) / 1000f;
        logger.info("Time ellapsed: {}s", String.format("%.2f", timeEllapsed));

        Source source = new Source(previousOut);
        OutputDocument outputDocument = new OutputDocument(source);
        List<Element> bodyElementList = source.getAllElements(HTMLElementName.BODY);
        if (!bodyElementList.isEmpty()) {
            Element bodyElement = bodyElementList.get(bodyElementList.size() - 1);
            EndTag bodyEndTag = bodyElement.getEndTag();
            outputDocument.replace(bodyEndTag.getBegin(), bodyEndTag.getBegin() + 1,
                    "<!-- Time ellapsed: " + String.format("%.2f", timeEllapsed) + "s --><");
        }

        return outputDocument.toString();
    }
}
