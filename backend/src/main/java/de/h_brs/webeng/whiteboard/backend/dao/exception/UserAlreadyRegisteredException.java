package de.h_brs.webeng.whiteboard.backend.dao.exception;

public class UserAlreadyRegisteredException extends Exception {
	private static final long serialVersionUID = -7864051532984641239L;

	public UserAlreadyRegisteredException() {
		super("This is already registered and cannot be registered twice!");
	}
	
	public UserAlreadyRegisteredException(String username) {
		super("User \""+username+"\" is already registered and cannot be registered twice!");
	}
}
