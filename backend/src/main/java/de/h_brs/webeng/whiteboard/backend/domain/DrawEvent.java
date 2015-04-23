package de.h_brs.webeng.whiteboard.backend.domain;

import java.util.UUID;

import javax.xml.bind.annotation.XmlEnum;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class DrawEvent {
	
	private UUID uuid;
	private Type type;
	private String coords;
	private String path;
	private String text;
	
	@XmlEnum(String.class)
	public enum Type {
		RECT,
		PATH,
		TEXT;
	}

}
