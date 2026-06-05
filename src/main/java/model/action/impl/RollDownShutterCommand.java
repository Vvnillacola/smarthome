package model.action.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import model.ActionType;
import model.action.BaseCommand;
import model.State;
import model.device.impl.Shutter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RollDownShutterCommand extends BaseCommand {

    private final Shutter shutter;

    @JsonCreator
    public RollDownShutterCommand(
            @JsonProperty("id") String id,
            @JsonProperty("device") Shutter shutter,
            @JsonProperty("actionType") ActionType actionType,
            @JsonProperty("orderIndex") int orderIndex
    ) {
        super(id, shutter, actionType, orderIndex);
        this.shutter = shutter;
    }

    @Override
    public void execute() {
        if (shutter != null) {
            shutter.setState(State.ROLLED_DOWN);
            shutter.setPosition(100);
        }
    }
}