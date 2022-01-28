package org.foo.modules.jahia.graphql;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import graphql.annotations.annotationTypes.GraphQLTypeExtension;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.modules.graphql.provider.dxm.node.GqlJcrProperty;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.settings.SettingsBean;
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

    public ResourceBundleExtension(GqlJcrProperty property) {
        this.property = property;
    }

    @GraphQLField
    public String getResourceBundleValue(@GraphQLName("language") String language) {
        if (property == null) {
            return null;
        }
        String propValue = property.getValue();

        Locale locale = LanguageCodeConverters.languageCodeToLocale(language);
        if (locale == null) {
            locale = SettingsBean.getInstance().getDefaultLocale();
        }

        try {
            ExtendedPropertyDefinition propertyDefinition = (ExtendedPropertyDefinition) property.getProperty().getDefinition();
            JahiaTemplatesPackage pkg = propertyDefinition.getDeclaringNodeType().getTemplatePackage();
            return Messages.get(pkg != null ? pkg : ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageById(JahiaTemplatesPackage.ID_DEFAULT),
                    propertyDefinition.getResourceBundleKey() + "." + JCRContentUtils.replaceColon(propValue),
                    locale, propValue);
        } catch (RepositoryException e) {
            logger.error("", e);
            return propValue;
        }
    }
}
