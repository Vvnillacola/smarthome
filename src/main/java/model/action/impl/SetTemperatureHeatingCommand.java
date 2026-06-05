package model.action.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import model.ActionType;
import model.State;
import model.action.BaseCommand;
import model.device.impl.Heating;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SetTemperatureHeatingCommand extends BaseCommand {

    private final Heating heating;
    private final double temperature;


    @JsonCreator
    public SetTemperatureHeatingCommand(
            @JsonProperty("id") String id,
            @JsonProperty("device") Heating heating,
            @JsonProperty("actionType") ActionType actionType,
            @JsonProperty("orderIndex") int orderIndex,
            @JsonProperty("temperature") double temperature
    ) {
        super(id, heating, actionType, orderIndex);
        this.heating = heating;
        this.temperature = temperature;
    }

    @Override
    public void execute() {
        if (heating != null && heating.getState() == State.TURNED_ON) {
            heating.setTemperature(temperature);
        }
    }

    public double getTemperature() {
        return temperature;
    }
}