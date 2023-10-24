package sap.escooters.infrastructure_layer.ui;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.*;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import sap.escooters.application_layer.*;
import sap.escooters.application_layer.exceptions.*;

public class EScooterManServer extends AbstractVerticle implements RideDashboardPort {

	private final int port;
	private final ApplicationAPI appAPI;
    static Logger logger = Logger.getLogger("[EScooter Server]");	

	public EScooterManServer(final int port, final ApplicationAPI appAPI) {
		this.port = port;
		this.appAPI = appAPI;
        EScooterManServer.logger.setLevel(Level.INFO);
	}
	
	
	public void start() {
        EScooterManServer.logger.log(Level.INFO, "EScooterMan server initializing...");
		final HttpServer server = this.vertx.createHttpServer();
		final Router router = Router.router(this.vertx);

		/* static files by default searched in "webroot" directory */
		router.route("/static/*").handler(StaticHandler.create().setCachingEnabled(false));
		router.route().handler(BodyHandler.create());
		
		router.route(HttpMethod.POST, "/api/users").handler(this::registerNewUser);
		router.route(HttpMethod.GET, "/api/users/:userId").handler(this::getUserInfo);
		router.route(HttpMethod.POST, "/api/escooters").handler(this::registerNewEScooter);
		router.route(HttpMethod.GET, "/api/escooters/:escooterId").handler(this::getEScooterInfo);
		router.route(HttpMethod.POST, "/api/rides").handler(this::startNewRide);
		router.route(HttpMethod.GET, "/api/rides/:rideId").handler(this::getRideInfo);
		router.route(HttpMethod.POST, "/api/rides/:rideId/end").handler(this::endRide);
		
		server.webSocketHandler(webSocket -> {
            EScooterManServer.logger.log(Level.INFO, "Ride monitoring request: " + webSocket.path());
			  
			  if ("/api/rides/monitoring".equals(webSocket.path())) {
				webSocket.accept();
                  EScooterManServer.logger.log(Level.INFO, "New ride monitoring observer registered.");
		    	final EventBus eb = this.vertx.eventBus();
		    	eb.consumer("ride-events", msg -> {
		    		final JsonObject ev = (JsonObject) msg.body();
                    EScooterManServer.logger.log(Level.INFO, "Changes in rides: " + ev.encodePrettily());
		    		webSocket.writeTextMessage(ev.encodePrettily());
		    	});
		    	/*
				webSocket.handler(buffer -> {
					
				});*/
			  } else {
                  EScooterManServer.logger.log(Level.INFO, "Ride monitoring observer rejected.");
				  webSocket.reject();
			  }
			});		
		
		
		
		server
		.requestHandler(router)
		.listen(this.port);

        EScooterManServer.logger.log(Level.INFO, "EScooterMan server ready - port: " + this.port);
	}
	
	protected void registerNewUser(final RoutingContext context) {
        EScooterManServer.logger.log(Level.INFO, "New registration user request - " + context.currentRoute().getPath());
		final JsonObject userInfo = context.body().asJsonObject();
        EScooterManServer.logger.log(Level.INFO, "Body: " + userInfo.encodePrettily());
		
		final String id = userInfo.getString("id");
		final String name = userInfo.getString("name");
		final String surname = userInfo.getString("surname");
		
		final JsonObject reply = new JsonObject();
		try {
            this.appAPI.registerNewUser(id, name, surname);
			reply.put("result", "ok");
		} catch (final UserIdAlreadyExistingException ex) {
			reply.put("result", "user-id-already-existing");
		}
        this.sendReply(context, reply);
	}
	
	protected void getUserInfo(final RoutingContext context) {
        EScooterManServer.logger.log(Level.INFO, "New user info request: " + context.currentRoute().getPath());
	    final String userId = context.pathParam("userId");
		final JsonObject reply = new JsonObject();
		try {
			final JsonObject info = this.appAPI.getUserInfo(userId);
			reply.put("result", "ok");
			reply.put("user", info);
		} catch (final UserNotFoundException ex) {
			reply.put("result", "user-not-found");
		}
        this.sendReply(context, reply);
	}

	protected void registerNewEScooter(final RoutingContext context) {
        EScooterManServer.logger.log(Level.INFO, "new EScooter registration request: " + context.currentRoute().getPath());
		final JsonObject escooterInfo = context.body().asJsonObject();
        EScooterManServer.logger.log(Level.INFO, "Body: " + escooterInfo.encodePrettily());
		
		final String id = escooterInfo.getString("id");
		
		final JsonObject reply = new JsonObject();
		try {
            this.appAPI.registerNewEScooter(id);
			reply.put("result", "ok");
		} catch (final UserIdAlreadyExistingException ex) {
			reply.put("result", "escooter-id-already-existing");
		}
        this.sendReply(context, reply);
	}

	protected void getEScooterInfo(final RoutingContext context) {
        EScooterManServer.logger.log(Level.INFO, "New escooter info request: " + context.currentRoute().getPath());
	    final String escooterId = context.pathParam("escooterId");
		final JsonObject reply = new JsonObject();
		try {
			final JsonObject info = this.appAPI.getEScooterInfo(escooterId);
			reply.put("result", "ok");
			reply.put("escooter", info);
		} catch (final EScooterNotFoundException ex) {
			reply.put("result", "escooter-not-found");
		}
        this.sendReply(context, reply);
	}
	
	protected void startNewRide(final RoutingContext context) {
        EScooterManServer.logger.log(Level.INFO, "Start new ride request: " + context.currentRoute().getPath());
		final JsonObject rideInfo = context.body().asJsonObject();
        EScooterManServer.logger.log(Level.INFO, "Body: " + rideInfo.encodePrettily());
		
		final String userId = rideInfo.getString("userId");
		final String escooterId = rideInfo.getString("escooterId");
		
		final JsonObject reply = new JsonObject();
		try {
			final String rideId = this.appAPI.startNewRide(userId, escooterId);
			reply.put("result", "ok");
			reply.put("rideId", rideId);
		} catch (final Exception  ex) {
			reply.put("result", "start-new-ride-failed");
		}
        this.sendReply(context, reply);
	}
	
	protected void getRideInfo(final RoutingContext context) {
		final String rideId = context.pathParam("rideId");
		final JsonObject reply = new JsonObject();
		try {
			final JsonObject info = this.appAPI.getRideInfo(rideId);
			reply.put("result", "ok");
			reply.put("ride", info);
		} catch (final RideNotFoundException ex) {
			reply.put("result", "ride-not-found");
		}
        this.sendReply(context, reply);
	}

	protected void endRide(final RoutingContext context) {
        EScooterManServer.logger.log(Level.INFO, "End ride request: " + context.currentRoute().getPath());
	    final String rideId = context.pathParam("rideId");
		final JsonObject reply = new JsonObject();
		try {
            this.appAPI.endRide(rideId);
			reply.put("result", "ok");
		} catch (final RideNotFoundException ex) {
			reply.put("result", "ride-not-found");
		} catch (final RideAlreadyEndedException ex) {
			reply.put("result", "ride-already-ended");
		}
        this.sendReply(context, reply);
	}
	
	@Override
	public void notifyNumOngoingRidesChanged(final int nOngoingRides) {
        EScooterManServer.logger.log(Level.INFO, "notify num rides changed");
		final EventBus eb = this.vertx.eventBus();
		
		final JsonObject obj = new JsonObject();
		obj.put("event", "num-ongoing-rides-changed");
		obj.put("nOngoingRides", nOngoingRides);
		
    	eb.publish("ride-events", obj);
	}
	
		
	private void sendReply(final RoutingContext request, final JsonObject reply) {
		final HttpServerResponse response = request.response();
		response.putHeader("content-type", "application/json");
		response.end(reply.toString());
	}
	
}
