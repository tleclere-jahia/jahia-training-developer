package org.foo.modules.jahia.jaxrs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/training/other")
@Produces(MediaType.APPLICATION_JSON)
public class OtherEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(OtherEndpoint.class);

    @GET
    public Response test(@Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse) {
        logger.info("Training REST endpoint");
        return Response.ok().build();
    }
}
