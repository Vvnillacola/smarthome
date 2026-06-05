package model.room;

public class Raum {
    private static int idcounter;
    private int id;
    private String name;

    public Raum() {}

    public Raum(String name) {
        this.id = idcounter++;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

