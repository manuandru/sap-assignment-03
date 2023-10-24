package sap.escooters.application_layer;

import java.util.Optional;

import io.vertx.core.json.JsonObject;
import sap.escooters.application_layer.exceptions.*;
import sap.escooters.business_logic_layer.*;

public class ApplicationLayerImpl implements ApplicationAPI {

	private DomainModel domainLayer;
	private RideDashboardPort rideDashboardPort;
	
	public void init(final DomainModel layer, final RideDashboardPort adapter) {
		domainLayer = layer;
		rideDashboardPort = adapter;
	}

	@Override
	public void registerNewUser(final String id, final String name, final String surname) throws UserIdAlreadyExistingException {
		final Optional<User> user = this.domainLayer.getUser(id);
		if (user.isEmpty()) {
            this.domainLayer.addNewUser(id, name, surname);
		} else {
			throw new UserIdAlreadyExistingException();
		}
	}

	@Override
	public JsonObject getUserInfo(final String id) throws UserNotFoundException  {
		final Optional<User> user = this.domainLayer.getUser(id);
		if (user.isPresent()) {
			return user.get().toJson();
		} else {
			throw new UserNotFoundException();
		}
	}

	
	@Override
	public void registerNewEScooter(final String id) throws UserIdAlreadyExistingException {
        this.domainLayer.addNewEScooter(id);
	}

	@Override
	public JsonObject getEScooterInfo(final String id) throws EScooterNotFoundException  {
		final Optional<EScooter> escooter = this.domainLayer.getEScooter(id);
		if (escooter.isPresent()) {
			return escooter.get().toJson();
		} else {
			throw new EScooterNotFoundException();
		}
	}

	@Override
	public String startNewRide(final String userId, final String escooterId) throws RideNotPossibleException {
		final Optional<User> user = this.domainLayer.getUser(userId);
		final Optional<EScooter> escooter = this.domainLayer.getEScooter(escooterId);
		if (user.isPresent() && escooter.isPresent()) {
			final EScooter sc = escooter.get();
			if (sc.isAvailable()) {
				final String id = this.domainLayer.startNewRide(user.get(), escooter.get());
				rideDashboardPort.notifyNumOngoingRidesChanged(this.domainLayer.getNumOnoingRides());
				return id;
			} else {
				throw new RideNotPossibleException();
			}
		} else {
			throw new RideNotPossibleException();
		}
	}
	
	@Override
	public JsonObject getRideInfo(final String id) throws RideNotFoundException  {
		final Optional<Ride> ride = this.domainLayer.getRide(id);
		if (ride.isPresent()) {
			return ride.get().toJson();
		} else {
			throw new RideNotFoundException();
		}
	}

	@Override
	public void endRide(final String rideId) throws RideNotFoundException, RideAlreadyEndedException {
		final Optional<Ride> ride = this.domainLayer.getRide(rideId);
		if (ride.isPresent()) {
			final Ride ri = ride.get();
			if (ri.isOngoing()) {
				ri.end();
				rideDashboardPort.notifyNumOngoingRidesChanged(this.domainLayer.getNumOnoingRides());
			} else {
				throw new RideAlreadyEndedException();
			}
		} else {
			throw new RideNotFoundException();
		}
	}
}
