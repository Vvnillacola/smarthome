package model.action.impl;

import junit.framework.TestCase;
import model.ActionType;
import model.State;
import model.device.impl.Shutter;
import model.room.Raum;

public class RollDownShutterCommandTest extends TestCase {

    private Raum raum;
    private Shutter shutter;

    @Override
    protected void setUp() {
        raum = new Raum("Schlafzimmer");
        shutter = new Shutter("S-TEST", "Rollladen Ost", raum);
    }

    public void testConstructorAndBaseCommandDelegation() {
        String customId = "CMD-SHUTTER-123";
        int orderIndex = 4;

        RollDownShutterCommand cmd = new RollDownShutterCommand(
                customId,
                shutter,
                ActionType.ROLL_DOWN,
                orderIndex
        );

        // Prüft, ob die Parameter korrekt über super() an BaseCommand weitergegeben werden.
        assertEquals(customId, cmd.getID());
        assertEquals(shutter, cmd.getDevice());
        assertEquals(ActionType.ROLL_DOWN, cmd.getActionType());
        assertEquals(orderIndex, cmd.getOrderIndex());
    }

    public void testExecuteChangesShutterStateAndPosition() {
        // Ausgangszustand explizit auf ROLLED_UP gesetzt, um die Zustandsänderung prüfbar zu machen.
        shutter.setState(State.ROLLED_UP);
        shutter.setPosition(0);

        RollDownShutterCommand cmd = new RollDownShutterCommand(
                "CMD-1",
                shutter,
                ActionType.ROLL_DOWN,
                0
        );

        cmd.execute();

        // Erwarteter Zustand nach Ausführung:
        assertEquals(State.ROLLED_DOWN, shutter.getState());
        assertEquals(100, shutter.getPosition());
    }

    public void testExecuteWithNullShutterDoesNotThrowException() {
        RollDownShutterCommand cmd = new RollDownShutterCommand(
                "CMD-NULL",
                null,
                ActionType.ROLL_DOWN,
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
