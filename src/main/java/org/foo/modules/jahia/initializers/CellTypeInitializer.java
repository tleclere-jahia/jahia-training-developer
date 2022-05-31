package org.foo.modules.jahia.initializers;

import org.jahia.services.content.JCRNodeIteratorWrapper;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.jahia.services.content.nodetypes.initializers.ModuleChoiceListInitializer;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.*;
import java.util.stream.Collectors;

@Component(service = ModuleChoiceListInitializer.class, immediate = true)
public class CellTypeInitializer implements ModuleChoiceListInitializer {
    private static final Logger logger = LoggerFactory.getLogger(CellTypeInitializer.class);
    private static final String KEY = "cellTypeInitializer";

    @Override
    public void setKey(String s) {
        // Do nothing
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition extendedPropertyDefinition, String param, List<ChoiceListValue> values, Locale locale, Map<String, Object> context) {
        List<ChoiceListValue> result;
        // find the cell index in the current row
        // go to the table parent and thead to find the cell with the same index
        // get the thead > cell[index].type and set this type as default in the choicelistvalue map properties
        try {
            JCRNodeWrapper trowNode;
            JCRNodeWrapper tCellNode = (JCRNodeWrapper) context.get("contextNode");
            long index;
            if (tCellNode == null) {
                trowNode = (JCRNodeWrapper) context.get("contextParent");
                index = trowNode.getNodes().getSize();
            } else {
                trowNode = tCellNode.getParent();
                index = findIndex(trowNode, tCellNode.getPath());
            }

            JCRNodeWrapper tableNode = trowNode.getParent(); // foont:table
            JCRNodeWrapper headNode = getHeadNode(tableNode, index);

            result = Arrays.stream(extendedPropertyDefinition.getValueConstraints())
                    .map(mixin -> {
                        Map<String, Object> params = new HashMap<>();
                        params.put("addMixin", mixin);
                        if (headNode != null && mixin.equals(headNode.getPropertyAsString("type"))) {
                            params.put("defaultProperty", true);
                        }
                        return new ChoiceListValue(mixin, params, new ValueImpl(mixin));
                    })
                    .collect(Collectors.toList());
        } catch (RepositoryException e) {
            logger.error("", e);
            result = Collections.emptyList();
        }
        return result;
    }

    private static long findIndex(JCRNodeWrapper parentNode, String nodePath) throws RepositoryException {
        long index = -1;
        JCRNodeIteratorWrapper it = parentNode.getNodes();
        while (it.hasNext()) {
            index++;
            if (nodePath.equals(it.nextNode().getPath())) {
                return index;
            }
        }
        return index;
    }

    private static JCRNodeWrapper getHeadNode(JCRNodeWrapper tableNode, long index) throws RepositoryException {
        JCRNodeIteratorWrapper it = tableNode.getNode("header").getNodes();
        JCRNodeWrapper headNode = null;
        int i = -1;
        while (it.hasNext() && i++ < index) {
            headNode = (JCRNodeWrapper) it.nextNode();
        }
        return headNode;
    }
}
