import javax.swing.UIManager;import com.digitalpersona.onetouch.DPFPDataPurpose;import javax.swing.SwingUtilities;
import com.digitalpersona.onetouch.DPFPFeatureSet;import com.digitalpersona.onetouch.DPFPGlobal;
import com.digitalpersona.onetouch.DPFPSample;import com.digitalpersona.onetouch.DPFPTemplate;import java.util.logging.Level;
import com.digitalpersona.onetouch.capture.DPFPCapture;import com.digitalpersona.onetouch.capture.event.DPFPDataAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPDataEvent;import com.digitalpersona.onetouch.capture.event.DPFPErrorAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPErrorEvent;import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusEvent;import com.digitalpersona.onetouch.capture.event.DPFPSensorAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPSensorEvent;import com.digitalpersona.onetouch.processing.DPFPEnrollment;
import com.digitalpersona.onetouch.processing.DPFPFeatureExtraction;import com.digitalpersona.onetouch.processing.DPFPImageQualityException;
import java.awt.Image;import java.io.ByteArrayInputStream;import java.sql.Connection;import java.sql.DriverManager;
import java.sql.PreparedStatement;import java.sql.ResultSet;import java.sql.SQLException;import java.sql.Statement;
import java.util.logging.Logger;import javax.swing.ImageIcon;import javax.swing.JOptionPane;

public class print1 extends javax.swing.JFrame {String JDBC_Driver="com.mysql.jdbc.Driver";
String dburl ="jdbc:mysql://localhost:3306/attendance?zeroDateTimeBehavior=convertToNull";
    
    public print1() {try{UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}catch(Exception e){
    JOptionPane.showMessageDialog(null,"Impossible Modification", "Invalid LookandFeel.", JOptionPane.ERROR_MESSAGE);}
        initComponents();}
    
    private DPFPCapture capturer=DPFPGlobal.getCaptureFactory().createCapture();
    private DPFPEnrollment enroller=DPFPGlobal.getEnrollmentFactory().createEnrollment();
    private DPFPTemplate template; private static final String TEMPLATE_PROPERTY="template";
    
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
    public void drawPicture(Image image) {jLabel1.setIcon(new ImageIcon(image.getScaledInstance(
    jLabel1.getWidth(), jLabel1.getHeight(), Image.SCALE_DEFAULT))); repaint(); }
    
    public void extractFinger(){makeReport("Displaying fingerprint."+enroller.getFeaturesNeeded());}
    
    public void makeReport(String string){jTextArea1.append(string+"\n");}
    
    public void start(){capturer.startCapture();makeReport("Using the fingerprint reader, scan your fingerprint.");}

    public void stop(){capturer.stopCapture();makeReport("Done.");}
    
    public DPFPTemplate getTemplate() {return template;}
    
    public void setTemplate(DPFPTemplate template) {DPFPTemplate old = this.template;
    this.template = template;firePropertyChange(TEMPLATE_PROPERTY, old, template);}
    
    public void ProcessCapture(DPFPSample sample){ features=extractFeatures(sample, DPFPDataPurpose.DATA_PURPOSE_ENROLLMENT);
    featuresverification=extractFeatures(sample, DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);
    if(features!=null)try{System.out.println("The fingerprint feature set was created.");
    enroller.addFeatures(features);Image image=convertSampleToBitmap(sample);drawPicture(image);} 
    catch (DPFPImageQualityException ex) {System.err.println("Error:"+ex.getMessage());}
    finally{extractFinger();switch(enroller.getTemplateStatus()){
    case TEMPLATE_STATUS_READY:stop();setTemplate(enroller.getTemplate());
    makeReport("Click Close, and then click Fingerprint Verification.");jButton3.setEnabled(true);jButton3.grabFocus();break;
    case TEMPLATE_STATUS_FAILED: enroller.clear();stop();extractFinger();setTemplate(null);
    JOptionPane.showMessageDialog(print1.this, "The fingerprint template is not valid. Repeat fingerprint enrollment."
    , "Fingerprint Enrollment", JOptionPane.ERROR_MESSAGE);start();break;}}}
    
    public void enrollFinger() throws SQLException, ClassNotFoundException{
    ByteArrayInputStream datosFinger=new ByteArrayInputStream(template.serialize());Integer tamanoFinger=template.serialize().length;
    String ID=JOptionPane.showInputDialog("Student ID:");String name=JOptionPane.showInputDialog("Student Name:");
    try{Class.forName("com.mysql.jdbc.Driver"); Connection con1= DriverManager.getConnection(dburl, "root", "");
    PreparedStatement stmt= (PreparedStatement) con1.prepareStatement("INSERT INTO finger(studentid, name, print) "
    + "VALUES(?,?,?)");stmt.setString(1, ID);stmt.setString(2, name);stmt.setBinaryStream(3, datosFinger, tamanoFinger);
    stmt.execute();jButton3.setEnabled(false);} 
    catch(SQLException ex) {JOptionPane.showMessageDialog(null, "Error saving FingerPrint!");System.err.println("Error saving FingerPrint!");}}
    	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();

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

        jPanel3.setBackground(new java.awt.Color(204, 51, 0));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), "DIGITAL FINGERPRINT:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14))); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(44, 44, 44))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), "ACTIONS:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14))); // NOI18N

        jButton3.setBackground(new java.awt.Color(255, 255, 255));
        jButton3.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton3.setText("ENROLL");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setBackground(new java.awt.Color(255, 255, 255));
        jButton4.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton4.setText("CANCEL");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(jButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton4)
                .addGap(40, 40, 40))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton3)
                    .addComponent(jButton4))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 343, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
    this.setVisible(false);
    }//GEN-LAST:event_jButton4ActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
    init();start();extractFinger();jButton3.setEnabled(false);jButton4.grabFocus();
    }//GEN-LAST:event_formWindowOpened

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
    stop();
    }//GEN-LAST:event_formWindowClosing

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
    try{Class.forName("com.mysql.jdbc.Driver"); Connection con1= DriverManager.getConnection(dburl, "root", "");
    String ID=JOptionPane.showInputDialog("Student ID:");Statement stnt=con1.createStatement();
    ResultSet rs=stnt.executeQuery("SELECT * FROM finger where studentid='"+ID+"'");
    if(rs.next()){JOptionPane.showMessageDialog(null, "Cannot Add! The Entry Adleady Exists!");}
    else{try {enrollFinger();enroller.clear();jLabel1.setIcon(null);start();} 
    catch (SQLException ex) {Logger.getLogger(print1.class.getName()).log(Level.SEVERE, null, ex);} 
   catch (ClassNotFoundException ex) {Logger.getLogger(print1.class.getName()).log(Level.SEVERE, null, ex);}}
    rs.close();stnt.close();con1.close();} 
    catch(SQLException ex) {Logger.getLogger(print1.class.getName()).log(Level.SEVERE, null, ex);
    } catch (ClassNotFoundException ex) {Logger.getLogger(print1.class.getName()).log(Level.SEVERE, null, ex);}
        
    }//GEN-LAST:event_jButton3ActionPerformed

    public static void main(String args[]) {java.awt.EventQueue.invokeLater(new Runnable() {
    public void run() {new print1().setVisible(true);}});}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables
}
