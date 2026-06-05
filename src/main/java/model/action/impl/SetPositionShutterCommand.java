package model.action.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import model.ActionType;
import model.State;
import model.action.BaseCommand;
import model.device.impl.Shutter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SetPositionShutterCommand extends BaseCommand {

    private final Shutter shutter;
    private final int position;

    @JsonCreator
    public SetPositionShutterCommand(
            @JsonProperty("id") String id,
            @JsonProperty("device") Shutter shutter,
            @JsonProperty("actionType") ActionType actionType,
            @JsonProperty("orderIndex") int orderIndex,
    @JsonProperty("position") int position
    ) {
        super(id, shutter, actionType, orderIndex);
        this.shutter = shutter;
        this.position = position;
    }

    @Override
    public void execute() {
        if (shutter != null && shutter.getState() == State.ROLLED_DOWN) {
            shutter.setPosition(position);
        }
    }

    public int getPosition() {
        return position;
    }
}