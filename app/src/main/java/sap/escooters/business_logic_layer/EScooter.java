package sap.escooters.business_logic_layer;

import java.util.Optional;

import io.vertx.core.json.JsonObject;

public class EScooter  {

	private final String id;
	public enum EScooterState { AVAILABLE, IN_USE, MAINTENANCE}	
	private EScooterState state;
	private Optional<Location> loc;
	
	public EScooter(final String id) {
		this.id = id;
		state = EScooterState.AVAILABLE;
		loc = Optional.empty();
	}
	
	public String getId() {
		return this.id;
	}

	public EScooterState getState() {
		return this.state;
	}
	
	public boolean isAvailable() {
		return this.state == EScooterState.AVAILABLE;
	}

	public void updateState(final EScooterState state) {
		this.state = state;
        this.save();
	}
	
	public void updateLocation(final Location newLoc) {
        this.loc = Optional.of(newLoc);
        this.save();
	}
	
	public Optional<Location> getCurrentLocation(){
		return this.loc;
	}
	
	public void save() {
		try {
			DomainModelImpl.getDataSourcePort().saveEScooter(this.toJson());
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public JsonObject toJson() {
		final JsonObject scooterObj = new JsonObject();
		scooterObj.put("id", id);
		scooterObj.put("state", state.toString());
		final Optional<Location> loc = loc;
		if (loc.isPresent()) {
			final JsonObject locObj = new JsonObject();
			locObj.put("latitude", loc.get().getLatitude());
			locObj.put("longitude", loc.get().getLongitude());
			scooterObj.put("location", locObj);			
		} else {
			scooterObj.putNull("location");			
		}			
		return scooterObj;
	}

	
}
