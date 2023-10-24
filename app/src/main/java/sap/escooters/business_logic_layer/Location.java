package sap.escooters.business_logic_layer;

public class Location {
	private final double latitude;
    private final double longitude;
	
	public Location(final double lat, final double lon) {
		latitude = lat;
		longitude = lon;
	}
	
	public double getLatitude() {
		return this.latitude;
	}

	public double getLongitude() {
		return this.longitude;
	}

}
