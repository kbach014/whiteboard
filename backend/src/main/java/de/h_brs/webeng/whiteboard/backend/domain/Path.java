package de.h_brs.webeng.whiteboard.backend.domain;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@EqualsAndHashCode(callSuper=false)
public class Path extends Shape {
	private static final long serialVersionUID = -9008979126088002100L;
	
	private List<Point> points; 
	
	public Path(User user, Whiteboard whiteboard, List<Point> points) {
		super(ShapeType.PATH, user.getUsername(), String.valueOf(whiteboard.getWbid()));
		
		this.points = points;
	}
	
	public Path(String username, String whiteboardID, List<Point> points) {
		super(ShapeType.PATH, username, whiteboardID);
		
		this.points = points;
	}
	
	public Path(String username, Long whiteboardID, List<Point> points) {
		super(ShapeType.PATH, username, String.valueOf(whiteboardID));
		
		this.points = points;
	}
}
