package org.foo.modules.jahia.probes;

import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.data.templates.ModuleState;
import org.jahia.modules.modulemanager.forge.ForgeService;
import org.jahia.modules.modulemanager.forge.Module;
import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.osgi.BundleUtils;
import org.jahia.osgi.FrameworkService;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.modulemanager.models.JahiaDepends;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.ModuleVersion;
import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

@Component(service = Probe.class)
public class ModulesUpdateProbe implements Probe {
    @Reference
    private MyOperationConstraintsService myOperationConstraintsService;

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
        Map<String, Module> availableUpdate = getAvailableUpdates();
        return new ProbeStatus(availableUpdate.size() + " modules must be updated", ProbeStatus.Health.GREEN);
    }

    private Map<String, Module> getAvailableUpdates() {
        Set<String> systemSiteRequiredModules = getSystemSiteRequiredModules();
        ForgeService forgeService = (ForgeService) SpringContextSingleton.getBean("forgeService");
        Map<String, Module> availableUpdate = new HashMap<>();
        Map<String, SortedMap<ModuleVersion, JahiaTemplatesPackage>> moduleStates = getAllModuleVersions();
        for (String key : moduleStates.keySet()) {
            SortedMap<ModuleVersion, JahiaTemplatesPackage> moduleVersions = moduleStates.get(key);
            Module forgeModule = forgeService.findModule(key, moduleVersions.get(moduleVersions.firstKey()).getGroupId());
            if (forgeModule != null) {
                ModuleVersion forgeVersion = new ModuleVersion(forgeModule.getVersion());
                org.osgi.framework.Version osgiVersion = new org.osgi.framework.Version(JahiaDepends.toOsgiVersion(forgeVersion.toString()));
                MyOperationConstraints ops = myOperationConstraintsService.getConstraintForBundle(key, osgiVersion);
                if (!isSameOrNewerVersionPresent(key, forgeVersion) && !systemSiteRequiredModules.contains(key) && (ops == null || ops.canDeploy(osgiVersion))) {
                    availableUpdate.put(key, forgeModule);
                }
            }
        }
        return availableUpdate;
    }

    public static Map<String, SortedMap<ModuleVersion, JahiaTemplatesPackage>> getAllModuleVersions() {
        Map<String, SortedMap<ModuleVersion, JahiaTemplatesPackage>> result = new TreeMap<>();
        Map<Bundle, ModuleState> moduleStatesByBundle = ((JahiaTemplateManagerService) SpringContextSingleton.getBean("JahiaTemplateManagerService")).getModuleStates();
        for (Bundle bundle : moduleStatesByBundle.keySet()) {
            JahiaTemplatesPackage module = BundleUtils.getModule(bundle);
            SortedMap<ModuleVersion, JahiaTemplatesPackage> modulesByVersion = result.computeIfAbsent(module.getId(), k -> new TreeMap<>());
            modulesByVersion.put(module.getVersion(), module);
        }
        return result;
    }

    private static boolean isSameOrNewerVersionPresent(String symbolicName, ModuleVersion forgeVersion) {
        for (Bundle bundle : FrameworkService.getBundleContext().getBundles()) {
            String n = bundle.getSymbolicName();
            if (StringUtils.equals(n, symbolicName)
                    && forgeVersion.compareTo(new ModuleVersion(BundleUtils.getModuleVersion(bundle))) <= 0) {
                // we've found either same or a new version present
                return true;
            }
        }
        return false;
    }

    private static Set<String> getSystemSiteRequiredModules() {
        JahiaTemplateManagerService templateManagerService = (JahiaTemplateManagerService) SpringContextSingleton.getBean("JahiaTemplateManagerService");
        Set<String> modules = new TreeSet<String>();
        for (String module : templateManagerService.getNonManageableModules()) {
            JahiaTemplatesPackage pkg = templateManagerService.getTemplatePackageById(module);
            if (pkg != null) {
                modules.add(pkg.getId());
                for (JahiaTemplatesPackage dep : pkg.getDependencies()) {
                    modules.add(dep.getId());
                }
            }
        }
        return modules;
    }
}
