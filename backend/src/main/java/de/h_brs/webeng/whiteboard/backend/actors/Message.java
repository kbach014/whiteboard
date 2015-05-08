package de.h_brs.webeng.whiteboard.backend.actors;

public enum Message {
	/**
	 * Sent by upstream actor, when websocket session is established.
	 */
	HELLO_WHITEBOARD,
	
	/**
	 * Sent by upstream actor, when websocket session is terminated.
	 */
	GOODBYE_WHITEBOARD;

}
