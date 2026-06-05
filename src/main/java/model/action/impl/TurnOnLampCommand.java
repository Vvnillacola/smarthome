package model.action.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import model.ActionType;
import model.action.BaseCommand;
import model.State;
import model.device.impl.Lamp;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TurnOnLampCommand extends BaseCommand {

    private final Lamp lamp;


    @JsonCreator
    public TurnOnLampCommand(
            @JsonProperty("id") String id,
            @JsonProperty("device") Lamp lamp,
            @JsonProperty("actionType") ActionType actionType,
            @JsonProperty("orderIndex") int orderIndex
    ) {
        super(id, lamp, actionType, orderIndex);
        this.lamp = lamp;
    }

    @Override
    public void execute() {
        if (lamp != null) {
            lamp.setState(State.TURNED_ON);
        }
    }
}