package org.foo.modules.jahia.initializers;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import org.apache.commons.collections.CollectionUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.osgi.BundleUtils;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.CacheHelper;
import org.jahia.services.cache.ModuleClassLoaderAwareCacheEntry;
import org.jahia.services.cache.ehcache.EhCacheProvider;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.jahia.services.content.nodetypes.initializers.ModuleChoiceListInitializer;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.View;
import org.jahia.utils.i18n.Messages;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component(service = ModuleChoiceListInitializer.class)
public class ViewCachedInitializer implements ModuleChoiceListInitializer {
    private static final Logger logger = LoggerFactory.getLogger(ViewCachedInitializer.class);

    private static final String MODULE_NAME = "jahia-training-developer";
    private static final String KEY = "viewCachedInitializer";
    private static final String CACHE_NAME = "viewCached";
    private static final long CACHE_TTL = 3600L;

    private Bundle bundle;
    private CacheManager cacheManager;
    @Reference
    private EhCacheProvider ehCacheProvider;
    private Ehcache cache;

    @Activate
    private void onActivate(BundleContext bundleContext) {
        bundle = bundleContext.getBundle();
        cacheManager = ehCacheProvider.getCacheManager();
        CacheConfiguration cacheConfiguration = cacheManager.getConfiguration().getDefaultCacheConfiguration() != null ?
                cacheManager.getConfiguration().getDefaultCacheConfiguration().clone() :
                new CacheConfiguration();
        cacheConfiguration.setName(CACHE_NAME);
        cacheConfiguration.setEternal(false);
        cacheConfiguration.setTimeToIdleSeconds(CACHE_TTL);
        // Create a new cache with the configuration
        cache = new Cache(cacheConfiguration);
        cache.setName(CACHE_NAME);
        // Cache name has been set now we can initialize it by putting it in the manager.
        // Only Cache manager is initializing caches.
        cacheManager.addCacheIfAbsent(cache);
    }

    @Deactivate
    private void onDeactivate() {
        cache.removeAll();
        cacheManager.removeCache(cache.getName());
    }

    @Override
    public void setKey(String key) {
        // Nothing to do
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition declaringPropertyDefinition, String param, List<ChoiceListValue> values, Locale locale, Map<String, Object> context) {
        if (context == null) {
            return Collections.emptyList();
        }
        JCRNodeWrapper node = Optional.of(context).map(ctx -> (JCRNodeWrapper) Optional.ofNullable(context.get("contextNode")).orElse(context.get("contextParent"))).orElse(null);
        ExtendedNodeType nodeType = declaringPropertyDefinition.getDeclaringNodeType();

        List<String> viewKeys = (List<String>) CacheHelper.getObjectValue(cache, nodeType.getName());
        if (CollectionUtils.isNotEmpty(viewKeys)) {
            return viewKeys.stream().map(viewKey -> {
                String displayName = Messages.get(BundleUtils.getModule(bundle), declaringPropertyDefinition.getResourceBundleKey() + "." + JCRContentUtils.replaceColon(viewKey),
                        locale, viewKey);
                return new ChoiceListValue(displayName, Collections.emptyMap(), new ValueImpl(viewKey, PropertyType.STRING, false));
            }).collect(Collectors.toList());
        }


        try {
            List<View> views = RenderService.getInstance().getViewsSet(nodeType, node.getResolveSite(), "html").stream()
                    .filter(view -> view.getModule().getBundle().equals(bundle))
                    .filter(view -> !view.getKey().startsWith("wrapper.") && !view.getKey().contains("hidden."))
                    .collect(Collectors.toList());
            viewKeys = views.stream().map(View::getKey).collect(Collectors.toList());
            cache.put(new Element(nodeType.getName(), new ModuleClassLoaderAwareCacheEntry(viewKeys, MODULE_NAME)));
            return views.stream().map(view -> {
                        JahiaTemplatesPackage pkg = view.getModule() != null ? view.getModule() : ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageById(JahiaTemplatesPackage.ID_DEFAULT);
                        String displayName = Messages.get(pkg, declaringPropertyDefinition.getResourceBundleKey() + "." + JCRContentUtils.replaceColon(view.getKey()),
                                locale, view.getKey());
                        return new ChoiceListValue(displayName, Collections.emptyMap(), new ValueImpl(view.getKey(), PropertyType.STRING, false));
                    })
                    .collect(Collectors.toList());
        } catch (RepositoryException e) {
            logger.error("Unable to resolve site", e);
        }
        return Collections.emptyList();
    }
}
