package de.h_brs.webeng.whiteboard.backend.dao.exception;


public class UserWhiteboardException extends Exception {
	private static final long serialVersionUID = 7452531290064362478L;

	public UserWhiteboardException(String username, Long whiteboardId) {
		super(username + " is already registered for " + "whiteboad#" + whiteboardId);
	}
}
