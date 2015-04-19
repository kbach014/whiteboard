package de.h_brs.webeng.whiteboard.backend;

import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;
import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.concurrent.duration.Duration;
import akka.actor.ActorSystem;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

@ApplicationPath("/rest")
public class JerseyApplication extends ResourceConfig {

	private static final Logger LOG = LoggerFactory.getLogger(JerseyApplication.class);

	private final ActorSystem system;

	public JerseyApplication() {
		this.system = ActorSystem.create("ExampleSystem");
		if (LOG.isDebugEnabled()) {
			register(new LoggingFilter(new JerseyLoggerAdapter(), false));
		}
		register(new JacksonJsonProvider());
		packages("de.h_brs.webeng.whiteboard.backend.services");
	}

	@PreDestroy
	private void shutdown() {
		system.shutdown();
		system.awaitTermination(Duration.create(15, TimeUnit.SECONDS));
	}

	private static class JerseyLoggerAdapter extends java.util.logging.Logger {
		JerseyLoggerAdapter() {
			super("Jersey", null);
		}

		@Override
		public void info(String msg) {
			LOG.info(msg);
		}
	}

}
