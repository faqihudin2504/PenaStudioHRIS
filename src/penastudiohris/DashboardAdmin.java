/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package penastudiohris;

/**
 *
 * @author HP
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
public class DashboardAdmin extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(DashboardAdmin.class.getName());

    /**
     * Creates new form DashboardAdmin
     * @param namaAdmin
     */
    public DashboardAdmin(String namaAdmin) {
        initComponents();
    this.setLocationRelativeTo(null); // Membuat form di tengah layar
    pnlContent.removeAll();
    pnlContent.add(pnlDashboard);
    pnlContent.repaint();
    pnlContent.revalidate();
 
    lblNamaAdmin.setText("Halo, " + namaAdmin);
    
    loadDataDashboard();
    loadTableKehadiran();
    loadNotifikasiCuti();
    loadTableKaryawan();
    loadTableDataPresensi("");
    loadTableCuti();
    loadTablePekerjaan("");
    
    }
    private String idKaryawanTerpilih = "";
    
    // Variabel untuk menyimpan ID Pengajuan Cuti yang diklik
    private String idCutiTerpilih = "";
    
    private void loadTableCuti() {
    javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel();
    model.addColumn("ID Cuti");
    model.addColumn("Nama Karyawan");
    model.addColumn("Mulai");
    model.addColumn("Selesai");
    model.addColumn("Alasan");
    model.addColumn("Status");
    
    try {
        java.sql.Connection conn = KoneksiDB.getKoneksi();
        // Menggabungkan tabel pengajuan_cuti dan karyawan
        String sql = "SELECT c.id, k.nama_lengkap, c.tanggal_mulai, c.tanggal_selesai, c.alasan, c.status_approval "
                   + "FROM pengajuan_cuti c "
                   + "JOIN karyawan k ON c.karyawan_id = k.id "
                   + "ORDER BY c.id DESC"; // Urutkan dari yang terbaru
                   
        java.sql.PreparedStatement pst = conn.prepareStatement(sql);
        java.sql.ResultSet rs = pst.executeQuery();
        
        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getString("id"),
                rs.getString("nama_lengkap"),
                rs.getString("tanggal_mulai"),
                rs.getString("tanggal_selesai"),
                rs.getString("alasan"),
                rs.getString("status_approval").toUpperCase()
            });
        }
    } catch (Exception e) {
        System.out.println("Error load data cuti: " + e.getMessage());
    }
}
    
    private void loadDataDashboard() {
    try {
        // Buka koneksi ke database
        Connection conn = KoneksiDB.getKoneksi();
        
        // ==========================================
        // 1. Menghitung Total Karyawan
        // ==========================================
        String sqlTotal = "SELECT COUNT(*) AS total FROM karyawan WHERE role = 'karyawan'";
        PreparedStatement pst1 = conn.prepareStatement(sqlTotal);
        ResultSet rs1 = pst1.executeQuery();
        if (rs1.next()) {
            lblTotalKaryawan.setText(rs1.getString("total"));
        }
        
        // ==========================================
        // 2. Menghitung Hadir Hari Ini
        // ==========================================
        String sqlHadir = "SELECT COUNT(*) AS total FROM presensi WHERE tanggal = CURDATE()";
        PreparedStatement pst2 = conn.prepareStatement(sqlHadir);
        ResultSet rs2 = pst2.executeQuery();
        if (rs2.next()) {
            lblHadir.setText(rs2.getString("total"));
        }
        
        // ==========================================
        // 3. Menghitung Karyawan Cuti (Hari Ini)
        // ==========================================
        // Hanya menghitung cuti yang statusnya 'approved' dan tanggal hari ini berada di antara tanggal mulai & selesai
        String sqlCuti = "SELECT COUNT(*) AS total FROM pengajuan_cuti WHERE CURDATE() BETWEEN tanggal_mulai AND tanggal_selesai AND status_approval = 'approved'";
        PreparedStatement pst3 = conn.prepareStatement(sqlCuti);
        ResultSet rs3 = pst3.executeQuery();
        if (rs3.next()) {
            lblCuti.setText(rs3.getString("total"));
        }
        
        // ==========================================
        // 4. Pembagian WFO dan WFH (Hari Ini)
        // ==========================================
        String sqlWfoWfh = "SELECT "
                + "SUM(CASE WHEN tipe_kerja = 'wfo' THEN 1 ELSE 0 END) AS total_wfo, "
                + "SUM(CASE WHEN tipe_kerja = 'wfh' THEN 1 ELSE 0 END) AS total_wfh "
                + "FROM presensi WHERE tanggal = CURDATE()";
        PreparedStatement pst4 = conn.prepareStatement(sqlWfoWfh);
        ResultSet rs4 = pst4.executeQuery();
        if (rs4.next()) {
            int wfo = rs4.getInt("total_wfo");
            int wfh = rs4.getInt("total_wfh");
            
            // Formatnya menjadi "X / Y" (Misal: 1 / 0)
            lblWfoWfh.setText(wfo + " / " + wfh);
        }

    } catch (Exception e) {
        System.out.println("Error load dashboard: " + e.getMessage());
    }
}
    
    private void loadTableKehadiran() {
    // 1. Membuat pengaturan kolom tabel
    DefaultTableModel model = new DefaultTableModel();
    model.addColumn("Nama Karyawan");
    model.addColumn("Jam Masuk");
    model.addColumn("Tipe Kerja");
    
    // Menerapkan model tersebut ke tabel di UI Anda
    tblPresensiHariIni.setModel(model);
    
    try {
        Connection conn = KoneksiDB.getKoneksi();
        
        // 2. Query JOIN untuk mengambil nama dari tabel karyawan dan jam dari tabel presensi (Khusus hari ini)
        String sql = "SELECT k.nama_lengkap, p.jam_masuk, p.tipe_kerja "
                   + "FROM presensi p "
                   + "JOIN karyawan k ON p.karyawan_id = k.id "
                   + "WHERE p.tanggal = CURDATE() "
                   + "ORDER BY p.jam_masuk DESC"; // Urutkan dari yang absen terbaru
                   
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        
        // 3. Looping untuk memasukkan semua baris data ke dalam tabel
        while (rs.next()) {
            String nama = rs.getString("nama_lengkap");
            String jam = rs.getString("jam_masuk");
            String tipe = rs.getString("tipe_kerja").toUpperCase(); // WFO/WFH dibuat huruf besar
            
            // Tambahkan data ke dalam baris tabel
            model.addRow(new Object[]{nama, jam, tipe});
        }
        
    } catch (Exception e) {
        System.out.println("Error load tabel: " + e.getMessage());
    }
}
    
    // Tambahkan parameter kataKunci untuk fitur pencarian
private void loadTableDataPresensi(String kataKunci) {
    DefaultTableModel model = new DefaultTableModel();
    model.addColumn("Tanggal");
    model.addColumn("Nama Karyawan");
    model.addColumn("Jam Masuk");
    model.addColumn("Jam Keluar");
    model.addColumn("Tipe Kerja");
    model.addColumn("Lokasi GPS");
    
    tblDataPresensi.setModel(model);
    
    try {
        java.sql.Connection conn = KoneksiDB.getKoneksi();
        String sql;
        java.sql.PreparedStatement pst;
        
        // Mengecek apakah ada kata kunci pencarian
        if (kataKunci.isEmpty()) {
            // Jika kosong, tampilkan semua data, urutkan dari yang paling baru
            sql = "SELECT p.tanggal, k.nama_lengkap, p.jam_masuk, p.jam_keluar, p.tipe_kerja, p.lokasi_gps "
                + "FROM presensi p "
                + "JOIN karyawan k ON p.karyawan_id = k.id "
                + "ORDER BY p.tanggal DESC, p.jam_masuk DESC";
            pst = conn.prepareStatement(sql);
        } else {
            // Jika ada kata kunci, cari berdasarkan nama lengkap
            sql = "SELECT p.tanggal, k.nama_lengkap, p.jam_masuk, p.jam_keluar, p.tipe_kerja, p.lokasi_gps "
                + "FROM presensi p "
                + "JOIN karyawan k ON p.karyawan_id = k.id "
                + "WHERE k.nama_lengkap LIKE ? "
                + "ORDER BY p.tanggal DESC, p.jam_masuk DESC";
            pst = conn.prepareStatement(sql);
            pst.setString(1, "%" + kataKunci + "%"); // Menggunakan % untuk pencarian mirip
        }
        
        java.sql.ResultSet rs = pst.executeQuery();
        
        while (rs.next()) {
            // Mengubah format null pada jam keluar menjadi teks yang lebih baik
            String jamKeluar = rs.getString("jam_keluar");
            if (jamKeluar == null) {
                jamKeluar = "Belum Keluar";
            }
            
            model.addRow(new Object[]{
                rs.getString("tanggal"),
                rs.getString("nama_lengkap"),
                rs.getString("jam_masuk"),
                jamKeluar,
                rs.getString("tipe_kerja").toUpperCase(),
                rs.getString("lokasi_gps")
            });
        }
    } catch (Exception e) {
        System.out.println("Error load data presensi: " + e.getMessage());
    }
}
    
    private void loadNotifikasiCuti() {
    // 1. Mengatur judul kolom untuk notifikasi
    DefaultTableModel modelNotif = new DefaultTableModel();
    modelNotif.addColumn("Nama Karyawan");
    modelNotif.addColumn("Mulai Cuti");
    modelNotif.addColumn("Status");
    
    // Menerapkan model ke tabel notifikasi
    tblNotifCuti.setModel(modelNotif);
    
    try {
        java.sql.Connection conn = KoneksiDB.getKoneksi();
        
        // 2. Query untuk mengambil pengajuan cuti yang masih 'pending'
        String sql = "SELECT k.nama_lengkap, c.tanggal_mulai, c.status_approval "
                   + "FROM pengajuan_cuti c "
                   + "JOIN karyawan k ON c.karyawan_id = k.id "
                   + "WHERE c.status_approval = 'pending' "
                   + "ORDER BY c.id DESC LIMIT 10"; // Mengambil 10 notifikasi terbaru
                   
        java.sql.PreparedStatement pst = conn.prepareStatement(sql);
        java.sql.ResultSet rs = pst.executeQuery();
        
        // 3. Memasukkan data ke dalam tabel
        while (rs.next()) {
            String nama = rs.getString("nama_lengkap");
            String tgl = rs.getString("tanggal_mulai");
            String status = rs.getString("status_approval").toUpperCase(); 
            
            modelNotif.addRow(new Object[]{nama, tgl, status});
        }
        
    } catch (Exception e) {
        System.out.println("Error load notifikasi: " + e.getMessage());
    }
}

    private void loadTableKaryawan() {
    // 1. Mengatur kolom tabel
    DefaultTableModel model = new DefaultTableModel();
    model.addColumn("ID");
    model.addColumn("Nama Lengkap");
    model.addColumn("Username");
    model.addColumn("Role");
    model.addColumn("Sisa Cuti");
    
    tblKaryawan.setModel(model);
    
    try {
        java.sql.Connection conn = KoneksiDB.getKoneksi();
        // 2. Query mengambil semua data karyawan
        String sql = "SELECT id, nama_lengkap, username, role, sisa_cuti FROM karyawan ORDER BY id DESC";
        java.sql.PreparedStatement pst = conn.prepareStatement(sql);
        java.sql.ResultSet rs = pst.executeQuery();
        
        // 3. Masukkan data ke tabel
        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getString("id"),
                rs.getString("nama_lengkap"),
                rs.getString("username"),
                rs.getString("role"),
                rs.getString("sisa_cuti")
            });
        }
    } catch (Exception e) {
        System.out.println("Error load data karyawan: " + e.getMessage());
    }
}
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlSidebar = new javax.swing.JPanel();
        logo = new javax.swing.JLabel();
        btnDashboard = new javax.swing.JButton();
        btnDataKaryawan = new javax.swing.JButton();
        btnDataPresensi = new javax.swing.JButton();
        btnCutiIzin = new javax.swing.JButton();
        btnDaftarPekerjaan = new javax.swing.JButton();
        btnLaporan = new javax.swing.JButton();
        btnPengaturan = new javax.swing.JButton();
        btnLogout = new javax.swing.JButton();
        lblHrisProgram = new javax.swing.JLabel();
        lblNamaAdmin = new javax.swing.JLabel();
        pnlContent = new javax.swing.JPanel();
        pnlDashboard = new javax.swing.JPanel();
        txtDashboard = new javax.swing.JLabel();
        pnlTotalKaryawan = new javax.swing.JPanel();
        lblTotalKaryawan = new javax.swing.JLabel();
        txtTotalKaryawan = new javax.swing.JLabel();
        pnlHadirHariIni = new javax.swing.JPanel();
        txtHadir = new javax.swing.JLabel();
        lblHadir = new javax.swing.JLabel();
        pnlKaryawanCuti = new javax.swing.JPanel();
        txtCuti = new javax.swing.JLabel();
        lblCuti = new javax.swing.JLabel();
        pnlWfoWfh = new javax.swing.JPanel();
        txtWFOWFH = new javax.swing.JLabel();
        lblWfoWfh = new javax.swing.JLabel();
        scrPresensiHariIni = new javax.swing.JScrollPane();
        tblPresensiHariIni = new javax.swing.JTable();
        txtPresensiHariIni = new javax.swing.JLabel();
        txtDashboard2 = new javax.swing.JLabel();
        scrNotifCuti = new javax.swing.JScrollPane();
        tblNotifCuti = new javax.swing.JTable();
        pnlKaryawan = new javax.swing.JPanel();
        txtKaryawan = new javax.swing.JLabel();
        lblNamaLengkap = new javax.swing.JLabel();
        lblUsername = new javax.swing.JLabel();
        lblPassword = new javax.swing.JLabel();
        txtNama = new javax.swing.JTextField();
        txtUsername = new javax.swing.JTextField();
        txtPassword = new javax.swing.JTextField();
        btnSimpan = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        btnHapus = new javax.swing.JButton();
        btnReset = new javax.swing.JButton();
        lblAksi = new javax.swing.JLabel();
        scrKaryawan = new javax.swing.JScrollPane();
        tblKaryawan = new javax.swing.JTable();
        lblDataKaryawan = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        pnlPresensi = new javax.swing.JPanel();
        txtPresensi = new javax.swing.JLabel();
        lblCariNama = new javax.swing.JLabel();
        txtCariPresensi = new javax.swing.JTextField();
        btnCariPresensi = new javax.swing.JButton();
        btnRefreshPresensi = new javax.swing.JButton();
        scrDataPresensi = new javax.swing.JScrollPane();
        tblDataPresensi = new javax.swing.JTable();
        pnlCutiIzin = new javax.swing.JPanel();
        txtCutiIzin = new javax.swing.JLabel();
        btnApprove = new javax.swing.JButton();
        btnReject = new javax.swing.JButton();
        pnlDaftarCuti = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblCuti = new javax.swing.JTable();
        pnlAlasanCuti = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtAlasanCuti = new javax.swing.JTextArea();
        lblFotoBukti = new javax.swing.JLabel();
        btnCancel = new javax.swing.JButton();
        pnlDaftarPekerjaan = new javax.swing.JPanel();
        txtDaftarPekerjaan = new javax.swing.JLabel();
        txtCariDataKaryawan = new javax.swing.JLabel();
        txtCariPekerjaan = new javax.swing.JTextField();
        btnCariPekerjaan = new javax.swing.JButton();
        btnRefreshPekerjaan = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblPekerjaan = new javax.swing.JTable();
        pnlLaporan = new javax.swing.JPanel();
        txtLaporan = new javax.swing.JLabel();
        cbBulan = new javax.swing.JComboBox<>();
        cbTahun = new javax.swing.JComboBox<>();
        btnTampilLaporan = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        tblLaporan = new javax.swing.JTable();
        pnlPengaturan = new javax.swing.JPanel();
        txtPengaturan = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        pnlSidebar.setBackground(new java.awt.Color(51, 102, 255));

        logo.setIcon(new javax.swing.ImageIcon("C:\\Users\\HP\\Downloads\\WhatsApp Image 2026-08-02 at 15.27.00.jpeg")); // NOI18N

        btnDashboard.setText("🏠 Dashboard");
        btnDashboard.addActionListener(this::btnDashboardActionPerformed);

        btnDataKaryawan.setText("👥 Data Karyawan");
        btnDataKaryawan.addActionListener(this::btnDataKaryawanActionPerformed);

        btnDataPresensi.setText("⏱️ Data Presensi");
        btnDataPresensi.addActionListener(this::btnDataPresensiActionPerformed);

        btnCutiIzin.setText("🏖️ Approval Cuti & Izin");
        btnCutiIzin.addActionListener(this::btnCutiIzinActionPerformed);

        btnDaftarPekerjaan.setText("📋 Daftar Pekerjaan");
        btnDaftarPekerjaan.addActionListener(this::btnDaftarPekerjaanActionPerformed);

        btnLaporan.setText("📊 Laporan (Report)");
        btnLaporan.addActionListener(this::btnLaporanActionPerformed);

        btnPengaturan.setText("⚙️ Pengaturan");
        btnPengaturan.addActionListener(this::btnPengaturanActionPerformed);

        btnLogout.setText("🚪 Logout");
        btnLogout.addActionListener(this::btnLogoutActionPerformed);

        lblHrisProgram.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblHrisProgram.setText("HRIS Program");

        lblNamaAdmin.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblNamaAdmin.setText("Halo, ");

        pnlContent.setLayout(new java.awt.CardLayout());

        txtDashboard.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtDashboard.setText("Dashboard");

        pnlTotalKaryawan.setBackground(new java.awt.Color(255, 102, 102));
        pnlTotalKaryawan.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        lblTotalKaryawan.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        lblTotalKaryawan.setText("0");

        txtTotalKaryawan.setText("Total Karyawan");

        javax.swing.GroupLayout pnlTotalKaryawanLayout = new javax.swing.GroupLayout(pnlTotalKaryawan);
        pnlTotalKaryawan.setLayout(pnlTotalKaryawanLayout);
        pnlTotalKaryawanLayout.setHorizontalGroup(
            pnlTotalKaryawanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTotalKaryawanLayout.createSequentialGroup()
                .addGroup(pnlTotalKaryawanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlTotalKaryawanLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(txtTotalKaryawan))
                    .addGroup(pnlTotalKaryawanLayout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addComponent(lblTotalKaryawan)))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        pnlTotalKaryawanLayout.setVerticalGroup(
            pnlTotalKaryawanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTotalKaryawanLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(txtTotalKaryawan)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTotalKaryawan)
                .addGap(23, 23, 23))
        );

        pnlHadirHariIni.setBackground(new java.awt.Color(102, 255, 102));
        pnlHadirHariIni.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        pnlHadirHariIni.setPreferredSize(new java.awt.Dimension(91, 103));

        txtHadir.setText("Hadir Hari Ini");

        lblHadir.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        lblHadir.setText("0");

        javax.swing.GroupLayout pnlHadirHariIniLayout = new javax.swing.GroupLayout(pnlHadirHariIni);
        pnlHadirHariIni.setLayout(pnlHadirHariIniLayout);
        pnlHadirHariIniLayout.setHorizontalGroup(
            pnlHadirHariIniLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHadirHariIniLayout.createSequentialGroup()
                .addGroup(pnlHadirHariIniLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlHadirHariIniLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(txtHadir))
                    .addGroup(pnlHadirHariIniLayout.createSequentialGroup()
                        .addGap(41, 41, 41)
                        .addComponent(lblHadir)))
                .addContainerGap(32, Short.MAX_VALUE))
        );
        pnlHadirHariIniLayout.setVerticalGroup(
            pnlHadirHariIniLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHadirHariIniLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtHadir)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblHadir)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlKaryawanCuti.setBackground(new java.awt.Color(102, 255, 204));
        pnlKaryawanCuti.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        pnlKaryawanCuti.setPreferredSize(new java.awt.Dimension(91, 103));

        txtCuti.setText("Karyawan Cuti");

        lblCuti.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        lblCuti.setText("0");

        javax.swing.GroupLayout pnlKaryawanCutiLayout = new javax.swing.GroupLayout(pnlKaryawanCuti);
        pnlKaryawanCuti.setLayout(pnlKaryawanCutiLayout);
        pnlKaryawanCutiLayout.setHorizontalGroup(
            pnlKaryawanCutiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlKaryawanCutiLayout.createSequentialGroup()
                .addGroup(pnlKaryawanCutiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlKaryawanCutiLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(txtCuti))
                    .addGroup(pnlKaryawanCutiLayout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addComponent(lblCuti)))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        pnlKaryawanCutiLayout.setVerticalGroup(
            pnlKaryawanCutiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlKaryawanCutiLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtCuti)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblCuti)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlWfoWfh.setBackground(new java.awt.Color(255, 255, 102));
        pnlWfoWfh.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        pnlWfoWfh.setPreferredSize(new java.awt.Dimension(91, 103));

        txtWFOWFH.setText("WFO / WFH");

        lblWfoWfh.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        lblWfoWfh.setText("0");

        javax.swing.GroupLayout pnlWfoWfhLayout = new javax.swing.GroupLayout(pnlWfoWfh);
        pnlWfoWfh.setLayout(pnlWfoWfhLayout);
        pnlWfoWfhLayout.setHorizontalGroup(
            pnlWfoWfhLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlWfoWfhLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlWfoWfhLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtWFOWFH)
                    .addGroup(pnlWfoWfhLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(lblWfoWfh)))
                .addGap(36, 36, 36))
        );
        pnlWfoWfhLayout.setVerticalGroup(
            pnlWfoWfhLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlWfoWfhLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtWFOWFH)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblWfoWfh)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tblPresensiHariIni.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Nama Karyawan", "Jam Masuk", "Tipe Kerja"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        scrPresensiHariIni.setViewportView(tblPresensiHariIni);

        txtPresensiHariIni.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtPresensiHariIni.setText("Presensi Hari Ini");

        txtDashboard2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtDashboard2.setText("Notifikasi Pengajuan Cuti");

        tblNotifCuti.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Nama Karyawan", "Mulai Cuti", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        scrNotifCuti.setViewportView(tblNotifCuti);

        javax.swing.GroupLayout pnlDashboardLayout = new javax.swing.GroupLayout(pnlDashboard);
        pnlDashboard.setLayout(pnlDashboardLayout);
        pnlDashboardLayout.setHorizontalGroup(
            pnlDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDashboardLayout.createSequentialGroup()
                .addGroup(pnlDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlDashboardLayout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(pnlTotalKaryawan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(25, 25, 25)
                        .addComponent(pnlHadirHariIni, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(pnlKaryawanCuti, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(pnlWfoWfh, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlDashboardLayout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(txtDashboard))
                    .addGroup(pnlDashboardLayout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addGroup(pnlDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(scrPresensiHariIni, javax.swing.GroupLayout.PREFERRED_SIZE, 258, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtPresensiHariIni))
                        .addGap(14, 14, 14)
                        .addGroup(pnlDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtDashboard2)
                            .addComponent(scrNotifCuti, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        pnlDashboardLayout.setVerticalGroup(
            pnlDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDashboardLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(txtDashboard)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(pnlHadirHariIni, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE)
                        .addComponent(pnlTotalKaryawan, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(pnlWfoWfh, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)
                        .addComponent(pnlKaryawanCuti, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)))
                .addGap(22, 22, 22)
                .addGroup(pnlDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPresensiHariIni)
                    .addComponent(txtDashboard2))
                .addGap(4, 4, 4)
                .addGroup(pnlDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(scrNotifCuti)
                    .addComponent(scrPresensiHariIni))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlContent.add(pnlDashboard, "card2");

        txtKaryawan.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtKaryawan.setText("Kelola Data Karyawan");

        lblNamaLengkap.setText("Nama Lengkap");

        lblUsername.setText("Username");

        lblPassword.setText("Password");

        txtNama.addActionListener(this::txtNamaActionPerformed);

        btnSimpan.setText("Simpan");
        btnSimpan.addActionListener(this::btnSimpanActionPerformed);

        btnEdit.setText("Edit");
        btnEdit.addActionListener(this::btnEditActionPerformed);

        btnHapus.setText("Hapus");
        btnHapus.addActionListener(this::btnHapusActionPerformed);

        btnReset.setText("Reset");
        btnReset.addActionListener(this::btnResetActionPerformed);

        lblAksi.setText("Aksi");

        tblKaryawan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "ID", "Nama Lengkap", "Username", "Role", "Sisa Cuti"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblKaryawan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblKaryawanMouseClicked(evt);
            }
        });
        scrKaryawan.setViewportView(tblKaryawan);

        lblDataKaryawan.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblDataKaryawan.setText("Daftar Data Karyawan");

        javax.swing.GroupLayout pnlKaryawanLayout = new javax.swing.GroupLayout(pnlKaryawan);
        pnlKaryawan.setLayout(pnlKaryawanLayout);
        pnlKaryawanLayout.setHorizontalGroup(
            pnlKaryawanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlKaryawanLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(pnlKaryawanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 492, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDataKaryawan)
                    .addGroup(pnlKaryawanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(txtKaryawan)
                        .addGroup(pnlKaryawanLayout.createSequentialGroup()
                            .addGroup(pnlKaryawanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblNamaLengkap)
                                .addComponent(lblUsername)
                                .addComponent(lblPassword)
                                .addComponent(lblAksi))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(pnlKaryawanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtNama, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(pnlKaryawanLayout.createSequentialGroup()
                                    .addComponent(btnSimpan, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(12, 12, 12)
                                    .addComponent(btnHapus, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addComponent(scrKaryawan)))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        pnlKaryawanLayout.setVerticalGroup(
            pnlKaryawanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlKaryawanLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(txtKaryawan)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlKaryawanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNamaLengkap)
                    .addComponent(txtNama, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlKaryawanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblUsername)
                    .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlKaryawanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPassword)
                    .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlKaryawanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSimpan)
                    .addComponent(btnEdit)
                    .addComponent(btnHapus)
                    .addComponent(btnReset)
                    .addComponent(lblAksi))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(lblDataKaryawan)
                .addGap(9, 9, 9)
                .addComponent(scrKaryawan, javax.swing.GroupLayout.PREFERRED_SIZE, 353, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(66, Short.MAX_VALUE))
        );

        pnlContent.add(pnlKaryawan, "card3");

        txtPresensi.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtPresensi.setText("Riwayat Presensi Karyawan");

        lblCariNama.setText("Cari Nama");

        btnCariPresensi.setText("Cari");
        btnCariPresensi.addActionListener(this::btnCariPresensiActionPerformed);

        btnRefreshPresensi.setText("Refresh");
        btnRefreshPresensi.addActionListener(this::btnRefreshPresensiActionPerformed);

        tblDataPresensi.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        scrDataPresensi.setViewportView(tblDataPresensi);

        javax.swing.GroupLayout pnlPresensiLayout = new javax.swing.GroupLayout(pnlPresensi);
        pnlPresensi.setLayout(pnlPresensiLayout);
        pnlPresensiLayout.setHorizontalGroup(
            pnlPresensiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPresensiLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(pnlPresensiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(scrDataPresensi)
                    .addGroup(pnlPresensiLayout.createSequentialGroup()
                        .addComponent(lblCariNama)
                        .addGap(18, 18, 18)
                        .addComponent(txtCariPresensi, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCariPresensi)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRefreshPresensi))
                    .addComponent(txtPresensi))
                .addContainerGap(14, Short.MAX_VALUE))
        );
        pnlPresensiLayout.setVerticalGroup(
            pnlPresensiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPresensiLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(txtPresensi)
                .addGap(18, 18, 18)
                .addGroup(pnlPresensiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCariNama)
                    .addComponent(txtCariPresensi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCariPresensi)
                    .addComponent(btnRefreshPresensi))
                .addGap(18, 18, 18)
                .addComponent(scrDataPresensi, javax.swing.GroupLayout.PREFERRED_SIZE, 495, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(51, Short.MAX_VALUE))
        );

        pnlContent.add(pnlPresensi, "card4");

        txtCutiIzin.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtCutiIzin.setText("Daftar Pengajuan Cuti & Izin");

        btnApprove.setText("Setujui");
        btnApprove.addActionListener(this::btnApproveActionPerformed);

        btnReject.setText("Tolak");
        btnReject.addActionListener(this::btnRejectActionPerformed);

        pnlDaftarCuti.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        tblCuti.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Nama", "Tanggal Mulai", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblCuti.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblCutiMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblCuti);

        javax.swing.GroupLayout pnlDaftarCutiLayout = new javax.swing.GroupLayout(pnlDaftarCuti);
        pnlDaftarCuti.setLayout(pnlDaftarCutiLayout);
        pnlDaftarCutiLayout.setHorizontalGroup(
            pnlDaftarCutiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDaftarCutiLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 476, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlDaftarCutiLayout.setVerticalGroup(
            pnlDaftarCutiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDaftarCutiLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlAlasanCuti.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel1.setText("Alasan Cuti");

        txtAlasanCuti.setEditable(false);
        txtAlasanCuti.setColumns(20);
        txtAlasanCuti.setRows(5);
        jScrollPane2.setViewportView(txtAlasanCuti);

        lblFotoBukti.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        javax.swing.GroupLayout pnlAlasanCutiLayout = new javax.swing.GroupLayout(pnlAlasanCuti);
        pnlAlasanCuti.setLayout(pnlAlasanCutiLayout);
        pnlAlasanCutiLayout.setHorizontalGroup(
            pnlAlasanCutiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAlasanCutiLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(pnlAlasanCutiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(pnlAlasanCutiLayout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblFotoBukti, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlAlasanCutiLayout.setVerticalGroup(
            pnlAlasanCutiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAlasanCutiLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlAlasanCutiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblFotoBukti, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE))
                .addContainerGap())
        );

        btnCancel.setText("Batalkan");
        btnCancel.addActionListener(this::btnCancelActionPerformed);

        javax.swing.GroupLayout pnlCutiIzinLayout = new javax.swing.GroupLayout(pnlCutiIzin);
        pnlCutiIzin.setLayout(pnlCutiIzinLayout);
        pnlCutiIzinLayout.setHorizontalGroup(
            pnlCutiIzinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCutiIzinLayout.createSequentialGroup()
                .addGroup(pnlCutiIzinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlCutiIzinLayout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addGroup(pnlCutiIzinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtCutiIzin)
                            .addComponent(pnlDaftarCuti, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(pnlAlasanCuti, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlCutiIzinLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnApprove)
                        .addGap(18, 18, 18)
                        .addComponent(btnReject)
                        .addGap(18, 18, 18)
                        .addComponent(btnCancel)))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        pnlCutiIzinLayout.setVerticalGroup(
            pnlCutiIzinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCutiIzinLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(txtCutiIzin)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlDaftarCuti, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlAlasanCuti, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlCutiIzinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnApprove)
                    .addComponent(btnReject)
                    .addComponent(btnCancel))
                .addContainerGap(69, Short.MAX_VALUE))
        );

        pnlContent.add(pnlCutiIzin, "card5");

        txtDaftarPekerjaan.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtDaftarPekerjaan.setText("Pantauan Pekerjaan Harian Karyawan");

        txtCariDataKaryawan.setText("Cari Data Karyawan");

        btnCariPekerjaan.setText("Cari");
        btnCariPekerjaan.addActionListener(this::btnCariPekerjaanActionPerformed);

        btnRefreshPekerjaan.setText("Refresh");
        btnRefreshPekerjaan.addActionListener(this::btnRefreshPekerjaanActionPerformed);

        tblPekerjaan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane3.setViewportView(tblPekerjaan);

        javax.swing.GroupLayout pnlDaftarPekerjaanLayout = new javax.swing.GroupLayout(pnlDaftarPekerjaan);
        pnlDaftarPekerjaan.setLayout(pnlDaftarPekerjaanLayout);
        pnlDaftarPekerjaanLayout.setHorizontalGroup(
            pnlDaftarPekerjaanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDaftarPekerjaanLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(pnlDaftarPekerjaanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pnlDaftarPekerjaanLayout.createSequentialGroup()
                        .addComponent(txtCariDataKaryawan)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtCariPekerjaan, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCariPekerjaan)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRefreshPekerjaan))
                    .addComponent(txtDaftarPekerjaan)
                    .addComponent(jScrollPane3))
                .addContainerGap(24, Short.MAX_VALUE))
        );
        pnlDaftarPekerjaanLayout.setVerticalGroup(
            pnlDaftarPekerjaanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDaftarPekerjaanLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(txtDaftarPekerjaan)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDaftarPekerjaanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCariDataKaryawan)
                    .addComponent(txtCariPekerjaan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCariPekerjaan)
                    .addComponent(btnRefreshPekerjaan))
                .addGap(39, 39, 39)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(110, Short.MAX_VALUE))
        );

        pnlContent.add(pnlDaftarPekerjaan, "card6");

        txtLaporan.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtLaporan.setText("Rekapitulasi Kehadiran Bulanan");

        cbBulan.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Bulan", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" }));

        cbTahun.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Tahun", "2026", "2027", "2028", "2029", "2030", "2031", "2032", "2033", "2034", "2035", "2036", "2037", "2038", "2039", "2040", "2041", "2042", "2043", "2044", "2045" }));

        btnTampilLaporan.setText("Tampilkan Laporan");
        btnTampilLaporan.addActionListener(this::btnTampilLaporanActionPerformed);

        tblLaporan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane4.setViewportView(tblLaporan);

        javax.swing.GroupLayout pnlLaporanLayout = new javax.swing.GroupLayout(pnlLaporan);
        pnlLaporan.setLayout(pnlLaporanLayout);
        pnlLaporanLayout.setHorizontalGroup(
            pnlLaporanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlLaporanLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(pnlLaporanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlLaporanLayout.createSequentialGroup()
                        .addComponent(cbBulan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbTahun, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnTampilLaporan))
                    .addComponent(txtLaporan))
                .addContainerGap(65, Short.MAX_VALUE))
        );
        pnlLaporanLayout.setVerticalGroup(
            pnlLaporanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlLaporanLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(txtLaporan)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlLaporanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbBulan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbTahun, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnTampilLaporan))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(137, Short.MAX_VALUE))
        );

        pnlContent.add(pnlLaporan, "card7");

        txtPengaturan.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtPengaturan.setText("Pengaturan");

        javax.swing.GroupLayout pnlPengaturanLayout = new javax.swing.GroupLayout(pnlPengaturan);
        pnlPengaturan.setLayout(pnlPengaturanLayout);
        pnlPengaturanLayout.setHorizontalGroup(
            pnlPengaturanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPengaturanLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(txtPengaturan)
                .addContainerGap(440, Short.MAX_VALUE))
        );
        pnlPengaturanLayout.setVerticalGroup(
            pnlPengaturanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPengaturanLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(txtPengaturan)
                .addContainerGap(605, Short.MAX_VALUE))
        );

        pnlContent.add(pnlPengaturan, "card8");

        javax.swing.GroupLayout pnlSidebarLayout = new javax.swing.GroupLayout(pnlSidebar);
        pnlSidebar.setLayout(pnlSidebarLayout);
        pnlSidebarLayout.setHorizontalGroup(
            pnlSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSidebarLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnPengaturan, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLogout, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(logo)
                    .addComponent(btnDashboard, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDataKaryawan, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlSidebarLayout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(lblHrisProgram))
                    .addComponent(btnDataPresensi, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCutiIzin, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDaftarPekerjaan, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLaporan, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNamaAdmin))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlContent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlSidebarLayout.setVerticalGroup(
            pnlSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlSidebarLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnlContent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlSidebarLayout.createSequentialGroup()
                        .addComponent(logo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblHrisProgram)
                        .addGap(14, 14, 14)
                        .addComponent(btnDashboard)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnDataKaryawan)
                        .addGap(14, 14, 14)
                        .addComponent(btnDataPresensi)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnCutiIzin)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnDaftarPekerjaan)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnLaporan)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnPengaturan)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblNamaAdmin)
                        .addGap(18, 18, 18)
                        .addComponent(btnLogout, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        getContentPane().add(pnlSidebar, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnPengaturanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPengaturanActionPerformed
        pnlContent.removeAll();
        pnlContent.add(pnlPengaturan);
        pnlContent.repaint();
        pnlContent.revalidate();
    }//GEN-LAST:event_btnPengaturanActionPerformed

    private void btnDashboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDashboardActionPerformed
        pnlContent.removeAll();
        pnlContent.add(pnlDashboard);
        pnlContent.repaint();
        pnlContent.revalidate();
    }//GEN-LAST:event_btnDashboardActionPerformed

    private void btnDataKaryawanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDataKaryawanActionPerformed
        pnlContent.removeAll();
        pnlContent.add(pnlKaryawan);
        pnlContent.repaint();
        pnlContent.revalidate();
    }//GEN-LAST:event_btnDataKaryawanActionPerformed

    private void btnDataPresensiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDataPresensiActionPerformed
        pnlContent.removeAll();
        pnlContent.add(pnlPresensi);
        pnlContent.repaint();
        pnlContent.revalidate();
    }//GEN-LAST:event_btnDataPresensiActionPerformed

    private void btnCutiIzinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCutiIzinActionPerformed
        pnlContent.removeAll();
        pnlContent.add(pnlCutiIzin);
        pnlContent.repaint();
        pnlContent.revalidate();
    }//GEN-LAST:event_btnCutiIzinActionPerformed

    private void btnDaftarPekerjaanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDaftarPekerjaanActionPerformed
        pnlContent.removeAll();
        pnlContent.add(pnlDaftarPekerjaan);
        pnlContent.repaint();
        pnlContent.revalidate();
    }//GEN-LAST:event_btnDaftarPekerjaanActionPerformed

    private void btnLaporanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLaporanActionPerformed
        pnlContent.removeAll();
        pnlContent.add(pnlLaporan);
        pnlContent.repaint();
        pnlContent.revalidate();
    }//GEN-LAST:event_btnLaporanActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
            // Menampilkan pop-up konfirmasi (Ya / Tidak)
    int pilihan = JOptionPane.showConfirmDialog(this, 
            "Apakah Anda yakin ingin keluar dari sistem?", 
            "Konfirmasi Logout", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.QUESTION_MESSAGE);

    // Jika admin menekan tombol "Yes"
    if (pilihan == JOptionPane.YES_OPTION) {
        // 1. Buka kembali halaman Form Login
        LoginForm login = new LoginForm();
        login.setVisible(true);

        // 2. Tutup (hancurkan) halaman Dashboard ini
        this.dispose(); 
    }
    // Jika admin menekan "No", tidak akan terjadi apa-apa dan pop-up tertutup
    }//GEN-LAST:event_btnLogoutActionPerformed

    private void txtNamaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNamaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNamaActionPerformed

    private void btnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanActionPerformed
            try {
        // 1. Validasi form kosong
        if(txtNama.getText().equals("") || txtUsername.getText().equals("") || txtPassword.getText().equals("")) {
            javax.swing.JOptionPane.showMessageDialog(this, "Semua kolom (Nama, Username, Password) wajib diisi!");
            return; // Hentikan proses jika ada yang kosong
        }

        // 2. Query Insert ke Database
        String sql = "INSERT INTO karyawan (nama_lengkap, username, password, role, sisa_cuti) VALUES (?, ?, ?, 'karyawan', 12)";

        java.sql.Connection conn = KoneksiDB.getKoneksi();
        java.sql.PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, txtNama.getText());
        pst.setString(2, txtUsername.getText());
        pst.setString(3, txtPassword.getText());

        pst.execute(); // Eksekusi query

        // 3. Notifikasi sukses dan refresh/bersihkan layar
        javax.swing.JOptionPane.showMessageDialog(this, "Data Karyawan Baru Berhasil Disimpan!");

        // Refresh tabel agar data baru langsung muncul
        loadTableKaryawan();

        // Kosongkan form input
        txtNama.setText("");
        txtUsername.setText("");
        txtPassword.setText("");

    } catch (Exception e) {
        javax.swing.JOptionPane.showMessageDialog(this, "Gagal menyimpan data (Mungkin Username sudah terpakai):\n" + e.getMessage());
    }
    }//GEN-LAST:event_btnSimpanActionPerformed

    private void tblKaryawanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblKaryawanMouseClicked
            // Mengambil nomor baris yang diklik
    int baris = tblKaryawan.rowAtPoint(evt.getPoint());

    if (baris != -1) {
        // Mengambil data dari kolom tabel (Kolom 0 = ID, Kolom 1 = Nama, Kolom 2 = Username)
        idKaryawanTerpilih = tblKaryawan.getValueAt(baris, 0).toString();
        txtNama.setText(tblKaryawan.getValueAt(baris, 1).toString());
        txtUsername.setText(tblKaryawan.getValueAt(baris, 2).toString());

        // Kosongkan password karena bersifat rahasia (bisa diisi baru jika ingin mengubah password)
        txtPassword.setText(""); 
    }
    }//GEN-LAST:event_tblKaryawanMouseClicked

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed
    txtNama.setText("");
    txtUsername.setText("");
    txtPassword.setText("");
    idKaryawanTerpilih = ""; // Kembalikan ID menjadi kosong
    tblKaryawan.clearSelection(); // Hilangkan sorotan warna di tabel       
    }//GEN-LAST:event_btnResetActionPerformed

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        try {
    // Cegah jika belum ada data yang dipilih
    if (idKaryawanTerpilih.equals("")) {
        javax.swing.JOptionPane.showMessageDialog(this, "Silakan klik data di tabel terlebih dahulu!");
        return;
    }
    
    java.sql.Connection conn = KoneksiDB.getKoneksi();
    String sql = "";
    java.sql.PreparedStatement pst;
    
    // Cek apakah kolom password diisi atau dibiarkan kosong
    if (txtPassword.getText().equals("")) {
        // Jika kosong, update Nama dan Username saja
        sql = "UPDATE karyawan SET nama_lengkap = ?, username = ? WHERE id = ?";
        pst = conn.prepareStatement(sql);
        pst.setString(1, txtNama.getText());
        pst.setString(2, txtUsername.getText());
        pst.setString(3, idKaryawanTerpilih);
    } else {
        // Jika password diisi, update semuanya
        sql = "UPDATE karyawan SET nama_lengkap = ?, username = ?, password = ? WHERE id = ?";
        pst = conn.prepareStatement(sql);
        pst.setString(1, txtNama.getText());
        pst.setString(2, txtUsername.getText());
        pst.setString(3, txtPassword.getText());
        pst.setString(4, idKaryawanTerpilih);
    }
    
    pst.execute();
    javax.swing.JOptionPane.showMessageDialog(this, "Data berhasil diubah!");
    
    // Refresh tabel dan bersihkan form
    loadTableKaryawan();
    btnResetActionPerformed(evt); // Memanggil method reset
    
} catch (Exception e) {
    javax.swing.JOptionPane.showMessageDialog(this, "Perubahan gagal: " + e.getMessage());
}
    }//GEN-LAST:event_btnEditActionPerformed

    private void btnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusActionPerformed
        try {
    // Cegah jika belum ada data yang dipilih
    if (idKaryawanTerpilih.equals("")) {
        javax.swing.JOptionPane.showMessageDialog(this, "Silakan klik data di tabel terlebih dahulu!");
        return;
    }
    
    // Tampilkan pop-up konfirmasi
    int pilihan = javax.swing.JOptionPane.showConfirmDialog(this, 
            "Apakah Anda yakin ingin menghapus karyawan ini?", 
            "Konfirmasi Hapus", 
            javax.swing.JOptionPane.YES_NO_OPTION);
            
    if (pilihan == javax.swing.JOptionPane.YES_OPTION) {
        java.sql.Connection conn = KoneksiDB.getKoneksi();
        String sql = "DELETE FROM karyawan WHERE id = ?";
        java.sql.PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, idKaryawanTerpilih);
        pst.execute();
        
        javax.swing.JOptionPane.showMessageDialog(this, "Data berhasil dihapus!");
        
        // Refresh tabel dan bersihkan form
        loadTableKaryawan();
        btnResetActionPerformed(evt);
    }
} catch (Exception e) {
    javax.swing.JOptionPane.showMessageDialog(this, "Gagal menghapus data: " + e.getMessage());
}
    }//GEN-LAST:event_btnHapusActionPerformed

    private void btnCariPresensiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCariPresensiActionPerformed
        String cari = txtCariPresensi.getText();
        loadTableDataPresensi(cari);
    }//GEN-LAST:event_btnCariPresensiActionPerformed

    private void btnRefreshPresensiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshPresensiActionPerformed
        txtCariPresensi.setText(""); // Kosongkan kolom pencarian
        loadTableDataPresensi("");   // Panggil semua data lagi
    }//GEN-LAST:event_btnRefreshPresensiActionPerformed

    private void btnRejectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRejectActionPerformed
        try {
            if (idCutiTerpilih.equals("")) {
                javax.swing.JOptionPane.showMessageDialog(this, "Silakan pilih data cuti di tabel terlebih dahulu!");
                return;
            }

            java.sql.Connection conn = KoneksiDB.getKoneksi();

            String sqlReject = "UPDATE pengajuan_cuti SET status_approval = 'rejected' WHERE id = ?";
            java.sql.PreparedStatement pst = conn.prepareStatement(sqlReject);
            pst.setString(1, idCutiTerpilih);
            pst.execute();

            javax.swing.JOptionPane.showMessageDialog(this, "Pengajuan Cuti Ditolak.");

            loadTableCuti();
            loadNotifikasiCuti();
            idCutiTerpilih = "";

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Gagal menolak cuti: " + e.getMessage());
        }
    }//GEN-LAST:event_btnRejectActionPerformed

    private void btnApproveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnApproveActionPerformed
        try {
            if (idCutiTerpilih.equals("")) {
                javax.swing.JOptionPane.showMessageDialog(this, "Silakan pilih data cuti di tabel terlebih dahulu!");
                return;
            }

            java.sql.Connection conn = KoneksiDB.getKoneksi();

            // 1. Ubah status menjadi approved
            String sqlApprove = "UPDATE pengajuan_cuti SET status_approval = 'approved' WHERE id = ?";
            java.sql.PreparedStatement pst1 = conn.prepareStatement(sqlApprove);
            pst1.setString(1, idCutiTerpilih);
            pst1.execute();

            // 2. Potong jatah cuti karyawan menggunakan DATEDIFF (Selisih Hari)
            // Rumus: sisa_cuti = sisa_cuti - (Jumlah Hari Cuti)
            String sqlPotongKuota = "UPDATE karyawan SET sisa_cuti = sisa_cuti - "
            + "(SELECT DATEDIFF(tanggal_selesai, tanggal_mulai) + 1 "
            + "FROM pengajuan_cuti WHERE id = ?) "
            + "WHERE id = (SELECT karyawan_id FROM pengajuan_cuti WHERE id = ?)";

            java.sql.PreparedStatement pst2 = conn.prepareStatement(sqlPotongKuota);
            pst2.setString(1, idCutiTerpilih);
            pst2.setString(2, idCutiTerpilih);
            pst2.execute();

            javax.swing.JOptionPane.showMessageDialog(this, "Cuti Disetujui! Kuota cuti karyawan telah dipotong.");

            // Refresh tabel cuti dan tabel notifikasi di Dashboard
            loadTableCuti();
            loadNotifikasiCuti(); // Agar tabel kecil di halaman depan juga terupdate
            idCutiTerpilih = ""; // Reset ID

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Gagal menyetujui cuti: " + e.getMessage());
        }
    }//GEN-LAST:event_btnApproveActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnCancelActionPerformed

    private void tblCutiMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblCutiMouseClicked
        int baris = tblCuti.rowAtPoint(evt.getPoint());

if (baris != -1) {
    // 1. Tangkap ID Cuti dari kolom pertama (kolom 0) tabel
    idCutiTerpilih = tblCuti.getValueAt(baris, 0).toString();
    
    try {
        java.sql.Connection conn = KoneksiDB.getKoneksi();
        // 2. Ambil alasan dan nama file foto dari database berdasarkan ID
        String sql = "SELECT alasan, bukti_foto FROM pengajuan_cuti WHERE id = ?";
        java.sql.PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, idCutiTerpilih);
        java.sql.ResultSet rs = pst.executeQuery();
        
        if (rs.next()) {
            // 3. Tampilkan Teks Alasan
            txtAlasanCuti.setText(rs.getString("alasan"));
            
            // 4. Proses Menampilkan Foto
            String namaFileFoto = rs.getString("bukti_foto");
            
            // Mengecek apakah karyawan melampirkan foto atau tidak
            if (namaFileFoto != null && !namaFileFoto.isEmpty()) {
                // Lokasi folder disesuaikan dengan direktori server yang digunakan
                String lokasiFolder = "C:/laragon/www/hris_images/";
                String pathFotoLengkap = lokasiFolder + namaFileFoto;
                
                javax.swing.ImageIcon ikonAsli = new javax.swing.ImageIcon(pathFotoLengkap);
                
                // Menyesuaikan ukuran gambar dengan bingkai
                int lebar = lblFotoBukti.getWidth();
                int tinggi = lblFotoBukti.getHeight();
                java.awt.Image gambarKecil = ikonAsli.getImage().getScaledInstance(lebar, tinggi, java.awt.Image.SCALE_SMOOTH);
                
                lblFotoBukti.setIcon(new javax.swing.ImageIcon(gambarKecil));
                lblFotoBukti.setText(""); // Hilangkan teks bantuan
            } else {
                // Jika tidak ada foto di database
                lblFotoBukti.setIcon(null);
                lblFotoBukti.setText("Tidak ada lampiran foto");
            }
        }
    } catch (Exception e) {
        System.out.println("Error load detail cuti: " + e.getMessage());
    }
}
    }//GEN-LAST:event_tblCutiMouseClicked

    private void btnCariPekerjaanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCariPekerjaanActionPerformed
        String cari = txtCariPekerjaan.getText();
        loadTablePekerjaan(cari);
    }//GEN-LAST:event_btnCariPekerjaanActionPerformed

    private void btnRefreshPekerjaanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshPekerjaanActionPerformed
        txtCariPekerjaan.setText(""); // Bersihkan kotak pencarian
        loadTablePekerjaan(""); // Panggil ulang semua data
    }//GEN-LAST:event_btnRefreshPekerjaanActionPerformed

    private void btnTampilLaporanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTampilLaporanActionPerformed
        // Mengambil nilai bulan dan tahun dari Dropdown (Combobox)
        String bulanDipilih = cbBulan.getSelectedItem().toString();
        String tahunDipilih = cbTahun.getSelectedItem().toString();

        // Memanggil metode untuk memuat tabel laporan
        loadTableLaporan(bulanDipilih, tahunDipilih);
    }//GEN-LAST:event_btnTampilLaporanActionPerformed
    
    // Parameter kataKunci digunakan untuk fitur pencarian
private void loadTablePekerjaan(String kataKunci) {
    javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel();
    model.addColumn("Tanggal");
    model.addColumn("Nama Karyawan");
    model.addColumn("Deskripsi Tugas / Pekerjaan");
    model.addColumn("Status Pekerjaan");
    
    tblPekerjaan.setModel(model);
    
    try {
        java.sql.Connection conn = KoneksiDB.getKoneksi();
        String sql;
        java.sql.PreparedStatement pst;
        
        // Cek apakah kolom pencarian kosong atau ada isinya
        if (kataKunci.isEmpty()) {
            // Tampilkan semua data, urutkan dari tanggal terbaru
            sql = "SELECT t.tanggal, k.nama_lengkap, t.deskripsi_tugas, t.status "
                + "FROM tugas_pekerjaan t "
                + "JOIN karyawan k ON t.karyawan_id = k.id "
                + "ORDER BY t.tanggal DESC, k.nama_lengkap ASC";
            pst = conn.prepareStatement(sql);
        } else {
            // Tampilkan data berdasarkan nama karyawan yang dicari
            sql = "SELECT t.tanggal, k.nama_lengkap, t.deskripsi_tugas, t.status "
                + "FROM tugas_pekerjaan t "
                + "JOIN karyawan k ON t.karyawan_id = k.id "
                + "WHERE k.nama_lengkap LIKE ? "
                + "ORDER BY t.tanggal DESC";
            pst = conn.prepareStatement(sql);
            pst.setString(1, "%" + kataKunci + "%");
        }
        
        java.sql.ResultSet rs = pst.executeQuery();
        
        while (rs.next()) {
            // Mengubah format status database (todo/in_progress/done) menjadi teks yang lebih rapi
            String statusDb = rs.getString("status");
            String statusRapi = "";
            
            switch (statusDb) {
                case "todo": statusRapi = "Belum Dikerjakan"; break;
                case "in_progress": statusRapi = "Sedang Dikerjakan"; break;
                case "done": statusRapi = "Selesai"; break;
                default: statusRapi = statusDb;
            }
            
            model.addRow(new Object[]{
                rs.getString("tanggal"),
                rs.getString("nama_lengkap"),
                rs.getString("deskripsi_tugas"),
                statusRapi
            });
        }
    } catch (Exception e) {
        System.out.println("Error load data pekerjaan: " + e.getMessage());
    }
}

private void loadTableLaporan(String bulan, String tahun) {
    javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel();
    model.addColumn("Nama Karyawan");
    model.addColumn("Total WFO");
    model.addColumn("Total WFH");
    
    tblLaporan.setModel(model);
    
    try {
        java.sql.Connection conn = KoneksiDB.getKoneksi();
        
        // Query ini akan menghitung total WFO dan WFH per karyawan berdasarkan bulan & tahun yang dipilih
        String sql = "SELECT k.nama_lengkap, "
                   + "SUM(CASE WHEN p.tipe_kerja = 'wfo' THEN 1 ELSE 0 END) AS total_wfo, "
                   + "SUM(CASE WHEN p.tipe_kerja = 'wfh' THEN 1 ELSE 0 END) AS total_wfh "
                   + "FROM karyawan k "
                   + "LEFT JOIN presensi p ON k.id = p.karyawan_id AND MONTH(p.tanggal) = ? AND YEAR(p.tanggal) = ? "
                   + "WHERE k.role = 'karyawan' "
                   + "GROUP BY k.id "
                   + "ORDER BY k.nama_lengkap ASC";
                   
        java.sql.PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, bulan);
        pst.setString(2, tahun);
        
        java.sql.ResultSet rs = pst.executeQuery();
        
        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getString("nama_lengkap"),
                rs.getString("total_wfo") + " Hari",
                rs.getString("total_wfh") + " Hari"
            });
        }
    } catch (Exception e) {
        System.out.println("Error load laporan: " + e.getMessage());
    }
}
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new DashboardAdmin("Faqih (Testing)").setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnApprove;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnCariPekerjaan;
    private javax.swing.JButton btnCariPresensi;
    private javax.swing.JButton btnCutiIzin;
    private javax.swing.JButton btnDaftarPekerjaan;
    private javax.swing.JButton btnDashboard;
    private javax.swing.JButton btnDataKaryawan;
    private javax.swing.JButton btnDataPresensi;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnHapus;
    private javax.swing.JButton btnLaporan;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnPengaturan;
    private javax.swing.JButton btnRefreshPekerjaan;
    private javax.swing.JButton btnRefreshPresensi;
    private javax.swing.JButton btnReject;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSimpan;
    private javax.swing.JButton btnTampilLaporan;
    private javax.swing.JComboBox<String> cbBulan;
    private javax.swing.JComboBox<String> cbTahun;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblAksi;
    private javax.swing.JLabel lblCariNama;
    private javax.swing.JLabel lblCuti;
    private javax.swing.JLabel lblDataKaryawan;
    private javax.swing.JLabel lblFotoBukti;
    private javax.swing.JLabel lblHadir;
    private javax.swing.JLabel lblHrisProgram;
    private javax.swing.JLabel lblNamaAdmin;
    private javax.swing.JLabel lblNamaLengkap;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblTotalKaryawan;
    private javax.swing.JLabel lblUsername;
    private javax.swing.JLabel lblWfoWfh;
    private javax.swing.JLabel logo;
    private javax.swing.JPanel pnlAlasanCuti;
    private javax.swing.JPanel pnlContent;
    private javax.swing.JPanel pnlCutiIzin;
    private javax.swing.JPanel pnlDaftarCuti;
    private javax.swing.JPanel pnlDaftarPekerjaan;
    private javax.swing.JPanel pnlDashboard;
    private javax.swing.JPanel pnlHadirHariIni;
    private javax.swing.JPanel pnlKaryawan;
    private javax.swing.JPanel pnlKaryawanCuti;
    private javax.swing.JPanel pnlLaporan;
    private javax.swing.JPanel pnlPengaturan;
    private javax.swing.JPanel pnlPresensi;
    private javax.swing.JPanel pnlSidebar;
    private javax.swing.JPanel pnlTotalKaryawan;
    private javax.swing.JPanel pnlWfoWfh;
    private javax.swing.JScrollPane scrDataPresensi;
    private javax.swing.JScrollPane scrKaryawan;
    private javax.swing.JScrollPane scrNotifCuti;
    private javax.swing.JScrollPane scrPresensiHariIni;
    private javax.swing.JTable tblCuti;
    private javax.swing.JTable tblDataPresensi;
    private javax.swing.JTable tblKaryawan;
    private javax.swing.JTable tblLaporan;
    private javax.swing.JTable tblNotifCuti;
    private javax.swing.JTable tblPekerjaan;
    private javax.swing.JTable tblPresensiHariIni;
    private javax.swing.JTextArea txtAlasanCuti;
    private javax.swing.JLabel txtCariDataKaryawan;
    private javax.swing.JTextField txtCariPekerjaan;
    private javax.swing.JTextField txtCariPresensi;
    private javax.swing.JLabel txtCuti;
    private javax.swing.JLabel txtCutiIzin;
    private javax.swing.JLabel txtDaftarPekerjaan;
    private javax.swing.JLabel txtDashboard;
    private javax.swing.JLabel txtDashboard2;
    private javax.swing.JLabel txtHadir;
    private javax.swing.JLabel txtKaryawan;
    private javax.swing.JLabel txtLaporan;
    private javax.swing.JTextField txtNama;
    private javax.swing.JTextField txtPassword;
    private javax.swing.JLabel txtPengaturan;
    private javax.swing.JLabel txtPresensi;
    private javax.swing.JLabel txtPresensiHariIni;
    private javax.swing.JLabel txtTotalKaryawan;
    private javax.swing.JTextField txtUsername;
    private javax.swing.JLabel txtWFOWFH;
    // End of variables declaration//GEN-END:variables
}
