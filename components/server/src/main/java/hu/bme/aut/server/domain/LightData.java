package hu.bme.aut.server.domain;

import org.hibernate.annotations.GenericGenerator;
import org.json.simple.JSONObject;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "LightData")
public class LightData {

    //TODO: Add validations

    @Id
    @Column(name = "id")
    //@GeneratedValue
    //@GenericGenerator(name = "inc-gen", strategy = "increment")
    private Integer id;

    @Column(name = "measure_date")
    private LocalDateTime measureDate;

    @Column(name = "is_on")
    private boolean isOn;

    @Column(name = "threshold")
    private int threshold;

    public LightData(){}

    public LightData(Integer id, LocalDateTime measureDate, boolean isOn, int threshold) {
        super();
        this.id = id;
        this.measureDate = measureDate;
        this.isOn = isOn;
        this.threshold = threshold;
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

    @Override
    public String toString() {
        return String.format("[ %d, %s, %b, %d ]", id, measureDate, isOn, threshold);
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("measure_date", measureDate);
        obj.put("is_on", isOn);
        obj.put("threshold", threshold);
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
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
