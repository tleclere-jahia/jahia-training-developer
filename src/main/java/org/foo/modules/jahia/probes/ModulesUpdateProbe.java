package org.foo.modules.jahia.probes;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Jahia;
import org.jahia.commons.Version;
import org.jahia.modules.modulemanager.forge.Module;
import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.osgi.BundleUtils;
import org.jahia.osgi.FrameworkService;
import org.jahia.services.notification.HttpClientService;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.ModuleVersion;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Component(service = Probe.class)
public class ModulesUpdateProbe implements Probe, BundleListener {
    private static final Logger logger = LoggerFactory.getLogger(ModulesUpdateProbe.class);

    private static final Version JAHIA_VERSION = new Version(Jahia.VERSION);
    private boolean needRefresh;
    private final Map<String, Module> availableUpdates;

    public ModulesUpdateProbe() {
        needRefresh = true;
        availableUpdates = new HashMap<>();
    }

    @Activate
    private void onActivate(BundleContext bundleContext) {
        bundleContext.addBundleListener(this);
    }

    @Deactivate
    private void onDeactivate(BundleContext bundleContext) {
        bundleContext.removeBundleListener(this);
    }

    @Override
    public void bundleChanged(BundleEvent bundleEvent) {
        needRefresh = true;
    }

    @Reference
    private HttpClientService httpClientService;

    @Reference
    private JahiaTemplateManagerService jahiaTemplateManagerService;

    @Override
    public String getName() {
        return "Modules update";
    }

    @Override
    public String getDescription() {
        return "Check if store.jahia.com provides update for your installed modules";
    }

    @Override
    public ProbeSeverity getDefaultSeverity() {
        return ProbeSeverity.LOW;
    }

    @Override
    public ProbeStatus getStatus() {
        if (needRefresh) {
            Map<String, Module> modules = loadModules();
            jahiaTemplateManagerService.getModuleStates().keySet().forEach(bundle -> {
                String moduleKey = BundleUtils.getModule(bundle).getId();
                Module forgeModule = modules.get(moduleKey);
                if (modules.containsKey(moduleKey) && isNewerVersionPresent(moduleKey, new ModuleVersion(forgeModule.getVersion()))) {
                    availableUpdates.put(moduleKey, forgeModule);
                }
            });
            needRefresh = false;
        }
        if (availableUpdates.isEmpty()) {
            return new ProbeStatus("No module must be updated", ProbeStatus.Health.GREEN);
        }
        StringBuilder sb = new StringBuilder();
        availableUpdates.forEach((moduleKey, module) -> {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(module.getGroupId()).append("/").append(module.getId()).append("/").append(module.getVersion());
        });
        return new ProbeStatus("Modules must be updated: " + sb, ProbeStatus.Health.RED);
    }

    private Map<String, Module> loadModules() {
        Map<String, Module> modules = new HashMap<>();
        JSONArray moduleList = new JSONArray(httpClientService.executeGet("https://store.jahia.com/contents/modules-repository.moduleList.json")).getJSONObject(0).getJSONArray("modules");
        for (int i = 0; i < moduleList.length(); i++) {
            final JSONObject moduleObject = moduleList.getJSONObject(i);

            SortedMap<Version, JSONObject> sortedVersions = new TreeMap<>();
            final JSONArray moduleVersions = moduleObject.getJSONArray("versions");
            for (int j = 0; j < moduleVersions.length(); j++) {
                JSONObject object = moduleVersions.getJSONObject(j);
                Version version = new Version(object.getString("version"));
                Version requiredVersion = new Version(StringUtils.substringAfter(object.getString("requiredVersion"), "version-"));
                if (requiredVersion.compareTo(JAHIA_VERSION) <= 0 && requiredVersion.getMajorVersion() == JAHIA_VERSION.getMajorVersion()) {
                    sortedVersions.put(version, object);
                }
            }
            if (!sortedVersions.isEmpty()) {
                Module module = new Module();
                JSONObject versionObject = sortedVersions.get(sortedVersions.lastKey());
                module.setRemoteUrl(moduleObject.getString("remoteUrl"));
                module.setRemotePath(moduleObject.getString("path"));
                if (moduleObject.has("icon")) {
                    module.setIcon(moduleObject.getString("icon"));
                }
                module.setVersion(versionObject.getString("version"));
                module.setName(moduleObject.getString("title"));
                module.setId(moduleObject.getString("name"));
                module.setGroupId(moduleObject.getString("groupId"));
                module.setDownloadUrl(versionObject.getString("downloadUrl"));
                module.setInstallable(!jahiaTemplateManagerService.differentModuleWithSameIdExists(module.getId(), module.getGroupId()));
                modules.put(moduleObject.getString("name"), module);
            }
        }
        return modules;
    }

    private static boolean isNewerVersionPresent(String symbolicName, ModuleVersion forgeVersion) {
        for (Bundle bundle : FrameworkService.getBundleContext().getBundles()) {
            if (StringUtils.equals(bundle.getSymbolicName(), symbolicName) && forgeVersion.compareTo(BundleUtils.getModule(bundle).getVersion()) > 0) {
                // we've found a new version present
                return true;
            }
        }
        return false;
    }
}
