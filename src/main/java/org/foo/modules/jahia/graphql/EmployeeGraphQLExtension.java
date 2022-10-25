package org.foo.modules.jahia.graphql;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLTypeExtension;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.modules.graphql.provider.dxm.DXGraphQLProvider;
import org.jahia.modules.graphql.provider.dxm.DataFetchingException;
import org.jahia.osgi.BundleUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;

import javax.jcr.RepositoryException;

@GraphQLTypeExtension(DXGraphQLProvider.Query.class)
public class EmployeeGraphQLExtension {
    @GraphQLField
    public static GqlEmployee getEmployee(@GraphQLName("path") String path) throws RepositoryException {
        JCRSessionWrapper jcrSessionWrapper = BundleUtils.getOsgiService(JCRSessionFactory.class, null).getCurrentUserSession(Constants.LIVE_WORKSPACE);
        if (StringUtils.isBlank(path) || !jcrSessionWrapper.nodeExists(path)) {
            return null;
        }
        JCRNodeWrapper node = jcrSessionWrapper.getNode(path);
        if (node.isNodeType("foont:employee")) {
            return new GqlEmployee(node);
        }
        throw new DataFetchingException("Invalid nodetype");
    }
}
