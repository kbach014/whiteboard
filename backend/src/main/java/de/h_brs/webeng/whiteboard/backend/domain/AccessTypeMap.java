package de.h_brs.webeng.whiteboard.backend.domain;

import java.util.HashMap;

public class AccessTypeMap extends HashMap<String, AccessType> {
	private static final long serialVersionUID = 1810530855324644809L;
	private static AccessTypeMap instance;
	
	private AccessTypeMap() {}
	
	public static AccessTypeMap getInstance() {
		if(instance == null) { 
			instance = new AccessTypeMap();
			instance.put("public", AccessType.PUBLIC);
			instance.put("private", AccessType.PRIVATE);
		}
		
		return instance;
	}
}
