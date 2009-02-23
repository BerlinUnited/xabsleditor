/*
 * 
 */

/*
 * Main.java
 *
 * Created on 08.01.2009, 01:03:25
 */
package de.hu_berlin.informatik.ki.jxabsleditor;

import att.grappa.Attribute;
import att.grappa.Element;
import att.grappa.GrappaBox;
import att.grappa.GrappaListener;
import att.grappa.GrappaPanel;
import att.grappa.GrappaPoint;
import att.grappa.Subgraph;
import de.hu_berlin.informatik.ki.jxabsleditor.editorpanel.XEditorPanel;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.metal.MetalLookAndFeel;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import xabslc.IC;
import xabslc.PrepareIC;
import xabslc.XabslLexer;
import xabslc.XabslParser;

/**
 *
 * @author Heinrich Mellmann
 */
public class Main extends javax.swing.JFrame
{

  private JFileChooser fileChooser = new JFileChooser();
  private Properties configuration = new Properties();
  private File fConfig;

  private FileFilter dotFilter = new DotFileFilter();
  private FileFilter xabslFilter = new XABSLFileFilter();
  private FileFilter icFilter = new FileNameExtensionFilter("Intermediate code (*.dat)", "dat");


  String defaultCompilationPath = null;

  /** Creates new form Main */
  public Main()
  {
    // no bold fonts please
    UIManager.put("swing.boldMetal", Boolean.FALSE);
    try
    {
      UIManager.setLookAndFeel(new MetalLookAndFeel());
    }
    catch(UnsupportedLookAndFeelException ex)
    {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
    }


    initComponents();

    // load configuration
    fConfig = new File(System.getProperty("user.home") + "/.jxabsleditor");

    try
    {
      if(fConfig.exists() && fConfig.canRead())
      {
        configuration.load(new FileReader(fConfig));
      }
    }
    catch(Exception ex)
    {
      handleException(ex);
    }

    fileChooser = new JFileChooser();
    fileChooser.setFileFilter(xabslFilter);
    fileChooser.setFileFilter(icFilter);
    fileChooser.setFileFilter(dotFilter);
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    fileChooser.setAcceptAllFileFilterUsed(true);

    loadConfiguration();

    this.xGraph.setListener(new MyGrappaListener());
  }

  private void loadConfiguration()
  {
    if(configuration.containsKey("lastOpenedFolder"))
    {
      fileChooser.setCurrentDirectory(
        new File(configuration.getProperty("lastOpenedFolder")));
    }

    if(configuration.containsKey("dotInstallationPath"))
    {
      this.xGraph.setLayoutEngine(configuration.getProperty("dotInstallationPath"));
    }

    if(configuration.containsKey("defaultCompilationPath"))
    {
      String path = configuration.getProperty("defaultCompilationPath");
      if(new File(path).exists())
      {
          this.defaultCompilationPath = path;
      }
    }
  }//end loadConfiguration

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jSplitPane = new javax.swing.JSplitPane();
    jTabbedPane = new javax.swing.JTabbedPane();
    xGraph = new de.hu_berlin.informatik.ki.jxabsleditor.graphpanel.XGraph();
    toolbarMain = new javax.swing.JToolBar();
    btNew = new javax.swing.JButton();
    btOpen = new javax.swing.JButton();
    btSave = new javax.swing.JButton();
    seperator1 = new javax.swing.JToolBar.Separator();
    btCompile = new javax.swing.JButton();
    mbMain = new javax.swing.JMenuBar();
    mFile = new javax.swing.JMenu();
    miNew = new javax.swing.JMenuItem();
    miOpen = new javax.swing.JMenuItem();
    miClose = new javax.swing.JMenuItem();
    jSeparator1 = new javax.swing.JSeparator();
    miSave = new javax.swing.JMenuItem();
    miSaveAs = new javax.swing.JMenuItem();
    jSeparator2 = new javax.swing.JSeparator();
    miQuit = new javax.swing.JMenuItem();
    mEdit = new javax.swing.JMenu();
    miCompile = new javax.swing.JMenuItem();
    miRefreshGraph = new javax.swing.JMenuItem();
    miOption = new javax.swing.JMenuItem();
    mHelp = new javax.swing.JMenu();
    miInfo = new javax.swing.JMenuItem();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setTitle("XABSL Editor");
    setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    setLocationByPlatform(true);

    jSplitPane.setDividerLocation(450);
    jSplitPane.setPreferredSize(new java.awt.Dimension(750, 600));

    jTabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
    jSplitPane.setLeftComponent(jTabbedPane);
    jSplitPane.setRightComponent(xGraph);

    getContentPane().add(jSplitPane, java.awt.BorderLayout.CENTER);

    toolbarMain.setFloatable(false);
    toolbarMain.setRollover(true);

    btNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/hu_berlin/informatik/ki/jxabsleditor/res/filenew22.png"))); // NOI18N
    btNew.setToolTipText("New file");
    btNew.setFocusable(false);
    btNew.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    btNew.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    btNew.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        newFileAction(evt);
      }
    });
    toolbarMain.add(btNew);

    btOpen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/hu_berlin/informatik/ki/jxabsleditor/res/fileopen22.png"))); // NOI18N
    btOpen.setToolTipText("Open File");
    btOpen.setFocusable(false);
    btOpen.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    btOpen.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    btOpen.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        openFileAction(evt);
      }
    });
    toolbarMain.add(btOpen);

    btSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/hu_berlin/informatik/ki/jxabsleditor/res/filesave22.png"))); // NOI18N
    btSave.setToolTipText("Save File");
    btSave.setFocusable(false);
    btSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    btSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    btSave.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        saveFileAction(evt);
      }
    });
    toolbarMain.add(btSave);

    seperator1.setOrientation(javax.swing.SwingConstants.HORIZONTAL);
    toolbarMain.add(seperator1);

    btCompile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/hu_berlin/informatik/ki/jxabsleditor/res/compfile22.png"))); // NOI18N
    btCompile.setToolTipText("Compile Behavior");
    btCompile.setFocusable(false);
    btCompile.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    btCompile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    btCompile.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        compileAction(evt);
      }
    });
    toolbarMain.add(btCompile);

    getContentPane().add(toolbarMain, java.awt.BorderLayout.PAGE_START);

    mFile.setMnemonic('F');
    mFile.setText("File");

    miNew.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
    miNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/hu_berlin/informatik/ki/jxabsleditor/res/filenew16.png"))); // NOI18N
    miNew.setMnemonic('N');
    miNew.setText("New");
    miNew.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        newFileAction(evt);
      }
    });
    mFile.add(miNew);

    miOpen.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
    miOpen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/hu_berlin/informatik/ki/jxabsleditor/res/fileopen16.png"))); // NOI18N
    miOpen.setMnemonic('O');
    miOpen.setText("Open");
    miOpen.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        openFileAction(evt);
      }
    });
    mFile.add(miOpen);

    miClose.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.CTRL_MASK));
    miClose.setMnemonic('C');
    miClose.setText("Close");
    miClose.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        miCloseActionPerformed(evt);
      }
    });
    mFile.add(miClose);
    mFile.add(jSeparator1);

    miSave.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
    miSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/hu_berlin/informatik/ki/jxabsleditor/res/filesave16.png"))); // NOI18N
    miSave.setMnemonic('S');
    miSave.setText("Save");
    miSave.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        saveFileAction(evt);
      }
    });
    mFile.add(miSave);

    miSaveAs.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
    miSaveAs.setMnemonic('a');
    miSaveAs.setText("Save As...");
    miSaveAs.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        miSaveAsActionPerformed(evt);
      }
    });
    mFile.add(miSaveAs);
    mFile.add(jSeparator2);

    miQuit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
    miQuit.setMnemonic('Q');
    miQuit.setText("Quit");
    miQuit.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        miQuitActionPerformed(evt);
      }
    });
    mFile.add(miQuit);

    mbMain.add(mFile);

    mEdit.setMnemonic('E');
    mEdit.setText("Edit");

    miCompile.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.ALT_MASK));
    miCompile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/hu_berlin/informatik/ki/jxabsleditor/res/compfile16.png"))); // NOI18N
    miCompile.setMnemonic('C');
    miCompile.setText("Compile Behavior");
    miCompile.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        compileAction(evt);
      }
    });
    mEdit.add(miCompile);

    miRefreshGraph.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
    miRefreshGraph.setMnemonic('R');
    miRefreshGraph.setText("Refresh Graph");
    miRefreshGraph.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        miRefreshGraphActionPerformed(evt);
      }
    });
    mEdit.add(miRefreshGraph);

    miOption.setMnemonic('O');
    miOption.setText("Options");
    miOption.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        miOptionActionPerformed(evt);
      }
    });
    mEdit.add(miOption);

    mbMain.add(mEdit);

    mHelp.setMnemonic('H');
    mHelp.setText("Help");

    miInfo.setMnemonic('I');
    miInfo.setText("Info");
    miInfo.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        miInfoActionPerformed(evt);
      }
    });
    mHelp.add(miInfo);

    mbMain.add(mHelp);

    setJMenuBar(mbMain);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void newFileAction(java.awt.event.ActionEvent evt)//GEN-FIRST:event_newFileAction
  {//GEN-HEADEREND:event_newFileAction
    // create new tab
    XEditorPanel editor = new XEditorPanel();
    int tabCount = jTabbedPane.getTabCount();
    jTabbedPane.addTab("New " + tabCount, editor);
    jTabbedPane.setSelectedComponent(editor);
}//GEN-LAST:event_newFileAction

    private void miCloseActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_miCloseActionPerformed
    {//GEN-HEADEREND:event_miCloseActionPerformed
      // close current tab
      jTabbedPane.remove(jTabbedPane.getSelectedComponent());
}//GEN-LAST:event_miCloseActionPerformed

    private void saveFileAction(java.awt.event.ActionEvent evt)//GEN-FIRST:event_saveFileAction
    {//GEN-HEADEREND:event_saveFileAction

      XEditorPanel editor = ((XEditorPanel) jTabbedPane.getSelectedComponent());
      String text = editor.getText();
      File selectedFile = editor.getFile();

      try
      {
        File file = saveStringToFile(selectedFile, text);
        if(file != null)
        {
          editor.setChanged(false);
        }
      }
      catch(IOException e)
      {
        JOptionPane.showMessageDialog(this,
          e.toString(), "The file could not be written.", JOptionPane.ERROR_MESSAGE);
      }
      catch(Exception e)
      {
        JOptionPane.showMessageDialog(this,
          e.toString(), "Could not save the file.", JOptionPane.ERROR_MESSAGE);
      }//end catch

}//GEN-LAST:event_saveFileAction

    private void miRefreshGraphActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_miRefreshGraphActionPerformed
    {//GEN-HEADEREND:event_miRefreshGraphActionPerformed
      String text = ((XEditorPanel) jTabbedPane.getSelectedComponent()).getText();
      this.xGraph.importGraphFromString(Xabsl2Dot.convert(text));
}//GEN-LAST:event_miRefreshGraphActionPerformed

    private void miSaveAsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_miSaveAsActionPerformed
    {//GEN-HEADEREND:event_miSaveAsActionPerformed
      XEditorPanel editor = ((XEditorPanel) jTabbedPane.getSelectedComponent());
      String text = editor.getText();

      try
      {
        // save as a new file
        File file = saveStringToFile(null, text);
        if(file != null)
        {
          int idx = jTabbedPane.getSelectedIndex();
          editor.setChanged(false);
          editor.setFile(file);
          jTabbedPane.setTitleAt(idx, file.getName());
          jTabbedPane.setToolTipTextAt(idx, file.getAbsolutePath());
        }
      }
      catch(IOException e)
      {
        JOptionPane.showMessageDialog(this,
          e.toString(), "The file could not be written.", JOptionPane.ERROR_MESSAGE);
      }
      catch(Exception e)
      {
        JOptionPane.showMessageDialog(this,
          e.toString(), "Could not save the file.", JOptionPane.ERROR_MESSAGE);
      }//end catch
}//GEN-LAST:event_miSaveAsActionPerformed

    private void miOptionActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_miOptionActionPerformed
    {//GEN-HEADEREND:event_miOptionActionPerformed
      OptionsDialog optionDialog = new OptionsDialog(this, true, this.configuration);
      optionDialog.setVisible(true);

      saveConfiguration();
      loadConfiguration(); // reload settings
}//GEN-LAST:event_miOptionActionPerformed

    private void miQuitActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_miQuitActionPerformed
    {//GEN-HEADEREND:event_miQuitActionPerformed

      System.exit(0);
    }//GEN-LAST:event_miQuitActionPerformed

    private void miInfoActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_miInfoActionPerformed
    {//GEN-HEADEREND:event_miInfoActionPerformed

      JOptionPane.showMessageDialog(this,
        "<html>" +
        "<h1>XabslEditor - Java edition</h1>" +
        "<p>(c) 2009 by NaoTeam Humboldt<br>" +
        "Humboldt-Universit&auml;t zu Berlin</p>" +
        "</html>",
        "XabslEditor",
        JOptionPane.INFORMATION_MESSAGE);

    }//GEN-LAST:event_miInfoActionPerformed

    private void openFileAction(java.awt.event.ActionEvent evt)//GEN-FIRST:event_openFileAction
    {//GEN-HEADEREND:event_openFileAction


      fileChooser.setFileFilter(xabslFilter);
      int result = fileChooser.showOpenDialog(this);
      if(JFileChooser.APPROVE_OPTION != result)
      {
        return;
      }

      File selectedFile = fileChooser.getSelectedFile();
      if(selectedFile == null)
      {
        return;
      }
      try
      {

        configuration.setProperty("lastOpenedFolder",
          fileChooser.getCurrentDirectory().getAbsolutePath());
        saveConfiguration();

        // read the file
        String content = readFileToString(selectedFile);

        // create new document
        XEditorPanel editor = new XEditorPanel(content);
        editor.setFile(selectedFile);

        // create a tab
        jTabbedPane.addTab(editor.getFile().getName(), null, editor, selectedFile.getAbsolutePath());
        jTabbedPane.setSelectedComponent(editor);
      }
      catch(IOException e)
      {
        JOptionPane.showMessageDialog(this,
          e.toString(), "The file could not be read.", JOptionPane.ERROR_MESSAGE);
      }

    }//GEN-LAST:event_openFileAction

    private void compileAction(java.awt.event.ActionEvent evt)//GEN-FIRST:event_compileAction
    {//GEN-HEADEREND:event_compileAction

      XEditorPanel editor = ((XEditorPanel) jTabbedPane.getSelectedComponent());
      File optionFile = editor.getFile();

      File agentsFile = Helper.getAgentFileForOption(optionFile);
      if(agentsFile == null)
      {
        JOptionPane.showMessageDialog(this, "Could not find agents.xabsl",
          "ERROR", JOptionPane.ERROR_MESSAGE);
        return;
      }//end file

      File fout = null;
      
      if(defaultCompilationPath == null)
      {
        fileChooser.setFileFilter(icFilter);
        int result = fileChooser.showSaveDialog(this);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if(result == JFileChooser.APPROVE_OPTION)
        {
            fout = fileChooser.getSelectedFile();
        }
      }else
      {
          fout = new File(defaultCompilationPath + "/behavior-ic.dat");
      }
      
      
        
    if(fout == null)
    {
      JOptionPane.showMessageDialog(this, "No file selected");
      return;
    }else if(fout.exists())
    {
        if(!fout.delete())
        {
            JOptionPane.showMessageDialog(this, "Can not overwrite the file " +
                    fout.getAbsolutePath());
            return;
        }
    }


      try
      {
        XabslLexer lexer = new XabslLexer(agentsFile.getAbsolutePath());
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        XabslParser parser = new XabslParser(tokenStream);

        CommonTree tree = (CommonTree) parser.xabsl().getTree();
        CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
        PrepareIC prepare = new PrepareIC(nodes);
        
        try
        {
          tree = (CommonTree) prepare.xabsl().getTree();
        }
        catch(RecognitionException ex)
        {
          Helper.handleException(ex);
          return;
        }

        //System.err.println(tree.toStringTree());

        nodes = new CommonTreeNodeStream(tree);
        IC ic = new IC(nodes);

        ic.outputFilename = fout.getAbsolutePath();
        ic.title = lexer.title;
        ic.symbols = parser.symbols;
        ic.enumNames = parser.enumNames;
        ic.enumValues = parser.enumValues;
        ic.parameterSymbols = parser.parameterSymbols;
        ic.parameterEnumNames = parser.parameterEnumNames;

        try
        {
          ic.xabsl();
          JOptionPane.showMessageDialog(this, "Intermediate code successfully " +
            "compiled and saved.");
        }
        catch(RecognitionException e)
        {
          Helper.handleException(e);
          return;
        }

      }
      catch(Exception ex)
      {
        Helper.handleException(ex);
      }

    }//GEN-LAST:event_compileAction

  /**
   * @param args the command line arguments
   */
  public static void main(String args[])
  {
    java.awt.EventQueue.invokeLater(new Runnable()
    {

      public void run()
      {
        new Main().setVisible(true);
      }
    });
  }

  private String readFileToString(File file) throws IOException
  {
    FileReader reader = new FileReader(file);
    StringBuilder buffer = new StringBuilder();

    int c = reader.read();
    while(c != -1)
    {
      buffer.append((char) c);
      c = reader.read();
    }//end while

    reader.close();
    return buffer.toString();
  }//end readFileToString

  private File saveStringToFile(File selectedFile, String text) throws Exception
  {
    if(selectedFile == null)
    {
      fileChooser.setFileFilter(xabslFilter);
      int result = fileChooser.showSaveDialog(this);
      if(JFileChooser.APPROVE_OPTION != result)
      {
        return null;
      }
      selectedFile = fileChooser.getSelectedFile();
      selectedFile = validateFileName(selectedFile, fileChooser.getFileFilter());
    }

    if(selectedFile == null)
    {
      return null;
    }

    FileWriter writer = new FileWriter(selectedFile);
    writer.write(text);
    writer.close();

    return selectedFile;
  }//end saveStringToFile

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton btCompile;
  private javax.swing.JButton btNew;
  private javax.swing.JButton btOpen;
  private javax.swing.JButton btSave;
  private javax.swing.JSeparator jSeparator1;
  private javax.swing.JSeparator jSeparator2;
  private javax.swing.JSplitPane jSplitPane;
  private javax.swing.JTabbedPane jTabbedPane;
  private javax.swing.JMenu mEdit;
  private javax.swing.JMenu mFile;
  private javax.swing.JMenu mHelp;
  private javax.swing.JMenuBar mbMain;
  private javax.swing.JMenuItem miClose;
  private javax.swing.JMenuItem miCompile;
  private javax.swing.JMenuItem miInfo;
  private javax.swing.JMenuItem miNew;
  private javax.swing.JMenuItem miOpen;
  private javax.swing.JMenuItem miOption;
  private javax.swing.JMenuItem miQuit;
  private javax.swing.JMenuItem miRefreshGraph;
  private javax.swing.JMenuItem miSave;
  private javax.swing.JMenuItem miSaveAs;
  private javax.swing.JToolBar.Separator seperator1;
  private javax.swing.JToolBar toolbarMain;
  private de.hu_berlin.informatik.ki.jxabsleditor.graphpanel.XGraph xGraph;
  // End of variables declaration//GEN-END:variables

  private class XABSLFileFilter extends javax.swing.filechooser.FileFilter
  {

    public boolean accept(File file)
    {
      if(file.isDirectory())
      {
        return true;
      }
      String filename = file.getName();

      return filename.endsWith(".xabsl") || filename.endsWith(".XABSL");
    }

    public String getDescription()
    {
      return "Extensible Agent Behavior Language (*.xabsl)";
    }

    @Override
    public String toString()
    {
      return "xabsl";
    }
  }//end class XABSLFileFilter

  private class DotFileFilter extends javax.swing.filechooser.FileFilter
  {

    public boolean accept(File file)
    {
      if(file.isDirectory())
      {
        return true;
      }
      String filename = file.getName();
      return filename.endsWith(".dot") || filename.endsWith(".DOT");
    }

    public String getDescription()
    {
      return "DOT (*.dot)";
    }

    @Override
    public String toString()
    {
      return "dot";
    }
  }//end class DotFileFilter

  private File validateFileName(final File file, final javax.swing.filechooser.FileFilter filter)
  {
    if(filter.accept(file))
    {
      return file;
    }
    // remove wrong file extension if any
    String fileName = file.getName();
    final int index = fileName.lastIndexOf(".");
    if(index > 0)
    {
      fileName = fileName.substring(0, index);
    }

    final String extension = filter.toString();
    final String newFileName = fileName + "." + extension;

    final File newFile = new File(file.getParent(), newFileName);

    return newFile;
  }//end validateFileName

  
  private void saveConfiguration()
  {
    try
    {
      configuration.store(new FileWriter(fConfig), "JXabslEditor configuration");
    }
    catch(IOException ex)
    {
      handleException(ex);
    }

  }//end saveConfiguration

  public static void handleException(Exception ex)
  {
    // log
    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
    // show
    ExceptionDialog dlg = new ExceptionDialog(null, ex);
    dlg.setVisible(true);
  }

  class MyGrappaListener implements GrappaListener
  {

    public void grappaClicked(Subgraph arg0, Element arg1, GrappaPoint arg2, int arg3, int arg4, GrappaPanel arg5)
    {
    }

    public void grappaPressed(Subgraph arg0, Element arg1, GrappaPoint arg2, int arg3, GrappaPanel arg4)
    {
      if(arg1 != null)
      {
        Attribute url = arg1.getAttribute("URL");
        if(url != null)
        {
          System.out.println(arg1.getName() + " " + url.getStringValue());
          XEditorPanel editor = ((XEditorPanel) jTabbedPane.getSelectedComponent());
          editor.setCarretPosition(Integer.parseInt(url.getStringValue()));
        }
      }//end if
    }//end grappaPressed

    public void grappaReleased(Subgraph arg0, Element arg1, GrappaPoint arg2, int arg3, Element arg4, GrappaPoint arg5, int arg6, GrappaBox arg7, GrappaPanel arg8)
    {
    }

    public void grappaDragged(Subgraph arg0, GrappaPoint arg1, int arg2, Element arg3, GrappaPoint arg4, int arg5, GrappaBox arg6, GrappaPanel arg7)
    {
    }

    public String grappaTip(Subgraph arg0, Element arg1, GrappaPoint arg2, int arg3, GrappaPanel arg4)
    {
      return "test";
    }
  }//end class MyGrappaListener
}
