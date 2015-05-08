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
public class FinishedShape implements Serializable {
	
	private static final long serialVersionUID = 9113616904249499836L;
	
	private UUID shapeUuid;
	private ShapeType shape;
	private String coords;
	private String path;
	private String text;

}
