package sap.escooters.infrastructure_layer.ui;

import java.util.logging.Logger;
import io.vertx.core.Vertx;
import sap.escooters.application_layer.*;

public class WebBasedUIAdapter implements RideDashboardPort {
    static Logger logger = Logger.getLogger("[EScooter Server]");	
	private final int port;
	private EScooterManServer server;
	
	public WebBasedUIAdapter(final int port) {
		this.port = port;
	}
		
	public void init(final ApplicationAPI appAPI) {
    	final Vertx vertx = Vertx.vertx();
		server = new EScooterManServer(this.port, appAPI);
		vertx.deployVerticle(this.server);
	}

	@Override
	public void notifyNumOngoingRidesChanged(final int nOngoingRides) {
        this.server.notifyNumOngoingRidesChanged(nOngoingRides);
	}
}
