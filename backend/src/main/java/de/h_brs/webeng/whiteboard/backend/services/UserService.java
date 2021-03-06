package de.h_brs.webeng.whiteboard.backend.services;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.h_brs.webeng.whiteboard.backend.dao.UserDAO;
import de.h_brs.webeng.whiteboard.backend.dao.exception.PasswordIncorrectException;
import de.h_brs.webeng.whiteboard.backend.dao.exception.UserAlreadyRegisteredException;
import de.h_brs.webeng.whiteboard.backend.dao.exception.UserNotFoundException;
import de.h_brs.webeng.whiteboard.backend.dao.impl.RedisUserDAO;
import de.h_brs.webeng.whiteboard.backend.domain.User;
import de.h_brs.webeng.whiteboard.backend.dto.CredentialDto;
import de.h_brs.webeng.whiteboard.backend.dto.UserDto;

// http://localhost:8080/backend/rest/users
@Path("users")
public class UserService {

	private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

	// TODO Vielleicht noch Validation im Frontend
	@POST
	@Path("/login")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response login(@Context HttpServletRequest req, CredentialDto credentials) {
		UserDAO userDAO = new RedisUserDAO();

		if (credentials.getUsername() == null || credentials.getPassword() == null)
			return Response.status(Status.UNAUTHORIZED).build();

		try {
			User user = userDAO.login(credentials.getUsername(), credentials.getPassword());
			LOG.info(credentials.getUsername() + " has logged in successfully.");

			final HttpSession session = req.getSession(true);
			session.setAttribute("username", user.getUsername());

			final UserDto result = new UserDto();
			result.setUsername(user.getUsername());
			result.setFirstname(user.getFirstname());
			result.setLastname(user.getLastname());

			return Response.ok().entity(result).build();
		} catch (UserNotFoundException e) {
			return Response.status(Status.UNAUTHORIZED).build();
		} catch (PasswordIncorrectException e) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
	}
	
	@GET
	@Path("/whoami")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCurrentUser(@Context HttpServletRequest req) {
		final String username = (String) req.getSession().getAttribute("username");
		if (username == null) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		
		try {
			final UserDAO userDAO = new RedisUserDAO();
			final User user = userDAO.findUserByUsername(username);
			final UserDto dto = new UserDto();
			dto.setUsername(user.getUsername());
			dto.setFirstname(user.getFirstname());
			dto.setLastname(user.getLastname());
			return Response.ok(dto).build();
		} catch (UserNotFoundException e) {
			return Response.status(Status.NOT_FOUND).build();
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
	public Response register(UserDto userDto) {
		UserDAO userDAO = new RedisUserDAO();
		String firstname = userDto.getFirstname();
		String lastname = userDto.getLastname();
		String password = userDto.getPassword();
		String username = userDto.getUsername();

		User user = new User(username, firstname, lastname, password);
		try {
			userDAO.register(user);
			LOG.info("User " + user.getUsername() + " was registered successfully!");
			return Response.status(Status.OK).build();
		} catch (UserAlreadyRegisteredException e) {
			LOG.info(e.getMessage());
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

}
