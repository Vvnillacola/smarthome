package model.action.impl;

import junit.framework.TestCase;
import model.ActionType;
import model.State;
import model.device.impl.Shutter;
import model.room.Raum;

public class SetPositionShutterCommandTest extends TestCase {

    private Raum raum;
    private Shutter shutter;

    @Override
    protected void setUp() {
        raum = new Raum("Schlafzimmer");
        shutter = new Shutter("S-TEST-POS", "Rollladen Ost", raum);
    }

    public void testConstructorAndBaseCommandDelegation() {
        String customId = "CMD-SHUTTER-POS-1";
        int orderIndex = 3;
        int targetPosition = 40;

        SetPositionShutterCommand cmd = new SetPositionShutterCommand(
                customId,
                shutter,
                ActionType.SET_POSITION,
                orderIndex,
                targetPosition
        );

        // Prüft die Weitergabe an BaseCommand und das gerätespezifische Feld.
        assertEquals(customId, cmd.getID());
        assertEquals(shutter, cmd.getDevice());
        assertEquals(ActionType.SET_POSITION, cmd.getActionType());
        assertEquals(orderIndex, cmd.getOrderIndex());
        assertEquals(targetPosition, cmd.getPosition());
    }

    public void testExecuteChangesPositionWhenShutterIsRolledDown() {
        // Zustand auf ROLLED_DOWN gesetzt, damit die Ausführungsbedingung erfüllt ist.
        shutter.setState(State.ROLLED_DOWN);
        shutter.setPosition(100);

        SetPositionShutterCommand cmd = new SetPositionShutterCommand(
                "CMD-1",
                shutter,
                ActionType.SET_POSITION,
                0,
                40
        );

        cmd.execute();

        // Erwartete Position nach Ausführung:
        assertEquals(40, shutter.getPosition());
    }

    public void testExecuteDoesNotChangePositionWhenShutterIsNotRolledDown() {
        // Zustand auf ROLLED_UP gesetzt, damit die Ausführungsbedingung nicht erfüllt ist.
        shutter.setState(State.ROLLED_UP);
        shutter.setPosition(0);

        SetPositionShutterCommand cmd = new SetPositionShutterCommand(
                "CMD-2",
                shutter,
                ActionType.SET_POSITION,
                0,
                40
        );

        cmd.execute();

        // Position bleibt unverändert, da der Rollladen nicht heruntergelassen ist.
        assertEquals(0, shutter.getPosition());
    }

    public void testExecuteWithNullShutterDoesNotThrowException() {
        SetPositionShutterCommand cmd = new SetPositionShutterCommand(
                "CMD-NULL",
                null,
                ActionType.SET_POSITION,
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
