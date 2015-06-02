package de.h_brs.webeng.whiteboard.backend.domain;

import javax.xml.bind.annotation.XmlEnum;

@XmlEnum(String.class)
public enum ShapeType {
	RECT,
	PATH,
	CIRCLE,
	TEXT;
}