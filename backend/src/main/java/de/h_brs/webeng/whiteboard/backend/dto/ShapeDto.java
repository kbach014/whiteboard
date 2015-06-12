package de.h_brs.webeng.whiteboard.backend.dto;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import de.h_brs.webeng.whiteboard.backend.domain.Color;
import de.h_brs.webeng.whiteboard.backend.domain.Point;
import de.h_brs.webeng.whiteboard.backend.domain.ShapeType;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class ShapeDto implements Serializable {
	
	private static final long serialVersionUID = -9074792725786133689L;
	
	private UUID uuid;
	private ShapeType type;
	private boolean finished;
	private Color color;
	
	// paths:
	private List<Point> points;
	
	// rects:
	private Point p1;
	private Point p2;

}
