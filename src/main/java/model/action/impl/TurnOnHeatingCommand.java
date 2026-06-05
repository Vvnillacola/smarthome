package model.action.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import model.ActionType;
import model.action.BaseCommand;
import model.State;
import model.device.impl.Heating;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TurnOnHeatingCommand extends BaseCommand {

    private final Heating heating;


    @JsonCreator
    public TurnOnHeatingCommand(
            @JsonProperty("id") String id,
            @JsonProperty("device") Heating heating,
            @JsonProperty("actionType") ActionType actionType,
            @JsonProperty("orderIndex") int orderIndex
    ) {
        super(id, heating, actionType, orderIndex);
        this.heating = heating;
    }

    @Override
    public void execute() {
        if (heating != null) {
            heating.setState(State.TURNED_ON);
        }
    }
}