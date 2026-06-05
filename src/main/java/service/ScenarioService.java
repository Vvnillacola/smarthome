package service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.scenario.Scenario;

import java.util.List;

public class ScenarioService {
    private ObservableList<Scenario> scenarios = FXCollections.observableArrayList();

    public ObservableList<Scenario> getScenarios() {
        return scenarios;
    }

    public void addScenario(Scenario scenario) {
        scenarios.add(scenario);
    }

    public void deleteScenario(Scenario scenario) {
        scenarios.remove(scenario);
    }

    public void setScenarios(List<Scenario> newScenarios) {
        this.scenarios.clear();

        if (newScenarios != null) {
            this.scenarios.addAll(newScenarios);
        }
    }

    public void clear() {
        this.scenarios.clear();
    }
}

