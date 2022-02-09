package org.foo.modules.jahia.graphql;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import graphql.annotations.annotationTypes.GraphQLTypeExtension;
import org.jahia.modules.graphql.provider.dxm.DataFetchingException;
import org.jahia.modules.graphql.provider.dxm.node.GqlJcrNode;
import org.jahia.modules.graphql.provider.dxm.node.GqlJcrProperty;

@GraphQLTypeExtension(GqlJcrNode.class)
public class GqlNodeExtension {
    private final GqlJcrNode gqlJcrNode;

    public GqlNodeExtension(GqlJcrNode gqlJcrNode) {
        this.gqlJcrNode = gqlJcrNode;
    }

    @GraphQLField
    public String getPropertyValue(@GraphQLName("name") @GraphQLNonNull String name, @GraphQLName("language") String language) {
        GqlJcrProperty property = gqlJcrNode.getProperty(name, language);
        if (property == null) {
            throw new DataFetchingException("Mandatory property: " + name);
        }
        return property.getValue();
    }
}
