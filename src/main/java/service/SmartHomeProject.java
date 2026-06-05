package service;

import model.device.Device;
import model.room.Raum;
import model.scenario.Scenario;

import java.util.List;

public class SmartHomeProject {

    private List<Raum> rooms;
    private List<Device> devices;
    private List<Scenario> scenarios;

    public SmartHomeProject() {}

    public SmartHomeProject(
            List<Raum> rooms,
            List<Device> devices,
            List<Scenario> scenarios) {

        this.rooms = rooms;
        this.devices = devices;
        this.scenarios = scenarios;
    }


    public List<Raum> getRooms() {
        return rooms;
    }

    public List<Device> getDevices() {
        return devices;
    }

    public List<Scenario> getScenarios() {
        return scenarios;
    }

    public void setRooms(List<Raum> rooms) {
        this.rooms = rooms;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }

    public void setScenarios(List<Scenario> scenarios) {
        this.scenarios = scenarios;
    }
}