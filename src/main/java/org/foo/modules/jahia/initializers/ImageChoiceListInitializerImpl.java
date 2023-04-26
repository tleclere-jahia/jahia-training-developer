package org.foo.modules.jahia.initializers;

import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.osgi.BundleUtils;
import org.jahia.services.content.JCRNodeIteratorWrapper;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.jahia.services.content.nodetypes.initializers.ModuleChoiceListInitializer;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component(service = ModuleChoiceListInitializer.class)
public class ImageChoiceListInitializerImpl implements ModuleChoiceListInitializer {
    private static final Logger logger = LoggerFactory.getLogger(ImageChoiceListInitializerImpl.class);

    private JahiaTemplatesPackage module;

    @Activate
    private void onActivate(BundleContext bundleContext) {
        module = BundleUtils.getModule(bundleContext.getBundle());
    }

    @Override
    public void setKey(String key) {
        // Nothing to do
    }

    @Override
    public String getKey() {
        return "image";
    }

    @Override
    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition declaringPropertyDefinition, String param, List<ChoiceListValue> values, Locale locale, Map<String, Object> context) {
        if (module == null || context == null) {
            return Collections.emptyList();
        }
        JCRNodeWrapper node = Optional.of(context).map(ctx -> (JCRNodeWrapper) Optional.ofNullable(context.get("contextNode")).orElse(context.get("contextParent"))).orElse(null);
        if (node == null) {
            return Collections.emptyList();
        }
        try {
            if (StringUtils.isNotBlank(param) && (StringUtils.startsWith(param, "/sites") || StringUtils.startsWith(param, "$currentSite"))) {
                return getImagesFromSite(node, param);
            }
            if (StringUtils.isNotBlank(param) && StringUtils.startsWith(param, "$currentModule")) {
                return getImagesFromCurrentModuleResources(module, param);
            }
            logger.warn("Invalid parameter in CND file");
            return Collections.emptyList();
        } catch (RepositoryException e) {
            logger.error("", e);
            return Collections.emptyList();
        }
    }

    private static List<ChoiceListValue> getImagesFromSite(JCRNodeWrapper node, String param) throws RepositoryException {
        List<ChoiceListValue> choiceListValues = new ArrayList<>();
        JCRSessionWrapper session = node.getSession();
        String query = "SELECT * FROM [jmix:image] WHERE ISDESCENDANTNODE('" +
                (StringUtils.isBlank(param) ? "/sites" : StringUtils.replace(param, "$currentSite", node.getResolveSite().getPath())) +
                "') ORDER BY [j:nodename]";
        JCRNodeIteratorWrapper it = session.getWorkspace().getQueryManager().createQuery(query, Query.JCR_SQL2).execute().getNodes();
        JCRNodeWrapper imageNode;
        while (it.hasNext()) {
            imageNode = (JCRNodeWrapper) it.nextNode();
            choiceListValues.add(new ChoiceListValue(imageNode.getName(), Collections.singletonMap("image", imageNode.getUrl()),
                    session.getValueFactory().createValue(imageNode.getIdentifier())));
        }
        return choiceListValues;
    }

    private static List<ChoiceListValue> getImagesFromCurrentModuleResources(JahiaTemplatesPackage module, String param) {
        String resourcesPath = StringUtils.substringAfter(param, "$currentModule");
        return Arrays.stream(module.getResources(resourcesPath))
                .map(resource ->
                        new ChoiceListValue(resource.getFilename(),
                                Collections.singletonMap("image", "/modules/" + module.getName() + resourcesPath + "/" + resource.getFilename()),
                                new ValueImpl(resource.getFilename())))
                .collect(Collectors.toList());
    }
}
