package model.action.impl;

import junit.framework.TestCase;
import model.ActionType;
import model.State;
import model.device.impl.Heating;
import model.room.Raum;

public class TurnOnHeatingCommandTest extends TestCase {

    private Raum raum;
    private Heating heating;

    @Override
    protected void setUp() {
        raum = new Raum("Wohnzimmer");
        heating = new Heating("H-TEST-ON", "Fußbodenheizung", raum);
    }

    public void testConstructorAndBaseCommandDelegation() {
        String customId = "CMD-HEAT-ON-999";
        int orderIndex = 1;

        TurnOnHeatingCommand cmd = new TurnOnHeatingCommand(
                customId,
                heating,
                ActionType.TURN_ON,
                orderIndex
        );

        // Prüft, ob die Parameter korrekt an BaseCommand weitergegeben werden.
        assertEquals(customId, cmd.getID());
        assertEquals(heating, cmd.getDevice());
        assertEquals(ActionType.TURN_ON, cmd.getActionType());
        assertEquals(orderIndex, cmd.getOrderIndex());
    }

    public void testExecuteChangesHeatingStateToTurnedOn() {
        // Ausgangszustand explizit auf TURNED_OFF gesetzt, um die Zustandsänderung prüfbar zu machen.
        heating.setState(State.TURNED_OFF);

        TurnOnHeatingCommand cmd = new TurnOnHeatingCommand(
                "CMD-1",
                heating,
                ActionType.TURN_ON,
                0
        );

        cmd.execute();

        // Erwarteter Zustand nach Ausführung:
        assertEquals(State.TURNED_ON, heating.getState());
    }

    public void testExecuteWithNullHeatingDoesNotThrowException() {
        TurnOnHeatingCommand cmd = new TurnOnHeatingCommand(
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
