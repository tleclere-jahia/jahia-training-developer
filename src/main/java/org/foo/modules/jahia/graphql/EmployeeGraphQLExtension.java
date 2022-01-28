package org.foo.modules.jahia.graphql;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLTypeExtension;
import org.jahia.modules.graphql.provider.dxm.DXGraphQLProvider;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;

import javax.jcr.RepositoryException;

@GraphQLTypeExtension(DXGraphQLProvider.Query.class)
public class EmployeeGraphQLExtension {
    @GraphQLField
    public static GqlEmployee getEmployee(@GraphQLName("path") String path) throws RepositoryException {
        JCRSessionWrapper jcrSessionWrapper = JCRSessionFactory.getInstance().getCurrentUserSession();
        if (!jcrSessionWrapper.nodeExists(path)) {
            return null;
        }
        return new GqlEmployee(jcrSessionWrapper.getNode(path));
    }
}
