package de.h_brs.webeng.whiteboard.backend.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode(callSuper=false)
@ToString
public class Rectangle extends Shape {
	private static final long serialVersionUID = 2659772471979744128L;

	private Point coordiante;

    private int width;

    private int height;
    
    public Rectangle(User artist) {
		super(ShapeType.RECT, artist);
	}
    
    public Rectangle(User artist, Point coordinate, int width, int height) {
    	this(artist);
    	this.coordiante = coordinate;
    	this.width = width;
    	this.height = height;
	}
}
