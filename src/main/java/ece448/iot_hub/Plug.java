package ece448.iot_hub;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Generated;


public class Plug {

    @JsonProperty("name")
    private String name;

    @JsonProperty("state")
    private String state;

    @JsonProperty("power")
    private String power;

    public Plug(String name, String state, String power) {
        this.name = name;
        this.state = state;
        this.power = power;
    }

    public String getName() {
        return name;
    }

    // use lombok to exclude for jacoco
    @Generated
    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    // use lombok to exclude for jacoco
    @Generated
    public void setState(String state) {
        this.state = state;
    }

    public String getPower() {
        return power;
    }

    // use lombok to exclude for jacoco
    @Generated
    public void setPower(String power) {
        this.power = power;
    }

    @Generated
    @Override
    public String toString() {
        return String.format("{name=%s, state=%s, power=%s}", name, state, power);
    }
    
}
