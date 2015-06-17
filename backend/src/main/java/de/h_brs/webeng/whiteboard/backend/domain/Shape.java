package de.h_brs.webeng.whiteboard.backend.domain;

import java.io.Serializable;
import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public abstract class Shape implements Serializable  {
	private static final long serialVersionUID = 9113616904249499836L;
	
    private UUID uuid;
    
    private String shapeKey;

    private String username;

    private ShapeType type;
    
    private Long wbID;
    
    
    public Shape(ShapeType type, String username, Long wbID) {
    	this.uuid = UUID.randomUUID();
    	this.wbID = wbID;
    	this.shapeKey = "whiteboard:"+wbID+":shape:"+uuid;
    	this.type = type;
    	this.username = username;
    }
}
