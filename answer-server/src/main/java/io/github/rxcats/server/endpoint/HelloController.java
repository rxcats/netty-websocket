package io.github.rxcats.server.endpoint;

import io.github.rxcats.core.netty.http.annotation.EndPoint;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Component
@EndPoint
@Path("/test")
public class HelloController {

    @GET
    @Path("/hello")
    public String hello() {
        return "hello";
    }

    @GET
    @Path("/hellojson")
    @Produces(MediaType.APPLICATION_JSON)
    public Response helloJson() {
        Map<String, String> map = new HashMap<>();
        map.put("message", "hello");

        return Response.ok().entity(map).build();
    }
}
