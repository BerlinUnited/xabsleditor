/*
 * 
 */

/*
 * Options.java
 *
 * Created on 28.01.2009, 01:18:01
 */
package de.hu_berlin.informatik.ki.jxabsleditor;

import java.io.File;
import java.util.Properties;
import javax.swing.JFileChooser;

/**
 *
 * @author Heinrich Mellmann
 */
public class OptionsDialog extends javax.swing.JDialog
{
    
  private Properties configuration;

  /** Creates new form Options */
  public OptionsDialog(java.awt.Frame parent, boolean modal, Properties configuration)
  {
    super(parent, modal);
    initComponents();
    this.setTitle("Options");

    this.configuration = configuration;
    loadOptions();
  }

  private void loadOptions()
  {
    if(configuration.containsKey("dotInstallationPath"))
    {
      this.dotInstallationPathTextField.setText(configuration.getProperty("dotInstallationPath"));
    }
    
    if(configuration.containsKey("defaultCompilationPath"))
    {
      this.defaultCompilationPathTextField.setText(configuration.getProperty("defaultCompilationPath"));
    }
  }//end loadOptions

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dotInstallationPathTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jButtonBrowse = new javax.swing.JButton();
        jButtonOK = new javax.swing.JButton();
        defaultCompilationPathTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jButtonBrowse1 = new javax.swing.JButton();
        jButtonOK1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setLocationByPlatform(true);
        setModal(true);
        setName("Options"); // NOI18N
        setResizable(false);

        jLabel1.setText("DOT layout engine installation path");

        jButtonBrowse.setText("Browse...");
        jButtonBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBrowseActionPerformed(evt);
            }
        });

        jButtonOK.setText("OK");
        jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOKActionPerformed(evt);
            }
        });

        jLabel2.setText("Default compilation path");

        jButtonBrowse1.setText("Browse...");
        jButtonBrowse1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBrowse1ActionPerformed(evt);
            }
        });

        jButtonOK1.setText("Cancel");
        jButtonOK1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOK1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(dotInstallationPathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButtonBrowse))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(defaultCompilationPathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButtonBrowse1))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(305, 305, 305)
                        .addComponent(jButtonOK1, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(305, 305, 305)
                        .addComponent(jButtonOK, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dotInstallationPathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonBrowse))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(defaultCompilationPathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonBrowse1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 47, Short.MAX_VALUE)
                .addComponent(jButtonOK)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonOK1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOKActionPerformed

      // save settings
      configuration.setProperty("dotInstallationPath", dotInstallationPathTextField.getText());
      configuration.setProperty("defaultCompilationPath", defaultCompilationPathTextField.getText());

      this.setVisible(false);
      this.dispose();
    }//GEN-LAST:event_jButtonOKActionPerformed

    private void jButtonBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBrowseActionPerformed
      JFileChooser dotFileChooser = new JFileChooser();
      dotFileChooser.setFileFilter(new DotExecutableFilter());
      int result = dotFileChooser.showOpenDialog(this);

      if(JFileChooser.APPROVE_OPTION == result)
      {
        String path = dotFileChooser.getSelectedFile().getAbsolutePath();
        this.defaultCompilationPathTextField.setText(path);
      }//end if
    }//GEN-LAST:event_jButtonBrowseActionPerformed

    private void jButtonBrowse1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBrowse1ActionPerformed
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      int result = fileChooser.showOpenDialog(this);

      if(JFileChooser.APPROVE_OPTION == result)
      {
        String path = fileChooser.getSelectedFile().getAbsolutePath();
        this.defaultCompilationPathTextField.setText(path);
      }//end if
    }//GEN-LAST:event_jButtonBrowse1ActionPerformed

    private void jButtonOK1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOK1ActionPerformed
      this.setVisible(false);
      this.dispose();
    }//GEN-LAST:event_jButtonOK1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField defaultCompilationPathTextField;
    private javax.swing.JTextField dotInstallationPathTextField;
    private javax.swing.JButton jButtonBrowse;
    private javax.swing.JButton jButtonBrowse1;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JButton jButtonOK1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    // End of variables declaration//GEN-END:variables

  private class DotExecutableFilter extends javax.swing.filechooser.FileFilter
  {

    public boolean accept(File file)
    {
      if(file.isDirectory())
      {
        return true;
      }
      String filename = file.getName();
      return filename.equalsIgnoreCase("dot.exe") || filename.equalsIgnoreCase("dot");
    }

    public String getDescription()
    {
      return "DOT Graph Layout Engine";
    }
  }//end class DotExecutableFilter
}
