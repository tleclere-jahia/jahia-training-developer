package org.foo.modules.jahia.graphql;

import graphql.annotations.annotationTypes.GraphQLField;
import org.jahia.services.content.JCRNodeWrapper;

public class GqlEmployee {
    private final JCRNodeWrapper node;

    public GqlEmployee(JCRNodeWrapper jcrNodeWrapper) {
        node = jcrNodeWrapper;
    }

    @GraphQLField
    public String getFirstname() {
        return node.getPropertyAsString("firstname");
    }

    @GraphQLField
    public String getLastname() {
        return node.getPropertyAsString("lastname");
    }

    @GraphQLField
    public String getFullName() {
        return getFirstname() + " " + getLastname();
    }
}
