package org.agoncal.sample.jaxrs.jwt.rest;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;
import org.agoncal.sample.jaxrs.jwt.domain.Attendee;
import org.agoncal.sample.jaxrs.jwt.repository.AttendeeRepository;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.security.Key;
import java.util.List;
import java.util.logging.Logger;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

/**
 * @author Antonio Goncalves
 *         http://www.antoniogoncalves.org
 *         --
 */
@Path("/attendees")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class AttendeeEndpoint {

    // ======================================
    // =          Injection Points          =
    // ======================================

    @Inject
    private AttendeeRepository attendeeRepository;

    @Context
    private UriInfo uriInfo;

    @Inject
    private Logger logger;

    // ======================================
    // =          Business methods          =
    // ======================================

    @POST
    @Path("/login")
    @Secured
    @Consumes(APPLICATION_FORM_URLENCODED)
    public Response authenticateUser(@FormParam("username") String username,
                                     @FormParam("password") String password) {

        try {

            logger.info("#### login/password : " + username + "/" + password);

            // Authenticate the user using the credentials provided
            authenticate(username, password);

            // Issue a token for the user
            String token = issueToken(username);

            // Return the token on the response
            return Response.ok().header(AUTHORIZATION, "Bearer " + token).build();

        } catch (Exception e) {
            return Response.status(UNAUTHORIZED).build();
        }
    }

    private void authenticate(String login, String password) throws Exception {
        Attendee attendee = attendeeRepository.findByLoginPassword(login, password);
        if (attendee == null)
            throw new SecurityException();
        // Authenticate against a database, LDAP, file or whatever
        // Throw an Exception if the credentials are invalid
    }

    private String issueToken(String username) {
        // Issue a token (can be a random String persisted to a database or a JWT token)
        // The issued token must be associated to a user
        // Return the issued token
        Key key = MacProvider.generateKey();
        String jwtToken = Jwts.builder().setSubject(username).signWith(SignatureAlgorithm.HS512, key).compact();
        return jwtToken;

    }

    @POST
    public Response create(Attendee attendee) {
        logger.info("#### create attendee : " + attendee);
        Attendee created = attendeeRepository.create(attendee);
        return Response.created(uriInfo.getAbsolutePathBuilder().path(created.getId()).build()).build();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") String id) {
        logger.info("#### find attendee by id : " + id);

        Attendee attendee = attendeeRepository.findById(id);

        if (attendee == null)
            return Response.status(NOT_FOUND).build();

        return Response.ok(attendee).build();
    }

    @GET
    @Path("/count")
    public Response countAllAttendees() {
        logger.info("#### count all attendee");

        Long nbAttendees = attendeeRepository.countNumberOfAttendees();

        return Response.ok(nbAttendees).build();
    }

    @GET
    public Response allAttendees() {
        logger.info("#### find all attendee");

        List<Attendee> allAttendees = attendeeRepository.findAllAttendees();

        if (allAttendees == null)
            return Response.status(NOT_FOUND).build();

        return Response.ok(allAttendees).build();
    }

    @DELETE
    @Path("/{id}")
    public Response remove(@PathParam("id") String id) {
        attendeeRepository.delete(id);
        return Response.noContent().build();
    }
}