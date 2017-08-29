/*
 * Copyright 2009 NaoTeam Humboldt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.naoth.xabsleditor;

import de.naoth.xabsleditor.compilerconnection.CompilationFinishedReceiver;
import de.naoth.xabsleditor.compilerconnection.CompileResult;
import de.naoth.xabsleditor.compilerconnection.CompilerDialog;
import de.naoth.xabsleditor.compilerconnection.CompilerOutputPanel.JumpListener;
import de.naoth.xabsleditor.compilerconnection.CompilerOutputPanel.JumpTarget;
import de.naoth.xabsleditor.editorpanel.EditorPanel;
import de.naoth.xabsleditor.editorpanel.EditorPanelTab;
import de.naoth.xabsleditor.parser.XABSLContext;
import de.naoth.xabsleditor.utils.DotFileFilter;
import de.naoth.xabsleditor.utils.XABSLFileFilter;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author Heinrich Mellmann
 */
public class Main extends javax.swing.JFrame implements CompilationFinishedReceiver, JumpListener
{

  private JFileChooser fileChooser = new JFileChooser();
  private SearchInProjectDialog searchInProjectDialog;
  
  private Properties configuration = new Properties();
  private File fConfig;
  private FileFilter dotFilter = new DotFileFilter();
  private FileFilter xabslFilter = new XABSLFileFilter();
  private FileFilter icFilter = new FileNameExtensionFilter("Intermediate code (*.dat)", "dat");

  private String defaultCompilationPath = null;
  private boolean splitterManuallySet = false;
  private boolean ignoreSplitterMovedEvent = false;

  private HelpDialog helpDialog = null;

  // xabsl files icons
  private final ImageIcon icon_xabsl_agent =
      new ImageIcon(this.getClass().getResource("res/xabsl_agents_file.png"));
  private final ImageIcon icon_xabsl_option =
      new ImageIcon(this.getClass().getResource("res/xabsl_option_file.png"));
  private final ImageIcon icon_xabsl_symbol =
      new ImageIcon(this.getClass().getResource("res/xabsl_symbols_file.png"));
  

  /** Creates new form Main */
  public Main(String file)
  {
    
    // no bold fonts please
    UIManager.put("swing.boldMetal", Boolean.FALSE);
    try
    {
      //UIManager.setLookAndFeel(new MetalLookAndFeel());
      UIManager.setLookAndFeel(new NimbusLookAndFeel());
    }
    catch (UnsupportedLookAndFeelException ex)
    {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
    }

    initComponents();

    graphPanel.addJumpListener(this);
    graphPanel.setEditor(editorPanel);
    editorPanel.setGraph(graphPanel);
    
    addWindowListener(new ShutdownHook());

    // icon
    Image icon = Toolkit.getDefaultToolkit().getImage(
      this.getClass().getResource("res/XabslEditor.png"));
    setIconImage(icon);

    searchInProjectDialog = new SearchInProjectDialog(this, false);

    // load configuration
    fConfig = new File(System.getProperty("user.home") + "/.jxabsleditor");

    try
    {
      if (fConfig.exists() && fConfig.canRead())
      {
        configuration.load(new FileReader(fConfig));
      }
    }
    catch (Exception ex)
    {
      Tools.handleException(ex);
    }

    fileChooser = new JFileChooser();
    fileChooser.setFileFilter(xabslFilter);
    fileChooser.setFileFilter(icFilter);
    fileChooser.setFileFilter(dotFilter);
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    fileChooser.setAcceptAllFileFilterUsed(true);

    loadConfiguration();
    
    String open = configuration.getProperty(OptionsDialog.OPEN_LAST,"");
    if(open.equals(OptionsDialog.OPEN_LAST_OPTIONS[1])) {
        // try to open last agent
        File laf = new File(configuration.getProperty(OptionsDialog.OPEN_LAST_VALUES[0], ""));
        if(laf.exists()) {
            editorPanel.openFile(laf);
            updateProjectDirectoryMenu();
        }
    } else if(open.equals(OptionsDialog.OPEN_LAST_OPTIONS[2])) {
        // try to open the last opened files
        String[] strFiles = configuration.getProperty(OptionsDialog.OPEN_LAST_VALUES[1], "").split("\\|");
        for (String strFile : strFiles) {
            File f = new File(strFile);
            if(f.exists()) {
                editorPanel.openFile(f);
            }
        }
        updateProjectDirectoryMenu();
    }
    
    String start = configuration.getProperty(OptionsDialog.START_POSITION,"");
    if(start.equals(OptionsDialog.START_POSITION_OPTIONS[1])) {
        // open window with last postion and size
        Dimension s = getPreferredSize();
        setLocation(
            Integer.parseInt(configuration.getProperty(OptionsDialog.START_POSITION_VALUE[0], String.valueOf(getX()))),
            Integer.parseInt(configuration.getProperty(OptionsDialog.START_POSITION_VALUE[1], String.valueOf(getY())))
        );
        setSize(
            Integer.parseInt(configuration.getProperty(OptionsDialog.START_POSITION_VALUE[2], String.valueOf(s.width))),
            Integer.parseInt(configuration.getProperty(OptionsDialog.START_POSITION_VALUE[3], String.valueOf(s.height)))
        );
    } else if(start.equals(OptionsDialog.START_POSITION_OPTIONS[2])) {
        // open window maximized
        setExtendedState(getExtendedState() | javax.swing.JFrame.MAXIMIZED_BOTH);
    }
    // set the divider location from the config
    if(configuration.getProperty("dividerPostionOne")!=null) {
        jSplitPaneMain.setDividerLocation(Integer.parseInt(configuration.getProperty("dividerPostionOne")));
    }
    if(configuration.getProperty("dividerPostionTwo")!=null) {
        jSplitPane.setDividerLocation(Integer.parseInt(configuration.getProperty("dividerPostionTwo")));
    }
    
    // set the cell renderer for the projects treeview
    fileTree.setCellRenderer(new DefaultTreeCellRenderer(){
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            if(value instanceof DefaultMutableTreeNode && ((DefaultMutableTreeNode)value).getUserObject() instanceof File) {
                File file = (File)((DefaultMutableTreeNode)value).getUserObject();
                if(file.isDirectory()) {
                    value = file.getName();
                } else {
                    value = file.getName().substring(0, file.getName().length()-6);
                }
            }
            Component c = super.getTreeCellRendererComponent(tree, value, leaf, expanded, leaf, row, hasFocus);
            c.setForeground(Color.BLACK);
            return c;
        }
    });
  }//end Main

  /** Reconstruct the Projects menu entry */
  /*
  private void updateProjectMenu()
  {
    mProject.removeAll();
    fileTree.removeAll();

    // get all opened agents
    TreeSet<File> foundAgents = new TreeSet<File>();
    for (int i = 0; i < tabbedPanelEditor.getTabCount(); i++)
    {
      XEditorPanel p = (XEditorPanel) tabbedPanelEditor.getComponentAt(i);
      File agentFile = file2Agent.get(p.getFile());
      if (agentFile != null && !foundAgents.contains(agentFile))
      {

        JMenu miAgent = new JMenu(agentFile.getParentFile().getName() + "/" + agentFile.getName());
        XABSLContext context = p.getXABSLContext();

        final Map<String, File> optionPathMap = context.getOptionPathMap();
        Map<String,JMenu> menuSubs = new TreeMap<String, JMenu>();

        for (final String option : optionPathMap.keySet())
        {
          String subCategory = option.substring(0,1).toUpperCase();
          if(menuSubs.get(subCategory) == null)
          {
            menuSubs.put(subCategory, new JMenu(subCategory));
          }

          JMenuItem miOptionOpener = new JMenuItem(option);
          menuSubs.get(subCategory).add(miOptionOpener);

          miOptionOpener.addActionListener(new ActionListener()
          {

            @Override
            public void actionPerformed(ActionEvent e)
            {
              File f = optionPathMap.get(option);
              openFile(f);
            }
          });
        }

        DefaultMutableTreeNode curDir = new DefaultMutableTreeNode(agentFile.getParentFile().getName() + "/" + agentFile.getName());
        for(String s : menuSubs.keySet())
        {
          curDir.add(new DefaultMutableTreeNode(menuSubs.get(s)));
        }
        
        for(String s : menuSubs.keySet())
        {
          miAgent.add(menuSubs.get(s));
        }

        fileTree.setModel(new DefaultTreeModel(curDir));
        mProject.add(miAgent);

        foundAgents.add(agentFile);
      }
    }
  }//end updateProjectMenu
*/

  private void addFilesToMenu(JMenu miParent, File folder, final XABSLContext context)
  {
    if(miParent == null || folder == null || context == null)
        return;

    final String XABSL_FILE_ENDING = ".xabsl";

    File[] fileList = folder.listFiles();
    for (final File file : fileList)
    {
      if (file.isDirectory())
      {
        JMenu miChild = new JMenu(file.getName());
        addFilesToMenu(miChild, file, context);
        if(miChild.getMenuComponentCount() > 0) {
          miParent.add(miChild);
        }
      }
      else if (file.getName().toLowerCase().endsWith(XABSL_FILE_ENDING))
      {
        // remove the file ending
        int dotIndex = file.getName().length() - XABSL_FILE_ENDING.length();
        final String name = file.getName().substring(0, dotIndex);

        if(!context.getOptionPathMap().containsKey(name)) {
          continue;
        }

        // create new item
        JMenuItem miOptionOpener = setJMenuItemXabslFont(new JMenuItem(name));
      
        // agent, option or symbol file
        String type = context.getFileTypeMap().get(name);
        if(type != null)
        {
          if(type.equals("option")) {
            miOptionOpener.setIcon(icon_xabsl_option);
          } else if(type.equals("symbol")) {
            miOptionOpener.setIcon(icon_xabsl_symbol);
          } else if(type.equals("agent")) {
            miOptionOpener.setIcon(icon_xabsl_agent);
          }
        }//end if

        miOptionOpener.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            editorPanel.openFile(context.getOptionPathMap().get(name));
          }
        });
        miParent.add(miOptionOpener);
      }
    }//end for
  }//end addFilesToMenu

  private void addFilesToTree(DefaultMutableTreeNode nodeParent, File folder, final XABSLContext context)
  {
    if(nodeParent == null || folder == null || context == null) {
        return;
    }

    final String XABSL_FILE_ENDING = ".xabsl";

    ArrayList<DefaultMutableTreeNode> childDirectories = new ArrayList<DefaultMutableTreeNode>();
    ArrayList<DefaultMutableTreeNode> childFiles = new ArrayList<DefaultMutableTreeNode>();
    
    File[] fileList = folder.listFiles();
    for (final File file : fileList)
    {
      if (file.isDirectory())
      {
        DefaultMutableTreeNode nodeDirectory = new DefaultMutableTreeNode(file);
        addFilesToTree(nodeDirectory, file, context);

        if(nodeDirectory.getChildCount() > 0) {
          //nodeParent.add(nodeDirectory);
          childDirectories.add(nodeDirectory);
        }
      }
      else if (file.getName().toLowerCase().endsWith(XABSL_FILE_ENDING))
      {
        // remove the file ending
        int dotIndex = file.getName().length() - XABSL_FILE_ENDING.length();
        final String name = file.getName().substring(0, dotIndex);

        if(!context.getOptionPathMap().containsKey(name)) {
          continue;
        }

        // create new item
        DefaultMutableTreeNode nodeChild = new DefaultMutableTreeNode(file);
        
        // agent, option or symbol file
        /*
        String type = context.getFileTypeMap().get(name);
        if(type != null)
        {
          if(type.equals("option")) {
            nodeChild.setIcon(icon_xabsl_option);
          } else if(type.equals("symbol")) {
            nodeChild.setIcon(icon_xabsl_symbol);
          } else if(type.equals("agent")) {
            nodeChild.setIcon(icon_xabsl_agent);
          }
        }//end if
        */

        //nodeParent.add(nodeChild);
        childFiles.add(nodeChild);
      }
    }//end for
    
    // added directories first
    for(DefaultMutableTreeNode n: childDirectories) {
        nodeParent.add(n);
    }
    for(DefaultMutableTreeNode n: childFiles) {
        nodeParent.add(n);
    }
  }

  /** Reconstruct the Projects menu entry */
  TreeSet<File> foundAgents = new TreeSet<File>();
  private void updateProjectDirectoryMenu()
  {
    mProject.removeAll();
    foundAgents.clear();
    
    // get all opened agents
    //TreeSet<File> foundAgents = new TreeSet<File>();
    for (EditorPanelTab tab : editorPanel) {
      File agentFile = tab.getAgent();
      if (agentFile != null && !foundAgents.contains(agentFile))
      {
        JMenu miAgent = new JMenu(agentFile.getParentFile().getName() + "/" + agentFile.getName());
        final XABSLContext context = tab.getXabslContext();

        addFilesToMenu(miAgent, agentFile.getParentFile(), context);
        mProject.add(miAgent);
        
        
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(agentFile.getParentFile().getName() + "/" + agentFile.getName());
        addFilesToTree(root, agentFile.getParentFile(), context);
        fileTree.setModel(new DefaultTreeModel(root));
        fileTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    TreePath selPath = fileTree.getPathForLocation(e.getX(), e.getY());
                    openFileFromTree(selPath);
                }
            }
        });
        /*
        fileTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent tse) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
                if (node == null || !node.isLeaf()) { 
                    return;
                }
                
                String name = (String)node.getUserObject();
                File f = context.getOptionPathMap().get(name);
                openFileDirectly(f);
            }
        });
*/
        foundAgents.add(agentFile);
      }
    }//end for

    if(mProject.getMenuComponentCount() == 0)
    {
      mProject.add(setJMenuItemXabslFont(new JMenuItem("empty")));
    }
  }//end updateProjectMenu

  private JMenuItem setJMenuItemXabslFont(JMenuItem jMenuItem)
  {
    jMenuItem.setFont(jMenuItem.getFont().deriveFont((jMenuItem.getFont().getStyle() | java.awt.Font.ITALIC)));
    return jMenuItem;
  }

  private void loadConfiguration()
  {
    if (configuration.containsKey("lastOpenedFolder"))
    {
      fileChooser.setCurrentDirectory(
        new File(configuration.getProperty("lastOpenedFolder")));
    }

    if (configuration.containsKey(OptionsDialog.DEFAULT_COMPILATION_PATH))
    {
      String path = configuration.getProperty(OptionsDialog.DEFAULT_COMPILATION_PATH);
      if (new File(path).exists())
      {
        this.defaultCompilationPath = path;
      }
    }
    
    // set "tab close button" default to true!
    if(!configuration.containsKey(OptionsDialog.EDITOR_TAB_CLOSE_BTN)) {
      configuration.setProperty(OptionsDialog.EDITOR_TAB_CLOSE_BTN, Boolean.toString(true));
    }
    
    // set tab size from configuration
    editorPanel.setTabSize(Integer.parseInt(configuration.getProperty(OptionsDialog.EDITOR_TAB_SIZE, "2")));
    // TODO: if tab-size changes, update in the editorpanel! (property/bean?!?)
    
    // set, if the tab close button should be shown or not
    editorPanel.setShowCloseButtons(Boolean.parseBoolean(configuration.getProperty(OptionsDialog.EDITOR_TAB_CLOSE_BTN)));
    
  }//end loadConfiguration

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolbarMain = new javax.swing.JToolBar();
        btNew = new javax.swing.JButton();
        btOpen = new javax.swing.JButton();
        btSave = new javax.swing.JButton();
        seperator1 = new javax.swing.JToolBar.Separator();
        btCompile = new javax.swing.JButton();
        jSplitPaneMain = new javax.swing.JSplitPane();
        jScrollPaneFileTree = new javax.swing.JScrollPane();
        fileTree = new javax.swing.JTree();
        jSplitPane = new javax.swing.JSplitPane();
        editorPanel = new de.naoth.xabsleditor.editorpanel.EditorPanel();
        graphPanel = new de.naoth.xabsleditor.graphpanel.GraphPanel();
        mbMain = new javax.swing.JMenuBar();
        mFile = new javax.swing.JMenu();
        miNew = new javax.swing.JMenuItem();
        miOpenFile = new javax.swing.JMenuItem();
        miClose = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        miSave = new javax.swing.JMenuItem();
        miSaveAs = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        miQuit = new javax.swing.JMenuItem();
        mEdit = new javax.swing.JMenu();
        miSearch = new javax.swing.JMenuItem();
        miSearchProject = new javax.swing.JMenuItem();
        miFindUnusedOptions = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        miCompile = new javax.swing.JMenuItem();
        miRefreshGraph = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        miOption = new javax.swing.JMenuItem();
        mProject = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        mHelp = new javax.swing.JMenu();
        miHelp = new javax.swing.JMenuItem();
        miInfo = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("XabslEditor");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setLocationByPlatform(true);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        toolbarMain.setFloatable(false);
        toolbarMain.setRollover(true);

        btNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/naoth/xabsleditor/res/filenew22.png"))); // NOI18N
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

        btOpen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/naoth/xabsleditor/res/fileopen22.png"))); // NOI18N
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

        btSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/naoth/xabsleditor/res/filesave22.png"))); // NOI18N
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

        btCompile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/naoth/xabsleditor/res/compfile22.png"))); // NOI18N
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

        jSplitPaneMain.setDividerLocation(200);
        jSplitPaneMain.setOneTouchExpandable(true);

        jScrollPaneFileTree.setPreferredSize(new java.awt.Dimension(300, 322));

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("<no project>");
        fileTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        fileTree.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                fileTreeKeyReleased(evt);
            }
        });
        jScrollPaneFileTree.setViewportView(fileTree);

        jSplitPaneMain.setLeftComponent(jScrollPaneFileTree);

        jSplitPane.setDividerLocation(450);
        jSplitPane.setOneTouchExpandable(true);
        jSplitPane.setPreferredSize(new java.awt.Dimension(750, 600));
        jSplitPane.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jSplitPanePropertyChange(evt);
            }
        });
        jSplitPane.setLeftComponent(editorPanel);
        jSplitPane.setRightComponent(graphPanel);

        jSplitPaneMain.setRightComponent(jSplitPane);

        getContentPane().add(jSplitPaneMain, java.awt.BorderLayout.CENTER);

        mFile.setMnemonic('F');
        mFile.setText("File");

        miNew.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        miNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/naoth/xabsleditor/res/filenew16.png"))); // NOI18N
        miNew.setMnemonic('N');
        miNew.setText("New");
        miNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newFileAction(evt);
            }
        });
        mFile.add(miNew);

        miOpenFile.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        miOpenFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/naoth/xabsleditor/res/fileopen16.png"))); // NOI18N
        miOpenFile.setMnemonic('O');
        miOpenFile.setText("Open File");
        miOpenFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openFileAction(evt);
            }
        });
        mFile.add(miOpenFile);

        miClose.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.CTRL_MASK));
        miClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/naoth/xabsleditor/res/fileclose16.png"))); // NOI18N
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
        miSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/naoth/xabsleditor/res/filesave16.png"))); // NOI18N
        miSave.setMnemonic('S');
        miSave.setText("Save");
        miSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveFileAction(evt);
            }
        });
        mFile.add(miSave);

        miSaveAs.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        miSaveAs.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/naoth/xabsleditor/res/filesaveas16.png"))); // NOI18N
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
        miQuit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/naoth/xabsleditor/res/exit16.png"))); // NOI18N
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
        miSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/naoth/xabsleditor/res/search16.png"))); // NOI18N
        miSearch.setMnemonic('S');
        miSearch.setText("Search");
        miSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miSearchActionPerformed(evt);
            }
        });
        mEdit.add(miSearch);

        miSearchProject.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        miSearchProject.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/naoth/xabsleditor/res/find.png"))); // NOI18N
        miSearchProject.setText("Search in Project");
        miSearchProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miSearchProjectActionPerformed(evt);
            }
        });
        mEdit.add(miSearchProject);

        miFindUnusedOptions.setText("Search unused options");
        miFindUnusedOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miFindUnusedOptionsActionPerformed(evt);
            }
        });
        mEdit.add(miFindUnusedOptions);
        mEdit.add(jSeparator4);

        miCompile.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.ALT_MASK));
        miCompile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/naoth/xabsleditor/res/compfile16.png"))); // NOI18N
        miCompile.setMnemonic('C');
        miCompile.setText("Compile Behavior");
        miCompile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                compileAction(evt);
            }
        });
        mEdit.add(miCompile);

        miRefreshGraph.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        miRefreshGraph.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/naoth/xabsleditor/res/reload.png"))); // NOI18N
        miRefreshGraph.setMnemonic('R');
        miRefreshGraph.setText("Refresh Graph");
        miRefreshGraph.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miRefreshGraphActionPerformed(evt);
            }
        });
        mEdit.add(miRefreshGraph);
        mEdit.add(jSeparator3);

        miOption.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        miOption.setMnemonic('O');
        miOption.setText("Preferences");
        miOption.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miOptionActionPerformed(evt);
            }
        });
        mEdit.add(miOption);

        mbMain.add(mEdit);

        mProject.setMnemonic('p');
        mProject.setText("Project");

        jMenuItem1.setFont(jMenuItem1.getFont().deriveFont((jMenuItem1.getFont().getStyle() | java.awt.Font.ITALIC)));
        jMenuItem1.setText("empty");
        mProject.add(jMenuItem1);

        mbMain.add(mProject);

        mHelp.setMnemonic('H');
        mHelp.setText("Help");

        miHelp.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.CTRL_MASK));
        miHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/naoth/xabsleditor/res/help.png"))); // NOI18N
        miHelp.setText("Help");
        miHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miHelpActionPerformed(evt);
            }
        });
        mHelp.add(miHelp);

        miInfo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/naoth/xabsleditor/res/info16.png"))); // NOI18N
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
    editorPanel.openFile(null);
}//GEN-LAST:event_newFileAction
  
    private void miCloseActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_miCloseActionPerformed
    {//GEN-HEADEREND:event_miCloseActionPerformed
        editorPanel.closeActiveTab(false);
}//GEN-LAST:event_miCloseActionPerformed

    private void saveFileAction(java.awt.event.ActionEvent evt)//GEN-FIRST:event_saveFileAction
    {//GEN-HEADEREND:event_saveFileAction
      editorPanel.save();
      updateProjectDirectoryMenu();
}//GEN-LAST:event_saveFileAction

    private void miRefreshGraphActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_miRefreshGraphActionPerformed
    {//GEN-HEADEREND:event_miRefreshGraphActionPerformed
      graphPanel.refreshGraph();
}//GEN-LAST:event_miRefreshGraphActionPerformed

    private void miSaveAsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_miSaveAsActionPerformed
    {//GEN-HEADEREND:event_miSaveAsActionPerformed
      editorPanel.saveAs();
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

      AboutDialog dlg = new AboutDialog(this, true);
      Point location = this.getLocation();
      location.translate(100, 100);
      dlg.setLocation(location);
      dlg.setVisible(true);

    }//GEN-LAST:event_miInfoActionPerformed

    private void openFileAction(java.awt.event.ActionEvent evt)//GEN-FIRST:event_openFileAction
    {//GEN-HEADEREND:event_openFileAction
        fileChooser.setFileFilter(xabslFilter);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile != null && selectedFile.exists()) {
                editorPanel.openFile(selectedFile);
                updateProjectDirectoryMenu();
                // update and save configuration
                configuration.setProperty("lastOpenedFolder", fileChooser.getCurrentDirectory().getAbsolutePath());
                saveConfiguration();
            } else {
                JOptionPane.showMessageDialog(this,
                        "File " + selectedFile.getAbsolutePath() + " doesn't exist.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_openFileAction

  private void openFileFromTree(TreePath selPath) {
        if (selPath != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
            if (node == null || !node.isLeaf()) {
                return;
            }
            editorPanel.openFile((File) node.getUserObject());
        }
    }

    private void compileAction(java.awt.event.ActionEvent evt)//GEN-FIRST:event_compileAction
    {//GEN-HEADEREND:event_compileAction
      if (editorPanel.hasOpenFiles()) {
        File optionFile = editorPanel.getActiveFile();

        File fout = null;

        if (defaultCompilationPath == null)
        {
          fileChooser.setFileFilter(icFilter);
          int result = fileChooser.showSaveDialog(this);
          fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
          if (result == JFileChooser.APPROVE_OPTION)
          {
            fout = fileChooser.getSelectedFile();
          }
        }
        else
        {
          fout = new File(defaultCompilationPath + "/behavior-ic.dat");
        }

        if (fout == null)
        {
          JOptionPane.showMessageDialog(this, "No file selected");
          return;
        }
        else if (fout.exists())
        {
          if (!fout.delete())
          {
            JOptionPane.showMessageDialog(this, "Can not overwrite the file "
              + fout.getAbsolutePath());
            return;
          }
        }

        CompilerDialog frame = new CompilerDialog(this, true, optionFile, fout,
          this, configuration);
        frame.setVisible(true);
      }
      else
      {
        JOptionPane.showMessageDialog(this,
          "Please open an *.xabsl file first before compiling", "Error",
          JOptionPane.ERROR_MESSAGE);
      }
    }//GEN-LAST:event_compileAction

    private void miSearchActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_miSearchActionPerformed
    {//GEN-HEADEREND:event_miSearchActionPerformed
      editorPanel.searchInActiveTab();
    }//GEN-LAST:event_miSearchActionPerformed

    private void formComponentResized(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_formComponentResized
    {//GEN-HEADEREND:event_formComponentResized

      if (!splitterManuallySet)
      {
        // position splitter in the middle
        ignoreSplitterMovedEvent = true;
        jSplitPane.setDividerLocation(this.getWidth() / 2);
        ignoreSplitterMovedEvent = false;
      }
    }//GEN-LAST:event_formComponentResized

    private void jSplitPanePropertyChange(java.beans.PropertyChangeEvent evt)//GEN-FIRST:event_jSplitPanePropertyChange
    {//GEN-HEADEREND:event_jSplitPanePropertyChange

      if (evt.getPropertyName().equals("dividerLocation"))
      {
        if (!ignoreSplitterMovedEvent)
        {
          splitterManuallySet = true;
        }
      }

    }//GEN-LAST:event_jSplitPanePropertyChange

    private void miSearchProjectActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_miSearchProjectActionPerformed
    {//GEN-HEADEREND:event_miSearchProjectActionPerformed

      searchInProjectDialog.setVisible(true);

    }//GEN-LAST:event_miSearchProjectActionPerformed

    private void miHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miHelpActionPerformed
    if(this.helpDialog == null)
        this.helpDialog = new HelpDialog(this, false);

    this.helpDialog.setVisible(true);
}//GEN-LAST:event_miHelpActionPerformed

  private void miFindUnusedOptionsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_miFindUnusedOptionsActionPerformed
  {//GEN-HEADEREND:event_miFindUnusedOptionsActionPerformed
    // get selected tab
    if (editorPanel.hasOpenFiles()) {
      new UnusedOptions(this, editorPanel.getActiveXABSLContext()).setVisible(true);
    }
    
  }//GEN-LAST:event_miFindUnusedOptionsActionPerformed

    private void fileTreeKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fileTreeKeyReleased
        if(evt.getKeyCode() == KeyEvent.VK_ENTER) {
            openFileFromTree(fileTree.getSelectionPath());
        }
    }//GEN-LAST:event_fileTreeKeyReleased


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
        new Main(null).setVisible(true);
      }
    });
  }//end main

  @Override
  public void jumpTo(JumpTarget target)
  {
    if(target.getFileName() == null) return;

    final String XABSL_FILE_ENDING = ".xabsl";
    int dotIndex = target.getFileName().length() - XABSL_FILE_ENDING.length();
    String name = target.getFileName().substring(0, dotIndex);

    editorPanel.openFile(getOptionPathMap().get(name));
    updateProjectDirectoryMenu();

    if(editorPanel.hasOpenFiles()) {
        editorPanel.getActiveTab().jumpToLine(target.getLineNumber());
    } else {
      System.err.println("Couldn't jump to taget " + target);
    }
  }//end jumpTo

    public Map<String, File> getOptionPathMap() {
        if (editorPanel.getActiveXABSLContext() != null) {
            return editorPanel.getActiveXABSLContext().getOptionPathMap();
        } else {
            return null;
        }
    }
    
    public EditorPanel getEditorPanel() {
        return editorPanel;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btCompile;
    private javax.swing.JButton btNew;
    private javax.swing.JButton btOpen;
    private javax.swing.JButton btSave;
    private de.naoth.xabsleditor.editorpanel.EditorPanel editorPanel;
    private javax.swing.JTree fileTree;
    private de.naoth.xabsleditor.graphpanel.GraphPanel graphPanel;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JScrollPane jScrollPaneFileTree;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSplitPane jSplitPane;
    private javax.swing.JSplitPane jSplitPaneMain;
    private javax.swing.JMenu mEdit;
    private javax.swing.JMenu mFile;
    private javax.swing.JMenu mHelp;
    private javax.swing.JMenu mProject;
    private javax.swing.JMenuBar mbMain;
    private javax.swing.JMenuItem miClose;
    private javax.swing.JMenuItem miCompile;
    private javax.swing.JMenuItem miFindUnusedOptions;
    private javax.swing.JMenuItem miHelp;
    private javax.swing.JMenuItem miInfo;
    private javax.swing.JMenuItem miNew;
    private javax.swing.JMenuItem miOpenFile;
    private javax.swing.JMenuItem miOption;
    private javax.swing.JMenuItem miQuit;
    private javax.swing.JMenuItem miRefreshGraph;
    private javax.swing.JMenuItem miSave;
    private javax.swing.JMenuItem miSaveAs;
    private javax.swing.JMenuItem miSearch;
    private javax.swing.JMenuItem miSearchProject;
    private javax.swing.JToolBar.Separator seperator1;
    private javax.swing.JToolBar toolbarMain;
    // End of variables declaration//GEN-END:variables

  @Override
  public void compilationFinished(CompileResult result)
  {
    //txtCompilerOutput.setText(result.messages);
    graphPanel.updateCompilerResult(result);
    if (result.errors || result.warnings)
    {
      graphPanel.selectTab("Compiler");
    }
  }//end compilationFinished

  private void saveConfiguration()
  {
    try
    {
      configuration.store(new FileWriter(fConfig), "JXabslEditor configuration");
    }
    catch (IOException ex)
    {
      Tools.handleException(ex);
    }
  }//end saveConfiguration

  // NOTICE: do we need this?!?
  class XABSLErrorOutputStream extends OutputStream
  {

    private StringBuilder messageBuffer = new StringBuilder();

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
      if (splStr.length == 4)
      {
        fileName = splStr[0];
        row = Integer.parseInt(splStr[1]);
        col = Integer.parseInt(splStr[2]);
        message = splStr[3];
      }//end if
    }//end parseMessage
  }//end class XABSLErrorOutputStream
  
  class ShutdownHook extends WindowAdapter
  {
      @Override
      public void windowClosing(WindowEvent e)
      {
        // save "last" opened agent file
        try {
            configuration.setProperty(OptionsDialog.OPEN_LAST_VALUES[0], editorPanel.getActiveAgent().getAbsolutePath());
        } catch(Exception ex) { /* ignore exceptions! */ }
        
        // retrieve all opened files and close tabs
        List<String> openedFiles = editorPanel.closeAllTabs(true).stream().map((t) -> t.getAbsolutePath()).collect(Collectors.toList());
        
        // save opened files to configuration
        configuration.setProperty(OptionsDialog.OPEN_LAST_VALUES[1], openedFiles.stream().collect(Collectors.joining("|")));
        
        // the last position 6 size of the main window should be saved.
        if(OptionsDialog.START_POSITION_OPTIONS[1].equals(configuration.getProperty(OptionsDialog.START_POSITION,""))) {
            configuration.setProperty(OptionsDialog.START_POSITION_VALUE[0], String.valueOf(getX()));       // x coordinate
            configuration.setProperty(OptionsDialog.START_POSITION_VALUE[1], String.valueOf(getY()));       // y coordinate
            configuration.setProperty(OptionsDialog.START_POSITION_VALUE[2], String.valueOf(getWidth()));   // window width
            configuration.setProperty(OptionsDialog.START_POSITION_VALUE[3], String.valueOf(getHeight()));  // window height
        }
        
        // save divider position
        configuration.setProperty("dividerPostionOne", String.valueOf(jSplitPaneMain.getDividerLocation()));
        configuration.setProperty("dividerPostionTwo", String.valueOf(jSplitPane.getDividerLocation()));
        
        saveConfiguration();

        System.exit(0);
      }
  }
}//end class Main

