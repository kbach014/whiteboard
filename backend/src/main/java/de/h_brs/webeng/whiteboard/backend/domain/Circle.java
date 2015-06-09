package de.h_brs.webeng.whiteboard.backend.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper=false)
public class Circle extends Shape {
	private static final long serialVersionUID = -4698733956568825077L;
	
	private Point coordinate;

    private int radius;
    
    public Circle(String username, String whiteboardID) {
    	super(ShapeType.CIRCLE, username, whiteboardID);
    }
    
    public Circle(String username, String whiteboardID, Point coordinate, int radius) {
    	this(username, whiteboardID);    	
    	this.coordinate = coordinate;
    	this.radius = radius;
    }
    
    public Circle(User user, Whiteboard whiteboard, Point coordinate, int radius) {
    	this(user.getUsername(), String.valueOf(whiteboard.getWbid()));    	
    	this.coordinate = coordinate;
    	this.radius = radius;
    }
    
    
    @Override
    public String toString() {
    	String s = "----- Circle ----\n"
    			+ "ID: "+getShapeKey()+"\n"
    			+ "X: "+getCoordinate().getX()+", "
    			+ "Y: "+getCoordinate().getY()+"\n"
    			+ "R: "+getRadius();
    	return s;
    }
}
