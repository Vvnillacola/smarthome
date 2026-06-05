package model.action.impl;

import junit.framework.TestCase;
import model.ActionType;
import model.State;
import model.device.impl.Shutter;
import model.room.Raum;

public class RollUpShutterCommandTest extends TestCase {

    private Raum raum;
    private Shutter shutter;

    @Override
    protected void setUp() {
        raum = new Raum("Wohnzimmer");
        shutter = new Shutter("S-TEST-UP", "Rollladen Süd", raum);
    }

    public void testConstructorAndBaseCommandDelegation() {
        String customId = "CMD-SHUTTER-456";
        int orderIndex = 2;

        RollUpShutterCommand cmd = new RollUpShutterCommand(
                customId,
                shutter,
                ActionType.ROLL_UP,
                orderIndex
        );

        // Prüft, ob die Parameter korrekt über super() an BaseCommand weitergegeben werden.
        assertEquals(customId, cmd.getID());
        assertEquals(shutter, cmd.getDevice());
        assertEquals(ActionType.ROLL_UP, cmd.getActionType());
        assertEquals(orderIndex, cmd.getOrderIndex());
    }

    public void testExecuteChangesShutterStateAndPosition() {
        // Ausgangszustand auf ROLLED_DOWN gesetzt, um die Änderung nach oben prüfbar zu machen.
        shutter.setState(State.ROLLED_DOWN);
        shutter.setPosition(100);

        RollUpShutterCommand cmd = new RollUpShutterCommand(
                "CMD-2",
                shutter,
                ActionType.ROLL_UP,
                0
        );

        cmd.execute();

        // Erwarteter Zustand nach Ausführung (oben = ROLLED_UP, Position 0):
        assertEquals(State.ROLLED_UP, shutter.getState());
        assertEquals(0, shutter.getPosition());
    }

    public void testExecuteWithNullShutterDoesNotThrowException() {
        RollUpShutterCommand cmd = new RollUpShutterCommand(
                "CMD-NULL-UP",
                null,
                ActionType.ROLL_UP,
                0
        );

        try {
            // Der Null-Check in execute() verhindert eine NullPointerException.
            cmd.execute();
        } catch (NullPointerException e) {
            fail("execute() muss bei einem null-Gerät robust sein und darf keine NullPointerException werfen.");
        }
    }
}
