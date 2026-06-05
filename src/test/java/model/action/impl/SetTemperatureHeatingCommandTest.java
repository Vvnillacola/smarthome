package model.action.impl;

import junit.framework.TestCase;
import model.ActionType;
import model.State;
import model.device.impl.Heating;
import model.room.Raum;

public class SetTemperatureHeatingCommandTest extends TestCase {

    private Raum raum;
    private Heating heating;

    @Override
    protected void setUp() {
        raum = new Raum("Badezimmer");
        heating = new Heating("H-TEST-TEMP", "Handtuchheizung", raum);
    }

    public void testConstructorAndBaseCommandDelegation() {
        String customId = "CMD-HEAT-TEMP-1";
        int orderIndex = 2;
        double targetTemperature = 22.5;

        SetTemperatureHeatingCommand cmd = new SetTemperatureHeatingCommand(
                customId,
                heating,
                ActionType.SET_TEMPERATURE,
                orderIndex,
                targetTemperature
        );

        // Prüft die Weitergabe an BaseCommand und das gerätespezifische double-Feld.
        assertEquals(customId, cmd.getID());
        assertEquals(heating, cmd.getDevice());
        assertEquals(ActionType.SET_TEMPERATURE, cmd.getActionType());
        assertEquals(orderIndex, cmd.getOrderIndex());
        assertEquals(targetTemperature, cmd.getTemperature(), 0.001);
    }

    public void testExecuteChangesTemperatureWhenHeatingIsTurnedOn() {
        // Zustand auf TURNED_ON gesetzt, damit die Ausführungsbedingung erfüllt ist.
        heating.setState(State.TURNED_ON);
        heating.setTemperature(18.0);

        SetTemperatureHeatingCommand cmd = new SetTemperatureHeatingCommand(
                "CMD-1",
                heating,
                ActionType.SET_TEMPERATURE,
                0,
                21.5
        );

        cmd.execute();

        // Erwartete Temperatur nach Ausführung:
        assertEquals(21.5, heating.getTemperature(), 0.001);
    }

    public void testExecuteDoesNotChangeTemperatureWhenHeatingIsTurnedOff() {
        // Zustand auf TURNED_OFF gesetzt, damit die Ausführungsbedingung nicht erfüllt ist.
        heating.setState(State.TURNED_OFF);
        heating.setTemperature(18.0);

        SetTemperatureHeatingCommand cmd = new SetTemperatureHeatingCommand(
                "CMD-2",
                heating,
                ActionType.SET_TEMPERATURE,
                0,
                21.5
        );

        cmd.execute();

        // Temperatur bleibt unverändert, da die Heizung ausgeschaltet ist.
        assertEquals(18.0, heating.getTemperature(), 0.001);
    }

    public void testExecuteWithNullHeatingDoesNotThrowException() {
        SetTemperatureHeatingCommand cmd = new SetTemperatureHeatingCommand(
                "CMD-NULL",
                null,
                ActionType.SET_TEMPERATURE,
                0,
                20.0
        );

        try {
            // Der Null-Check in execute() verhindert eine NullPointerException.
            cmd.execute();
        } catch (NullPointerException e) {
            fail("execute() muss bei einem null-Gerät robust sein und darf keine NullPointerException werfen.");
        }
    }
}
