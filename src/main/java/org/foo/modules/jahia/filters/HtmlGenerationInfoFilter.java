package org.foo.modules.jahia.filters;

import net.htmlparser.jericho.*;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.services.render.filter.RenderFilter;
import org.osgi.service.component.annotations.Component;

import java.util.List;

@Component(service = RenderFilter.class)
public class HtmlGenerationInfoFilter extends AbstractFilter {
    private long startTime;

    public HtmlGenerationInfoFilter() {
        setApplyOnMainResource(true);
        setApplyOnModes("preview,live");
        setPriority(10f);
    }

    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        startTime = System.currentTimeMillis();
        return null;
    }

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        double timeEllapsed = (System.currentTimeMillis() - startTime) / 1000f;

        Source source = new Source(previousOut);
        OutputDocument outputDocument = new OutputDocument(source);
        List<Element> bodyElementList = source.getAllElements(HTMLElementName.BODY);
        if (bodyElementList.size() > 0) {
            Element bodyElement = bodyElementList.get(bodyElementList.size() - 1);
            EndTag bodyEndTag = bodyElement.getEndTag();
            outputDocument.replace(bodyEndTag.getBegin(), bodyEndTag.getBegin() + 1,
                    "<pre>" + "Time ellapsed: " + String.format("%.2f", timeEllapsed) + "</pre><");
        }

        return outputDocument.toString();
    }
}
