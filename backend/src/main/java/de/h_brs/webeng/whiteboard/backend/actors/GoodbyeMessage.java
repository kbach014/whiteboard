package de.h_brs.webeng.whiteboard.backend.actors;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GoodbyeMessage {
	private String sessionId;
}
