package hu.bme.aut.server.domain.restapi;

import java.time.LocalTime;

// used to handle incoming and outgoing (GET and POST) light settings - not used in the actual LightModel
public class LightSettingsBody {
	private Integer sensitivity; // between 0-100
	private String from; // e.g. 15:00
	private String to; // e.g. 19:00

	public LightSettingsBody(Integer sensitivity, LocalTime from, LocalTime to) {
		this.sensitivity = sensitivity;
		this.from = String.format("%02d", from.getHour()) + ":" + String.format("%02d", from.getMinute());
		this.to = String.format("%02d", to.getHour()) + ":" + String.format("%02d", to.getMinute());
	}

	public String getTo() {
		return to;
	}

	public String getFrom() {
		return from;
	}

	public Integer getSensitivity() {
		return sensitivity;
	}
}
