package de.h_brs.webeng.whiteboard.backend.domain;

import java.util.ArrayList;
import java.util.List;

public class Path extends Shape {
	private static final long serialVersionUID = -9008979126088002100L;
	
	private List<Point> points;
	
	public Path(String username, String whiteboardID) {
		super(ShapeType.PATH, username, whiteboardID);
		// TODO Ist dies die richtige Datenstruktur?
		points = new ArrayList<Point>();
	}
	
	public Path(String username, String whiteboardID, List<Point> points) {
		this(username, whiteboardID);
		this.points = points;
	}
}
