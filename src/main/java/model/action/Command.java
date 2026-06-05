package model.action;

import model.ActionType;
import model.device.Device;
import com.fasterxml.jackson.annotation.*;
import model.action.impl.RollDownShutterCommand;
import model.action.impl.RollUpShutterCommand;
import model.action.impl.SetBrightnessLampCommand;
import model.action.impl.SetPositionShutterCommand;
import model.action.impl.SetTemperatureHeatingCommand;
import model.action.impl.TurnOFfLampCommand;
import model.action.impl.TurnOffHeatingCommand;
import model.action.impl.TurnOnHeatingCommand;
import model.action.impl.TurnOnLampCommand;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "commandClassType",
        visible = false
)
@JsonSubTypes({
        // Shutter Commands
        @JsonSubTypes.Type(value = RollDownShutterCommand.class, name = "ShutterDown"),
        @JsonSubTypes.Type(value = RollUpShutterCommand.class, name = "ShutterUp"),
        @JsonSubTypes.Type(value = SetPositionShutterCommand.class, name = "ShutterPos"),

        // Heating Commands
        @JsonSubTypes.Type(value = TurnOffHeatingCommand.class, name = "HeatingOff"),
        @JsonSubTypes.Type(value = TurnOnHeatingCommand.class, name = "HeatingOn"),
        @JsonSubTypes.Type(value = SetTemperatureHeatingCommand.class, name = "HeatingTemp"),

        // Lamp Commands
        @JsonSubTypes.Type(value = TurnOnLampCommand.class, name = "LampOn"),
        @JsonSubTypes.Type(value = TurnOFfLampCommand.class, name = "LampOff"),
        @JsonSubTypes.Type(value = SetBrightnessLampCommand.class, name = "LampBrightness")
})
public interface Command {
    void execute();
    Device getDevice();

    ActionType getActionType();

    String getID();
    @Override
    String toString();
    int getOrderIndex();
    void setOrderIndex(int i);
}