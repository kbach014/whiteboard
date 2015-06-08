package de.h_brs.webeng.whiteboard.backend.dao.exception;

import de.h_brs.webeng.whiteboard.backend.domain.User;

public class UserNotFoundException extends Exception {
	private static final long serialVersionUID = -5717090392482109651L;

	public UserNotFoundException() {
		super("No such user exists in database!");
	}
	
	public UserNotFoundException(String username) {
		super("\""+username+"\" not found in database!");
	}
	
	public UserNotFoundException(User user) {
		this(user.getUsername());
	}
}
