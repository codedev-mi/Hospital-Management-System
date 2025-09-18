package HospitalManagementSystem;

import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/hospital";
        String username = "Shruti";
        String password = "Shruti@16";

        SwingUtilities.invokeLater(() -> {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection conn = DriverManager.getConnection(url, username, password);
                new HospitalManagementUI(conn).setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                        "Failed to connect to DB: " + ex.getMessage(), "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
    }
}
