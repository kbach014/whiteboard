package de.h_brs.webeng.whiteboard.backend.dao.exception;

public class IllegalShapeException extends Exception {
	private static final long serialVersionUID = 4347619341431466203L;

	public IllegalShapeException() {
		super("WARNING! Shape is not ready to persist!");
	}
}
