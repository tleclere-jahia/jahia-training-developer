package org.foo.modules.jahia.initializers;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRNodeIteratorWrapper;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.jahia.services.content.nodetypes.initializers.ModuleChoiceListInitializer;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component(service = ModuleChoiceListInitializer.class)
public class ImageChoiceListInitializerImpl implements ModuleChoiceListInitializer {
	private static final Logger logger = LoggerFactory.getLogger(ImageChoiceListInitializerImpl.class);

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
		if (context == null) {
			return Collections.emptyList();
		}
		JCRNodeWrapper node = Optional.of(context).map(ctx -> (JCRNodeWrapper) Optional.ofNullable(context.get("contextNode")).orElse(context.get("contextParent"))).orElse(null);
		if (node == null) {
			return Collections.emptyList();
		}
		try {
			List<ChoiceListValue> choiceListValues = new ArrayList<>();
			JCRSessionWrapper session = node.getSession();
			JCRNodeIteratorWrapper it = session.getWorkspace().getQueryManager().createQuery("SELECT * FROM [jmix:image] WHERE ISDESCENDANTNODE('" + StringUtils.replace(param, "$currentSite", node.getResolveSite().getPath()) + "/files') ORDER BY [j:nodename]", Query.JCR_SQL2).execute().getNodes();
			JCRNodeWrapper imageNode;
			while (it.hasNext()) {
				imageNode = (JCRNodeWrapper) it.nextNode();
				choiceListValues.add(new ChoiceListValue(imageNode.getName(), Collections.singletonMap("image", imageNode.getUrl()),
						session.getValueFactory().createValue(imageNode.getIdentifier())));
			}
			return choiceListValues;
		} catch (RepositoryException e) {
			logger.error("", e);
			return Collections.emptyList();
		}
	}
}
