package de.h_brs.webeng.whiteboard.backend.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode(callSuper=false)
@ToString
public class Circle extends Shape {
	private static final long serialVersionUID = -4698733956568825077L;
	
	private Point coordinate;

    private int radius;
    
    public Circle(User artist) {
    	super(ShapeType.CIRCLE, artist);
    }
    
    public Circle(User artist, Point coordinate, int radius) {
    	this(artist);    	
    	this.coordinate = coordinate;
    	this.radius = radius;
    }
}
