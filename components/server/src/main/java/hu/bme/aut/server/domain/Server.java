package hu.bme.aut.server.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.*;

@Entity
public class Server {

    @Id
    @GeneratedValue
    private Long id;

    @Positive
    private int lightThreshold;

    private boolean lightState;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getLightThreshold() {
        return lightThreshold;
    }

    public void setLightThreshold(int lightThreshold) {
        this.lightThreshold = lightThreshold;
    }

    public boolean getLightState() {
        return lightState;
    }

    public void setLightState(boolean lightState) {
        this.lightState = lightState;
    }
}
