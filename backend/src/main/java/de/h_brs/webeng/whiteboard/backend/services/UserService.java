package de.h_brs.webeng.whiteboard.backend.services;

import java.util.Arrays;
import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import de.h_brs.webeng.whiteboard.backend.domain.User;

@Path("users")
public class UserService {
	
	//private static final Logger LOG = LoggerFactory.getLogger(UserService.class);
	
	@GET
    @Produces(MediaType.APPLICATION_JSON)
	public Collection<User> getAllUsers() {
		User mockUser = new User("test");
		return Arrays.asList(mockUser);
	}

}
