package org.foo.modules.jahia.interceptors;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Source;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.interceptor.BaseInterceptor;
import org.jahia.services.content.interceptor.RichTextInterceptor;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.HtmlTagAttributeTraverser;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component(service = BaseInterceptor.class, immediate = true)
public class LinkInterceptor extends RichTextInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(LinkInterceptor.class);

    private final HtmlTagAttributeTraverser linkTraverser;
    private static final Pattern PATTERN = Pattern.compile("##cms-context##/\\{mode}/\\{lang}/##ref:link([0-9]+)##.html");

    public LinkInterceptor() {
        linkTraverser = new HtmlTagAttributeTraverser(Collections.singletonMap("a", Collections.singleton("href")));
    }

    @Reference
    private JCRStoreService jcrStoreService;

    @Activate
    private void start() {
        jcrStoreService.addInterceptor(this);
    }

    @Deactivate
    private void stop() {
        jcrStoreService.removeInterceptor(this);
    }

    @Override
    public Value[] beforeSetValues(JCRNodeWrapper node, String name, ExtendedPropertyDefinition definition, Value[] originalValues) throws RepositoryException {
        Value[] res = new Value[originalValues.length];
        for (int i = 0; i < originalValues.length; i++) {
            Value originalValue = originalValues[i];
            res[i] = beforeSetValue(node, name, definition, originalValue);
        }
        return res;
    }

    @Override
    public Value beforeSetValue(JCRNodeWrapper node, String name, ExtendedPropertyDefinition definition, Value originalValue) throws RepositoryException {
        String content = originalValue.getString();
        String result = linkTraverser.traverse(content, (String value, RenderContext context, String tagName, String attrName, Resource resource) -> {
            Matcher matcher = PATTERN.matcher(value);
            if (matcher.matches()) {
                try {
                    JCRNodeWrapper refNode;
                    if (definition.isInternationalized()) {
                        refNode = node.getNode("j:referenceInField_" + name + "_" + node.getSession().getLocale() + "_" + matcher.group(1));
                    } else {
                        refNode = node.getNode("j:referenceInField_" + name + "_" + matcher.group(1));
                    }
                    if (refNode != null) {
                        refNode = (JCRNodeWrapper) refNode.getProperty("j:reference").getNode();
                        if (refNode != null && refNode.isNodeType(Constants.JAHIANT_PAGE_LINK)) {
                            return value + "\" data-ref=\"" + refNode.getIdentifier();
                        }
                    }
                } catch (RepositoryException e) {
                    logger.error("", e);
                }
            }
            return value;
        });
        return node.getSession().getValueFactory().createValue(clearDataPath(result));
    }

    private String clearDataPath(String content) {
        Source source = new Source(content);
        OutputDocument doc = new OutputDocument(source);
        for (Element a : source.getAllElements(HTMLElementName.A)) {
            for (Attribute attribute : a.getAttributes()) {
                if ("data-path".equals(attribute.getName())) {
                    doc.replace(attribute.getBegin(), attribute.getEnd(), "");
                }
            }
        }
        return doc.toString();
    }

    @Override
    public Value afterGetValue(JCRPropertyWrapper property, Value storedValue) throws RepositoryException {
        Source source = new Source(storedValue.getString());
        OutputDocument doc = new OutputDocument(source);
        for (Element a : source.getAllElements(HTMLElementName.A)) {
            boolean found = false;
            for (Attribute attribute : a.getAttributes()) {
                if ("data-ref".equals(attribute.getName())) {
                    if (found) {
                        doc.replace(attribute.getBegin(), attribute.getEnd(), "");
                    }
                    found = true;
                }
            }
        }
        return property.getSession().getValueFactory().createValue(doc.toString());
    }

    @Override
    public Value[] afterGetValues(JCRPropertyWrapper property, Value[] storedValues) throws RepositoryException {
        Value[] res = new Value[storedValues.length];
        for (int i = 0; i < storedValues.length; i++) {
            Value storedValue = storedValues[i];
            res[i] = afterGetValue(property, storedValue);
        }
        return res;
    }
}
