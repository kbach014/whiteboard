package de.h_brs.webeng.whiteboard.backend.domain;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@EqualsAndHashCode(callSuper=false)
public class Path extends Shape {
	private static final long serialVersionUID = -9008979126088002100L;
	
	private String points; 
	
	public Path(String username, String whiteboardID, List<Point> coords) {
		super(ShapeType.PATH, username, whiteboardID);
		
		StringBuilder sb = new StringBuilder();
		for(int x=0; x<coords.size(); ++x) {
			sb.append(coords.get(x).getX());
			sb.append(",");
			sb.append(coords.get(x).getY());
			if(x != coords.size() - 1)
				sb.append(":");
		}
		points = sb.toString();
	}
}
