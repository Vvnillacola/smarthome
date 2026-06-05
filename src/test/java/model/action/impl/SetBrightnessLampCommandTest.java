package model.action.impl;

import junit.framework.TestCase;
import model.ActionType;
import model.State;
import model.device.impl.Lamp;
import model.room.Raum;

public class SetBrightnessLampCommandTest extends TestCase {

    private Raum raum;
    private Lamp lamp;

    @Override
    protected void setUp() {
        raum = new Raum("Wohnzimmer");
        lamp = new Lamp("L-TEST", "Dimmbare Stehlampe", raum);
    }

    public void testConstructorAndBaseCommandDelegation() {
        String customId = "CMD-LAMP-BRIGHT";
        int orderIndex = 1;
        int targetBrightness = 75;

        SetBrightnessLampCommand cmd = new SetBrightnessLampCommand(
                customId,
                lamp,
                ActionType.SET_BRIGHTNESS,
                orderIndex,
                targetBrightness
        );

        // Prüft die Weitergabe an BaseCommand und das gerätespezifische Feld.
        assertEquals(customId, cmd.getID());
        assertEquals(lamp, cmd.getDevice());
        assertEquals(ActionType.SET_BRIGHTNESS, cmd.getActionType());
        assertEquals(orderIndex, cmd.getOrderIndex());
        assertEquals(targetBrightness, cmd.getBrightness());
    }

    public void testExecuteChangesBrightnessWhenLampIsTurnedOn() {
        // Zustand auf TURNED_ON gesetzt, damit die Ausführungsbedingung erfüllt ist.
        lamp.setState(State.TURNED_ON);
        lamp.setBrightness(10);

        SetBrightnessLampCommand cmd = new SetBrightnessLampCommand(
                "CMD-1",
                lamp,
                ActionType.SET_BRIGHTNESS,
                0,
                85
        );

        cmd.execute();

        // Erwartete Helligkeit nach Ausführung:
        assertEquals(85, lamp.getBrightness());
    }

    public void testExecuteDoesNotChangeBrightnessWhenLampIsTurnedOff() {
        // Zustand auf TURNED_OFF gesetzt, damit die Ausführungsbedingung nicht erfüllt ist.
        lamp.setState(State.TURNED_OFF);
        lamp.setBrightness(10);

        SetBrightnessLampCommand cmd = new SetBrightnessLampCommand(
                "CMD-2",
                lamp,
                ActionType.SET_BRIGHTNESS,
                0,
                85
        );

        cmd.execute();

        // Helligkeit bleibt unverändert, da die Lampe ausgeschaltet ist.
        assertEquals(10, lamp.getBrightness());
    }

    public void testExecuteWithNullLampDoesNotThrowException() {
        SetBrightnessLampCommand cmd = new SetBrightnessLampCommand(
                "CMD-NULL",
                null,
                ActionType.SET_BRIGHTNESS,
                0,
                50
        );

        try {
            // Der Null-Check in execute() verhindert eine NullPointerException.
            cmd.execute();
        } catch (NullPointerException e) {
            fail("execute() muss bei einem null-Gerät robust sein und darf keine NullPointerException werfen.");
        }
    }
}
