/*
 * Main.java
 *
 * Created on 08.01.2009, 01:03:25
 */
package de.hu_berlin.informatik.ki.jxabsleditor;

import de.hu_berlin.informatik.ki.jxabsleditor.compilerconnection.CompilationFinishedReceiver;
import de.hu_berlin.informatik.ki.jxabsleditor.compilerconnection.CompileResult;
import de.hu_berlin.informatik.ki.jxabsleditor.compilerconnection.CompilerDialog;
import de.hu_berlin.informatik.ki.jxabsleditor.editorpanel.DocumentChangedListener;
import de.hu_berlin.informatik.ki.jxabsleditor.editorpanel.XEditorPanel;
import de.hu_berlin.informatik.ki.jxabsleditor.graphpanel.GraphVisualizer;
import de.hu_berlin.informatik.ki.jxabsleditor.parser.XParser;
import de.hu_berlin.informatik.ki.jxabsleditor.parser.XabslNode;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 *
 * @author Heinrich Mellmann
 */
public class Main extends javax.swing.JFrame implements CompilationFinishedReceiver
{

  private JFileChooser fileChooser = new JFileChooser();
  private Properties configuration = new Properties();
  private File fConfig;
  private FileFilter dotFilter = new DotFileFilter();
  private FileFilter xabslFilter = new XABSLFileFilter();
  private FileFilter icFilter = new FileNameExtensionFilter("Intermediate code (*.dat)", "dat");
  private HashMap<String, Component> openDocumentsMap;
  private HashMap<String, File> optionPathMap;
  private String defaultCompilationPath = null;
  private GraphVisualizer optionVisualizer;
  private boolean splitterManuallySet = false;
  private boolean ignoreSplitterMovedEvent = false;

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

    // icon
    Image icon = Toolkit.getDefaultToolkit().getImage(
      this.getClass().getResource("res/XabslEditor.png"));
    setIconImage(icon);

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

    this.openDocumentsMap = new HashMap<String, Component>();
    this.optionPathMap = new HashMap<String, File>();

    optionVisualizer = new GraphVisualizer();

    optionVisualizer.setGraphMouseListener(new GraphMouseListener<XabslNode>()
    {

      public void graphClicked(XabslNode v, MouseEvent me)
      {
        XEditorPanel editor = ((XEditorPanel) tabbedPanelEditor.getSelectedComponent());
        if(editor != null && v.getPosInText() > -1)
        {
          editor.setCarretPosition(v.getPosInText());
        }
      }

      public void graphPressed(XabslNode v, MouseEvent me)
      {
      }

      public void graphReleased(XabslNode v, MouseEvent me)
      {
      }
    });

    panelOption.add(optionVisualizer, BorderLayout.CENTER);

  }

  private void createOptionList(File folder)
  {
    File[] fileList = folder.listFiles();
    for(File file : fileList)
    {
      if(file.isDirectory())
      {
        createOptionList(file);
      }
      else if(file.getName().toLowerCase().endsWith(".xabsl"))
      {
        String name = file.getName().toLowerCase().replace(".xabsl", "");
        optionPathMap.put(name, file);
      //System.out.println(name + " : " + file.getAbsolutePath());
      }
    }//end for
  }//end createOptionList

  private void loadConfiguration()
  {
    if(configuration.containsKey("lastOpenedFolder"))
    {
      fileChooser.setCurrentDirectory(
        new File(configuration.getProperty("lastOpenedFolder")));
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
    tabbedPanelEditor = new javax.swing.JTabbedPane();
    tabbedPanelView = new javax.swing.JTabbedPane();
    panelOption = new javax.swing.JPanel();
    panelCompiler = new javax.swing.JPanel();
    scrollPaneCompilerOutput = new javax.swing.JScrollPane();
    txtCompilerOutput = new javax.swing.JTextArea();
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
    miSearch = new javax.swing.JMenuItem();
    jSeparator4 = new javax.swing.JSeparator();
    miCompile = new javax.swing.JMenuItem();
    miRefreshGraph = new javax.swing.JMenuItem();
    jSeparator3 = new javax.swing.JSeparator();
    miOption = new javax.swing.JMenuItem();
    mHelp = new javax.swing.JMenu();
    miInfo = new javax.swing.JMenuItem();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setTitle("XABSL Editor");
    setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    setLocationByPlatform(true);
    addComponentListener(new java.awt.event.ComponentAdapter() {
      public void componentResized(java.awt.event.ComponentEvent evt) {
        formComponentResized(evt);
      }
    });

    jSplitPane.setDividerLocation(450);
    jSplitPane.setPreferredSize(new java.awt.Dimension(750, 600));
    jSplitPane.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
      public void propertyChange(java.beans.PropertyChangeEvent evt) {
        jSplitPanePropertyChange(evt);
      }
    });

    tabbedPanelEditor.setAutoscrolls(true);
    jSplitPane.setLeftComponent(tabbedPanelEditor);

    panelOption.setLayout(new java.awt.BorderLayout());
    tabbedPanelView.addTab("Option", panelOption);

    txtCompilerOutput.setColumns(20);
    txtCompilerOutput.setEditable(false);
    txtCompilerOutput.setRows(5);
    scrollPaneCompilerOutput.setViewportView(txtCompilerOutput);

    javax.swing.GroupLayout panelCompilerLayout = new javax.swing.GroupLayout(panelCompiler);
    panelCompiler.setLayout(panelCompilerLayout);
    panelCompilerLayout.setHorizontalGroup(
      panelCompilerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(scrollPaneCompilerOutput, javax.swing.GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE)
    );
    panelCompilerLayout.setVerticalGroup(
      panelCompilerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(scrollPaneCompilerOutput, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
    );

    tabbedPanelView.addTab("Compiler", null, panelCompiler, "The status and output of the compiler.");

    tabbedPanelView.setSelectedComponent(panelOption);

    jSplitPane.setRightComponent(tabbedPanelView);

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
    miClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/hu_berlin/informatik/ki/jxabsleditor/res/fileclose16.png"))); // NOI18N
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
    miSaveAs.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/hu_berlin/informatik/ki/jxabsleditor/res/filesaveas16.png"))); // NOI18N
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
    miQuit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/hu_berlin/informatik/ki/jxabsleditor/res/exit16.png"))); // NOI18N
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

    miSearch.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
    miSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/hu_berlin/informatik/ki/jxabsleditor/res/search16.png"))); // NOI18N
    miSearch.setMnemonic('S');
    miSearch.setText("Search");
    miSearch.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        miSearchActionPerformed(evt);
      }
    });
    mEdit.add(miSearch);
    mEdit.add(jSeparator4);

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
    mEdit.add(jSeparator3);

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

    miInfo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/hu_berlin/informatik/ki/jxabsleditor/res/info16.png"))); // NOI18N
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
    createDocumentTab(null);
}//GEN-LAST:event_newFileAction

    private void miCloseActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_miCloseActionPerformed
    {//GEN-HEADEREND:event_miCloseActionPerformed
      // close current tab

      XEditorPanel editor = ((XEditorPanel) tabbedPanelEditor.getSelectedComponent());
      if(!editor.isChanged())
      {
        tabbedPanelEditor.remove(editor);
        return;
      }


      int result = JOptionPane.showConfirmDialog(this, "Save changes?", "File was modified.",
        JOptionPane.YES_NO_CANCEL_OPTION);

      if(result == JOptionPane.CANCEL_OPTION)
      {
        return;
      }
      else if(result == JOptionPane.NO_OPTION)
      {
        tabbedPanelEditor.remove(editor);
        return;
      }



      String text = editor.getText();
      File selectedFile = editor.getFile();
      try
      {
        File file = saveStringToFile(selectedFile, text);
        if(file != null)
        {
          editor.setChanged(false);
          editor.setFile(file);
          tabbedPanelEditor.setTitleAt(tabbedPanelEditor.getSelectedIndex(), file.getName());
        }//end if
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

      if(!editor.isChanged())
      {
        tabbedPanelEditor.remove(editor);
      }//end if
}//GEN-LAST:event_miCloseActionPerformed

    private void saveFileAction(java.awt.event.ActionEvent evt)//GEN-FIRST:event_saveFileAction
    {//GEN-HEADEREND:event_saveFileAction

      XEditorPanel editor = ((XEditorPanel) tabbedPanelEditor.getSelectedComponent());
      String text = editor.getText();
      File selectedFile = editor.getFile();

      try
      {
        File file = saveStringToFile(selectedFile, text);
        if(file != null)
        {
          editor.setChanged(false);
          editor.setFile(file);
          tabbedPanelEditor.setTitleAt(tabbedPanelEditor.getSelectedIndex(), file.getName());
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
      String text = ((XEditorPanel) tabbedPanelEditor.getSelectedComponent()).getText();

      // JUNG
      XParser p = new XParser();
      p.parse(new StringReader(text));
      optionVisualizer.setGraph(p.getVisualizerGraph());

}//GEN-LAST:event_miRefreshGraphActionPerformed

    private void miSaveAsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_miSaveAsActionPerformed
    {//GEN-HEADEREND:event_miSaveAsActionPerformed
      XEditorPanel editor = ((XEditorPanel) tabbedPanelEditor.getSelectedComponent());
      String text = editor.getText();

      try
      {
        // save as a new file
        File file = saveStringToFile(null, text);
        if(file != null)
        {
          int idx = tabbedPanelEditor.getSelectedIndex();
          editor.setChanged(false);
          editor.setFile(file);
          tabbedPanelEditor.setTitleAt(idx, file.getName());
          tabbedPanelEditor.setToolTipTextAt(idx, file.getAbsolutePath());
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

      // test if the file is allready opened
      for(int i = 0; i < tabbedPanelEditor.getTabCount(); i++)
      {
        Component c = tabbedPanelEditor.getComponentAt(i);
        if(((XEditorPanel) c).getFile().compareTo(selectedFile) == 0)
        {
          tabbedPanelEditor.setSelectedComponent(c);
          return;
        }//end if
      }//end for

      configuration.setProperty("lastOpenedFolder",
        fileChooser.getCurrentDirectory().getAbsolutePath());
      saveConfiguration();

      // TODO: make it better
      this.optionPathMap.clear();
      File agentsFile = Helper.getAgentFileForOption(selectedFile);
      createOptionList(agentsFile.getParentFile());

      createDocumentTab(selectedFile);
    }//GEN-LAST:event_openFileAction

    private void compileAction(java.awt.event.ActionEvent evt)//GEN-FIRST:event_compileAction
    {//GEN-HEADEREND:event_compileAction

      XEditorPanel editor = ((XEditorPanel) tabbedPanelEditor.getSelectedComponent());
      File optionFile = editor.getFile();

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
      }
      else
      {
        fout = new File(defaultCompilationPath + "/behavior-ic.dat");
      }

      if(fout == null)
      {
        JOptionPane.showMessageDialog(this, "No file selected");
        return;
      }
      else if(fout.exists())
      {
        if(!fout.delete())
        {
          JOptionPane.showMessageDialog(this, "Can not overwrite the file " +
            fout.getAbsolutePath());
          return;
        }
      }

      CompilerDialog frame = new CompilerDialog(this, true, optionFile, fout, this);
      frame.setVisible(true);
    }//GEN-LAST:event_compileAction

    private void miSearchActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_miSearchActionPerformed
    {//GEN-HEADEREND:event_miSearchActionPerformed
      
      if(tabbedPanelEditor.getSelectedComponent() != null)
      {
        XEditorPanel editor = ((XEditorPanel) tabbedPanelEditor.getSelectedComponent());
        editor.getSearchPanel().setVisible(false);
        editor.getSearchPanel().setVisible(true);

      }
    }//GEN-LAST:event_miSearchActionPerformed

    private void formComponentResized(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_formComponentResized
    {//GEN-HEADEREND:event_formComponentResized
      
      if(!splitterManuallySet)
      {
        // position splitter in the middle
        ignoreSplitterMovedEvent = true;
        jSplitPane.setDividerLocation(this.getWidth() / 2);
        ignoreSplitterMovedEvent = false;
      }
    }//GEN-LAST:event_formComponentResized

    private void jSplitPanePropertyChange(java.beans.PropertyChangeEvent evt)//GEN-FIRST:event_jSplitPanePropertyChange
    {//GEN-HEADEREND:event_jSplitPanePropertyChange

      if(evt.getPropertyName().equals("dividerLocation"))
      {
        if(!ignoreSplitterMovedEvent)
        {
          splitterManuallySet = true;
        }
      }

    }//GEN-LAST:event_jSplitPanePropertyChange

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

  private void createDocumentTab(File file)
  {
    try
    {
      // create new document
      XEditorPanel editor = null;
      if(file == null)
      {
        editor = new XEditorPanel();
        int tabCount = tabbedPanelEditor.getTabCount();
        tabbedPanelEditor.addTab("New " + tabCount, editor);
      }
      else
      {
        String content = readFileToString(file);
        editor = new XEditorPanel(content);
        editor.setFile(file);
        // create a tab
        tabbedPanelEditor.addTab(editor.getFile().getName(), null, editor, file.getAbsolutePath());
      }

      tabbedPanelEditor.setSelectedComponent(editor);

      editor.addDocumentChangedListener(new DocumentChangedListener()
      {

        public void documentChanged(XEditorPanel document)
        {
          tabbedPanelEditor.setSelectedComponent(document);
          int i = tabbedPanelEditor.getSelectedIndex();
          if(document.isChanged())
          {
            String title = tabbedPanelEditor.getTitleAt(i) + " *";
            tabbedPanelEditor.setTitleAt(i, title);
          }//end if
        }
      });

      editor.addHyperlinkListener(new HyperlinkListener()
      {

        public void hyperlinkUpdate(HyperlinkEvent e)
        {
          String option = e.getDescription();
          option = option.replace("no protocol: ", "");
          File file = optionPathMap.get(option);

          if(file != null)
          {
            createDocumentTab(file);
          }
        //System.out.println(option);
        }
      });
    }
    catch(Exception e)
    {
      JOptionPane.showMessageDialog(this,
        e.toString(), "The file could not be read.", JOptionPane.ERROR_MESSAGE);
    }

  }//end createDocumentTab

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
  private javax.swing.JSeparator jSeparator3;
  private javax.swing.JSeparator jSeparator4;
  private javax.swing.JSplitPane jSplitPane;
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
  private javax.swing.JMenuItem miSearch;
  private javax.swing.JPanel panelCompiler;
  private javax.swing.JPanel panelOption;
  private javax.swing.JScrollPane scrollPaneCompilerOutput;
  private javax.swing.JToolBar.Separator seperator1;
  private javax.swing.JTabbedPane tabbedPanelEditor;
  private javax.swing.JTabbedPane tabbedPanelView;
  private javax.swing.JToolBar toolbarMain;
  private javax.swing.JTextArea txtCompilerOutput;
  // End of variables declaration//GEN-END:variables

  public void compilationFinished(CompileResult result)
  {
    txtCompilerOutput.setText(result.messages);
    if(result.errors || result.warnings)
    {
      tabbedPanelView.setSelectedIndex(tabbedPanelView.getTabCount()-1);
    }
  }
  // End of variables declaration

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

  class XABSLErrorOutputStream extends OutputStream
  {

    private StringBuffer messageBuffer = new StringBuffer();

    @Override
    public void write(int b) throws IOException
    {
      messageBuffer.append((char) b);
    }

    public String getMessage()
    {
      return messageBuffer.toString();
    }//end getMessage
    public String fileName;
    int row;
    int col;
    String message;

    public void parseMessage()
    {
      String str = messageBuffer.toString();
      str = str.replaceAll("\\(|(\\) : )|,", ";");
      String[] splStr = str.split(";");
      if(splStr.length == 4)
      {
        fileName = splStr[0];
        row = Integer.parseInt(splStr[1]);
        col = Integer.parseInt(splStr[2]);
        message = splStr[3];
      }
    }//end parseMessage
  }//end class XABSLErrorOutputStream
}
