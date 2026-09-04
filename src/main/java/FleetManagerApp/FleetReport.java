package FleetManagerApp;

public class FleetReport {
    private int id;
    private String vehicleModel;
    private String plate;
    private String status;
    private int totalTrips;
    private double totalKms;
    private double totalExpenses;
    private double costPerKm;

    public FleetReport() {}

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }

    public String getPlate() { return plate; }
    public void setPlate(String plate) { this.plate = plate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getTotalTrips() { return totalTrips; }
    public void setTotalTrips(int totalTrips) { this.totalTrips = totalTrips; }

    public double getTotalKms() { return totalKms; }
    public void setTotalKms(double totalKms) { this.totalKms = totalKms; }

    public double getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(double totalExpenses) { this.totalExpenses = totalExpenses; }

    public double getCostPerKm() { return costPerKm; }
    public void setCostPerKm(double costPerKm) { this.costPerKm = costPerKm; }
}