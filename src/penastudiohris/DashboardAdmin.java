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
    
    }
    private String idKaryawanTerpilih = "";
    
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
        pnlPengaturan = new javax.swing.JPanel();
        txtPengaturan = new javax.swing.JLabel();
        pnlLaporan = new javax.swing.JPanel();
        txtLaporan = new javax.swing.JLabel();
        pnlDaftarPekerjaan = new javax.swing.JPanel();
        txtDaftarPekerjaan = new javax.swing.JLabel();
        pnlCutiIzin = new javax.swing.JPanel();
        txtCutiIzin = new javax.swing.JLabel();
        pnlPresensi = new javax.swing.JPanel();
        txtPresensi = new javax.swing.JLabel();
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
        jScrollPane1 = new javax.swing.JScrollPane();
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
                .addContainerGap(578, Short.MAX_VALUE))
        );

        pnlContent.add(pnlPengaturan, "card8");

        txtLaporan.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtLaporan.setText("Laporan (Report)");

        javax.swing.GroupLayout pnlLaporanLayout = new javax.swing.GroupLayout(pnlLaporan);
        pnlLaporan.setLayout(pnlLaporanLayout);
        pnlLaporanLayout.setHorizontalGroup(
            pnlLaporanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlLaporanLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(txtLaporan)
                .addContainerGap(402, Short.MAX_VALUE))
        );
        pnlLaporanLayout.setVerticalGroup(
            pnlLaporanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlLaporanLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(txtLaporan)
                .addContainerGap(578, Short.MAX_VALUE))
        );

        pnlContent.add(pnlLaporan, "card7");

        txtDaftarPekerjaan.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtDaftarPekerjaan.setText("Daftar Pekerjaan");

        javax.swing.GroupLayout pnlDaftarPekerjaanLayout = new javax.swing.GroupLayout(pnlDaftarPekerjaan);
        pnlDaftarPekerjaan.setLayout(pnlDaftarPekerjaanLayout);
        pnlDaftarPekerjaanLayout.setHorizontalGroup(
            pnlDaftarPekerjaanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDaftarPekerjaanLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(txtDaftarPekerjaan)
                .addContainerGap(404, Short.MAX_VALUE))
        );
        pnlDaftarPekerjaanLayout.setVerticalGroup(
            pnlDaftarPekerjaanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDaftarPekerjaanLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(txtDaftarPekerjaan)
                .addContainerGap(578, Short.MAX_VALUE))
        );

        pnlContent.add(pnlDaftarPekerjaan, "card6");

        txtCutiIzin.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtCutiIzin.setText("Approval Cuti & Izin");

        javax.swing.GroupLayout pnlCutiIzinLayout = new javax.swing.GroupLayout(pnlCutiIzin);
        pnlCutiIzin.setLayout(pnlCutiIzinLayout);
        pnlCutiIzinLayout.setHorizontalGroup(
            pnlCutiIzinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCutiIzinLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(txtCutiIzin)
                .addContainerGap(381, Short.MAX_VALUE))
        );
        pnlCutiIzinLayout.setVerticalGroup(
            pnlCutiIzinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCutiIzinLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(txtCutiIzin)
                .addContainerGap(578, Short.MAX_VALUE))
        );

        pnlContent.add(pnlCutiIzin, "card5");

        txtPresensi.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtPresensi.setText("Data Presensi");

        javax.swing.GroupLayout pnlPresensiLayout = new javax.swing.GroupLayout(pnlPresensi);
        pnlPresensi.setLayout(pnlPresensiLayout);
        pnlPresensiLayout.setHorizontalGroup(
            pnlPresensiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPresensiLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(txtPresensi)
                .addContainerGap(427, Short.MAX_VALUE))
        );
        pnlPresensiLayout.setVerticalGroup(
            pnlPresensiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPresensiLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(txtPresensi)
                .addContainerGap(578, Short.MAX_VALUE))
        );

        pnlContent.add(pnlPresensi, "card4");

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
                .addContainerGap()
                .addComponent(txtTotalKaryawan))
            .addGroup(pnlTotalKaryawanLayout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addComponent(lblTotalKaryawan))
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
                .addContainerGap()
                .addComponent(txtHadir)
                .addContainerGap(11, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlHadirHariIniLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblHadir)
                .addGap(31, 31, 31))
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                    .addComponent(lblWfoWfh)
                    .addComponent(txtWFOWFH))
                .addGap(28, 28, 28))
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
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(tblPresensiHariIni);

        txtPresensiHariIni.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtPresensiHariIni.setText("Presensi Hari Ini");

        txtDashboard2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtDashboard2.setText("Notifikasi Pengajuan Cuti");

        tblNotifCuti.setModel(new javax.swing.table.DefaultTableModel(
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
        scrNotifCuti.setViewportView(tblNotifCuti);

        javax.swing.GroupLayout pnlDashboardLayout = new javax.swing.GroupLayout(pnlDashboard);
        pnlDashboard.setLayout(pnlDashboardLayout);
        pnlDashboardLayout.setHorizontalGroup(
            pnlDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDashboardLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(pnlTotalKaryawan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pnlHadirHariIni, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(39, 39, 39)
                .addComponent(pnlKaryawanCuti, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addComponent(pnlWfoWfh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(pnlDashboardLayout.createSequentialGroup()
                .addGroup(pnlDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlDashboardLayout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(txtDashboard))
                    .addGroup(pnlDashboardLayout.createSequentialGroup()
                        .addGroup(pnlDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlDashboardLayout.createSequentialGroup()
                                .addGap(25, 25, 25)
                                .addComponent(txtPresensiHariIni)
                                .addGap(153, 153, 153))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDashboardLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 258, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(14, 14, 14)))
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
                    .addComponent(jScrollPane1))
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
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblKaryawan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblKaryawanMouseClicked(evt);
            }
        });
        scrKaryawan.setViewportView(tblKaryawan);

        javax.swing.GroupLayout pnlKaryawanLayout = new javax.swing.GroupLayout(pnlKaryawan);
        pnlKaryawan.setLayout(pnlKaryawanLayout);
        pnlKaryawanLayout.setHorizontalGroup(
            pnlKaryawanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlKaryawanLayout.createSequentialGroup()
                .addGap(25, 25, 25)
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
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnHapus, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(scrKaryawan))
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
                .addGap(18, 18, 18)
                .addComponent(scrKaryawan, javax.swing.GroupLayout.PREFERRED_SIZE, 378, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(39, Short.MAX_VALUE))
        );

        pnlContent.add(pnlKaryawan, "card3");

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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSimpan;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblAksi;
    private javax.swing.JLabel lblCuti;
    private javax.swing.JLabel lblHadir;
    private javax.swing.JLabel lblHrisProgram;
    private javax.swing.JLabel lblNamaAdmin;
    private javax.swing.JLabel lblNamaLengkap;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblTotalKaryawan;
    private javax.swing.JLabel lblUsername;
    private javax.swing.JLabel lblWfoWfh;
    private javax.swing.JLabel logo;
    private javax.swing.JPanel pnlContent;
    private javax.swing.JPanel pnlCutiIzin;
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
    private javax.swing.JScrollPane scrKaryawan;
    private javax.swing.JScrollPane scrNotifCuti;
    private javax.swing.JTable tblKaryawan;
    private javax.swing.JTable tblNotifCuti;
    private javax.swing.JTable tblPresensiHariIni;
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
