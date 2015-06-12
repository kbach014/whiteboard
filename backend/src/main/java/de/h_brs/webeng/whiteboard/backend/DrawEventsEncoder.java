package de.h_brs.webeng.whiteboard.backend;

import java.io.IOException;
import java.io.Writer;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.h_brs.webeng.whiteboard.backend.dto.DrawEventDto;

public final class DrawEventsEncoder implements Encoder.TextStream<DrawEventDto> {
	
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void init(EndpointConfig config) {
		// noop
	}

	@Override
	public void destroy() {
		// noop
	}

	@Override
	public void encode(DrawEventDto event, Writer writer) throws EncodeException, IOException {
		objectMapper.writeValue(writer, event);
	}
	
}