package FleetManagerApp;

public class Vehicle {
    private int id;
    private String make;
    private String model;
    private int year;
    private String status;

    // Default Constructor
    public Vehicle() {}

    // Parameterized Constructor
    public Vehicle(int id, String make, String model, int year, String status) {
        this.id = id;
        this.make = make;
        this.model = model;
        this.year = year;
        this.status = status;
    }

    // Getters and Setters (Required for Spring to generate JSON)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}