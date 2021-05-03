package hu.bme.aut.server.domain.database;

import org.json.simple.JSONObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "MeasurementDays")
public class MeasurementDay {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id = 0;

	@Column(name = "measurementDayDate", unique = true)
	private LocalDate measurementDayDate;

	public MeasurementDay() { prePersist(); }

	@PrePersist
	private void prePersist() {
		if (measurementDayDate == null) measurementDayDate = LocalDate.now();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public LocalDate getMeasureDate() {
		return measurementDayDate;
	}

	@Override
	public String toString() {
		return "[ DEBUG ] id: " + id + ", " + toJSON().toString();
		//return String.format("[ %d, %s, %b, %d, %d ]", id, measureDate, isOn, threshold, actualValue);
	}

	public JSONObject toJSON() {
		JSONObject obj = new JSONObject();
		obj.put("measure_date", measurementDayDate);   //
		return obj;
	}
}
