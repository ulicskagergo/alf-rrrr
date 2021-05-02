package hu.bme.aut.server.domain.restapi;

import java.time.LocalTime;

// used to handle incoming and outgoing (GET and POST) light settings - not used in the actual LightModel
public class LightSettingsBody {
	private Integer sensitivity; // between 0-100
	private String from; // e.g. 15:00
	private String to; // e.g. 19:00

	public LightSettingsBody(Integer sensitivity, LocalTime from, LocalTime to) {
		this.sensitivity = sensitivity;
		this.from = from.getHour() + ":" + from.getMinute();
		this.to = to.getHour() + ":" + to.getMinute();
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
