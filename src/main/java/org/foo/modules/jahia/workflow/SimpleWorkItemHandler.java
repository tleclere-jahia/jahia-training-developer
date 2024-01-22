package org.foo.modules.jahia.workflow;

import org.jahia.services.workflow.WorkflowService;
import org.jahia.services.workflow.jbpm.JBPM6WorkflowProvider;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Component(immediate = true)
public class SimpleWorkItemHandler implements WorkItemHandler {
    private static final Logger logger = LoggerFactory.getLogger(SimpleWorkItemHandler.class);

    private static final String TASK_NAME = "Random task";

    @Reference
    private WorkflowService workflowService;

    private final Random random;

    public SimpleWorkItemHandler() {
        random = new Random();
    }

    @Activate
    private void start() {
        logger.info("Registering custom work item handler {}", TASK_NAME);
        JBPM6WorkflowProvider workflowProvider = (JBPM6WorkflowProvider) workflowService.getProviders().get("jBPM");
        workflowProvider.registerWorkItemHandler(TASK_NAME, this);
    }

    @Deactivate
    private void stop() {
        logger.info("Un-registering custom work item handler {}", TASK_NAME);
        JBPM6WorkflowProvider workflowProvider = (JBPM6WorkflowProvider) workflowService.getProviders().get("jBPM");
        workflowProvider.unregisterWorkItemHandler(TASK_NAME);
    }

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager workItemManager) {
        int randomValue = random.nextInt(2);
        logger.info("ExecuteWorkItem {}, entered value is {}, result is {}", workItem.getName(), workItem.getParameter("value"), randomValue);

        Map<String, Object> results = new HashMap<>();
        results.put("randomValue", randomValue);

        workItemManager.completeWorkItem(workItem.getId(), results);
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager workItemManager) {
        logger.info("AbortWorkItem {}", workItem);
    }
}
