package de.h_brs.webeng.whiteboard.backend;

import java.io.IOException;
import java.io.Writer;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.h_brs.webeng.whiteboard.backend.domain.FinishedShape;

public final class FinishedShapeEncoder implements Encoder.TextStream<FinishedShape> {
	
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
	public void encode(FinishedShape object, Writer writer) throws EncodeException, IOException {
		objectMapper.writeValue(writer, object);
	}
	
}