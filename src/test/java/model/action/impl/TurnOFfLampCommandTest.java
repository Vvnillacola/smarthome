package model.action.impl;

import junit.framework.TestCase;
import model.ActionType;
import model.State;
import model.device.impl.Lamp;
import model.room.Raum;

public class TurnOFfLampCommandTest extends TestCase {

    private Raum raum;
    private Lamp lamp;

    @Override
    protected void setUp() {
        raum = new Raum("Wohnzimmer");
        lamp = new Lamp("L-TEST-OFF", "Deckenleuchte", raum);
    }

    public void testConstructorAndBaseCommandDelegation() {
        String customId = "CMD-LAMP-OFF-123";
        int orderIndex = 1;

        TurnOFfLampCommand cmd = new TurnOFfLampCommand(
                customId,
                lamp,
                ActionType.TURN_OFF,
                orderIndex
        );

        // Prüft, ob die Parameter korrekt an BaseCommand weitergegeben werden.
        assertEquals(customId, cmd.getID());
        assertEquals(lamp, cmd.getDevice());
        assertEquals(ActionType.TURN_OFF, cmd.getActionType());
        assertEquals(orderIndex, cmd.getOrderIndex());
    }

    public void testExecuteChangesLampStateToTurnedOff() {
        // Ausgangszustand explizit auf TURNED_ON gesetzt, um die Zustandsänderung prüfbar zu machen.
        lamp.setState(State.TURNED_ON);

        TurnOFfLampCommand cmd = new TurnOFfLampCommand(
                "CMD-1",
                lamp,
                ActionType.TURN_OFF,
                0
        );

        cmd.execute();

        // Erwarteter Zustand nach Ausführung:
        assertEquals(State.TURNED_OFF, lamp.getState());
    }

    public void testExecuteWithNullLampDoesNotThrowException() {
        TurnOFfLampCommand cmd = new TurnOFfLampCommand(
                "CMD-NULL",
                null,
                ActionType.TURN_OFF,
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
