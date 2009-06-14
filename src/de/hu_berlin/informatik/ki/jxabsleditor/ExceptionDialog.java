/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ExceptionDialog.java
 *
 * Created on 27.01.2009, 20:29:11
 */
package de.hu_berlin.informatik.ki.jxabsleditor;

/**
 *
 * @author thomas
 */
public class ExceptionDialog extends javax.swing.JDialog
{

  /** Creates new form ExceptionDialog */
  public ExceptionDialog(java.awt.Frame parent, Exception exception)
  {
    super(parent, true);
    initComponents();

    if(exception != null)
    {
      txtMessage.setText(exception.getLocalizedMessage());
      txtMessage.setCaretPosition(0);
      StringBuffer details = new StringBuffer();
      details.append(exception.getLocalizedMessage());
      details.append("\nat\n");
      StackTraceElement[] st = exception.getStackTrace();
      for(int i=0; i < st.length; i++)
      {
        details.append(st[i].toString());
        details.append("\n");
      }
      txtDetails.setText(details.toString());
    }
  }


  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    lblIcon = new javax.swing.JLabel();
    lblCaption = new javax.swing.JLabel();
    btClose = new javax.swing.JButton();
    spDetails = new javax.swing.JScrollPane();
    txtDetails = new javax.swing.JTextArea();
    btDetails = new javax.swing.JToggleButton();
    spMessage = new javax.swing.JScrollPane();
    txtMessage = new javax.swing.JTextArea();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    setTitle("Exception thrown");
    addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowOpened(java.awt.event.WindowEvent evt) {
        formWindowOpened(evt);
      }
    });

    lblIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/hu_berlin/informatik/ki/jxabsleditor/res/warning32.png"))); // NOI18N

    lblCaption.setFont(new java.awt.Font("DejaVu Sans", 1, 13));
    lblCaption.setText("Exception thrown:");

    btClose.setMnemonic('C');
    btClose.setText("Close");
    btClose.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btCloseActionPerformed(evt);
      }
    });

    txtDetails.setColumns(20);
    txtDetails.setRows(5);
    txtDetails.setText("<no details>");
    spDetails.setViewportView(txtDetails);

    btDetails.setMnemonic('D');
    btDetails.setText("Details");
    btDetails.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btDetailsActionPerformed(evt);
      }
    });

    txtMessage.setColumns(20);
    txtMessage.setEditable(false);
    txtMessage.setLineWrap(true);
    txtMessage.setRows(5);
    txtMessage.setText("<no message>");
    txtMessage.setWrapStyleWord(true);
    spMessage.setViewportView(txtMessage);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(lblIcon)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(spDetails, javax.swing.GroupLayout.DEFAULT_SIZE, 484, Short.MAX_VALUE)
          .addGroup(layout.createSequentialGroup()
            .addComponent(btDetails)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 337, Short.MAX_VALUE)
            .addComponent(btClose))
          .addComponent(lblCaption, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 484, Short.MAX_VALUE)
          .addComponent(spMessage, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 484, Short.MAX_VALUE))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(lblIcon)
          .addGroup(layout.createSequentialGroup()
            .addComponent(lblCaption)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(spMessage, javax.swing.GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addComponent(spDetails, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(btClose)
          .addComponent(btDetails))
        .addContainerGap())
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void btDetailsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btDetailsActionPerformed
  {//GEN-HEADEREND:event_btDetailsActionPerformed

    spDetails.setVisible(btDetails.isSelected());
    pack();
    validate();
  }//GEN-LAST:event_btDetailsActionPerformed

  private void btCloseActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btCloseActionPerformed
  {//GEN-HEADEREND:event_btCloseActionPerformed
    
    this.setVisible(false);
    this.dispose();

  }//GEN-LAST:event_btCloseActionPerformed

  private void formWindowOpened(java.awt.event.WindowEvent evt)//GEN-FIRST:event_formWindowOpened
  {//GEN-HEADEREND:event_formWindowOpened

      spDetails.setVisible(false);
      pack();
      validate();

  }//GEN-LAST:event_formWindowOpened

  /**
   * @param args the command line arguments
   */
  public static void main(String args[])
  {
    java.awt.EventQueue.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        ExceptionDialog dialog = new ExceptionDialog(new javax.swing.JFrame(), null);
        dialog.addWindowListener(new java.awt.event.WindowAdapter()
        {

          @Override
          public void windowClosing(java.awt.event.WindowEvent e)
          {
            System.exit(0);
          }
        });
        dialog.setVisible(true);
      }
    });
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton btClose;
  private javax.swing.JToggleButton btDetails;
  private javax.swing.JLabel lblCaption;
  private javax.swing.JLabel lblIcon;
  private javax.swing.JScrollPane spDetails;
  private javax.swing.JScrollPane spMessage;
  private javax.swing.JTextArea txtDetails;
  private javax.swing.JTextArea txtMessage;
  // End of variables declaration//GEN-END:variables
}
