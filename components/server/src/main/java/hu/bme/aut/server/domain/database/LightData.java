package hu.bme.aut.server.domain.database;

import org.json.simple.JSONObject;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "LightData")
public class LightData {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id = 0;

    @Column(name = "measure_date")
    @Past
    private LocalDateTime measureDate;

    @Column(name = "is_on")
    @NotNull
    private boolean isOn;

    // TODO minmax validation
    @Column(name = "threshold")
    @Min(0)
    @Max(100000)
    private int threshold;

    // 50-3000 - 0-100 map és 50 alatt 0, 3000 felett 100
    @Column(name = "actual_value")
    @Min(0)
    @Max(100000)
    private int actualValue;

    public LightData(){
        prePersist();
    }

    public LightData(LocalDateTime measureDate, boolean isOn, int threshold, int actualValue) {
        super();
        this.measureDate = measureDate;
        this.isOn = isOn;
        this.threshold = threshold;
        this.actualValue = actualValue;
    }

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

    public boolean getIsOn() {
        return isOn;
    }

    public void setIsOn(boolean on) {
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
        return "[ DEBUG ]" + toJSON().toString();
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("measure_date", measureDate);   //
        obj.put("is_on", isOn);
        obj.put("threshold", threshold);        // %, sensitivity
        obj.put("actual_value", actualValue);   // %
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
