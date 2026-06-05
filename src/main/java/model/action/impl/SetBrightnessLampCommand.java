package model.action.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import model.ActionType;
import model.State;
import model.action.BaseCommand;
import model.device.impl.Lamp;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SetBrightnessLampCommand extends BaseCommand {

    private final Lamp lamp;
    private final int brightness;


    @JsonCreator
    public SetBrightnessLampCommand(
            @JsonProperty("id") String id,
            @JsonProperty("device") Lamp lamp,
            @JsonProperty("actionType") ActionType actionType,
            @JsonProperty("orderIndex") int orderIndex,
            @JsonProperty("brightness") int brightness
    ) {
        super(id, lamp, actionType, orderIndex);
        this.lamp = lamp;
        this.brightness = brightness;
    }

    @Override
    public void execute() {
        if (lamp != null && lamp.getState() == State.TURNED_ON) {
            lamp.setBrightness(brightness);
        }
    }

    public int getBrightness() {
        return brightness;
    }
}