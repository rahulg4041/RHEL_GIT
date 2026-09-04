package FleetManagerApp;

public class Expense {
    private long expenseId;
    private long vehicleId;
    private String expenseType;
    private String description;
    private double amount;
    private String expenseDate;

    public Expense() {}

    // Getters and Setters
    public long getExpenseId() { return expenseId; }
    public void setExpenseId(long expenseId) { this.expenseId = expenseId; }

    public long getVehicleId() { return vehicleId; }
    public void setVehicleId(long vehicleId) { this.vehicleId = vehicleId; }

    public String getExpenseType() { return expenseType; }
    public void setExpenseType(String expenseType) { this.expenseType = expenseType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getExpenseDate() { return expenseDate; }
    public void setExpenseDate(String expenseDate) { this.expenseDate = expenseDate; }
}