package penastudiohris;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class KoneksiDB {
    private static Connection koneksi;
    
    public static Connection getKoneksi() {
        // Mengecek apakah koneksi masih kosong
        if (koneksi == null) {
            try {
                // Konfigurasi database sesuai dengan yang di XAMPP
                String url = "jdbc:mysql://localhost:3306/db_penastudio_hris";
                String user = "root"; // Default username XAMPP
                String password = ""; // Default password XAMPP biasanya kosong
                
                // Mendaftarkan Driver MySQL
                DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
                
                // Membuat koneksi
                koneksi = DriverManager.getConnection(url, user, password);
                System.out.println("Koneksi Database Berhasil!");
                
            } catch (SQLException e) {
                System.out.println("Koneksi Database Gagal: " + e.getMessage());
                JOptionPane.showMessageDialog(null, "Koneksi Database Gagal!\n" + e.getMessage());
            }
        }
        return koneksi;
    }
}