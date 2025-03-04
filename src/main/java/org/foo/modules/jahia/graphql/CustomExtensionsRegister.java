package org.foo.modules.jahia.graphql;

import org.jahia.modules.graphql.provider.dxm.DXGraphQLExtensionsProvider;
import org.osgi.service.component.annotations.Component;

@Component(service = DXGraphQLExtensionsProvider.class)
public class CustomExtensionsRegister implements DXGraphQLExtensionsProvider {
}
