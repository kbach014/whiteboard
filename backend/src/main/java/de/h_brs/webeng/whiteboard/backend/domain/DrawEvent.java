package de.h_brs.webeng.whiteboard.backend.domain;

import java.io.Serializable;
import java.util.UUID;

import javax.xml.bind.annotation.XmlEnum;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class DrawEvent implements Serializable {
	
	private static final long serialVersionUID = 5048139899638762015L;
	
	private UUID shapeUuid;
	private UUID eventUuid;
	// Which Type of Event? (start drawing, update, finish, cancel)
	private EventType type;
	// Which shape is drawn? (rect, path, text...)
	private ShapeType shape;
	private String coords;
	private String path;
	private String text;
	
	@XmlEnum(String.class)
	public enum EventType {
		START,
		UPDATE,
		FINISH,
		CANCEL;
	}

}
