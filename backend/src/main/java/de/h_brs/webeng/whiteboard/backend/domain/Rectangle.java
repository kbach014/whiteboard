package de.h_brs.webeng.whiteboard.backend.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper=false)
public class Rectangle extends Shape {
	private static final long serialVersionUID = 2659772471979744128L;

	private Point p1, p2;
    
    private Rectangle(String username, String whiteboardID) {
		super(ShapeType.RECT, username, whiteboardID);
	}
    
    public Rectangle(String username, String whiteboardID,  Point p1, Point p2) {
    	this(username, whiteboardID);
    	this.p1 = p1;
    	this.p2 = p2;
	}
    
    public Rectangle(User user, Whiteboard whiteboard, Point p1, Point p2) {
    	this(user.getUsername(), String.valueOf(whiteboard.getWbid()));
    	this.p1 = p1;
    	this.p2 = p2;
	}
    
    @Override
    public String toString() {
    	String s = "----- Rectangle ----\n"
    			+ "ID: "+this.getShapeKey()+"\n"
    			+ "X1: "+p1.getX()+", "
    			+ "Y1: "+p2.getY()+"\n"
    			+ "W: "+(p2.getX() - p1.getX())+", "
    			+ "H: "+(p2.getY() - p1.getY());
    	return s;
    }
}

