/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.naoth.xabsleditor;

import de.naoth.xabsleditor.events.EventManager;
import de.naoth.xabsleditor.events.OpenFileEvent;
import de.naoth.xabsleditor.parser.XABSLContext;
import de.naoth.xabsleditor.parser.XABSLContext.XABSLOption;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.HashSet;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

/**
 *
 * @author thomas
 */
public class UnusedOptions extends javax.swing.JDialog
{
  private final EventManager evtManager = EventManager.getInstance();
  private Main parent;
  private XABSLContext context;

  /**
   * Creates new form UnusedOptions
   */
  public UnusedOptions(Main parent, XABSLContext context)
  {
    super(parent, true);
    this.parent = parent;
    this.context = context;
    
    initComponents();

    update(context);
    
    // hide dialog when pressing ESC
    this.getRootPane().registerKeyboardAction(e -> {
        setVisible(false);
    }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
  }

  private void update(XABSLContext context)
  {
    HashSet<String> usedOptions = new HashSet<String>();
    for (String rootOption : context.getAgentMap().values())
    {
      fillUsedActionList(usedOptions, context, rootOption);
    }

    DefaultListModel m = new DefaultListModel();
    for (XABSLOption o : context.getOptionMap().values())
    {
      if (!usedOptions.contains(o.getName()))
      {
        File path = context.getOptionPathMap().get(o.getName());
        
        if(path != null)
        {
          m.addElement(path);
        }
      }
    }
    lstUnused.setModel(m);
    
  }

  private void fillUsedActionList(HashSet<String> usedOptions,
    XABSLContext context, String action)
  {
    if (!usedOptions.contains(action))
    {
      usedOptions.add(action);
      for (String out : context.getOptionMap().get(action).getActions())
      {
        fillUsedActionList(usedOptions, context, out);
      }
    }
  }

  private void openFile()
  {
      File f = (File) lstUnused.getModel().getElementAt(lstUnused.getSelectedIndex());
      evtManager.publish(new OpenFileEvent(this, f));
  }
  
  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        lstUnused = new javax.swing.JList();
        btClose = new javax.swing.JButton();
        btDelete = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Unused options");

        lstUnused.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lstUnusedMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(lstUnused);

        btClose.setMnemonic('c');
        btClose.setText("Close");
        btClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCloseActionPerformed(evt);
            }
        });

        btDelete.setMnemonic('d');
        btDelete.setText("Delete");
        btDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btDeleteActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btDelete)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btClose))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 835, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 429, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btClose)
                    .addComponent(btDelete)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

  private void btCloseActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btCloseActionPerformed
  {//GEN-HEADEREND:event_btCloseActionPerformed
    this.setVisible(false);
  }//GEN-LAST:event_btCloseActionPerformed

  private void lstUnusedMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lstUnusedMouseClicked
  {//GEN-HEADEREND:event_lstUnusedMouseClicked
    if(evt.getClickCount() == 2)
    {
      openFile();
    }

  }//GEN-LAST:event_lstUnusedMouseClicked

  private void btDeleteActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btDeleteActionPerformed
  {//GEN-HEADEREND:event_btDeleteActionPerformed
    
    int idx = lstUnused.getSelectedIndex();
    if(idx < 0)
    {
      return;
    }
    
    File f = (File) lstUnused.getModel().getElementAt(idx);
    if(f.isFile())
    {
      if(f.delete())
      {
        DefaultListModel m = (DefaultListModel) lstUnused.getModel();
        m.remove(idx);
        if(!m.isEmpty())
        {
          lstUnused.setSelectedIndex(0);
        }
      }
    }
    else
    {
      JOptionPane.showMessageDialog(this, "Could not find file " + f.getAbsolutePath());
    }
    
  }//GEN-LAST:event_btDeleteActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btClose;
    private javax.swing.JButton btDelete;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList lstUnused;
    // End of variables declaration//GEN-END:variables
}
