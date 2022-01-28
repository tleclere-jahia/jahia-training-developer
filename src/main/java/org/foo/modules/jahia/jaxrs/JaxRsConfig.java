package org.foo.modules.jahia.jaxrs;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.server.ResourceConfig;

public class JaxRsConfig extends ResourceConfig {
    public JaxRsConfig() {
        super(ApiEndpoint.class, OtherEndpoint.class, JacksonJaxbJsonProvider.class);
    }
}
