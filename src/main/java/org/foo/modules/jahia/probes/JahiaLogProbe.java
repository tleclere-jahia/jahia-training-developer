package org.foo.modules.jahia.probes;

import org.apache.commons.lang.StringUtils;
import org.foo.modules.jahia.helpers.DataSize;
import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.utils.FileUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import java.io.File;
import java.util.Map;

@Component(service = Probe.class)
public class JahiaLogProbe implements Probe {
    private long yellowThreshold = 0L;
    private long redThreshold = 0L;

    @Activate
    private void onActivate(Map<String, ?> config) {
        if (config.containsKey("probe.jahialog.threshold.yellow")) {
            yellowThreshold = DataSize.parse((String) config.get("probe.jahialog.threshold.yellow")).toBytes();
        }
        if (config.containsKey("probe.jahialog.threshold.red")) {
            redThreshold = DataSize.parse((String) config.get("probe.jahialog.threshold.red")).toBytes();
        }
    }

    @Override
    public String getName() {
        return "Jahia log size";
    }

    @Override
    public String getDescription() {
        return "Get jahia.log size";
    }

    @Override
    public ProbeSeverity getDefaultSeverity() {
        return ProbeSeverity.LOW;
    }

    @Override
    public ProbeStatus getStatus() {
        String logDirName = System.getProperty("jahia.log.dir");
        if (StringUtils.isNotEmpty(logDirName)) {
            File jahiaLog = new File(logDirName, "jahia.log");
            if (jahiaLog.exists()) {
                long length = jahiaLog.length();
                ProbeStatus.Health health = ProbeStatus.Health.GREEN;
                if (length >= redThreshold) {
                    health = ProbeStatus.Health.RED;
                } else if (length >= yellowThreshold) {
                    health = ProbeStatus.Health.YELLOW;
                }
                return new ProbeStatus("jahia.log length: " + FileUtils.humanReadableByteCount(jahiaLog.length()), health);
            }
        }
        return new ProbeStatus("jahia.log not found", ProbeStatus.Health.RED);
    }
}
