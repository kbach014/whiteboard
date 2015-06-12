package de.h_brs.webeng.whiteboard.backend.domain;

import javax.xml.bind.annotation.XmlEnum;

/**
 * Possible values are http://www.w3schools.com/cssref/css_colornames.asp
 */
@XmlEnum(String.class)
public enum Color {
	RED, GREEN, BLUE, MAGENTA, CYAN, YELLOW, BROWN;
}