package FleetManagerApp;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ServiceCenterDAO {
    private final Connection conn;

    public ServiceCenterDAO(Connection conn) {
        this.conn = conn;
    }

    /**
     * Inserts a new service center and returns its auto-generated ID.
     */
    public long insertServiceCenter(String name, String location, String phone, String specialty) throws Exception {
        String sql = "INSERT INTO service_centers (name, location, contact_phone, specialty) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql, new String[] { "CENTER_ID" })) {
            pstmt.setString(1, name);
            pstmt.setString(2, location);
            pstmt.setString(3, phone);
            pstmt.setString(4, specialty);

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
        }
        throw new Exception("❌ Failed to retrieve ID for service center......");
    }
}