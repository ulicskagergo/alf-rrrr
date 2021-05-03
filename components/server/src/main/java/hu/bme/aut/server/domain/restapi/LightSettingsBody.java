package hu.bme.aut.server.domain.restapi;

import java.time.LocalTime;

/**
 * Class for handling incoming ang outgoing light settings (GET and POST)
 */
public class LightSettingsBody {

	/**
	 * Light settings
	 * 		sensitivity of light	(between 0 and 100)
	 * 		measurement begin		(format: 15:00)
	 * 		measurement end			(format: 19:00)
	 */
	private Integer sensitivity;
	private String from;
	private String to;

	/**
	 * Constructor from given settings
	 *
	 * @param sensitivity	sensitivity of light
	 * @param from			measurement begin
	 * @param to			measurement end
	 */
	public LightSettingsBody(Integer sensitivity, LocalTime from, LocalTime to) {
		this.sensitivity = sensitivity;
		this.from = String.format("%02d", from.getHour()) + ":" + String.format("%02d", from.getMinute());
		this.to = String.format("%02d", to.getHour()) + ":" + String.format("%02d", to.getMinute());
	}

	/**
	 * Getters for parameters
	 */
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
