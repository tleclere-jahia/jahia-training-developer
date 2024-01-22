package org.foo.modules.jahia.probes;

import org.osgi.framework.Version;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Wrapper object representing collection of <code>OperationConstraint</code>s for each given pid
 */
public class MyOperationConstraints {

    Set<MyOperationConstraint> ops = Collections.synchronizedSet(new HashSet<>());

    /**
     * @param o Add to list of MyOperationConstraint.
     */
    public void add(MyOperationConstraint o) {
        ops.add(o);
    }

    public void remove(String pid) {
        ops.removeIf(oc -> oc.getPid().equals(pid));
    }

    public boolean isEmpty() {
        return ops.isEmpty();
    }

    public boolean inRange(Version v) {
        return ops.stream().anyMatch(o -> o.inRange(v));
    }

    public boolean canDeploy(Version v) {
        return ops.stream().allMatch(o -> !o.inRange(v) || o.canDeploy());
    }

    public boolean canUndeploy(Version v) {
        return ops.stream().allMatch(o -> !o.inRange(v) || o.canUndeploy());
    }

    public boolean canStop(Version v) {
        return ops.stream().allMatch(o -> !o.inRange(v) || o.canStop());
    }

    public boolean canStart(Version v) {
        return ops.stream().allMatch(o -> !o.inRange(v) || o.canStart());
    }
}
