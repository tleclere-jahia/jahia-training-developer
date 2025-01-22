package org.foo.modules.jahia.graphql;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import graphql.annotations.annotationTypes.GraphQLTypeExtension;
import org.jahia.api.settings.SettingsBean;
import org.jahia.api.templates.JahiaTemplateManagerService;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.modules.graphql.provider.dxm.node.GqlJcrProperty;
import org.jahia.modules.graphql.provider.dxm.osgi.annotations.GraphQLOsgiService;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.Locale;

@GraphQLTypeExtension(GqlJcrProperty.class)
public class ResourceBundleExtension {
    private static final Logger logger = LoggerFactory.getLogger(ResourceBundleExtension.class);

    private final GqlJcrProperty property;

    @GraphQLOsgiService
    private SettingsBean settingsBean;
    @GraphQLOsgiService
    private JahiaTemplateManagerService jahiaTemplateManagerService;

    public ResourceBundleExtension(GqlJcrProperty property) {
        this.property = property;
    }

    @GraphQLField
    public String getResourceBundleValue(@GraphQLName("language") @GraphQLNonNull String language) {
        if (property == null) {
            return null;
        }
        String propValue = property.getValue();

        Locale locale = LanguageCodeConverters.languageCodeToLocale(language);
        if (locale == null) {
            locale = settingsBean.getDefaultLocale();
        }

        try {
            ExtendedPropertyDefinition propertyDefinition = (ExtendedPropertyDefinition) property.getProperty().getDefinition();
            JahiaTemplatesPackage pkg = propertyDefinition.getDeclaringNodeType().getTemplatePackage();
            return Messages.get(pkg != null ? pkg : jahiaTemplateManagerService.getTemplatePackageById(JahiaTemplatesPackage.ID_DEFAULT),
                    propertyDefinition.getResourceBundleKey() + "." + JCRContentUtils.replaceColon(propValue),
                    locale, propValue);
        } catch (RepositoryException e) {
            logger.error("", e);
            return propValue;
        }
    }
}
