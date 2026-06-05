package factory;

import junit.framework.TestCase;
import model.ActionType;
import model.State;
import model.action.Command;
import model.action.impl.*;
import model.device.impl.Heating;
import model.device.impl.Lamp;
import model.device.impl.Shutter;
import model.room.Raum;

public class CommandFactoryTest extends TestCase {

    private Raum raum;
    private Lamp lamp;
    private Heating heating;
    private Shutter shutter;

    @Override
    protected void setUp() {
        raum = new Raum("Wohnzimmer");
        lamp = new Lamp("L1", "Lampe", raum);
        heating = new Heating("H1", "Heizung", raum);
        shutter = new Shutter("S1", "Rollladen", raum);
    }

    // --- Lampe ---

    public void testCreateTurnOnLampCommand() {
        Command cmd = CommandFactory.create(lamp, ActionType.TURN_ON, null, 0);

        assertTrue(cmd instanceof TurnOnLampCommand);
        cmd.execute();
        assertEquals(State.TURNED_ON, lamp.getState());
    }

    public void testCreateTurnOffLampCommand() {
        lamp.setState(State.TURNED_ON);
        Command cmd = CommandFactory.create(lamp, ActionType.TURN_OFF, null, 0);

        assertTrue(cmd instanceof TurnOFfLampCommand); // Exakt wie das "OFf" in der Factory
        cmd.execute();
        assertEquals(State.TURNED_OFF, lamp.getState());
    }

    // --- Heizung ---

    public void testCreateTurnOnHeatingCommand() {
        Command cmd = CommandFactory.create(heating, ActionType.TURN_ON, null, 0);

        assertTrue(cmd instanceof TurnOnHeatingCommand);
        cmd.execute();
        assertEquals(State.TURNED_ON, heating.getState());
    }

    public void testCreateTurnOffHeatingCommand() {
        heating.setState(State.TURNED_ON);
        Command cmd = CommandFactory.create(heating, ActionType.TURN_OFF, null, 0);

        assertTrue(cmd instanceof TurnOffHeatingCommand);
        cmd.execute();
        assertEquals(State.TURNED_OFF, heating.getState());
    }

    // --- Rollladen ---

    public void testCreateRollUpShutterCommand() {
        shutter.setState(State.ROLLED_DOWN);
        shutter.setPosition(100);
        Command cmd = CommandFactory.create(shutter, ActionType.ROLL_UP, null, 0);

        assertTrue(cmd instanceof RollUpShutterCommand);
        cmd.execute();
        assertEquals(State.ROLLED_UP, shutter.getState());
        assertEquals(0, shutter.getPosition());
    }

    public void testCreateRollDownShutterCommand() {
        Command cmd = CommandFactory.create(shutter, ActionType.ROLL_DOWN, null, 0);

        assertTrue(cmd instanceof RollDownShutterCommand);
        cmd.execute();
        assertEquals(State.ROLLED_DOWN, shutter.getState());
        assertEquals(100, shutter.getPosition());
    }


    // --- Parametrisierte Wert-Befehle ---

    public void testCreateSetBrightnessLampCommand() {
        lamp.setState(State.TURNED_ON);
        Command cmd = CommandFactory.create(lamp, ActionType.SET_BRIGHTNESS, "70", 0);

        assertTrue(cmd instanceof SetBrightnessLampCommand);
        cmd.execute();
        assertEquals(70, ((Lamp) cmd.getDevice()).getBrightness());
    }

    public void testCreateSetTemperatureHeatingCommand() {
        heating.setState(State.TURNED_ON);
        Command cmd = CommandFactory.create(heating, ActionType.SET_TEMPERATURE, "21.5", 0);

        assertTrue(cmd instanceof SetTemperatureHeatingCommand);
        cmd.execute();
        assertEquals(21.5, ((Heating) cmd.getDevice()).getTemperature(), 0.001);
    }

    public void testCreateSetPositionShutterCommand() {
        shutter.setState(State.ROLLED_DOWN);
        Command cmd = CommandFactory.create(shutter, ActionType.SET_POSITION, "50", 0);

        assertTrue(cmd instanceof SetPositionShutterCommand);
        cmd.execute();
        assertEquals(50, ((Shutter) cmd.getDevice()).getPosition());
    }

    // --- Fehlerfall ---

    public void testCreateInvalidCombinationThrowsException() {
        try {
            // Ungültige Kombination: Lampe mit SET_TEMPERATURE ist nicht erlaubt.
            CommandFactory.create(lamp, ActionType.SET_TEMPERATURE, "22", 0);
            fail("Erwartet: IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("Invalid action type"));
        }
    }
}