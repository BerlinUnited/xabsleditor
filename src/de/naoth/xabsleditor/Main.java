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

import de.naoth.xabsleditor.compilerconnection.CompilerOutputPanel.JumpListener;
import de.naoth.xabsleditor.compilerconnection.CompilerOutputPanel.JumpTarget;
import de.naoth.xabsleditor.editorpanel.EditorPanelTab;
import de.naoth.xabsleditor.editorpanel.EditorPanelTabbedPaneUI;
import de.naoth.xabsleditor.events.EventListener;
import de.naoth.xabsleditor.events.EventManager;
import de.naoth.xabsleditor.events.NewFileEvent;
import de.naoth.xabsleditor.events.OpenFileEvent;
import de.naoth.xabsleditor.events.RefreshGraphEvent;
import de.naoth.xabsleditor.events.ReloadProjectEvent;
import de.naoth.xabsleditor.events.RenameFileEvent;
import de.naoth.xabsleditor.events.UpdateProjectEvent;
import de.naoth.xabsleditor.utils.DotFileFilter;
import de.naoth.xabsleditor.utils.FileWatcher;
import de.naoth.xabsleditor.utils.Project;
import de.naoth.xabsleditor.utils.ProjectMenu;
import de.naoth.xabsleditor.utils.XABSLFileFilter;
import de.naoth.xabsleditor.utils.XabslCompiler;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

/**
 *
 * @author Heinrich Mellmann
 */
public class Main extends javax.swing.JFrame implements JumpListener
{
    /** Manager for distributing events. */
    private final EventManager evtManager = EventManager.getInstance();
    /** The Xabsl file extension. */
    public final String XABSL_FILE_ENDING = ".xabsl";
    /** Dialog for searching all projects for a given string. */
    private SearchInProjectDialog searchInProjectDialog;
    /** The user configuration for the editor */
    private Properties configuration = new Properties();
    /** The file where the configuration is saved. */
    private File fConfig;
    /** Default file chooser */
    private JFileChooser fileChooser = new JFileChooser();
    /** A filter for files ending with ".dot" for the file chooser. */
    private FileFilter dotFilter = new DotFileFilter();
    /** A filter for files ending with ".xabsl" for the file chooser. */
    private FileFilter xabslFilter = new XABSLFileFilter();
    /** */
    private boolean splitterManuallySet = false;
    /** */
    private boolean ignoreSplitterMovedEvent = false;
    /** The help dialog for the xabsl editor. */
    private HelpDialog helpDialog = null;
    /** File change watcher */
    private FileWatcher watcher = null;
    /** Stores the currently opened projects. */
    HashMap<String, Project> projects = new HashMap<>();
    /** The xabsl compiler. */
    private final XabslCompiler compiler = new XabslCompiler();

    /**
     * Callback for drag'n'drop xabsl files on the editor.
     */
    public FileDrop.Listener dropHandler = (File[] files) -> {
        ArrayList<String> notaXabslFile = new ArrayList<>();
        // iterate through dropped files and open them - if their xabsl files
        for (File f : files) {
            if (f.getName().toLowerCase().endsWith(XABSL_FILE_ENDING)) {
                evtManager.publish(new OpenFileEvent(this, f));
            } else {
                notaXabslFile.add(f.getAbsolutePath());
            }
        }
        // check if a file couldn't be opened
        if (!notaXabslFile.isEmpty()) {
            JOptionPane.showMessageDialog(rootPane,
                    "The File(s):\n" + notaXabslFile.stream().collect(Collectors.joining(",\n")) + "\naren't XABSL-files.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    };

    /**
     * Creates new form Main
     */
    public Main(String file) {
        // no bold fonts please
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        try {
            //UIManager.setLookAndFeel(new MetalLookAndFeel());
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        // register event handler
        evtManager.add(this);

        // add file drop
        new FileDrop(this, dropHandler);

        // start the file watcher service
        try {
            watcher = new FileWatcher();
            watcher.start();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        initComponents();

        graphPanel.addJumpListener(this);
        editorPanel.setFileWatcher(watcher);

        addWindowListener(new ShutdownHook());

        // icon
        Image icon = Toolkit.getDefaultToolkit().getImage(
                this.getClass().getResource("res/XabslEditor.png"));
        setIconImage(icon);

        searchInProjectDialog = new SearchInProjectDialog(this, false);

        // load configuration
        fConfig = new File(System.getProperty("user.home") + "/.jxabsleditor");

        try {
            if (fConfig.exists() && fConfig.canRead()) {
                configuration.load(new FileReader(fConfig));
            }
        } catch (Exception ex) {
            Tools.handleException(ex);
        }

        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(xabslFilter);
        fileChooser.setFileFilter(dotFilter);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(true);

        loadConfiguration();

        String open = configuration.getProperty(OptionsDialog.OPEN_LAST, "");
        if (open.equals(OptionsDialog.OPEN_LAST_OPTIONS[1])) {
            // try to open last agent
            File laf = new File(configuration.getProperty(OptionsDialog.OPEN_LAST_VALUES[0], ""));
            if (laf.exists()) {
                evtManager.publish(new OpenFileEvent(this, laf));
            }
        } else if (open.equals(OptionsDialog.OPEN_LAST_OPTIONS[2])) {
            // try to open the last opened files
            String[] strFiles = configuration.getProperty(OptionsDialog.OPEN_LAST_VALUES[1], "").split("\\|");
            for (String strFile : strFiles) {
                File f = new File(strFile);
                if (f.exists()) {
                    evtManager.publish(new OpenFileEvent(this, f));
                }
            }
        }

        String start = configuration.getProperty(OptionsDialog.START_POSITION, "");
        if (start.equals(OptionsDialog.START_POSITION_OPTIONS[1])) {
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
        } else if (start.equals(OptionsDialog.START_POSITION_OPTIONS[2])) {
            // open window maximized
            setExtendedState(getExtendedState() | javax.swing.JFrame.MAXIMIZED_BOTH);
        }
        // set the divider location from the config
        if (configuration.getProperty("dividerPostionOne") != null) {
            jSplitPaneMain.setDividerLocation(Integer.parseInt(configuration.getProperty("dividerPostionOne")));
        }
        if (configuration.getProperty("dividerPostionTwo") != null) {
            jSplitPane.setDividerLocation(Integer.parseInt(configuration.getProperty("dividerPostionTwo")));
        }
    }//end Main

    /**
     * Listener for the OpenFileEvent.
     * If a (new) file should be opened, it is first search in the already opened projects.
     * If not found, a new project is loaded.
     * 
     * @param evt 
     */
    @EventListener
    public void openFile(OpenFileEvent evt) {
        Project p = null;
        for (Map.Entry<String, Project> entry : projects.entrySet()) {
            if (entry.getValue().contains(evt.file)) {
                p = entry.getValue();
                break;
            }
        }
        if (p == null) {
            File agent = Tools.getAgentFileForOption(evt.file);
            p = new Project(agent);
            projects.put(agent.getAbsolutePath(), p);
            evtManager.publish(new UpdateProjectEvent(this, projects));
        }
        editorPanel.openFile(evt.file, p.agent(), p.context(), evt.carretPosition, evt.search);
    }

    /**
     * Listener for the ReloadProjectEvent.
     * If something changed in the project structure (file created/deleted/renamed ...)
     * this method is called. It updates all currently opened projects and schedules the
     * UpdateProjectEvent.
     * 
     * @param e 
     */
    @EventListener
    public void updateProject(ReloadProjectEvent e) {
        projects.entrySet().forEach((entry) -> {
            entry.getValue().update();
        });
        evtManager.publish(new UpdateProjectEvent(this, projects));
    } // END updateProject()

    /**
     * Listener for the RenameFileEvent.
     * Asks for a new file name and renames the selected file/directory. The xabsl
     * extension is appended if necessary.
     * 
     * @param evt 
     */
    @EventListener
    public void renameFile(RenameFileEvent evt) {
        if (evt.file.exists()) {
            String newName = JOptionPane.showInputDialog("Rename file '" + evt.file.getName() + "': ", evt.file.getName());
            if (newName != null && !newName.trim().isEmpty()) {
                // add extension, if not set and if its not a directory
                if (!evt.file.isDirectory() && !newName.endsWith(".xabsl")) {
                    newName += ".xabsl";
                }
                // rename
                evt.file.renameTo(new File(evt.file.getParent(), newName));
                // update project
                evtManager.publish(new ReloadProjectEvent(this));
            }
        }
    }
    
    /**
     * Listener for the NewFileEvent.
     * Creates a new file and schedules the ReloadProjectEvent & OpenFileEvent.
     * 
     * @param e 
     */
    @EventListener
    public void newFile(NewFileEvent e) {
        fileChooser.setCurrentDirectory(e.startDirectory);
        fileChooser.resetChoosableFileFilters();
        fileChooser.setSelectedFile(new File(""));
        fileChooser.setFileFilter(xabslFilter);
        // show save dialog
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = Tools.validateFileName(fileChooser.getSelectedFile(), fileChooser.getFileFilter());
            if(f != null) {
                try {
                    f.createNewFile();
                    evtManager.publish(new ReloadProjectEvent(this));
                    evtManager.publish(new OpenFileEvent(this, f));
                } catch (IOException ex) {}
            } else {
                JOptionPane.showMessageDialog(null, "Not a valid xabsl file", "Invalid file", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    /**
     * (Re-)loads the editor configuration.
     */
    private void loadConfiguration() {
        if (configuration.containsKey("lastOpenedFolder")) {
            fileChooser.setCurrentDirectory(
                    new File(configuration.getProperty("lastOpenedFolder")));
        }

        // set "tab close button" default to true!
        if (!configuration.containsKey(OptionsDialog.EDITOR_TAB_CLOSE_BTN)) {
            configuration.setProperty(OptionsDialog.EDITOR_TAB_CLOSE_BTN, Boolean.toString(true));
        }

        // set "tab layout" default to true!
        if (!configuration.containsKey(OptionsDialog.EDITOR_TAB_LAYOUT)) {
            configuration.setProperty(OptionsDialog.EDITOR_TAB_LAYOUT, Boolean.toString(true));
        }

        // set "tab last used" default to false!
        if (!configuration.containsKey(OptionsDialog.EDITOR_TAB_LAST_USED)) {
            configuration.setProperty(OptionsDialog.EDITOR_TAB_LAST_USED, Boolean.toString(false));
        }

        // set "tab save before compile" default to false!
        if (!configuration.containsKey(OptionsDialog.EDITOR_SAVE_BEFOR_COMPILE)) {
            configuration.setProperty(OptionsDialog.EDITOR_SAVE_BEFOR_COMPILE, Boolean.toString(false));
        }

        // set tab size from configuration
        editorPanel.setTabSize(Integer.parseInt(configuration.getProperty(OptionsDialog.EDITOR_TAB_SIZE, "2")));
        editorPanel.setFontSize(Float.parseFloat(configuration.getProperty(OptionsDialog.EDITOR_FONT_SIZE, "14")));

        // set, if the tab close button should be shown or not
        editorPanel.setShowCloseButtons(Boolean.parseBoolean(configuration.getProperty(OptionsDialog.EDITOR_TAB_CLOSE_BTN)));
        editorPanel.setTabLayout(Boolean.parseBoolean(configuration.getProperty(OptionsDialog.EDITOR_TAB_LAYOUT)) ? JTabbedPane.WRAP_TAB_LAYOUT : JTabbedPane.SCROLL_TAB_LAYOUT);
        // a custom ui manager is needed, in order to traverse tabs in 'last used' order
        editorPanel.setTabUI(Boolean.parseBoolean(configuration.getProperty(OptionsDialog.EDITOR_TAB_LAST_USED)) ? new EditorPanelTabbedPaneUI() : new javax.swing.plaf.synth.SynthTabbedPaneUI());
        
        // update configuration of the xabsl compiler
        compiler.setConfiguration(configuration);
    }//end loadConfiguration

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
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
        jSplitPane = new javax.swing.JSplitPane();
        editorPanel = new de.naoth.xabsleditor.editorpanel.EditorPanel();
        graphPanel = new de.naoth.xabsleditor.graphpanel.GraphPanel();
        projectTree1 = new de.naoth.xabsleditor.utils.ProjectTree();
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
        mProject = new ProjectMenu();
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

        jSplitPane.setDividerLocation(450);
        jSplitPane.setOneTouchExpandable(true);
        jSplitPane.setPreferredSize(new java.awt.Dimension(750, 600));
        jSplitPane.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jSplitPanePropertyChange(evt);
            }
        });

        editorPanel.setFocusCycleRoot(true);
        jSplitPane.setLeftComponent(editorPanel);

        graphPanel.setFocusCycleRoot(true);
        jSplitPane.setRightComponent(graphPanel);

        jSplitPaneMain.setRightComponent(jSplitPane);
        jSplitPaneMain.setLeftComponent(projectTree1);

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
      editorPanel.openFile(null, null, null, 0, null);
}//GEN-LAST:event_newFileAction

    private void miCloseActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_miCloseActionPerformed
    {//GEN-HEADEREND:event_miCloseActionPerformed
        editorPanel.closeActiveTab(false);
}//GEN-LAST:event_miCloseActionPerformed

    private void saveFileAction(java.awt.event.ActionEvent evt)//GEN-FIRST:event_saveFileAction
    {//GEN-HEADEREND:event_saveFileAction
        editorPanel.save(configuration.getProperty("lastOpenedFolder"));
        evtManager.publish(new ReloadProjectEvent(this));
}//GEN-LAST:event_saveFileAction

    private void miRefreshGraphActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_miRefreshGraphActionPerformed
    {//GEN-HEADEREND:event_miRefreshGraphActionPerformed
        evtManager.publish(new RefreshGraphEvent(editorPanel.getActiveTab()));
}//GEN-LAST:event_miRefreshGraphActionPerformed

    private void miSaveAsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_miSaveAsActionPerformed
    {//GEN-HEADEREND:event_miSaveAsActionPerformed
        if (editorPanel.getActiveTab() != null && editorPanel.getActiveTab().getFile() != null) {
            editorPanel.saveAs(editorPanel.getActiveTab().getFile().getParent());
        } else {
            editorPanel.saveAs();
        }
        evtManager.publish(new ReloadProjectEvent(this));
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
                evtManager.publish(new OpenFileEvent(this, selectedFile));
                evtManager.publish(new ReloadProjectEvent(this));
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

    private void compileAction(java.awt.event.ActionEvent evt)//GEN-FIRST:event_compileAction
    {//GEN-HEADEREND:event_compileAction
        if (editorPanel.hasOpenFiles()) {
            // retrieve unsaved tabs
            ArrayList<EditorPanelTab> unsaved = editorPanel.hasOpenUnsavedFiles();
            if (!unsaved.isEmpty()) {
                // if set via config, otherwise ask ...
                if (Boolean.parseBoolean(configuration.getProperty(OptionsDialog.EDITOR_SAVE_BEFOR_COMPILE))
                        || JOptionPane.showConfirmDialog(this, "There are unsaved changes. Save files before compiling?", "Unsaved changes", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    // save all changed tabs
                    unsaved.forEach((t) -> {
                        t.save(configuration.getProperty("lastOpenedFolder"));
                    });
                }
            }

            compiler.compile(editorPanel.getActiveFile());
        } else {
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
        if (!splitterManuallySet) {
            // position splitter in the middle
            ignoreSplitterMovedEvent = true;
            jSplitPane.setDividerLocation(this.getWidth() / 2);
            ignoreSplitterMovedEvent = false;
        }
    }//GEN-LAST:event_formComponentResized

    private void jSplitPanePropertyChange(java.beans.PropertyChangeEvent evt)//GEN-FIRST:event_jSplitPanePropertyChange
    {//GEN-HEADEREND:event_jSplitPanePropertyChange
        if (evt.getPropertyName().equals("dividerLocation")) {
            if (!ignoreSplitterMovedEvent) {
                splitterManuallySet = true;
            }
        }
    }//GEN-LAST:event_jSplitPanePropertyChange

    private void miSearchProjectActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_miSearchProjectActionPerformed
    {//GEN-HEADEREND:event_miSearchProjectActionPerformed
        searchInProjectDialog.setProjects(projects);
        searchInProjectDialog.setVisible(true);
    }//GEN-LAST:event_miSearchProjectActionPerformed

    private void miHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miHelpActionPerformed
        if (this.helpDialog == null) {
            this.helpDialog = new HelpDialog(this, false);
        }

        this.helpDialog.setVisible(true);
}//GEN-LAST:event_miHelpActionPerformed

  private void miFindUnusedOptionsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_miFindUnusedOptionsActionPerformed
  {//GEN-HEADEREND:event_miFindUnusedOptionsActionPerformed
      // get selected tab
      if (editorPanel.hasOpenFiles()) {
          new UnusedOptions(this, projects).setVisible(true);
      }
  }//GEN-LAST:event_miFindUnusedOptionsActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Main(null).setVisible(true);
            }
        });
    }//end main

    @Override
    public void jumpTo(JumpTarget target) {
        if (target.getFileName() == null) {
            return;
        }

        final String XABSL_FILE_ENDING = ".xabsl";
        int dotIndex = target.getFileName().length() - XABSL_FILE_ENDING.length();
        String name = target.getFileName().substring(0, dotIndex);

        evtManager.publish(new OpenFileEvent(this, editorPanel.getActiveXABSLContext().getOptionPathMap().get(name)));

        if (editorPanel.hasOpenFiles()) {
            editorPanel.getActiveTab().jumpToLine(target.getLineNumber());
        } else {
            System.err.println("Couldn't jump to taget " + target);
        }
    }//end jumpTo

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btCompile;
    private javax.swing.JButton btNew;
    private javax.swing.JButton btOpen;
    private javax.swing.JButton btSave;
    private de.naoth.xabsleditor.editorpanel.EditorPanel editorPanel;
    private de.naoth.xabsleditor.graphpanel.GraphPanel graphPanel;
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
    private de.naoth.xabsleditor.utils.ProjectTree projectTree1;
    private javax.swing.JToolBar.Separator seperator1;
    private javax.swing.JToolBar toolbarMain;
    // End of variables declaration//GEN-END:variables

    private void saveConfiguration() {
        try {
            configuration.store(new FileWriter(fConfig), "JXabslEditor configuration");
        } catch (IOException ex) {
            Tools.handleException(ex);
        }
    }//end saveConfiguration

    class ShutdownHook extends WindowAdapter {

        @Override
        public void windowClosing(WindowEvent e) {
            // save "last" opened agent file
            try {
                configuration.setProperty(OptionsDialog.OPEN_LAST_VALUES[0], editorPanel.getActiveAgent().getAbsolutePath());
            } catch (Exception ex) {
                /* ignore exceptions! */ }

            // retrieve all opened files and close tabs
            List<String> openedFiles = editorPanel.closeAllTabs(true).stream().map((t) -> t.getAbsolutePath()).collect(Collectors.toList());

            // save opened files to configuration
            configuration.setProperty(OptionsDialog.OPEN_LAST_VALUES[1], openedFiles.stream().collect(Collectors.joining("|")));

            // the last position 6 size of the main window should be saved.
            if (OptionsDialog.START_POSITION_OPTIONS[1].equals(configuration.getProperty(OptionsDialog.START_POSITION, ""))) {
                configuration.setProperty(OptionsDialog.START_POSITION_VALUE[0], String.valueOf(getX()));       // x coordinate
                configuration.setProperty(OptionsDialog.START_POSITION_VALUE[1], String.valueOf(getY()));       // y coordinate
                configuration.setProperty(OptionsDialog.START_POSITION_VALUE[2], String.valueOf(getWidth()));   // window width
                configuration.setProperty(OptionsDialog.START_POSITION_VALUE[3], String.valueOf(getHeight()));  // window height
            }

            // save divider position
            configuration.setProperty("dividerPostionOne", String.valueOf(jSplitPaneMain.getDividerLocation()));
            configuration.setProperty("dividerPostionTwo", String.valueOf(jSplitPane.getDividerLocation()));

            saveConfiguration();

            if (watcher != null) {
                try {
                    watcher.running.set(false);
                    watcher.interrupt();
                    watcher.join();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            System.exit(0);
        }
    }
}//end class Main