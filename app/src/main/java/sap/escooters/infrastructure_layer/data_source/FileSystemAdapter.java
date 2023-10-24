package sap.escooters.infrastructure_layer.data_source;

import java.io.*;
import java.nio.charset.StandardCharsets;

import io.vertx.core.json.JsonObject;
import sap.escooters.business_logic_layer.exceptions.DataSourceException;
import sap.escooters.business_logic_layer.DataSourcePort;

public class FileSystemAdapter implements DataSourcePort {

	private final String USERS_PATH = "users";
	private final String ESCOOTERS_PATH = "escooters";
	private final String RIDES_PATH = "rides";
	
	private final String dbaseFolder;
	
	public FileSystemAdapter(final String dbaseFolder) {
		this.dbaseFolder =  dbaseFolder;
	}

	public void init() {
        this.makeDir(this.dbaseFolder);
        this.makeDir(this.dbaseFolder + File.separator + this.USERS_PATH);
        this.makeDir(this.dbaseFolder + File.separator + this.ESCOOTERS_PATH);
        this.makeDir(this.dbaseFolder + File.separator + this.RIDES_PATH);
	}	

	@Override
	public void saveUser(final JsonObject user) throws DataSourceException {
		saveObj(this.USERS_PATH, user.getString("id"), user);
	}

	@Override
	public void saveEScooter(final JsonObject escooter) throws DataSourceException {
		saveObj(this.ESCOOTERS_PATH, escooter.getString("id"), escooter);
	}

	@Override
	public void saveRide(final JsonObject ride) throws DataSourceException {
		saveObj(this.RIDES_PATH, ride.getString("id"), ride);
	}

	private void saveObj(final String db, final String id, final JsonObject obj) throws DataSourceException {
		try {									
			final FileWriter fw = new FileWriter(this.dbaseFolder + File.separator + db + File.separator + id + ".json", StandardCharsets.UTF_8);
			final java.io.BufferedWriter wr = new BufferedWriter(fw);
			wr.write(obj.encodePrettily());
			wr.flush();
			fw.close();
		} catch (final Exception ex) {
			ex.printStackTrace();
			throw new DataSourceException(ex.getMessage());
		}
	}
	
	private void makeDir(final String name) {
		try {
			final File dir = new File(name);
			if (!dir.exists()) {
				dir.mkdir();
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	
}
