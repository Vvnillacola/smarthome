package model.action.impl;

import junit.framework.TestCase;
import model.ActionType;
import model.State;
import model.device.impl.Lamp;
import model.room.Raum;

public class TurnOnLampCommandTest extends TestCase {

    private Raum raum;
    private Lamp lamp;

    @Override
    protected void setUp() {
        raum = new Raum("Wohnzimmer");
        lamp = new Lamp("L-TEST-ON", "Wohnzimmerlampe", raum);
    }

    public void testConstructorAndBaseCommandDelegation() {
        String customId = "CMD-LAMP-ON-123";
        int orderIndex = 0;

        TurnOnLampCommand cmd = new TurnOnLampCommand(
                customId,
                lamp,
                ActionType.TURN_ON,
                orderIndex
        );

        // Prüft, ob die Parameter korrekt an BaseCommand weitergegeben werden.
        assertEquals(customId, cmd.getID());
        assertEquals(lamp, cmd.getDevice());
        assertEquals(ActionType.TURN_ON, cmd.getActionType());
        assertEquals(orderIndex, cmd.getOrderIndex());
    }

    public void testExecuteChangesLampStateToTurnedOn() {
        // Ausgangszustand explizit auf TURNED_OFF gesetzt, um die Zustandsänderung prüfbar zu machen.
        lamp.setState(State.TURNED_OFF);

        TurnOnLampCommand cmd = new TurnOnLampCommand(
                "CMD-1",
                lamp,
                ActionType.TURN_ON,
                0
        );

        cmd.execute();

        // Erwarteter Zustand nach Ausführung:
        assertEquals(State.TURNED_ON, lamp.getState());
    }

    public void testExecuteWithNullLampDoesNotThrowException() {
        TurnOnLampCommand cmd = new TurnOnLampCommand(
                "CMD-NULL",
                null,
                ActionType.TURN_ON,
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
