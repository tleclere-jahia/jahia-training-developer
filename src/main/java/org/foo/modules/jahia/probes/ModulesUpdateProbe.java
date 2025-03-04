package org.foo.modules.jahia.probes;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Jahia;
import org.jahia.commons.Version;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.data.templates.ModuleState;
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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Component(service = Probe.class)
public class ModulesUpdateProbe implements Probe {
    private static final Logger logger = LoggerFactory.getLogger(ModulesUpdateProbe.class);

    private static final Version JAHIA_VERSION = new Version(Jahia.VERSION);
    private final Map<String, org.jahia.modules.modulemanager.forge.Module> modules;

    public ModulesUpdateProbe() {
        modules = new HashMap<>();
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
        Map<String, org.jahia.modules.modulemanager.forge.Module> availableUpdate = getAvailableUpdates();
        if (availableUpdate.isEmpty()) {
            return new ProbeStatus("No module must be updated", ProbeStatus.Health.GREEN);
        }
        StringBuilder sb = new StringBuilder();
        availableUpdate.forEach((moduleKey, module) -> {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(module.getGroupId()).append("/").append(module.getId()).append("/").append(module.getVersion());
        });
        return new ProbeStatus("Modules must be updated: " + sb, ProbeStatus.Health.RED);
    }

    @Activate
    private void loadModules() {
        modules.clear();
        try {
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
                    org.jahia.modules.modulemanager.forge.Module module = new org.jahia.modules.modulemanager.forge.Module();
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
        } catch (Exception e) {
            logger.error("", e);
        }
        logger.info("Modules updated");
    }

    private Map<String, org.jahia.modules.modulemanager.forge.Module> getAvailableUpdates() {
        Map<String, org.jahia.modules.modulemanager.forge.Module> availableUpdate = new HashMap<>();
        Map<String, SortedMap<ModuleVersion, JahiaTemplatesPackage>> moduleStates = getAllModuleVersions();
        org.jahia.modules.modulemanager.forge.Module forgeModule;
        for (String key : moduleStates.keySet()) {
            try {
                forgeModule = modules.get(key);
                if (modules.containsKey(key) && isNewerVersionPresent(key, new ModuleVersion(forgeModule.getVersion()))) {
                    availableUpdate.put(key, forgeModule);
                }
            } catch (Exception e) {
                logger.error("", e);
            }
        }
        return availableUpdate;
    }

    public Map<String, SortedMap<ModuleVersion, JahiaTemplatesPackage>> getAllModuleVersions() {
        Map<String, SortedMap<ModuleVersion, JahiaTemplatesPackage>> result = new TreeMap<>();
        Map<Bundle, ModuleState> moduleStatesByBundle = jahiaTemplateManagerService.getModuleStates();
        for (Bundle bundle : moduleStatesByBundle.keySet()) {
            JahiaTemplatesPackage module = BundleUtils.getModule(bundle);
            SortedMap<ModuleVersion, JahiaTemplatesPackage> modulesByVersion = result.computeIfAbsent(module.getId(), k -> new TreeMap<>());
            modulesByVersion.put(module.getVersion(), module);
        }
        return result;
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
