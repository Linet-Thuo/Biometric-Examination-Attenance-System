import com.digitalpersona.onetouch.DPFPDataPurpose;import com.digitalpersona.onetouch.DPFPFeatureSet;
import com.digitalpersona.onetouch.DPFPGlobal;import com.digitalpersona.onetouch.DPFPSample;
import com.digitalpersona.onetouch.DPFPTemplate;import com.digitalpersona.onetouch.capture.DPFPCapture;
import com.digitalpersona.onetouch.capture.event.DPFPDataAdapter;import com.digitalpersona.onetouch.capture.event.DPFPDataEvent;
import com.digitalpersona.onetouch.capture.event.DPFPErrorAdapter;import com.digitalpersona.onetouch.capture.event.DPFPErrorEvent;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusAdapter;import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusEvent;
import com.digitalpersona.onetouch.capture.event.DPFPSensorAdapter;import com.digitalpersona.onetouch.capture.event.DPFPSensorEvent;
import com.digitalpersona.onetouch.processing.DPFPEnrollment;import com.digitalpersona.onetouch.processing.DPFPFeatureExtraction;
import com.digitalpersona.onetouch.processing.DPFPImageQualityException;import com.digitalpersona.onetouch.verification.DPFPVerification;
import com.digitalpersona.onetouch.verification.DPFPVerificationResult;import java.awt.HeadlessException;import java.awt.Image;import java.awt.event.ActionEvent;
import java.io.IOException;import java.sql.Connection;import java.sql.DriverManager;import java.sql.PreparedStatement;
import java.sql.ResultSet;import java.sql.SQLException;import java.sql.Statement;import java.text.SimpleDateFormat;
import java.util.ArrayList;import java.util.Date;import java.util.logging.Level;import java.util.logging.Logger;
import javax.swing.ImageIcon;import javax.swing.JOptionPane;import javax.swing.SwingUtilities;
import javax.swing.Timer;import javax.swing.UIManager;import javax.swing.table.DefaultTableModel;

public class attendance extends javax.swing.JFrame {String JDBC_Driver="com.mysql.jdbc.Driver";    
    String dburl ="jdbc:mysql://localhost:3306/attendance?zeroDateTimeBehavior=convertToNull";
    Connection connects=null;    ResultSet rs=null;    Statement stnt=null; PreparedStatement stnt1=null;

    public attendance() {try{UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}catch(Exception e){
    JOptionPane.showMessageDialog(null,"Impossible Modification", "Invalid LookandFeel.", JOptionPane.ERROR_MESSAGE);}
        initComponents();date();time();
    }

public void date(){Date dat = new Date();SimpleDateFormat a=new SimpleDateFormat("yyyy-MM-dd");jLabel13.setText(a.format(dat));}
public void time(){new Timer(0, (ActionEvent ae) -> {Date d = new Date();SimpleDateFormat s=new SimpleDateFormat("hh:mm:ss");jLabel15.setText(s.format(d));}).start();}

public void table(){if (jTextField1.getText().length()==0){JOptionPane.showMessageDialog(null, "Please enter Lecturer ID.");}
    else if (jTextField2.getText().length()==0){JOptionPane.showMessageDialog(null, "Please enter Password.");}
    else {String unitID=jTextField1.getText(); String dates=jTextField2.getText();
    try{Class.forName("com.mysql.jdbc.Driver"); connects = DriverManager.getConnection(dburl, "root", "");
    stnt=connects.createStatement();rs=stnt.executeQuery("SELECT * FROM indunit WHERE unitid='"+unitID+"' AND date='"+dates+"'");
    while (rs.next()){ArrayList<User3> list=new ArrayList<>();User3 user;
    user= new User3(rs.getString("studid"),rs.getString("name"),rs.getTime("timein"),rs.getTime("timeout"),rs.getInt("bookno"));
    list.add(user);DefaultTableModel model= (DefaultTableModel) jTable2.getModel();
    Object [] row = new Object [5];for (int i=0; i<list.size();i++){row[0]=list.get(i).getstudid();row[1]=list.get(i).getname();
    row[2]=list.get(i).gettimein();row[3]=list.get(i).gettimeout();row[4]=list.get(i).getbookno();model.addRow(row);}}} 
    catch (ClassNotFoundException | SQLException | HeadlessException e){JOptionPane.showMessageDialog(null, e.getMessage());
    try {connects.close();} catch (SQLException ex) {Logger.getLogger(attendance.class.getName()).log(Level.SEVERE, null, ex);}}}}
   
    private DPFPCapture capturer=DPFPGlobal.getCaptureFactory().createCapture();
    private DPFPEnrollment enroller=DPFPGlobal.getEnrollmentFactory().createEnrollment();
    private DPFPVerification verify=DPFPGlobal.getVerificationFactory().createVerification();
    private DPFPTemplate template;private static String TEMPLATE_PROPERTY="template";
    
    protected void init(){ capturer.addDataListener(new DPFPDataAdapter() {
    @Override public void dataAcquired(final DPFPDataEvent e) {	SwingUtilities.invokeLater(new Runnable() {@Override
    public void run() {makeReport("The fingerprint sample was captured.");ProcessCapture(e.getSample());}});}});
    capturer.addReaderStatusListener(new DPFPReaderStatusAdapter() {public void readerConnected(final DPFPReaderStatusEvent e) {
    SwingUtilities.invokeLater(new Runnable() {	public void run() {makeReport("The fingerprint reader was connected.");}});}
    public void readerDisconnected(final DPFPReaderStatusEvent e) {SwingUtilities.invokeLater(new Runnable() {	public void run() {
    makeReport("The fingerprint reader was disconnected.");}});}});capturer.addSensorListener(new DPFPSensorAdapter() {
    @Override public void fingerTouched(final DPFPSensorEvent e) {SwingUtilities.invokeLater(new Runnable() {public void run() {
    makeReport("The fingerprint reader was touched.");}});}
    @Override public void fingerGone(final DPFPSensorEvent e) {SwingUtilities.invokeLater(new Runnable() {public void run() {
    makeReport("The finger was removed from the fingerprint reader.");}});}});	
    capturer.addErrorListener(new DPFPErrorAdapter(){public void errorReader(final DPFPErrorEvent e){
    SwingUtilities.invokeLater(new Runnable() {@Override public void run(){makeReport("Error:"+e.getError());}});}});}
    public DPFPFeatureSet  features;public DPFPFeatureSet   featuresverification;
    public DPFPFeatureSet extractFeatures(DPFPSample sample, DPFPDataPurpose purpose){
    DPFPFeatureExtraction extractor = DPFPGlobal.getFeatureExtractionFactory().createFeatureExtraction();
    try {return extractor.createFeatureSet(sample, purpose);} catch (DPFPImageQualityException e) {return null;}}
    
    public Image convertSampleToBitmap(DPFPSample sample) {return DPFPGlobal.getSampleConversionFactory().createImage(sample);}
    public void drawPicture(Image image) {jLabel7.setIcon(new ImageIcon(image.getScaledInstance(
    jLabel7.getWidth(), jLabel7.getHeight(), Image.SCALE_DEFAULT))); repaint(); }
    
    public void extractFinger(){makeReport("Displaying fingerprint."+enroller.getFeaturesNeeded());}
    
    public void makeReport(String string){System.out.append(string+"\n");}
    public void start(){capturer.startCapture();makeReport("Using the fingerprint reader, scan your fingerprint.");}

    public void stop(){capturer.stopCapture();makeReport("Done.");}
    
    public DPFPTemplate getTemplate() {return template;}
    
    public void setTemplate(DPFPTemplate template) {DPFPTemplate old = this.template;
    this.template = template;firePropertyChange(TEMPLATE_PROPERTY, old, template);}
    
    public void ProcessCapture(DPFPSample sample){features=extractFeatures(sample, DPFPDataPurpose.DATA_PURPOSE_ENROLLMENT);
    featuresverification=extractFeatures(sample, DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);
    if(features!=null)try{System.out.println("The fingerprint feature set was created.");
    enroller.addFeatures(features);Image image=convertSampleToBitmap(sample);drawPicture(image);
    jButton2.setEnabled(true);jButton3.setEnabled(true);jButton4.setEnabled(true);}            
    catch (DPFPImageQualityException ex) {System.err.println("Error:"+ex.getMessage());}
    finally{switch(enroller.getTemplateStatus()){
    case TEMPLATE_STATUS_READY:stop();setTemplate(enroller.getTemplate());jButton2.setEnabled(false);
    jButton3.setEnabled(false);jButton4.setEnabled(false);jButton4.grabFocus();break;
    case TEMPLATE_STATUS_FAILED:enroller.clear();stop();setTemplate(null);
    JOptionPane.showMessageDialog(attendance.this, "The fingerprint template is not valid. Repeat fingerprint enrollment.", 
    "Fingerprint Enrollment", JOptionPane.ERROR_MESSAGE);start();break;}}}
    
    public void identifyFinger()throws IOException, ClassNotFoundException{
    try{Class.forName("com.mysql.jdbc.Driver");Connection con1= DriverManager.getConnection(dburl, "root", "");
    PreparedStatement stmt= (PreparedStatement) con1.prepareStatement("SELECT studentid, name, print FROM finger");
    ResultSet rs1 = stmt.executeQuery();while(rs1.next()){String studid=rs1.getString("studentid"),name=rs1.getString("name");
    byte[] templateBuffer=rs1.getBytes("print");DPFPTemplate referenceTemplate =DPFPGlobal.getTemplateFactory().createTemplate(templateBuffer);
    setTemplate(referenceTemplate);DPFPVerificationResult result=verify.verify(featuresverification, getTemplate());
    if(result.isVerified()){ jLabel2.setText(studid);jLabel4.setText(name);rs1.close();stmt.close();con1.close();return;}}
    JOptionPane.showMessageDialog(null, "No existing registration of the Finger",
    "Searching Fingerprint", JOptionPane.ERROR_MESSAGE); setTemplate(null);}
    catch( SQLException e){System.err.println("Error in Searching FingerPrint"+e.getMessage());}}

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jTextField2 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jButton5 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jButton6 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("STUDENT EXAMINATION ATTENDANCE SYSTEM ");
        setLocation(new java.awt.Point(200, 200));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(204, 51, 0));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "ATTENDANCE", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Times New Roman", 1, 18))); // NOI18N

        jLabel5.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("Unit ID:");

        jLabel6.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("Date:");

        jButton1.setBackground(new java.awt.Color(255, 255, 255));
        jButton1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton1.setText("SEARCH");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jTextField1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
                .addContainerGap())
        );

        jButton2.setBackground(new java.awt.Color(255, 255, 255));
        jButton2.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton2.setText("SEARCH");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel3.setText("Student ID:");

        jLabel8.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel8.setText("Student Name:");

        jButton3.setBackground(new java.awt.Color(255, 255, 255));
        jButton3.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton3.setText("SIGN IN");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setBackground(new java.awt.Color(255, 255, 255));
        jButton4.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton4.setText("SIGN OUT");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jTextField2.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel1.setText("BookLet No:");

        jTextField3.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N

        jButton5.setBackground(new java.awt.Color(255, 255, 255));
        jButton5.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton5.setText("BACK");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jTable2.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Student ID", "Student Name", "Time IN", "Time OUT", "Booklet No."
            }
        ));
        jScrollPane1.setViewportView(jTable2);

        jButton6.setBackground(new java.awt.Color(255, 255, 255));
        jButton6.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton6.setText("CLEAR");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel2.setText("NONE");

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel4.setText("NONE");

        jLabel12.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel12.setText("Date:");

        jLabel13.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel13.setText("0");

        jLabel14.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel14.setText("Time:");

        jLabel15.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel15.setText("0");

        jButton7.setBackground(new java.awt.Color(255, 255, 255));
        jButton7.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton7.setText("CLEAR");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 463, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jButton1)
                        .addGap(50, 50, 50)
                        .addComponent(jButton6)
                        .addGap(50, 50, 50)
                        .addComponent(jButton5))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(19, 19, 19)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(52, 52, 52)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel1)))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel14)
                                            .addComponent(jButton3))
                                        .addGap(18, 18, 18)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jButton4)
                                            .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 13, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addComponent(jLabel12)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(61, 61, 61)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton7)
                        .addGap(86, 86, 86))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton2)
                            .addComponent(jButton7))
                        .addGap(21, 21, 21)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addComponent(jLabel8)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(jButton4)
                            .addComponent(jButton3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel12)
                            .addComponent(jLabel13)
                            .addComponent(jLabel14)
                            .addComponent(jLabel15)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton1)
                            .addComponent(jButton5)
                            .addComponent(jButton6))
                        .addGap(26, 26, 26)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 316, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    DefaultTableModel model = (DefaultTableModel) jTable2.getModel();model.setRowCount(0);table();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
    try{identifyFinger();enroller.clear();}
    catch (ClassNotFoundException ex) {Logger.getLogger(attendance.class.getName()).log(Level.SEVERE, null, ex);} 
    catch (IOException ex) {Logger.getLogger(attendance.class.getName()).log(Level.SEVERE, null, ex);}
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
     if(jTextField1.getText().length()==0){JOptionPane.showMessageDialog(null, "Please enter Unit ID.");}
     else if(jTextField2.getText().length()==0){JOptionPane.showMessageDialog(null, "Please enter Date.");}
     else{try{try{Class.forName("com.mysql.jdbc.Driver");}
     catch (ClassNotFoundException ex) {Logger.getLogger(attendance.class.getName()).log(Level.SEVERE, null, ex);}
    String studid=jLabel2.getText();String name=jLabel4.getText(); String unitID=jTextField1.getText();String dates=jTextField2.getText();
    connects=(Connection) DriverManager.getConnection(dburl, "root", "");stnt=(Statement) connects.createStatement();
    rs=stnt.executeQuery("SELECT * FROM indunit WHERE unitid='"+unitID+"'AND studid='"+studid+"'AND name='"+name+"' AND date='"+dates+"' ");
    if(rs.next()){if(jTextField3.getText().length()==0){JOptionPane.showMessageDialog(null, "Please enter Booklet Number.");}
    else{String timein=jLabel15.getText();String booklet=jTextField3.getText();
    stnt1=(PreparedStatement) connects.prepareStatement("UPDATE indunit SET timein='"+timein+"', bookno='"+booklet+"' "
    + "WHERE unitid='"+unitID+"'AND studid='"+studid+"'AND name='"+name+"' AND date='"+dates+"'");stnt1.executeUpdate();
    DefaultTableModel model = (DefaultTableModel) jTable2.getModel();model.setRowCount(0);table();}} 
    else{JOptionPane.showMessageDialog(null, "Cannot Add! The Entry Doesn't Exists!");}
    rs.close();stnt1.close();stnt.close(); connects.close();}catch (SQLException | HeadlessException e) {
    try {JOptionPane.showMessageDialog(null, e.getMessage());rs.close();stnt1.close();stnt.close();connects.close();}
    catch (SQLException ex) {Logger.getLogger(attendance.class.getName()).log(Level.SEVERE, null, ex);}}}

    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
    try{String studid=jLabel2.getText();String name=jLabel4.getText(); String unitID=jTextField1.getText();
    String dates=jTextField2.getText(); String timeout=jLabel15.getText();try{Class.forName("com.mysql.jdbc.Driver");}
    catch (ClassNotFoundException ex) {Logger.getLogger(attendance.class.getName()).log(Level.SEVERE, null, ex);}
    connects=(com.mysql.jdbc.Connection) DriverManager.getConnection(dburl, "root", "");
    stnt=(com.mysql.jdbc.Statement) connects.createStatement();
    rs=stnt.executeQuery("SELECT * FROM indunit WHERE unitid='"+unitID+"'AND studid='"+studid+"'AND name='"+name+"' AND date='"+dates+"' ");
    if(rs.next()){stnt1=(PreparedStatement) connects.prepareStatement("UPDATE indunit SET timeout='"+timeout+"'"
        + "WHERE unitid='"+unitID+"'AND studid='"+studid+"'AND name='"+name+"' AND date='"+dates+"'");
    if(jTextField1.getText().length()==0){JOptionPane.showMessageDialog(null, "Please enter Unit ID.");}
    else if(jTextField2.getText().length()==0){JOptionPane.showMessageDialog(null, "Please enter Date.");}
    else{stnt1.executeUpdate();DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
        model.setRowCount(0);table();}}else{JOptionPane.showMessageDialog(null, "Cannot add. The Entry Does Not Exists.");}
    rs.close();stnt1.close();stnt.close(); connects.close();}catch (SQLException | HeadlessException e) {
    try {JOptionPane.showMessageDialog(null, e.getMessage());rs.close();stnt1.close();stnt.close();connects.close();}
    catch (SQLException ex) {Logger.getLogger(attendance.class.getName()).log(Level.SEVERE, null, ex);}}

    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        lectpage frame = new lectpage();frame.setVisible(true);this.setVisible(false);
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        jTextField1.setText("");jTextField2.setText("");
        DefaultTableModel model = (DefaultTableModel) jTable2.getModel();model.setRowCount(0);
    }//GEN-LAST:event_jButton6ActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
       init();start();extractFinger();jButton2.setEnabled(false);
       jButton3.setEnabled(false);jButton4.setEnabled(false);jButton4.grabFocus();
    }//GEN-LAST:event_formWindowOpened

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
    stop();
    }//GEN-LAST:event_formWindowClosing

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
    jLabel2.setText("NONE"); jLabel4.setText("NONE");jTextField3.setText("");
    }//GEN-LAST:event_jButton7ActionPerformed

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
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(attendance.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(attendance.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(attendance.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(attendance.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new attendance().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    // End of variables declaration//GEN-END:variables
}
