package org.foo.modules.jahia.jaxrs;

import org.foo.modules.jahia.graphql.GqlHelper;
import org.json.JSONException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collections;

@Path("/training/test")
@Produces(MediaType.APPLICATION_JSON)
public class ApiEndpoint {
    /**
     * curl --location --request POST 'http://${environment.jahiaHost}/modules/cmscontenus' \\n--header 'Content-Type: application/json' \\n--data-raw '{
     * "pathjahia":/sites/sitetutocustom/contents
     * "typedecontenu": "jntuto:tutorialItem"
     * "champdetrie":"jcr:lastModified"
     * "langue": "fr",
     * "offset": 2,
     * "limit":2
     * }'
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getData(RequestData requestData, @Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse) {
        try {
            callGraphQL(httpServletRequest, httpServletResponse);
            return Response.ok().build();
        } catch (IOException | ServletException | JSONException e) {
            return Response.serverError().build();
        }
    }

    @GET
    public Response test(@Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse) {
        try {
            callGraphQL(httpServletRequest, httpServletResponse);
            return Response.ok().build();
        } catch (IOException | ServletException | JSONException e) {
            return Response.serverError().build();
        }
    }

    private void callGraphQL(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, JSONException, IOException {
        GqlHelper.execute(httpServletRequest, httpServletResponse,
                "query test($path: String!) {\n" +
                        "  jcr(workspace: LIVE) {\n" +
                        "    nodesByCriteria(\n" +
                        "      criteria: { nodeType: \"jnt:page\", paths: [$path] }\n" +
                        "      fieldSorter: { fieldName: \"title.value\" }\n" +
                        "      offset: 2\n" +
                        "      limit: 2\n" +
                        "    ) {\n" +
                        "      nodes {\n" +
                        "        path\n" +
                        "        title: property(name: \"jcr:title\", language: \"fr\") {\n" +
                        "          value\n" +
                        "        }\n" +
                        "      }\n" +
                        "    }\n" +
                        "  }\n" +
                        "}", Collections.singletonMap("path", "/sites/digitall/home"));

    }
}
