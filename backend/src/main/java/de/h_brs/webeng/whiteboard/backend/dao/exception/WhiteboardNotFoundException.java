package de.h_brs.webeng.whiteboard.backend.dao.exception;

public class WhiteboardNotFoundException extends Exception {
	private static final long serialVersionUID = 3834275191724010013L;

	public WhiteboardNotFoundException() {
		super("No such user exists in database!");
	}
	
	public WhiteboardNotFoundException(String wbid) {
		super("Whiteboard#"+wbid+" not found in database!");
	}
	
	public WhiteboardNotFoundException(Long wbid) {
		super("Whiteboard#"+wbid+" not found in database!");
	}
}
