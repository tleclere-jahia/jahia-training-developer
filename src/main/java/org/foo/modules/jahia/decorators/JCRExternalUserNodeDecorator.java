package org.foo.modules.jahia.decorators;

import org.jahia.modules.external.users.JCRExternalUserNode;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.utils.LanguageCodeConverters;

import java.util.Locale;

public class JCRExternalUserNodeDecorator extends JCRExternalUserNode {
    public JCRExternalUserNodeDecorator(JCRNodeWrapper node) {
        super(node);
    }

    @Override
    public String getPropertyAsString(String name) {
        String language = super.getPropertyAsString(name);
        if (language == null && "preferredLanguage".equals(name)) {
            return LanguageCodeConverters.localeToLanguageTag(Locale.FRENCH);
        }
        return language;
    }
}
