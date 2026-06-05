import javafx.scene.control.TableView;
import javafx.stage.Stage;
import model.ActionType;
import model.State;
import model.action.impl.TurnOnLampCommand;
import model.device.impl.Lamp;
import model.room.Raum;
import model.scenario.Scenario;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

public class SmartHomeGuiTest extends ApplicationTest {

    private SmartHomeApp app;
    private Lamp testLamp;
    private Scenario testScenario;

    @Override
    public void start(Stage stage) throws Exception {
        app = new SmartHomeApp();
        app.start(stage);

        // Testdaten anlegen: Raum, Gerät und Szenario mit einer Aktion.
        Raum room = new Raum("Testzimmer");
        app.getRoomService().addRoom(room);

        testLamp = new Lamp(UUID.randomUUID().toString(), "Testlampe", room);
        app.getDeviceService().addDevice(testLamp);

        testScenario = new Scenario("Abend-Test", "Automatisch erstelltes Testszenario");
        testScenario.getCommands().add(new TurnOnLampCommand(null, testLamp, ActionType.TURN_ON, 0));
        app.getScenarioService().addScenario(testScenario);
    }

    @Test
    public void testSzenarioAnlegenViaGui() {
        // Navigationsbereich: Szenarien auswählen.
        clickOn("🎬 Szenarien");

        verifyThat(".table-view", isVisible());

        TableView<?> table = lookup(".table-view").queryAs(TableView.class);
        int initialCount = table.getItems().size();

        // Formular zum Anlegen eines neuen Szenarios öffnen.
        clickOn("Neu");

        // Namen eingeben und bestätigen.
        clickOn(".text-field").write("GUI-Testszenario");
        clickOn("Erstellen");

        WaitForAsyncUtils.waitForFxEvents();

        // Tabelle muss genau einen Eintrag mehr enthalten.
        assertEquals(initialCount + 1, table.getItems().size());
    }

    @Test
    public void testSzenarioAusfuehrenAendertGeraetezustand() {
        // Ausgangszustand: Lampe ist ausgeschaltet.
        assertEquals(State.TURNED_OFF, testLamp.getState());

        clickOn("🎬 Szenarien");

        // Szenario in der Tabelle auswählen und ausführen.
        clickOn("Abend-Test");
        clickOn("Ausführen");

        // Kurze Wartezeit, da die Ausführung asynchron erfolgen kann.
        WaitForAsyncUtils.sleep(200, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();

        // Erwarteter Gerätezustand nach Ausführung des Szenarios:
        assertEquals(State.TURNED_ON, testLamp.getState());
    }
}
