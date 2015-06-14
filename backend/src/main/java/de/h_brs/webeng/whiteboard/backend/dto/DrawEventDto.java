package de.h_brs.webeng.whiteboard.backend.dto;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlTransient;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class DrawEventDto implements Serializable {

	private static final long serialVersionUID = 5048139899638762015L;

	private EventType type;
	private ShapeDto shape;
	@XmlTransient
	private String username;

	@XmlEnum(String.class)
	public enum EventType {
		UPDATE, FINISH, CANCEL;
	}

}
