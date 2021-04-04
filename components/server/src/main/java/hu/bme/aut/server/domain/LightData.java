package hu.bme.aut.server.domain;

import org.hibernate.annotations.GenericGenerator;
import org.json.simple.JSONObject;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "LightData")
public class LightData {

    //TODO add validations

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id = 0;

    @Column(name = "measure_date")
    private LocalDateTime measureDate;

    @Column(name = "is_on")
    private boolean isOn;

    @Column(name = "threshold")
    private int threshold;

    @Column(name = "actual_value")
    private int actualValue;

    public LightData(){}

    public LightData(LocalDateTime measureDate, boolean isOn, int threshold, int actualValue) {
        super();
        this.measureDate = measureDate;
        this.isOn = isOn;
        this.threshold = threshold;
        this.actualValue = actualValue;
    }

    /*public LightData(LocalDateTime measureDate, boolean isOn, int threshold) {
        super();
        this.measureDate = measureDate;
        this.isOn = isOn;
        this.threshold = threshold;
    }

    public LightData(boolean isOn, int threshold) {
        super();
        this.measureDate = LocalDateTime.now();
        this.isOn = isOn;
        this.threshold = threshold;
    }*/

    @PrePersist
    private void prePersist() {
        if (measureDate == null) measureDate = LocalDateTime.now();
    }

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

    public boolean isOn() {
        return isOn;
    }

    public void setOn(boolean on) {
        isOn = on;
    }

    public int getThreshold() {
        return threshold;
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

    @Override
    public String toString() {
        return String.format("[ %d, %s, %b, %d, %d ]", id, measureDate, isOn, threshold, actualValue);
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("measure_date", measureDate);
        obj.put("is_on", isOn);
        obj.put("threshold", threshold);
        obj.put("actual_value", actualValue);
        // TODO write to file
        System.out.println(obj);
        return obj;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

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
