package de.h_brs.webeng.whiteboard.backend;

import scala.collection.immutable.IndexedSeq;
import akka.actor.ActorSystem;
import akka.routing.CustomRouterConfig;
import akka.routing.Routee;
import akka.routing.Router;
import akka.routing.RoutingLogic;

public class WhiteboardRouter extends CustomRouterConfig {
	
	private static final long serialVersionUID = -1099134892749508814L;

	@Override
	public Router createRouter(ActorSystem system) {
		return new Router(new RoutingLogic() {
			
			@Override
			public Routee select(Object message, IndexedSeq<Routee> routees) {
				return null;
			}
		});
	}

}
