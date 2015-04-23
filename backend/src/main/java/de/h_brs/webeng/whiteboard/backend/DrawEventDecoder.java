package de.h_brs.webeng.whiteboard.backend;

import java.io.IOException;
import java.io.Reader;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.h_brs.webeng.whiteboard.backend.domain.DrawEvent;

public final class DrawEventDecoder implements Decoder.TextStream<DrawEvent> {
	
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
	public DrawEvent decode(Reader reader) throws DecodeException, IOException {
		return objectMapper.readValue(reader, DrawEvent.class);
	}
	
}