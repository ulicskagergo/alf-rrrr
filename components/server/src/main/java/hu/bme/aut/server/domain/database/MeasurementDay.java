package hu.bme.aut.server.domain.database;

import org.json.simple.JSONObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import java.time.LocalDate;

/**
 * Entity for measurement days
 * Represents MeasurementDay SQL table
 */
@Entity
@Table(name = "MeasurementDay")
public class MeasurementDay {

	/**
	 * Identifier generated automatically
	 */
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id = 0;

	/**
	 * Unique day of measurement in date format
	 */
	@Column(name = "measurement_day_date", unique = true)
	private LocalDate measurementDayDate;

	/**
	 * Constructor for prepersist
	 */
	public MeasurementDay() { prePersist(); }

	/**
	 * Constructor from given date
	 *
	 * @param localDate		given date
	 */
	public MeasurementDay(LocalDate localDate) {
		super();
		this.measurementDayDate = localDate;
	}

	/**
	 * If date is not given, generate today
	 */
	@PrePersist
	private void prePersist() {
		if (measurementDayDate == null) measurementDayDate = LocalDate.now();
	}

	/**
	 * Getters for parameters
	 */
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public LocalDate getMeasureDate() {
		return measurementDayDate;
	}

	/**
	 * Print measurement days in JSON format
	 *
	 * @return	days in string JSON format
	 */
	@Override
	public String toString() {
		return "id: " + id + ", " + toJSON().toString();
	}

	/**
	 * Construct JSON object
	 *
	 * @return	JSON object
	 */
	public JSONObject toJSON() {
		JSONObject obj = new JSONObject();
		obj.put("measure_date", measurementDayDate);
		return obj;
	}
}
