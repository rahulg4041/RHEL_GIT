package FleetManagerApp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;

public class ExpenseDAO {
    private final Connection conn;

    public ExpenseDAO(Connection conn) {
        this.conn = conn;
    }

    /**
     * Logs a new expense into the database.
     * centerId and tripId can be null depending on the expense type.
     */
    public void logExpense(long vehicleId, Long tripId, Long centerId, double amount, String expenseType, String description) throws Exception {
        String sql = "INSERT INTO EXPENSES (vehicle_id, trip_id, center_id, amount, expense_type, description) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, vehicleId);

            // Handle optional foreign keys using standard SQL BIGINT/INTEGER types for MySQL
            if (tripId != null) {
                pstmt.setLong(2, tripId);
            } else {
                pstmt.setNull(2, Types.BIGINT);
            }

            if (centerId != null) {
                pstmt.setLong(3, centerId);
            } else {
                pstmt.setNull(3, Types.BIGINT);
            }

            pstmt.setDouble(4, amount);
            pstmt.setString(5, expenseType); // Must be 'FUEL', 'TOLL', 'MAINTENANCE', etc.
            pstmt.setString(6, description);

            pstmt.executeUpdate();
        }
    }
}