import atlantafx.base.theme.PrimerLight;
import config.DeviceConfig;
import factory.CommandFactory;
import factory.DeviceFactory;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.ActionType;
import model.DeviceType;
import model.State;
import model.action.impl.SetBrightnessLampCommand;
import model.action.impl.SetPositionShutterCommand;
import model.action.impl.SetTemperatureHeatingCommand;
import model.device.Device;
import model.device.impl.Heating;
import model.device.impl.Lamp;
import model.device.impl.Shutter;
import model.room.Raum;
import model.action.Command;
import model.scenario.Scenario;
import service.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SmartHomeApp extends Application {

    private BorderPane root;

    private RoomService roomService = new RoomService();

    private DeviceService deviceService = new DeviceService();

    private ScenarioService scenarioService = new ScenarioService();

    private PersistenceService persistenceService = new PersistenceService();

    private Runnable currentRefreshAction;

    private TextArea logArea;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public RoomService getRoomService() {
        return roomService;
    }

    public DeviceService getDeviceService() {
        return deviceService;
    }

    public ScenarioService getScenarioService() {
        return scenarioService;
    }

    private void log(String message) {
        if (logArea != null) {
            String timestamp = LocalTime.now().format(timeFormatter);
            logArea.appendText("[" + timestamp + "] " + message + "\n");
        }
    }

    @Override
    public void start(Stage stage) {
        root = new BorderPane();

        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(220);
        sidebar.getStyleClass().add("sidebar");

        Label title = new Label("Smart Home");
        title.getStyleClass().add("title");

        Button roomsBtn = new Button("🏠 Räume");
        roomsBtn.setOnAction(e -> {
            openRooms();
        });

        Button devicesBtn = new Button("🔌 Geräte");
        devicesBtn.setOnAction(e -> {
            openDevices();
        });
        Button scenariosBtn = new Button("🎬 Szenarien");
        scenariosBtn.setOnAction(e -> {
            openScenarios();
        });

        roomsBtn.setMaxWidth(Double.MAX_VALUE);
        devicesBtn.setMaxWidth(Double.MAX_VALUE);
        scenariosBtn.setMaxWidth(Double.MAX_VALUE);

        sidebar.getChildren().addAll(title, roomsBtn, devicesBtn, scenariosBtn);

        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("topBar");

        Label header = new Label("Dashboard");
        header.getStyleClass().add("header");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button neuBtn = new Button("\uD83D\uDDD2 Neu");

        neuBtn.setOnAction(e -> {

            roomService.clear();
            deviceService.clear();
            scenarioService.clear();
            log("Neues Projekt erstellt. Alle Daten wurden zurückgesetzt.");
            openRooms();
        });

        Button openBtn = new Button("📂 Öffnen");

        openBtn.setOnAction(e -> {

            FileChooser chooser =
                    new FileChooser();

            File file =
                    chooser.showOpenDialog(
                            root.getScene().getWindow()
                    );

            if(file != null) {

                SmartHomeProject project =
                        null;
                try {
                    project = persistenceService.load(file);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                roomService.clear();
                deviceService.clear();
                scenarioService.clear();

                roomService.setRooms(
                        project.getRooms()
                );

                deviceService.setDevices(
                        project.getDevices()
                );

                scenarioService.setScenarios(
                        project.getScenarios()
                );
                log("Projekt erfolgreich aus Datei '" + file.getName() + "' geladen.");
                openDevices();
            }
        });

        Button saveBtn = new Button("💾 Speichern");

        saveBtn.setOnAction(e -> {

            FileChooser chooser = new FileChooser();

            chooser.setTitle("Projekt speichern");

            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(
                            "SmartHome Projekt",
                            "*.json"
                    )
            );

            File file = chooser.showSaveDialog(
                    root.getScene().getWindow()
            );

            if(file != null) {

                SmartHomeProject project =
                        new SmartHomeProject(
                                roomService.getAllRooms(),
                                deviceService.getDevices(),
                                scenarioService.getScenarios()
                        );

                try {
                    persistenceService.save(
                            project,
                            file
                    );
                    log("Projekt erfolgreich in Datei '" + file.getName() + "' gespeichert.");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        ComboBox<Scenario> scenarioSelect = new ComboBox<>();
        scenarioSelect.setPrefWidth(200);

        scenarioSelect.setItems(scenarioService.getScenarios());

        scenarioSelect.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Scenario item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        scenarioSelect.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Scenario item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Szenario auswählen" : item.getName());
            }
        });

        Button runScenario = new Button("▶ Ausführen");

        runScenario.setOnAction(e -> {
            Scenario selected = scenarioSelect.getValue();
            if (selected != null) {
                runaScenario(selected);
                refreshCurrentView();
            }
        });

        topBar.getChildren().addAll(
                header,
                neuBtn,
                openBtn,
                saveBtn,
                spacer,
                scenarioSelect,
                runScenario
        );
        GridPane dashboard = new GridPane();
        dashboard.setPadding(new Insets(20));
        dashboard.setHgap(20);
        dashboard.setVgap(20);




        VBox logPanel = new VBox(10);
        logPanel.setPadding(new Insets(15));
        logPanel.setPrefWidth(250);

        Label logTitle = new Label("Aktivität");

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);

        logPanel.getChildren().addAll(logTitle, logArea);

        root.setLeft(sidebar);
        root.setTop(topBar);
        root.setCenter(dashboard);
        root.setRight(logPanel);

        Scene scene = new Scene(root, 1200, 700);

        scene.getStylesheets().add(new PrimerLight().getUserAgentStylesheet());

        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        stage.setTitle("Smart Home");
        stage.setScene(scene);
        stage.show();

        log("Anwendung erfolgreich gestartet.");
    }

    private void runaScenario(Scenario scenario) {
        if (scenario != null) {
            if (!scenario.getCommands().isEmpty()) {
                log("Szenario '" + scenario.getName() + "' gestartet.");
                String result = "";
                // Details der Aktionen loggen
                for (Command cmd : scenario.getCommands()) {
                    cmd.execute();
                    switch (cmd.getActionType()){
                        case ROLL_UP -> result = cmd.getDevice().toString() + " hochgefahren.";
                        case TURN_ON ->  result = cmd.getDevice().toString() + " angeschaltet.";
                        case TURN_OFF ->  result = cmd.getDevice().toString() + " ausgeschaltet.";
                        case ROLL_DOWN -> result = cmd.getDevice().toString() + " runtergefahren.";
                        case SET_POSITION -> {
                            if (cmd.getDevice().getState() ==State. ROLLED_UP ){
                                result = cmd.getDevice().toString() +" sind noch hochgerollt. Position wurde nicht eingestellt.";
                            } else {
                                result = cmd.getDevice().toString() + " auf Position " + ((SetPositionShutterCommand) cmd).getPosition()+ " gestellt.";
                            }
                        }
                        case SET_BRIGHTNESS -> {
                            if (cmd.getDevice().getState() ==State.TURNED_OFF ){
                                result = cmd.getDevice().toString() +" ist ausgeschaltet. Helligkeit wurde nicht gesetzt.";
                            } else {
                                result = cmd.getDevice().toString() + " auf Helligkeit " + ((SetBrightnessLampCommand) cmd).getBrightness()+ " gestellt.";
                            }
                        }
                        case SET_TEMPERATURE -> {
                            if (cmd.getDevice().getState() == State.TURNED_OFF) {
                                result = cmd.getDevice().toString() + " ist ausgeschaltet. Temperatur wurde nicht gesetzt.";
                            } else {
                                result = cmd.getDevice().toString() + " auf Temperatur " + ((SetTemperatureHeatingCommand) cmd).getTemperature()+ " gestellt.";
                            }
                        }
                    }
                    log("Szenario: " + scenario.getName() + " Aktion: " + cmd.getActionType() + " Ergebnis: " + result);
                }
                log("Szenario '" + scenario.getName() + "' ausgeführt.");
            } else {
                log("Szenario " + scenario.getName() + " besitzt keine Aktion.");
            }
        }
    }

    private void refreshCurrentView() {
        if (currentRefreshAction != null) {
            currentRefreshAction.run();
        }
    }

    private void openDevices() {
        currentRefreshAction = this::openDevices;
        VBox devicesView = new VBox(10);
        devicesView.setPadding(new Insets(20));

        Label title = new Label("Geräte");
        title.getStyleClass().add("header");
        TableView<Device> table = new TableView<>();
        TableColumn<Device, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getName())
        );

        TableColumn<Device, String> typeCol = new TableColumn<>("Typ");
        typeCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getType().toString())
        );

        TableColumn<Device, String> roomCol = new TableColumn<>("Raum");
        roomCol.setCellValueFactory(data -> {
                    Raum room = data.getValue().getRoom();

                    String roomName = (room != null)
                            ? room.getName()
                            : "Nicht zugeordnet";
                    return new SimpleStringProperty(roomName);
                }
        );

        TableColumn<Device, String> stateCol = new TableColumn<>("Zustand");
        stateCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getState().toString())
        );

        table.getColumns().addAll(nameCol, typeCol, roomCol, stateCol);
        table.setItems(deviceService.getDevices());

        Button addDevice = new Button("Neu");
        addDevice.setOnAction(e -> {
            openAddDevice();
        });

        Button viewDevice = new Button("Anzeigen");
        viewDevice.setOnAction(e -> {
            Device gerät = table.getSelectionModel().getSelectedItem();
            if (null != gerät){
                log("Gerät details angezeigt: " + gerät.getName());
                openDeviceEditor(false, gerät);
            }
        });


        Button changeDevice = new Button("Bearbeiten");
        changeDevice.setOnAction(e -> {
            Device gerät = table.getSelectionModel().getSelectedItem();
            if (null != gerät){
                openDeviceEditor(true, gerät);
            }
        });

        Button deleteDevice = new Button("Löschen");
        deleteDevice.setOnAction(e -> {
            Device gerät = table.getSelectionModel().getSelectedItem();
            if (gerät != null) {
                boolean isUsedInScenario = false;
                String scenarioName = "";

                for (Scenario scenario : scenarioService.getScenarios()) {
                    for (Command cmd : scenario.getCommands()) {
                        if (cmd.getDevice() != null && cmd.getDevice().getId().equals(gerät.getId())) {
                            isUsedInScenario = true;
                            scenarioName = scenario.getName();
                            break;
                        }
                    }
                    if (isUsedInScenario) break;
                }

                if (isUsedInScenario) {
                    log("Fehler: '" + gerät.getName() + "' kann nicht gelöscht werden, da es im Szenario '" + scenarioName + "' verwendet wird!");

                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Löschen nicht möglich");
                    alert.setHeaderText("Gerät wird verwendet");
                    alert.setContentText("Das Gerät '" + gerät.getName() + "' kann nicht gelöscht werden, da es im Szenario '" + scenarioName + "' verknüpft ist.");
                    alert.showAndWait();
                } else {
                    log("Gerät gelöscht: " + gerät.getName());
                    deviceService.getDevices().remove(gerät);
                    openDevices();
                }
            }
        });

        HBox buttonBar = new HBox(10);
        buttonBar.setPadding(new Insets(10));
        buttonBar.getChildren().addAll(addDevice, viewDevice, changeDevice, deleteDevice);

        devicesView.getChildren().addAll(title, table, buttonBar);

        root.setCenter(devicesView);
    }

    private void openDeviceEditor(boolean edit, Device device) {
        currentRefreshAction = () -> openDeviceEditor(edit, device);
        VBox deviceEditor = new VBox(15);
        deviceEditor.setPadding(new Insets(20));

        Label title = new Label("Gerät");
        title.getStyleClass().add("header");

        final boolean[] isEditing = {edit};

        Label idLabel = new Label("ID: " + device.getId());

        Label nameLabel = new Label("Name:");

        TextField nameField = new TextField(device.getName());
        nameField.setEditable(isEditing[0]);

        HBox nameBar = new HBox(10, nameLabel, nameField);

        Label typeLabel = new Label("Typ: " + device.getType());

        Label roomLabel = new Label("Raum:");

        ComboBox<Raum> roomBox = new ComboBox<>();
        roomBox.getItems().addAll(roomService.getAllRooms());

        roomBox.setValue(device.getRoom());
        roomBox.setDisable(!isEditing[0]);

        HBox roomBar = new HBox(10, roomLabel, roomBox);

        Label stateLabel = new Label("Zustand:");

        ToggleButton stateToggle = new ToggleButton();
        stateToggle.setText(device.getState().toString());

        stateToggle.setDisable(!isEditing[0]);
        HBox stateBar = new HBox(10, stateLabel, stateToggle);

        deviceEditor.getChildren().addAll(
                title,
                idLabel,
                typeLabel,
                nameBar,
                roomBar,
                stateBar );


        Button backBtn = new Button("Zurück");
        backBtn.setOnAction(e -> openDevices());

        Button editBtn = new Button();

        editBtn.setText(isEditing[0]
                ? "Speichern"
                : "Bearbeiten"
        );

        DeviceConfig config = null;

        if (device instanceof Lamp lamp) {

            config = new DeviceConfig(
                    "Helligkeit:",
                    0,
                    100,
                    lamp.getBrightness(),
                    State.TURNED_ON,
                    State.TURNED_OFF,
                    value -> lamp.setBrightness(value),
                    () -> "Lampen-Änderungen gespeichert: "
                            + device.getName()
                            + " (Helligkeit: "
                            + lamp.getBrightness()
                            + "%, Status: "
                            + device.getState()
                            + ")"
            );

        } else if (device instanceof Heating heating) {

            config = new DeviceConfig(
                    "Temperatur:",
                    10,
                    35,
                    heating.getTemperature(),
                    State.TURNED_ON,
                    State.TURNED_OFF,
                    value -> heating.setTemperature(value),
                    () -> "Heizungs-Änderungen gespeichert: "
                            + device.getName()
                            + " ("
                            + heating.getTemperature()
                            + "°C, Status: "
                            + device.getState()
                            + ")"
            );

        } else if (device instanceof Shutter shutter) {

            config = new DeviceConfig(
                    "Position:",
                    0,
                    100,
                    shutter.getPosition(),
                    State.ROLLED_DOWN,
                    State.ROLLED_UP,
                    value -> shutter.setPosition(value),
                    () -> "Rollladen-Änderungen gespeichert: "
                            + device.getName()
                            + " (Position: "
                            + shutter.getPosition()
                            + "%, Status: "
                            + device.getState()
                            + ")"
            );
        }

        if (config != null) {

            Slider slider = new Slider(
                    config.min,
                    config.max,
                    config.value
            );

            slider.setShowTickLabels(true);
            slider.setDisable(true);

            HBox sliderBar = new HBox(
                    10,
                    new Label(config.label),
                    slider
            );

            deviceEditor.getChildren().add(sliderBar);

            DeviceConfig finalConfig = config;

            stateToggle.setOnAction(e -> {

                State current =
                        State.getValue(stateToggle.getText());

                if (current == finalConfig.inactiveState) {

                    stateToggle.setText(
                            finalConfig.activeState.toString()
                    );

                    if (isEditing[0]) {
                        slider.setDisable(false);
                    }

                } else {

                    stateToggle.setText(
                            finalConfig.inactiveState.toString()
                    );

                    slider.setDisable(true);
                }
            });

            editBtn.setOnAction(e -> {

                if (isEditing[0]) {

                    device.setName(nameField.getText());
                    device.setRoom(roomBox.getValue());
                    device.setState(
                            State.getValue(stateToggle.getText())
                    );

                    finalConfig.saveAction.accept(
                            (int) slider.getValue()
                    );

                    log(finalConfig.logMessage.get());

                    isEditing[0] = false;

                    nameField.setEditable(false);
                    roomBox.setDisable(true);
                    stateToggle.setDisable(true);
                    slider.setDisable(true);

                    editBtn.setText("Bearbeiten");

                } else {

                    isEditing[0] = true;

                    nameField.setEditable(true);
                    roomBox.setDisable(false);
                    stateToggle.setDisable(false);

                    if (State.getValue(stateToggle.getText())
                            == finalConfig.activeState) {

                        slider.setDisable(false);
                    }

                    editBtn.setText("Speichern");
                }
            });
        }


        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttonBar = new HBox(
                10,
                backBtn,
                spacer,
                editBtn
        );
        deviceEditor.getChildren().addAll(buttonBar);
        root.setCenter(deviceEditor);
    }

    private void openAddDevice() {
        currentRefreshAction = this::openAddDevice;
        Dialog<Device> dialog = new Dialog<>();
        dialog.setTitle("Neues Gerät");

        ButtonType saveButtonType = new ButtonType("Speichern", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);


        // Form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        ComboBox<DeviceType> typeBox = new ComboBox<>();
        typeBox.getItems().addAll(DeviceType.values());


        ComboBox<Raum> roomBox = new ComboBox<>();
        roomBox.getItems().addAll(roomService.getAllRooms());

        Runnable validate = () -> {
            boolean invalid =
                    nameField.getText().trim().isEmpty()
                            || typeBox.getValue() == null || roomBox.getValue() == null;

            saveButton.setDisable(invalid);
        };

        nameField.textProperty().addListener((obs, oldVal, newVal) -> validate.run());

        typeBox.valueProperty().addListener((obs, oldVal, newVal) -> validate.run());

        roomBox.valueProperty().addListener((obs, oldVal, newVal) -> validate.run());

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Typ:"), 0, 1);
        grid.add(typeBox, 1, 1);
        grid.add(new Label("Raum:"), 0, 2);
        grid.add(roomBox, 1, 2);


        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == saveButtonType) {
                String name = nameField.getText();
                DeviceType deviceType = typeBox.getValue();
                Raum room = roomBox.getValue();

                return DeviceFactory.create(deviceType, UUID.randomUUID().toString(), name, room);
            }
            return null;
        });

        Optional<Device> result = dialog.showAndWait();

        result.ifPresent(device -> {
            deviceService.addDevice(device);
            log("Neues Gerät hinzugefügt: " + device.getName() + " (" + device.getType() + ")");
        });
    }

    private void openRooms() {
        currentRefreshAction = this::openRooms;
        VBox roomsView = new VBox(10);
        roomsView.setPadding(new Insets(20));

        Label title = new Label("Räume");
        title.getStyleClass().add("header");

        ListView<Raum> roomList = new ListView<>();
        roomList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Raum room, boolean empty) {
                super.updateItem(room, empty);

                if (empty || room == null) {
                    setText(null);
                } else {
                    setText(room.getName());
                }
            }
        });
        roomList.getItems().clear();
        roomList.getItems().addAll(roomService.getAllRooms());

        Button addRoom = new Button("Neu");
        addRoom.setOnAction(e -> {
            openAddRoom();
        });

        Button viewRoom = new Button("Anzeigen");
        viewRoom.setOnAction(e -> {
            Raum room = roomList.getSelectionModel().getSelectedItem();
            if (room != null) {
                log("Raum details angezeigt: " + room.getName());
                openRoomEditor(false, room);
            }
        });

        Button changeRoom = new Button("Bearbeiten");
        changeRoom.setOnAction(e -> {
            Raum room = roomList.getSelectionModel().getSelectedItem();
            if (room != null) {
                log("Raum '" + room.getName() + "' wird bearbeitet.");
                openRoomEditor(true, room);
            }
        });

        Button deleteRoom = new Button("Löschen");
        deleteRoom.setOnAction(e -> {
            Raum room = roomList.getSelectionModel().getSelectedItem();
            if (room != null) {
                // LOGIK: Prüfen, ob noch Geräte diesem Raum zugeordnet sind
                boolean roomHasDevices = false;
                for (Device device : deviceService.getDevices()) {
                    if (device.getRoom() != null && device.getRoom().getName().equals(room.getName())) {
                        roomHasDevices = true;
                        break;
                    }
                }

                if (roomHasDevices) {
                    log("Fehler: Raum '" + room.getName() + "' kann nicht gelöscht werden, da er noch Geräte enthält!");

                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Löschen nicht möglich");
                    alert.setHeaderText("Raum ist nicht leer");
                    alert.setContentText("Der Raum '" + room.getName() + "' enthält noch registrierte Geräte. Bitte weisen Sie die Geräte zuerst um oder löschen Sie diese.");
                    alert.showAndWait();
                } else {
                    log("Raum gelöscht: " + room.getName());
                    roomService.deleteRoom(room);
                    openRooms();
                }
            }
        });

        HBox buttonBar = new HBox(10);
        buttonBar.setPadding(new Insets(10));
        buttonBar.getChildren().addAll(addRoom, viewRoom, changeRoom, deleteRoom);

        roomsView.getChildren().addAll(title, roomList, buttonBar);

        root.setCenter(roomsView);
    }

    private void openAddRoom() {
        currentRefreshAction = this::openAddRoom;
        VBox newRoom = new VBox(10);
        newRoom.setPadding(new Insets(20));

        Label title = new Label("Räume");
        title.getStyleClass().add("header");

        TextField roomName = new TextField();
        roomName.setPromptText("Name");

        Button addRoomBtn = new Button("Erstellen");
        addRoomBtn.setOnAction(f -> {
            if (roomName.getText().isEmpty()) {
                TextField errorLabel = new TextField("Bitte einen Namen eingeben");
                newRoom.getChildren().add(errorLabel);
                errorLabel.setEditable(false);
                roomName.requestFocus();
                log("Fehler beim Erstellen eines Raums: Kein Name angegeben.");
            }
            else{
                roomService.addRoom(new Raum(roomName.getText()));
                log("Neuer Raum erstellt: " + roomName.getText());
                openRooms();
            }
        });
        Button backtoView = new Button("Zurück");
        backtoView.setOnAction(f -> {
            openRooms();
        });

        HBox buttonBar = new HBox(10);
        buttonBar.setPadding(new Insets(10));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        buttonBar.getChildren().addAll(backtoView, spacer, addRoomBtn);

        newRoom.getChildren().addAll(title, roomName, buttonBar);
        root.setCenter(newRoom);
    }

    private void openRoomEditor(boolean edit, Raum room) {
        currentRefreshAction = () -> openRoomEditor(edit, room);
        VBox roomEditor = new VBox(10);
        roomEditor.setPadding(new Insets(20));

        Label title = new Label("Raum");
        title.getStyleClass().add("header");

        Label nameLabel = new Label("Raumname:");
        TextField nameField = new TextField(room.getName());

        final boolean[] isEditing = {edit};

        nameField.setEditable(isEditing[0]);

        HBox raumnamebar = new HBox(10);
        raumnamebar.setPadding(new Insets(20));
        raumnamebar.getChildren().addAll(nameLabel, nameField);

        Label idLabel = new Label("Id: " + room.getId());
        idLabel.setPadding(new Insets(20));

        Button backtoView = new Button("Zurück");
        backtoView.setOnAction(f -> openRooms());

        Button editBtn = new Button();

        editBtn.setText(isEditing[0] ? "Speichern" : "Bearbeiten");

        editBtn.setOnAction(e -> {
            if (isEditing[0]) {
                String oldName = room.getName();
                room.setName(nameField.getText());
                log("Raum umbenannt von '" + oldName + "' zu '" + room.getName() + "'");

                isEditing[0] = false;
                nameField.setEditable(false);
                editBtn.setText("Bearbeiten");

            } else {
                isEditing[0] = true;
                nameField.setEditable(true);
                editBtn.setText("Speichern");
            }
        });

        HBox buttonBar = new HBox(10);
        buttonBar.setPadding(new Insets(10));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        buttonBar.getChildren().addAll(backtoView, spacer, editBtn);

        roomEditor.getChildren().addAll(title, idLabel, raumnamebar, buttonBar);

        root.setCenter(roomEditor);
    }

    private void openScenarios(){
        currentRefreshAction = this::openScenarios;
        VBox scenariosView = new VBox(10);
        scenariosView.setPadding(new Insets(20));

        Label title = new Label("Szenarien");
        title.getStyleClass().add("header");

        TableView<Scenario> table = new TableView<>();
        TableColumn<Scenario, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getName())
        );

        TableColumn<Scenario, String> descCol = new TableColumn<>("Kurzbeschreibung");
        descCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDescription())
        );

        TableColumn<Scenario, String> actionsCol = new TableColumn<>("Anzahl enthaltener Aktionen");
        actionsCol.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getCommands().size()))
        );

        table.getColumns().addAll(nameCol, descCol, actionsCol);
        table.setItems(scenarioService.getScenarios());

        Button addScenario = new Button("Neu");
        addScenario.setOnAction(e -> {openAddScenario();});

        Button viewScenario = new Button("Anzeigen");
        viewScenario.setOnAction(e -> {
            Scenario scenario = table.getSelectionModel().getSelectedItem();
            if (null != scenario){
                log("Szenario details angezeigt: " + scenario.getName());
                openScenarioEditor(false, scenario);
            }
        });

        Button changeScenario = new Button("Bearbeiten");
        changeScenario.setOnAction(e -> {
            Scenario scenario = table.getSelectionModel().getSelectedItem();
            if (null != scenario){
                log("Szenario '" + scenario.getName() + "' wird bearbeitet.");
                openScenarioEditor(true, scenario);
            }
        });

        Button deleteScenario = new Button("Löschen");
        deleteScenario.setOnAction(e -> {
            Scenario scenario = table.getSelectionModel().getSelectedItem();
            if (null != scenario){
                log("Szenario gelöscht: " + scenario.getName());
                scenarioService.deleteScenario(scenario);
                openScenarios();
            }
        });

        Button runScenario = new Button("Ausführen");
        runScenario.setOnAction(e -> {
            Scenario scenario = table.getSelectionModel().getSelectedItem();
            if (scenario != null) {
                runaScenario(scenario);
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttonBar = new HBox(10);
        buttonBar.setPadding(new Insets(10));
        buttonBar.getChildren().addAll(addScenario, viewScenario, changeScenario, deleteScenario, spacer, runScenario);

        scenariosView.getChildren().addAll(title, table, buttonBar);
        root.setCenter(scenariosView);

    }

    private void openAddScenario() {
        currentRefreshAction = this::openAddScenario;
        VBox newScenario = new VBox(10);
        newScenario.setPadding(new Insets(20));

        Label title = new Label("Szenarien");
        title.getStyleClass().add("header");

        TextField scenarioName = new TextField();
        scenarioName.setPromptText("Name");

        TextField scenarioDescription = new TextField();
        scenarioDescription.setPromptText("Beschreibung");

        Button addScenarioBtn = new Button("Erstellen");
        addScenarioBtn.setOnAction(f -> {
            if (scenarioName.getText().isEmpty()) {
                TextField errorField = new TextField("Bitte einen Namen eingeben");
                newScenario.getChildren().add(errorField);
                errorField.setEditable(false);
                scenarioName.requestFocus();
                log("Fehler beim Erstellen eines Szenarios: Kein Name angegeben.");
            }
            else{
                scenarioService.addScenario(new Scenario(scenarioName.getText(), scenarioDescription.getText()));
                log("Neues Szenario erstellt: " + scenarioName.getText());
                openScenarios();
            }
        });
        Button backtoView = new Button("Zurück");
        backtoView.setOnAction(f -> {
            openScenarios();
        });

        HBox buttonBar = new HBox(10);
        buttonBar.setPadding(new Insets(10));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        buttonBar.getChildren().addAll(backtoView, spacer, addScenarioBtn);

        newScenario.getChildren().addAll(title, scenarioName, scenarioDescription, buttonBar);
        root.setCenter(newScenario);
    }

    private void openScenarioEditor(boolean edit, Scenario scenario) {
        currentRefreshAction = () -> openScenarioEditor(edit, scenario);
        VBox scenarioEditor = new VBox(15);
        scenarioEditor.setPadding(new Insets(20));

        Label title = new Label("Szenario");
        title.getStyleClass().add("header");

        final boolean[] isEditing = {edit};

        Label nameLabel = new Label("Name:");
        TextField nameField = new TextField(scenario.getName());
        nameField.setEditable(isEditing[0]);
        HBox nameBar = new HBox(10, nameLabel, nameField);

        Label descriptionLabel = new Label("Beschreibung:");
        TextField descriptionField = new TextField(scenario.getDescription());
        descriptionField.setEditable(isEditing[0]);
        HBox descriptionBar = new HBox(10, descriptionLabel, descriptionField);

        TableView<Command> tableDeviceCommands = new TableView<>();
        TableColumn<Command, String> orderCol = new TableColumn<>("Reihenfolge");
        orderCol.setCellValueFactory(data ->
                new SimpleStringProperty(Integer.toString(data.getValue().getOrderIndex()))
        );

        TableColumn<Command, String> deviceCommandCol = new TableColumn<>("Aktion");
        deviceCommandCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().toString())
        );

        tableDeviceCommands.getColumns().addAll(orderCol, deviceCommandCol);
        ObservableList<Command> commands =
                FXCollections.observableArrayList(scenario.getCommands());
        tableDeviceCommands.setItems(commands);


        scenarioEditor.getChildren().addAll(
                title,
                nameBar,
                descriptionBar,
                tableDeviceCommands);
        Button backBtn = new Button("Zurück");
        backBtn.setOnAction(e -> openScenarios());

        Button addCommandBtn = new Button("Aktion hinzufügen");
        addCommandBtn.setOnAction(e -> {
            scenario.setName(nameField.getText());
            scenario.setDescription(descriptionField.getText());
            openAddCommand(scenario);
            openScenarioEditor(isEditing[0], scenario);
        });
        addCommandBtn.setDisable(!isEditing[0]);

        Button viewCommandBtn = new Button("Aktion anzeigen");
        viewCommandBtn.setOnAction(e -> {
            scenario.setName(nameField.getText());
            scenario.setDescription(descriptionField.getText());
            Command command = tableDeviceCommands.getSelectionModel().getSelectedItem();
            if (null != command){
                openCommandEditor(command, false, scenario, edit);
            }
        });
        viewCommandBtn.setDisable(!isEditing[0]);

        Button changeCommandBtn = new Button("Aktion ändern");
        changeCommandBtn.setOnAction(e -> {
            scenario.setName(nameField.getText());
            scenario.setDescription(descriptionField.getText());
            Command command = tableDeviceCommands.getSelectionModel().getSelectedItem();
            if (null != command){
                openCommandEditor(command, true, scenario, edit);
            }
        });
        changeCommandBtn.setDisable(!isEditing[0]);

        Button deleteCommandBtn = new Button("Aktion löschen");
        deleteCommandBtn.setOnAction(e -> {
            scenario.setName(nameField.getText());
            scenario.setDescription(descriptionField.getText());
            Command command = tableDeviceCommands.getSelectionModel().getSelectedItem();
            if (command != null) {
                for (Command command2 : tableDeviceCommands.getItems()) {
                    if (command2.getOrderIndex() > command.getOrderIndex()) {
                        command2.setOrderIndex(command2.getOrderIndex() - 1);
                    }
                }
                scenario.getCommands().remove(command);
                log("Aktion aus Szenario '" + scenario.getName() + "' gelöscht.");
                openScenarioEditor(isEditing[0], scenario);
            }
        });
        deleteCommandBtn.setDisable(!isEditing[0]);

        Button editBtn = new Button();

        editBtn.setText(isEditing[0]
                ? "Speichern"
                : "Bearbeiten"
        );

        editBtn.setOnAction(e -> {
            if (isEditing[0]) {
                scenario.setName(nameField.getText());
                scenario.setDescription(descriptionField.getText());
                log("Szenario-Änderungen gespeichert: " + scenario.getName());

                isEditing[0] = false;
                nameField.setEditable(false);
                descriptionField.setEditable(false);
                addCommandBtn.setDisable(true);
                changeCommandBtn.setDisable(true);
                viewCommandBtn.setDisable(true);
                deleteCommandBtn.setDisable(true);
                editBtn.setText("Bearbeiten");

            } else {
                isEditing[0] = true;
                nameField.setEditable(true);
                descriptionField.setEditable(true);
                addCommandBtn.setDisable(false);
                changeCommandBtn.setDisable(false);
                viewCommandBtn.setDisable(false);
                deleteCommandBtn.setDisable(false);
                editBtn.setText("Speichern");
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttonBar = new HBox(
                10,
                backBtn,
                addCommandBtn,
                viewCommandBtn,
                changeCommandBtn,
                deleteCommandBtn,
                spacer,
                editBtn
        );
        scenarioEditor.getChildren().addAll(buttonBar);
        root.setCenter(scenarioEditor);
    }

    private void openAddCommand(Scenario scenario) {
        currentRefreshAction = () -> openAddCommand(scenario);

        Dialog<Command> dialog = new Dialog<>();
        dialog.setTitle("Aktion hinzufügen");

        ButtonType saveButtonType = new ButtonType("Speichern", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<Device> deviceBox = new ComboBox<>();
        deviceBox.getItems().addAll(deviceService.getDevices());

        ComboBox<String> actionTypeBox = new ComboBox<>();

        deviceBox.valueProperty().addListener((obs, oldDevice, newDevice) -> {

            actionTypeBox.getItems().clear();

            if (newDevice == null) return;

            switch (newDevice.getType()) {

                case HEATING -> actionTypeBox.getItems().addAll(
                        ActionType.TURN_OFF.toString(),
                        ActionType.TURN_ON.toString(),
                        ActionType.SET_TEMPERATURE.toString()
                );

                case LAMP -> actionTypeBox.getItems().addAll(
                        ActionType.TURN_OFF.toString(),
                        ActionType.TURN_ON.toString(),
                        ActionType.SET_BRIGHTNESS.toString()
                );

                case SHUTTER -> actionTypeBox.getItems().addAll(
                        ActionType.ROLL_UP.toString(),
                        ActionType.ROLL_DOWN.toString(),
                        ActionType.SET_POSITION.toString()
                );
            }
        });

        TextField valueBox = new TextField();

        enum ValueType { NONE, INT, DOUBLE }

        java.util.function.Function<ActionType, ValueType> getValueType = (actionType) -> {
            if (actionType == null) return ValueType.NONE;

            return switch (actionType) {
                case SET_TEMPERATURE -> ValueType.DOUBLE;
                case SET_BRIGHTNESS,
                     SET_POSITION -> ValueType.INT;
                default -> ValueType.NONE;
            };
        };

        Runnable applyValueState = () -> {

            ValueType type = getValueType.apply(ActionType.getValue(actionTypeBox.getValue()));
            boolean requiresValue = type != ValueType.NONE;

            valueBox.setDisable(!requiresValue);

            if (!requiresValue) {
                valueBox.clear();
            }
        };

        Runnable validate = () -> {

            Device device = deviceBox.getValue();

            ActionType actionType = null;
            if (actionTypeBox.getValue() != null) {
                actionType = ActionType.getValue(actionTypeBox.getValue());
            }

            ValueType valueType = getValueType.apply(actionType);

            String text = valueBox.getText() == null ? "" : valueBox.getText().trim();

            boolean valueValid = false;

            try {

                switch (valueType) {

                    case NONE -> valueValid = true;

                    case INT -> {
                        int value = Integer.parseInt(text);

                        if (actionType == ActionType.SET_BRIGHTNESS
                                || actionType == ActionType.SET_POSITION) {

                            valueValid = value >= 0 && value <= 100;
                            if (!valueValid) {
                                log("Bitte einen Wert zwischen 0 und 100 eingeben.");
                            }
                        }
                    }

                    case DOUBLE -> {
                        double value = Double.parseDouble(text);

                        if (actionType == ActionType.SET_TEMPERATURE) {
                            valueValid = value >= 10 && value <= 35;
                            if (!valueValid) {
                                log("Bitte einen Wert zwischen 10 und 35 eingeben.");
                            }
                        }
                    }
                }

            } catch (NumberFormatException ex) {
                valueValid = false;
            }

            boolean invalid =
                    device == null
                            || actionType == null
                            || !valueValid;

            saveButton.setDisable(invalid);
        };

        actionTypeBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            applyValueState.run();
            validate.run();
        });

        deviceBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            validate.run();
        });

        valueBox.textProperty().addListener((obs, oldVal, newVal) -> {
            validate.run();
        });

        grid.add(new Label("Gerät:"), 0, 0);
        grid.add(deviceBox, 1, 0);

        grid.add(new Label("Aktionstyp:"), 0, 1);
        grid.add(actionTypeBox, 1, 1);

        grid.add(new Label("Wert:"), 0, 2);
        grid.add(valueBox, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {

            if (button == saveButtonType) {

                Device device = deviceBox.getValue();
                ActionType actionType = ActionType.getValue(actionTypeBox.getValue());

                int orderIndex = scenario.getCommands().size();

                Command command = CommandFactory.create(device, actionType, valueBox.getText(), scenario.getCommands().size());
                return command;
            }

            return null;
        });

        Optional<Command> result = dialog.showAndWait();

        result.ifPresent(deviceCommand -> {
            scenario.getCommands().add(deviceCommand);
            log("Aktion '" + deviceCommand.toString() + "' zu Szenario '" + scenario.getName() + "' hinzugefügt.");
        });
    }

    private boolean needsValue(String actionType) {
        if (actionType == null) {
            return false;
        }
        return switch (actionType) {
            case "Temperatur setzen",
                 "Helligkeit setzen",
                 "Position setzen" -> true;
            default -> false;
        };
    }

    private List<ActionType> getActionsForDevice(Device device) {
        return switch (device.getType()) {
            case HEATING -> List.of(
                    ActionType.TURN_OFF,
                    ActionType.TURN_ON,
                    ActionType.SET_TEMPERATURE
            );

            case LAMP -> List.of(
                    ActionType.TURN_OFF,
                    ActionType.TURN_ON,
                    ActionType.SET_BRIGHTNESS
            );

            case SHUTTER -> List.of(
                    ActionType.ROLL_UP,
                    ActionType.ROLL_DOWN,
                    ActionType.SET_POSITION
            );
        };
    }

    private void openCommandEditor(Command command,
                                   boolean edit,
                                   Scenario scenario,
                                   boolean editScenario) {

        currentRefreshAction = () -> openCommandEditor(
                command,
                edit,
                scenario,
                editScenario
        );

        VBox editor = new VBox(15);
        editor.setPadding(new Insets(20));

        Label title = new Label("Aktion");
        title.getStyleClass().add("header");

        final boolean[] isEditing = {edit};

        Label deviceLabel = new Label(
                "Gerät: " + command.getDevice().getName()
        );


        Label actionTypeLabel = new Label("Aktionstyp:");

        ComboBox<ActionType> actionTypeBox = new ComboBox<>();

        actionTypeBox.getItems().addAll(
                ActionType.forDevice(command.getDevice())
        );

        actionTypeBox.setValue(command.getActionType());

        actionTypeBox.setDisable(!isEditing[0]);

        HBox actionTypeBar = new HBox(10, actionTypeLabel, actionTypeBox);


        Label valueLabel = new Label("Wert:");
        TextField valueField = new TextField();

        valueField.setDisable(!isEditing[0]);

        if (command instanceof SetBrightnessLampCommand c) {
            valueField.setText(String.valueOf(c.getBrightness()));
        } else if (command instanceof SetPositionShutterCommand c) {
            valueField.setText(String.valueOf(c.getPosition()));
        } else if (command instanceof SetTemperatureHeatingCommand c) {
            valueField.setText(String.valueOf(c.getTemperature()));
        } else {
            valueField.setText("");
        }

        HBox valueBar = new HBox(10, valueLabel, valueField);

        editor.getChildren().addAll(
                title,
                deviceLabel,
                actionTypeBar,
                valueBar
        );


        Button backBtn = new Button("Zurück");
        backBtn.setOnAction(e -> openScenarioEditor(editScenario, scenario));


        enum ValueType { NONE, INT, DOUBLE }

        java.util.function.Function<ActionType, ValueType> getValueType = (type) -> {
            if (type == null) return ValueType.NONE;

            return switch (type) {
                case SET_TEMPERATURE -> ValueType.DOUBLE;
                case SET_BRIGHTNESS,
                     SET_POSITION -> ValueType.INT;
                default -> ValueType.NONE;
            };
        };


        Runnable applyValueState = () -> {
            ValueType type = getValueType.apply(actionTypeBox.getValue());
            boolean requiresValue = type != ValueType.NONE;

            valueField.setDisable(!isEditing[0] || !requiresValue);

            if (!requiresValue && isEditing[0]) {
                valueField.clear();
            }
        };


        Button editBtn = new Button(isEditing[0] ? "Speichern" : "Bearbeiten");

        editBtn.setOnAction(e -> {

            if (isEditing[0]) {

                Device device = command.getDevice();
                ActionType type = actionTypeBox.getValue();
                String value = valueField.getText();

                Command newCommand =
                        CommandFactory.create(device, type, value, command.getOrderIndex());

                scenario.replaceCommand(command, newCommand);
                log("Szenario Aktion geändert zu: " + newCommand.toString()); // NEU

                isEditing[0] = false;

                actionTypeBox.setDisable(true);
                valueField.setDisable(true);

                editBtn.setText("Bearbeiten");

                openScenarioEditor(editScenario, scenario);
                return;
            }

            // EDIT MODE
            isEditing[0] = true;

            actionTypeBox.setDisable(false);
            applyValueState.run();

            editBtn.setText("Speichern");
        });

        Runnable validate = () -> {

            if (!isEditing[0]) {
                editBtn.setDisable(false);
                return;
            }

            ActionType type = actionTypeBox.getValue();
            String text = valueField.getText() == null
                    ? ""
                    : valueField.getText().trim();

            boolean valueValid = true;

            try {
                switch (type) {
                    case SET_BRIGHTNESS, SET_POSITION -> {
                        int value = Integer.parseInt(text);
                        valueValid = value >= 0 && value <= 100;
                        if (!valueValid) {
                            log("Bitte einen Wert zwischen 0 und 100 eingeben.");
                        }
                    }

                    case SET_TEMPERATURE -> {
                        double value = Double.parseDouble(text);
                        valueValid = value >= 10 && value <= 35;
                        if (!valueValid) {
                            log("Bitte einen Wert zwischen 10 und 35 eingeben.");
                        }
                    }

                    default -> {}
                }
            } catch (Exception ex) {
                valueValid = false;
            }

            editBtn.setDisable(type == null || !valueValid);
        };


        actionTypeBox.valueProperty().addListener((obs, o, n) -> {
            applyValueState.run();
            validate.run();
        });

        valueField.textProperty().addListener((obs, o, n) -> {
            validate.run();
        });

        applyValueState.run();
        validate.run();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttonBar = new HBox(
                10,
                backBtn,
                spacer,
                editBtn
        );

        editor.getChildren().add(buttonBar);

        root.setCenter(editor);
    }

    private VBox createCard(String type, String room, String value) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setPrefSize(200, 120);
        card.getStyleClass().add("card");

        Label typeLabel = new Label(type);
        typeLabel.getStyleClass().add("card-title");

        Label roomLabel = new Label(room);
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("card-value");

        card.getChildren().addAll(typeLabel, roomLabel, valueLabel);
        return card;
    }

    public static void main(String[] args) {
        launch();
    }
}