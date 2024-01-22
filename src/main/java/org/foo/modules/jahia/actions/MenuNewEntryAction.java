package org.foo.modules.jahia.actions;

import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GWT menu entry extension
 * You need a Spring Configuration :
 * src/main/resources/META-INF/spring/jahia-training-developer.xml
 *     <bean class="org.jahia.services.uicomponents.bean.toolbar.Item">
 *         <property name="actionItem">
 *             <bean class="org.jahia.ajax.gwt.client.widget.toolbar.action.ExecuteActionItem">
 *                 <property name="action" value="menuNewEntry"/>
 *                 <property name="allowedNodeTypes" value="jnt:page"/>
 *             </bean>
 *         </property>
 *         <property name="requiredPermission" value="menuCreatePageArticle"/>
 *         <property name="titleKey" value="label.menuNewEntry@resources.jahia-training-developer"/>
 *         <property name="parent" value="editmode.tabs[0].treeContextMenu"/>
 *         <property name="positionAfter" value="Toolbar.Item.EditContent"/>
 *         <property name="hideWhenDisabled" value="true"/>
 *         <property name="requiredModule" value="jahia-training-developer"/>
 *     </bean>
 *
 * pom.xml
 *     <properties>
 *         <require-capability>osgi.extender;filter:="(osgi.extender=org.jahia.bundles.blueprint.extender.config)"</require-capability>
 *     </properties>
 */
@Component(service = Action.class)
public class MenuNewEntryAction extends Action {
    private static final Logger logger = LoggerFactory.getLogger(MenuNewEntryAction.class);

    public MenuNewEntryAction() {
        setName("menuNewEntry");
    }

    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        logger.info("Menu new entry");

        ActionResult actionResult = ActionResult.OK_JSON;
        JSONObject jsonObject = new JSONObject();
        Map<String, Object> data = new HashMap<>();
        data.put(Linker.REFRESH_ALL, true);
        jsonObject.put("refreshData", data);
        JSONObject messageDisplay = new JSONObject();
        messageDisplay.put("messageBoxType", "info");
        jsonObject.put("messageDisplay", messageDisplay);
        actionResult.setJson(jsonObject);
        return actionResult;
    }
}
