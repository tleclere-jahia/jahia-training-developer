package org.foo.modules.jahia.probes;

import org.jahia.services.modulemanager.util.PropertiesList;
import org.jahia.services.modulemanager.util.PropertiesManager;
import org.jahia.services.modulemanager.util.PropertiesValues;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component(service = MyOperationConstraintsService.class, immediate = true, configurationPid = "org.jahia.modules.modulemanager.configuration.constraints")
public class MyOperationConstraintsService {

    private static final Logger logger = LoggerFactory.getLogger(MyOperationConstraintsService.class);
    private static final String CONSTRAINTS_CONFIG_KEY = "moduleLifeCycleConstraints";
    private static final Map<String, MyOperationConstraints> constraints = Collections.synchronizedMap(new HashMap<>());

    @Activate
    @Modified
    public void activate(Map<String, String> props) {
        String pid = props.get("service.pid");
        logger.debug("Adding/updating configuration {}...", pid);
        clearConstraintsByPid(pid);
        parseConfig(props);
    }

    @Deactivate
    public void deactivate(Map<String, String> props) {
        String pid = props.get("service.pid");
        logger.debug("Removing configuration {}...", pid);
        clearConstraintsByPid(pid);
    }

    /**
     * Add constraint to list of definitions.
     * Replace constraint if it exists.
     */
    private void parseConfig(Map<String, String> props) {
        logger.debug("Parsing configuration property values");
        String pid = props.get("service.pid");
        PropertiesManager pm = new PropertiesManager(props);
        PropertiesList constraintsProp = pm.getValues().getList(CONSTRAINTS_CONFIG_KEY);
        for (int i = 0; i < constraintsProp.getSize(); i++) {
            PropertiesValues constraintProp = constraintsProp.getValues(i);
            MyOperationConstraint constraint = MyOperationConstraint.parse(pid, constraintProp);
            if (constraint != null) {
                MyOperationConstraints ops = constraints.get(constraint.getModuleId());
                if (ops == null) {
                    ops = new MyOperationConstraints();
                }
                ops.add(constraint);
                constraints.put(constraint.getModuleId(), ops);
            }
        }
        logger.debug("Configuration parsed");
    }

    private void clearConstraintsByPid(String pid) {
        if (pid != null) {
            /* Go through each MyOperationConstraints element and remove any constraint associated with 'pid' configuration.
             * If any MyOperationConstraints is empty after removal, then also remove them from constraints map as well. */
            Set<String> moduleIds = constraints.entrySet().stream()
                    .filter(e -> {
                        MyOperationConstraints ops = e.getValue();
                        ops.remove(pid);
                        return ops.isEmpty();
                    })
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());
            constraints.keySet().removeAll(moduleIds);
        }
    }

    public MyOperationConstraints getConstraintForBundle(Bundle b) {
        return getConstraintForBundle(b.getSymbolicName(), b.getVersion());
    }

    public MyOperationConstraints getConstraintForBundle(String symbolicName, Version version) {
        MyOperationConstraints c = constraints.get(symbolicName);
        return (c != null && c.inRange(version)) ? c : null;
    }
}
