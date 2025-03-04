package org.foo.modules.jahia.graphql;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import graphql.annotations.annotationTypes.GraphQLTypeExtension;
import org.jahia.modules.graphql.provider.dxm.node.GqlJcrNodeMutation;
import org.jahia.services.content.JCRAutoSplitUtils;

import javax.jcr.RepositoryException;

@GraphQLTypeExtension(GqlJcrNodeMutation.class)
public final class MutateNodeExtension {
    private final GqlJcrNodeMutation gqlJcrNode;

    public MutateNodeExtension(GqlJcrNodeMutation gqlJcrNode) {
        this.gqlJcrNode = gqlJcrNode;
    }

    @GraphQLField
    public GqlJcrNodeMutation enableAutoSplit(@GraphQLName("splitConfiguration") @GraphQLNonNull String splitConfiguration) throws RepositoryException {
        if (gqlJcrNode != null && gqlJcrNode.jcrNode.isNodeType("jnt:contentFolder")) {
            JCRAutoSplitUtils.enableAutoSplitting(gqlJcrNode.jcrNode, splitConfiguration, "jnt:contentFolder");
        }
        return gqlJcrNode;
    }
}
