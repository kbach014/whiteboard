package de.h_brs.webeng.whiteboard.backend.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper=false)
public class Rectangle extends Shape {
	private static final long serialVersionUID = 2659772471979744128L;

	private Point coordinate;

    private int width;

    private int height;
    
    private Rectangle(String username, String whiteboardID) {
		super(ShapeType.RECT, username, whiteboardID);
	}
    
    public Rectangle(String username, String whiteboardID,  Point coordinate, int width, int height) {
    	this(username, whiteboardID);
    	this.coordinate = coordinate;
    	this.width = width;
    	this.height = height;
	}
    
    public Rectangle(User user, Whiteboard whiteboard,  Point coordinate, int width, int height) {
    	this(user.getUsername(), String.valueOf(whiteboard.getWbid()));
    	this.coordinate = coordinate;
    	this.width = width;
    	this.height = height;
	}
    
    @Override
    public String toString() {
    	String s = "----- Rectangle ----\n"
    			+ "ID: "+this.getShapeKey()+"\n"
    			+ "X: "+getCoordinate().getX()+", "
    			+ "Y: "+getCoordinate().getY()+"\n"
    			+ "W: "+getWidth()+", "
    			+ "H: "+getHeight();
    	return s;
    }
}

