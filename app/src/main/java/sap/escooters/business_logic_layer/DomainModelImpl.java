package sap.escooters.business_logic_layer;

import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DomainModelImpl implements DomainModel {

	private static DataSourcePort dataSourcePort;
	
	private final HashMap<String, User> users;
	private final HashMap<String, EScooter> escooters;
	private final HashMap<String, Ride> rides;
	private int rideCounter;
    static Logger logger = Logger.getLogger("[DomainModel]");	
	
	public DomainModelImpl() {
        this.users = new HashMap<String, User>();
        this.escooters = new HashMap<String, EScooter>();
        this.rides = new HashMap<String, Ride>();
	}
	
	public void init(final DataSourcePort port) {
        DomainModelImpl.dataSourcePort = port;
        this.rideCounter = 0;
	}
	
	public static DataSourcePort getDataSourcePort() {
		return DomainModelImpl.dataSourcePort;
	}
	
	@Override
	public void addNewUser(final String id, final String name, final String surname) {
		final User user = new User(id, name, surname);
        this.users.put(id, user);
		user.save();
        DomainModelImpl.logger.log(Level.INFO, "New user registered: " + id);
	}

	@Override
	public void addNewEScooter(final String id) {
		final EScooter escooter = new EScooter(id);
        this.escooters.put(id, escooter);
		escooter.save();
        DomainModelImpl.logger.log(Level.INFO, "New escooter registered: " + id);
	}
	
	@Override
	public String startNewRide(final User user, final EScooter escooter) {
		escooter.updateState(EScooter.EScooterState.IN_USE);
        this.rideCounter++;
		final String rideId = "ride-" + this.rideCounter;
		final Ride ride = new Ride(rideId, user, escooter);
        this.rides.put(rideId, ride);
		escooter.save();
		ride.save();
        DomainModelImpl.logger.log(Level.INFO, "Started ride: " + rideId);
		return rideId;
	}

	@Override
	public Optional<User> getUser(final String userId) {
		return Optional.ofNullable(this.users.get(userId));
	}

	@Override
	public Optional<EScooter> getEScooter(final String id) {
		return Optional.ofNullable(this.escooters.get(id));
	}

	@Override
	public Optional<Ride> getRide(final String id) {
		return Optional.ofNullable(this.rides.get(id));
	}

	@Override
	public int getNumOnoingRides() {
		return this.rideCounter;
	}
}
