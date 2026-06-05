package model.action.impl;

import junit.framework.TestCase;
import model.ActionType;
import model.State;
import model.device.impl.Heating;
import model.room.Raum;

public class TurnOffHeatingCommandTest extends TestCase {

    private Raum raum;
    private Heating heating;

    @Override
    protected void setUp() {
        raum = new Raum("Wohnzimmer");
        heating = new Heating("H-TEST-OFF", "Wandheizung", raum);
    }

    public void testConstructorAndBaseCommandDelegation() {
        String customId = "CMD-HEAT-OFF-123";
        int orderIndex = 3;

        TurnOffHeatingCommand cmd = new TurnOffHeatingCommand(
                customId,
                heating,
                ActionType.TURN_OFF,
                orderIndex
        );

        // Prüft, ob die Parameter korrekt an BaseCommand weitergegeben werden.
        assertEquals(customId, cmd.getID());
        assertEquals(heating, cmd.getDevice());
        assertEquals(ActionType.TURN_OFF, cmd.getActionType());
        assertEquals(orderIndex, cmd.getOrderIndex());
    }

    public void testExecuteChangesHeatingStateToTurnedOff() {
        // Ausgangszustand explizit auf TURNED_ON gesetzt, um die Zustandsänderung prüfbar zu machen.
        heating.setState(State.TURNED_ON);

        TurnOffHeatingCommand cmd = new TurnOffHeatingCommand(
                "CMD-1",
                heating,
                ActionType.TURN_OFF,
                0
        );

        cmd.execute();

        // Erwarteter Zustand nach Ausführung:
        assertEquals(State.TURNED_OFF, heating.getState());
    }

    public void testExecuteWithNullHeatingDoesNotThrowException() {
        TurnOffHeatingCommand cmd = new TurnOffHeatingCommand(
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
