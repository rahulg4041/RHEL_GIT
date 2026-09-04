package FleetManagerApp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class DriverDAO {
    private final Connection conn;

    public DriverDAO(Connection conn) {
        this.conn = conn;
    }

    /**
     * Inserts a new driver and returns their auto-generated primary key ID.
     */
    public long insertDriver(String firstName, String lastName, String licenseNumber, String phone) throws Exception {
        String sql = "INSERT INTO DRIVERS (first_name, last_name, license_number, phone) VALUES (?, ?, ?, ?)";

        // Use Statement.RETURN_GENERATED_KEYS for standard MySQL AUTO_INCREMENT key retrieval
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, licenseNumber);
            pstmt.setString(4, phone);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new Exception("❌ Inserting driver failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
        }
        throw new Exception("❌ Failed to retrieve auto-generated ID for new driver.");
    }
}