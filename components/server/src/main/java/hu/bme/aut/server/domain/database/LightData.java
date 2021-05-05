package hu.bme.aut.server.domain.database;

import hu.bme.aut.server.domain.LightModel;
import org.json.simple.JSONObject;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * Entity for measured light data
 * Represents LightData SQL table
 */
@Entity
@Table(name = "LightData")
public class LightData {

    /**
     * Identifier generated automatically
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id = 0;

    /**
     * Timestamp of measurement
     */
    @Column(name = "measure_date")
    @Past
    private LocalDateTime measureDate;

    /**
     * Switch for measurement
     */
    @Column(name = "is_on")
    @NotNull
    private boolean isOn;

    /**
     * Sensitivity of light
     */
    @Column(name = "threshold")
    @Min(0)
    @Max(100000)
    private int threshold;

    /**
     * Actual measured value
     */
    @Column(name = "actual_value")
    @Min(0)
    @Max(100000)
    private int actualValue;

    /**
     * Constructor for prepersist
     */
    public LightData(){
        prePersist();
    }

    /**
     * Constructor from given data
     *
     * @param measureDate   timestamp of measurement
     * @param isOn          is light turned in
     * @param threshold     sensitivity of light
     * @param actualValue   actual measured value
     */
    public LightData(LocalDateTime measureDate, boolean isOn, int threshold, int actualValue) {
        super();
        this.measureDate = measureDate;
        this.isOn = isOn;
        this.threshold = threshold;
        this.actualValue = actualValue;
    }

    /**
     * If timestamp is not given, generate actual time
     */
    @PrePersist
    private void prePersist() {
        if (measureDate == null) measureDate = LocalDateTime.now();
    }

    /**
     * Getters and setters for parameters
     */
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getMeasureDate() {
        return measureDate;
    }

    public void setMeasureDate(LocalDateTime measureDate) {
        this.measureDate = measureDate;
    }

    public boolean getIsOn() {
        return isOn;
    }

    public void setIsOn(boolean on) {
        isOn = on;
    }

    public int getThreshold() {
        return microsecToPercentage(threshold);
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public int getActualValue() {
        return actualValue;
    }

    public void setActualValue(int actualValue) {
        this.actualValue = actualValue;
    }

    /**
     * Print light data in JSON format
     *
     * @return  light data in string JSON format
     */
    @Override
    public String toString() {
        return toJSON().toString();
    }

    /**
     * Construct JSON object
     *
     * @return	JSON object
     */
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("measure_date", measureDate);
        obj.put("is_on", isOn);
        obj.put("threshold", microsecToPercentage(threshold));
        obj.put("actual_value", actualValue);
        return obj;
    }

    /**
     * Convert microsec to percentage (used when sending data to frontend)
     *
     * @param microsec   to be converted microsec
     * @return                  converted percentages
     */
    private static int microsecToPercentage(int microsec) {
        int microsecHigh = LightModel.getMicrosecHigh();
        if(microsec>microsecHigh) {
            microsec = microsecHigh;
        }
        double percentage = Math.log((double)microsec/(double)microsecHigh*Math.pow(1.03, 100))/Math.log(1.03);
        return (int) Math.round(percentage);
    }

    /**
     * Generate hash code for database
     *
     * @return  hash code
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    /**
     * Compare to light data
     *
     * @param obj   to be compared object
     * @return      true if the same
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LightData other = (LightData) obj;
        if (id == null) {
            return other.id == null;
        } else return id.equals(other.id);
    }
}
