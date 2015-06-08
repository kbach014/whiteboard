package de.h_brs.webeng.whiteboard.backend.dao.exception;

public class PasswordIncorrectException extends Exception {
	private static final long serialVersionUID = -31250202112235989L;

	public PasswordIncorrectException() {
		super("The password is incorrect!");
	}
}
