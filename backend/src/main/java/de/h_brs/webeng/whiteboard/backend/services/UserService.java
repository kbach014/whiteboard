package de.h_brs.webeng.whiteboard.backend.services;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.h_brs.webeng.whiteboard.backend.dto.CredentialDto;
import de.h_brs.webeng.whiteboard.backend.dto.UserDto;

// http://localhost:8080/backend/rest/users
@Path("users")
public class UserService {
	
	private static final Logger LOG = LoggerFactory.getLogger(UserService.class);
	
	@POST
	@Path("/login")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response login(@Context HttpServletRequest req, CredentialDto credentials) {
		// TODO check user/pw against database. right now all usernames > 3 chars are "successful" login attempts.
		if (credentials.getUsername().length() > 3) {
			LOG.info(credentials.getUsername() + " has logged in successfully.");
			// get user from database and transfer public attributes to userDto:
			final UserDto user = new UserDto();
			user.setFirstname("Max");
			user.setFirstname("Mustermann");
			user.setUsername(credentials.getUsername());
			// login successful! store userId as session attribute for use in AuthFilter:
			final HttpSession session= req.getSession(true);
			session.setAttribute("userId", 42);
			return Response.ok().entity(user).build();
		} else {
			// login failed:
			return Response.status(Status.UNAUTHORIZED).build();
		}
	}
	
	@POST
	@Path("/logout")
	public Response login(@Context HttpServletRequest req) {
		final HttpSession session = req.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		return Response.noContent().build();
	}
	
	@POST
	@Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
	public Response register(UserDto user) {
		// TODO create user ...
		return Response.noContent().build();
	}

}
