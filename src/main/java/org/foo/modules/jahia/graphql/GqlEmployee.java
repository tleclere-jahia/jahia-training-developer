package org.foo.modules.jahia.graphql;

import graphql.annotations.annotationTypes.GraphQLField;
import org.jahia.modules.graphql.provider.dxm.node.GqlJcrNodeImpl;
import org.jahia.services.content.JCRNodeWrapper;

public class GqlEmployee {
    private final GqlJcrNodeImpl node;

    public GqlEmployee(JCRNodeWrapper jcrNodeWrapper) {
        this.node = new GqlJcrNodeImpl(jcrNodeWrapper);
    }

    @GraphQLField
    public String getFirstname() {
        return node.getNode().getPropertyAsString("firstname");
    }

    @GraphQLField
    public String getLastname() {
        return node.getNode().getPropertyAsString("lastname");
    }

    @GraphQLField
    public String getFullName() {
        return getFirstname() + " " + getLastname();
    }
}
