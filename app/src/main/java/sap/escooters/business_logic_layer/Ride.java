package sap.escooters.business_logic_layer;

import java.util.Date;
import java.util.Optional;

import io.vertx.core.json.JsonObject;

public class Ride {

	private final Date startedDate;
	private Optional<Date> endDate;
	private final User user;
	private final EScooter scooter;
	private boolean ongoing;
	private final String id;
	
	public Ride(final String id, final User user, final EScooter scooter) {
		this.id = id;
		startedDate = new Date();
		endDate = Optional.empty();
		this.user = user;
		this.scooter = scooter;
        this.ongoing = true;
	}
	
	public String getId() {
		return this.id;
	}
	
	public void end() {
        this.endDate = Optional.of(new Date());
        this.ongoing = false;
        this.save();
	}

	public Date getStartedDate() {
		return this.startedDate;
	}

	public boolean isOngoing() {
		return ongoing;
	}
	
	public Optional<Date> getEndDate() {
		return this.endDate;
	}

	public User getUser() {
		return this.user;
	}

	public EScooter getEScooter() {
		return this.scooter;
	}
	
	public void save() {
		try {
			DomainModelImpl.getDataSourcePort().saveRide(this.toJson());
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public JsonObject toJson() {
		final JsonObject rideObj = new JsonObject();
		rideObj.put("id", id);
		rideObj.put("userId", user.getId());
		rideObj.put("escooterId", scooter.getId());
		rideObj.put("startDate", startedDate.toString());
		final Optional<Date> endDate = endDate;
		
		if (endDate.isPresent()) {
			rideObj.put("endDate", endDate.get().toString());			
		} else {
			rideObj.putNull("location");			
		}			
		return rideObj;
	}

	
}
