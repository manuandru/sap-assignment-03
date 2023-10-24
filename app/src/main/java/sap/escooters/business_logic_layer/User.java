package sap.escooters.business_logic_layer;

import io.vertx.core.json.JsonObject;

public class User {

	private final String id;
	private final String name;
	private final String surname;
	
	
	public User(final String id, final String name, final String surname) {
		this.id = id;
		this.name = name;
		this.surname = surname;
	}
	
	public String getId() {
		return this.id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getSurname() {
		return this.surname;
	}
	
	public void save() {
		try {
			DomainModelImpl.getDataSourcePort().saveUser(this.toJson());
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public JsonObject toJson() {
		final JsonObject userObj = new JsonObject();
		userObj.put("id", this.id);
		userObj.put("name", this.name);
		userObj.put("surname", this.surname);
		return userObj;
	}
	
	
}
