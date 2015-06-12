package de.h_brs.webeng.whiteboard.backend;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.List;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.h_brs.webeng.whiteboard.backend.dto.DrawEventDto;

public final class DrawEventsDecoder implements Decoder.TextStream<Collection<DrawEventDto>> {
	
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
	public List<DrawEventDto> decode(Reader reader) throws DecodeException, IOException {
		return objectMapper.readValue(reader, objectMapper.getTypeFactory().constructCollectionType(List.class, DrawEventDto.class));
	}
	
}