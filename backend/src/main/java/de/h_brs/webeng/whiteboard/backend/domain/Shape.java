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

    private User artist;

    private ShapeType type;
    
    public Shape(ShapeType type, User artist) {
    	this.uuid = UUID.randomUUID();
    	this.type = type;
    	this.artist = artist;
    }
}
