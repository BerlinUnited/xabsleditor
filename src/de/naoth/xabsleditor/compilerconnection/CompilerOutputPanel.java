/*
 * CompilerOutputPanel.java
 *
 * Created on 13.06.2010, 23:24:16
 */

package de.naoth.xabsleditor.compilerconnection;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import javax.swing.text.BadLocationException;

/**
 *
 * @author Heinrich Mellmann
 */
public class CompilerOutputPanel extends javax.swing.JPanel {

    private CompileResult currentCompileResult = null;

    /** Creates new form CompilerOutputPanel */
    public CompilerOutputPanel() {
      initComponents();

      this.txtCompilerOutput.addMouseMotionListener(new MouseMotionAdapter() 
      {
        @Override
        public void mouseMoved(MouseEvent e) {
          int offset = txtCompilerOutput.viewToModel(e.getPoint());
          try{
            int line = txtCompilerOutput.getLineOfOffset(offset);
            if(currentCompileResult != null)
            {
              CompileResult.CompilerNotice notice = currentCompileResult.getNotice(line);
              if(notice != null)
              {
                txtCompilerOutput.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
              }else
              {
                txtCompilerOutput.setCursor(Cursor.getDefaultCursor());
              }
            }
          }catch(BadLocationException ex)
          {
            // shouldn't be here
          }
        }
      });

      this.txtCompilerOutput.addMouseListener(new MouseAdapter()
      {
        @Override
        public void mouseClicked(MouseEvent e) {
          //if(e.getClickCount() < 2) return;

          int offset = txtCompilerOutput.viewToModel(e.getPoint());
          try{
            int line = txtCompilerOutput.getLineOfOffset(offset);
            if(currentCompileResult != null)
            {
              CompileResult.CompilerNotice notice = currentCompileResult.getNotice(line);
              if(notice != null)
              {
                System.out.println(notice.lineNumber);
                
                JumpTarget jumpTarget = new JumpTarget();
                jumpTarget.setFileName(notice.fileName);
                jumpTarget.setLineNumber(notice.lineNumber);
                fireJumpEvent(jumpTarget);
              }
            }
          }catch(BadLocationException ex)
          {
            // shouldn't be here
          }
        }
      });
    }

    public void setCompilerResult(CompileResult result)
    {
      this.currentCompileResult = result;
      
      if(result == null)
      {
        txtCompilerOutput.setText("");
        return;
      }
      
      txtCompilerOutput.setText(result.messages);
      System.err.print(result);
    }//end setCompilerResult

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollPaneCompilerOutput = new javax.swing.JScrollPane();
        txtCompilerOutput = new javax.swing.JTextArea();

        txtCompilerOutput.setColumns(20);
        txtCompilerOutput.setEditable(false);
        txtCompilerOutput.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        txtCompilerOutput.setRows(5);
        scrollPaneCompilerOutput.setViewportView(txtCompilerOutput);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scrollPaneCompilerOutput, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scrollPaneCompilerOutput, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane scrollPaneCompilerOutput;
    private javax.swing.JTextArea txtCompilerOutput;
    // End of variables declaration//GEN-END:variables

  public static class JumpTarget
  {
    private String fileName = null;
    private String elementName = null;
    private int lineNumber = -1;
    private int offset = -1;

    public String getElementName() {
      return elementName;
    }

    public void setElementName(String elementName) {
      this.elementName = elementName;
    }

    public String getFileName() {
      return fileName;
    }

    public void setFileName(String fileName) {
      this.fileName = fileName;
    }

    public int getLineNumber() {
      return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
      this.lineNumber = lineNumber;
    }

    public int getOffset() {
      return offset;
    }

    public void setOffset(int offset) {
      this.offset = offset;
    }

    @Override
    public String toString()
    {
      StringBuilder sb = new StringBuilder();
      sb.append(fileName).append(':').append(lineNumber);
      return sb.toString();
    }//end toString
  }//end class JumpTarget

  public static interface JumpListener
  {
    public void jumpTo(JumpTarget target);
  }//end JumpListener

  private ArrayList<JumpListener> jumpListeners = new ArrayList<JumpListener>();

  public boolean removeJumpListener(JumpListener e) {
    return jumpListeners.remove(e);
  }

  public boolean addJumpListener(JumpListener e) {
    return jumpListeners.add(e);
  }

  private void fireJumpEvent(JumpTarget jumpTarget)
  {
    for(JumpListener jumpListener: this.jumpListeners)
    {
      jumpListener.jumpTo(jumpTarget);
    }//end for
  }//end fireJumpEvent

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        if(txtCompilerOutput != null) {
            txtCompilerOutput.setFont(font);
        }
    }
}//end CompilerOutputPanel
