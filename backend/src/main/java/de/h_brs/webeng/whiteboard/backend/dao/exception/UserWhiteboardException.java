package de.h_brs.webeng.whiteboard.backend.dao.exception;

import de.h_brs.webeng.whiteboard.backend.domain.User;
import de.h_brs.webeng.whiteboard.backend.domain.Whiteboard;


public class UserWhiteboardException extends Exception {
	private static final long serialVersionUID = 7452531290064362478L;

	public UserWhiteboardException(User user, Whiteboard whiteboard) {
		super(user.getUsername() + " is already registered for " + 
				"whiteboad#" + whiteboard.getWbid());
	}
}
