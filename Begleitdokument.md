# Begleitdokument zum Leistungsnachweis Softwaredesign
**Thema:** Smart-Home-Szenario-Editor  
**Modul:** Anwendungsentwicklung III: Software Engineering

**Studierende:** Anna Knötgen, Marcel Kecker, Marcel Woker, Viktor Kalka

---

### 1. Anwendungsstruktur & Architektur
Die Anwendung implementiert strikt das **Model-View-Controller (MVC) Architekturmuster** zur sauberen Entkopplung von Fachlogik, Datenhaltung und Präsentationsebene:

- **Model (Fachlogik & Daten):** Beinhaltet die Domänenobjekte wie `Raum`, `Device` (sowie die konkreten Implementierungen `Lamp`, `Heating`, `Shutter`) und `Scenario`. Das Model besitzt keinerlei Kenntnisse oder Abhängigkeiten zur grafischen Benutzeroberfläche (GUI).
- **View (Präsentation):** Realisiert durch die JavaFX-Komponenten innerhalb von `SmartHomeApp.java`. Sie bindet Steuerelemente an die Datenstrukturen und visualisiert die Zustände.
- **Controller / Service-Layer (Vermittlung):** Die Klassen `RoomService`, `DeviceService` und `ScenarioService` verwalten den globalen Zustand der Anwendung im Arbeitsspeicher, steuern die Datenströme und kapseln die Geschäftslogik ab.

---

### 2. GUI-Aufbau und Interaktionskonzept
Das GUI-Layout folgt dem im Anhang des Leistungsnachweises empfohlenen Drei-Spalten-Prinzip für Desktop-Anwendungen:

- **Hauptfenster (`BorderPane`):** Bildet den äußeren Rahmen.
- **TopBar (`North`):** Enthält persistente Aktionen wie das Laden und Speichern von Konfigurationsdateien sowie globale Steuerungselemente.
- **Navigation (`West`):** Eine Toolbox oder Liste, um zwischen den Hauptbereichen (Räume, Geräte, Szenarien) zu wechseln.
- **Arbeitsbereich (`Center`):** Zeigt dynamisch Tabellen, Formulare oder Detail-Editoren für die aktuell ausgewählten Elemente an.
- **Protokollbereich (`South`):** Ein Aktivitäts-Log, das Systemereignisse, Konsolenausgaben und Fehler interaktiv visualisiert.

---

### 3. Datenhaltung & JSON-Persistenz
Die Speicherung und das Laden von Projekten erfolgt über das Framework **Jackson (ObjectMapper)**. Da es sich bei den Szenario-Aktionen um polymorphe Strukturen handelt (verschiedene Implementierungen des `Command`-Interfaces für Lampen, Heizungen und Rollläden), wurde ein robustes Mapping implementiert:

- **Typerkennung:** Jackson nutzt die Annotation `@JsonTypeInfo` zur Identifikation der konkreten Implementierungsklassen über das interne Feld `commandClassType`.
- **Entkopplung:** Die technischen Subtype-Namen (`LampOn`, `HeatingOff` etc.) wurden von den UI-Enums (`ActionType`) entkoppelt (`@JsonSubTypes`). Dies verhindert doppelte Attribute oder Namenskonflikte in den JSON-Dateien (`NeuTest.json`, `Smarthome.json`), falls verschiedene Geräte identische Befehlsnamen (z. B. `TURN_ON`) besitzen.
- **Referenzintegrität:** Da Jackson beim Laden Objekte neu instanziiert, arbeiten alle Schutzmechanismen und Abgleiche auf Basis von eindeutigen IDs (`getID()`) oder eindeutigen Raumnamen anstelle volatiler Objektreferenzen.

---

### 4. Eingesetzte Entwurfsmuster (Design Patterns)
Zur Sicherung der Codequalität und zur flexiblen Objektverwaltung wurden bewährte Entwurfsmuster der Softwaretechnik implementiert:

- **Command Pattern (Kommando-Muster):** Alle im Szenario ausführbaren Aktionen implementieren das gemeinsame Interface `Command`. Die konkrete Ausführungslogik ist in den Klassen gekapselt (z. B. `TurnOnLampCommand`, `SetTemperatureHeatingCommand`). Die GUI steuert die Aktionen ausschließlich abstrakt über das Interface an (`execute()`), was eine einfache Protokollierung und flexible Verkettung ermöglicht.
- **Factory Pattern (Fabrikmuster):** Die Erzeugung der polymorphen Geräte und Kommandos wird über dedizierte Fabriken (`DeviceFactory` und `CommandFactory`) entkoppelt. Die Anwendung fordert Objekte an, ohne die exakten Konstruktoren oder Subklassen kennen zu müssen.
- **Service Locator / Singleton-Ansatz:** Die Verwaltungskomponenten (`DeviceService`, `RoomService`, `ScenarioService`) arbeiten als zentrale Anlaufstellen im System, um den globalen Zustand im Arbeitsspeicher über den gesamten Lebenszyklus der Anwendung hinweg konsistent bereitzustellen.

---

### 5. Erweiterbarkeit um neue Gerätetypen
Die Architektur der Anwendung wurde gezielt nach dem **Open-Closed-Prinzip** (offen für Erweiterungen, geschlossen für Modifikationen) entworfen. Das Hinzufügen eines komplett neuen Gerätetyps (z. B. einer *Steckdose* oder eines *Lüfters*) erfordert keine strukturellen Änderungen am bestehenden Kerncode:

1. **Modell erweitern:** Es wird eine neue Klasse erstellt, die vom Basis-Interface `Device` erbt bzw. die abstrakten Eigenschaften implementiert.
2. **Kommandos anlegen:** Die gerätespezifischen Befehlsklassen werden erstellt und implementieren das `Command`-Interface.
3. **Jackson-Registrierung:** Im zentralen `Command`-Interface wird die neue Klasse mit einer einzigen Zeile im `@JsonSubTypes`-Block registriert (z. B. `@JsonSubTypes.Type(value = TurnOnPlugCommand.class, name = "PlugOn")`).
4. **GUI/Factory-Update:** In der `DeviceFactory` und in der ComboBox-Auswahl der GUI wird das neue Gerät registriert. Die restliche Anwendung (Tabellen, Szenario-Ausführung, Speicher- und Ladelogik) verarbeitet das neue Gerät dank der Schnittstellen-Abstraktion vollautomatisch.

---

### 6. Datenvalidierung & Löschschutz
Die Geschäftslogik der Anwendung blockiert fehlerhafte Benutzeraktionen in Echtzeit und schützt die strukturelle Integrität des Smart Homes vor verwaisten Referenzen („Datenleichen“):

- **Raum-Löschschutz:** Ein Raum kann im System erst gelöscht werden, wenn ihm im `DeviceService` keine aktiven Geräte mehr zugeordnet sind. Andernfalls wird der Löschvorgang blockiert und ein JavaFX `Alert`-Warning-Dialog angezeigt.
- **Geräte-Löschschutz:** Ein Gerät darf nicht gelöscht werden, solange es noch als Aktor in mindestens einem Szenario-Kommando innerhalb des `ScenarioService` hinterlegt ist.
- **JSON-Resistenz durch ID-Entkopplung:** Da Jackson beim Laden einer JSON-Datei Objekte im Arbeitsspeicher komplett neu instanziiert, würden klassische Objektreferenz-Vergleiche (`==` oder Standard-`.equals()`) nach dem Import versagen. Die Schutzmechanismen wurden daher so entworfen, dass sie die Integrität über eindeutige funktionale Schlüssel prüfen:
    - Der Raum-Löschschutz prüft die Zuordnung über den eindeutigen `String` des Raumnamens (`device.getRoom().getName()`).
    - Der Geräte-Löschschutz prüft die Verwendung innerhalb von Szenarien über den Abgleich der eindeutigen Geräte-UUID (`cmd.getDevice().getId()`).

---

### 7. Qualitätssicherung & Testkonzept
- **Automatisierte Unit-Tests:** Mithilfe von **JUnit 4** und **TestFX** werden funktionale Prüfungen der Core-Logik sowie automatisierte Oberflächen-Interaktionen durchgeführt. Getestet wird das Erstellen von Räumen, das Hinzufügen von Aktoren sowie die korrekte Kaskadierung von Werteänderungen bei der Ausführung komplexer Szenarien. Eine Mindestabdeckung von **50% Line-Coverage** ist im Build-Prozess fest verankert.
- **GUI-Testing / Validierung:** Die Benutzeroberfläche fängt fehlerhafte Zustände interaktiv ab. Die Speichern-Schaltflächen in Dialogen werden über Listener dynamisch deaktiviert (`setDisable(true)`), solange Pflichtfelder leer sind oder ungültige Datentypen (z. B. Buchstaben im Temperaturfeld) eingegeben werden.

---

### 8. Build-Ablauf & Statische Codeanalyse
Der gesamte Build-Prozess ist über den Maven-Lifecycle standardisiert. Er erzwingt beim Aufruf von `mvn clean verify` die strikte Einhaltung des Qualitäts-Gateways in folgender Reihenfolge:

1. **`validate`:** Der Quellcode wird mittels **Checkstyle** vollautomatisch verifiziert. Hierbei ist das offizielle und strenge Regelwerk des **Google Java Styles** (`google_checks.xml`) fest integriert. Es deckt weit über die geforderten 10 Mindestregeln ab und prüft Kriterien wie:
    - Namenskonventionen (Klassen, Methoden, Variablen und Konstanten)
    - Blockaufbau und Klammerplatzierung (`LeftCurly`, `RightCurly`)
    - Vermeidung von Wildcard-Imports
    - Whitespace-Regeln und Code-Formatierung
      Schlägt ein Stil-Verstoß an, bricht das Plug-in den Build sofort mit einem Fehler ab (`failsOnError=true`).
2. **`compile`:** Kompilierung des Quellcodes auf Basis von JDK 17.
3. **`test`:** Ausführung aller JUnit-Tests. Schlägt ein funktionaler Test fehl, bricht Maven ab.

### 9. Starten der Anwendung
`mvn javafx:run`