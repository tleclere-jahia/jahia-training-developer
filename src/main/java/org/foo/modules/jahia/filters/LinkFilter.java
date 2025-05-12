package org.foo.modules.jahia.filters;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Source;
import org.apache.commons.lang.StringUtils;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.services.render.filter.RenderFilter;
import org.jahia.services.seo.urlrewrite.UrlRewriteService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

@Component(service = RenderFilter.class)
public class LinkFilter extends AbstractFilter {
    private static final Logger logger = LoggerFactory.getLogger(LinkFilter.class);

    public LinkFilter() {
        setPriority(2f);
        setApplyOnConfigurations(Resource.CONFIGURATION_PAGE);
    }

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) {
        Source source = new Source(previousOut);
        OutputDocument doc = new OutputDocument(source);
        String dataRef;
        JCRNodeWrapper refNode;
        for (Element a : source.getAllElements(HTMLElementName.A)) {
            dataRef = a.getAttributeValue("data-ref");
            if (dataRef != null) {
                try {
                    refNode = resource.getNode().getSession().getNodeByIdentifier(dataRef);
                    if (refNode != null) {
                        String href = null;
                        if (refNode.isNodeType("jnt:externalLink")) {
                            if (refNode.isNodeType("foomix:lienExterne")) {
                                String prefixe = StringUtils.defaultIfBlank(refNode.getPropertyAsString("prefixe"), "");
                                String finUrl = StringUtils.defaultIfBlank(refNode.getPropertyAsString("url"), "");
                                String debutUrl = "";
                                href = prefixe + debutUrl + finUrl;
                            } else {
                                href = refNode.getPropertyAsString("j:url");
                            }
                        } else if (refNode.isNodeType("jnt:nodeLink")) {
                            href = renderContext.getURLGenerator().getBase() + refNode.getProperty("j:node").getNode().getPath() + ".html";
                            try {
                                href = ((UrlRewriteService) SpringContextSingleton.getBean("UrlRewriteService")).rewriteOutbound(href, renderContext.getRequest(), renderContext.getResponse());
                            } catch (IOException | ServletException | InvocationTargetException e) {
                                logger.error("", e);
                            }
                        }
                        if (href != null) {
                            for (Attribute attribute : a.getAttributes()) {
                                if ("href".equals(attribute.getName())) {
                                    doc.replace(attribute.getValueSegment().getBegin(), attribute.getValueSegment().getEnd(), href);
                                }
                            }
                        }
                    }
                } catch (RepositoryException e) {
                    logger.error("", e);
                }
            }
        }
        return doc.toString();
    }
}
