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
package de.hu_berlin.informatik.ki.jxabsleditor;

import de.hu_berlin.informatik.ki.jxabsleditor.compilerconnection.CompilationFinishedReceiver;
import de.hu_berlin.informatik.ki.jxabsleditor.compilerconnection.CompileResult;
import de.hu_berlin.informatik.ki.jxabsleditor.compilerconnection.CompilerDialog;
import de.hu_berlin.informatik.ki.jxabsleditor.compilerconnection.CompilerOutputPanel.JumpListener;
import de.hu_berlin.informatik.ki.jxabsleditor.compilerconnection.CompilerOutputPanel.JumpTarget;
import de.hu_berlin.informatik.ki.jxabsleditor.editorpanel.DocumentChangedListener;
import de.hu_berlin.informatik.ki.jxabsleditor.editorpanel.XABSLEnumCompletion;
import de.hu_berlin.informatik.ki.jxabsleditor.editorpanel.XABSLOptionCompletion;
import de.hu_berlin.informatik.ki.jxabsleditor.editorpanel.XABSLStateCompetion;
import de.hu_berlin.informatik.ki.jxabsleditor.editorpanel.XABSLSymbolCompletion;
import de.hu_berlin.informatik.ki.jxabsleditor.editorpanel.XABSLSymbolSimpleCompletion;
import de.hu_berlin.informatik.ki.jxabsleditor.editorpanel.XEditorPanel;
import de.hu_berlin.informatik.ki.jxabsleditor.graphpanel.AgentVisualizer;
import de.hu_berlin.informatik.ki.jxabsleditor.graphpanel.OptionVisualizer;
import de.hu_berlin.informatik.ki.jxabsleditor.parser.XABSLContext;
import de.hu_berlin.informatik.ki.jxabsleditor.parser.XABSLContext.XABSLSymbol;
import de.hu_berlin.informatik.ki.jxabsleditor.parser.XABSLOptionContext.State;
import de.hu_berlin.informatik.ki.jxabsleditor.parser.XParser;
import de.hu_berlin.informatik.ki.jxabsleditor.parser.XabslNode;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.metal.MetalLookAndFeel;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.ShorthandCompletion;

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

  private OptionVisualizer optionVisualizer;
  private AgentVisualizer agentVisualizer;

  private String defaultCompilationPath = null;
  private boolean splitterManuallySet = false;
  private boolean ignoreSplitterMovedEvent = false;

  /** Map from an file to it's agent file (means "project") */
  private Map<File, File> file2Agent = new HashMap<File, File>();
  private FileDrop fileDrop = null;
  
  private HelpDialog helpDialog = null;

  /** Creates new form Main */
  public Main(String file)
  {
    
    // no bold fonts please
    UIManager.put("swing.boldMetal", Boolean.FALSE);
    try
    {
      UIManager.setLookAndFeel(new MetalLookAndFeel());
      //UIManager.setLookAndFeel(new NimbusLookAndFeel());
    }
    catch (UnsupportedLookAndFeelException ex)
    {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
    }

    initComponents();

    this.compilerOutputPanel.addJumpListener(this);

    this.fileDrop = new FileDrop(this.tabbedPanelEditor, new FileDrop.Listener()
    {

      @Override
      public void filesDropped(File[] files)
      {
        // open all the droppt files
        for (File file : files)
        {
          openFile(file);
        }
      }
    });

    //fileDrop.install(this.tabbedPanelEditor);
    //fileDrop.setBorderHighlightingEnabled(true);

    addWindowListener(new WindowAdapter()
    {

      @Override
      public void windowClosing(WindowEvent e)
      {
        // check if there are unsaved files
        for (Component component : tabbedPanelEditor.getComponents())
        {
          XEditorPanel editor = ((XEditorPanel) component);
          String tabName = tabbedPanelEditor.getTitleAt(tabbedPanelEditor.indexOfComponent(component));
          
          if(editor.isChanged())
          {
            tabbedPanelEditor.setSelectedComponent(component);
            
            int result = JOptionPane.showConfirmDialog(tabbedPanelEditor.getParent(),
              "The file " + tabName + " is modified. Close anyway?",
              "File Not Saved", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

            // TODO: try to close the tabs
            if(result != JOptionPane.YES_OPTION)
            {
              return;
            }
          }
        }//end for

        System.exit(0);
      }
    });


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

    optionVisualizer = new OptionVisualizer();

    GraphMouseListener<XabslNode> mouseListener = new GraphMouseListener<XabslNode>()
    {

      @Override
      public void graphClicked(XabslNode v, MouseEvent me)
      {
        XEditorPanel editor = ((XEditorPanel) tabbedPanelEditor.getSelectedComponent());
        if (editor != null && v.getType() == XabslNode.Type.State && v.getPosInText() > -1)
        {
          editor.setCarretPosition(v.getPosInText());
        }
        else if (v.getType() == XabslNode.Type.Option)
        {
          String option = v.getName();
          File file = null;
          if (editor.getXABSLContext() != null)
          {
            file = editor.getXABSLContext().getOptionPathMap().get(option);
          }

          if (file != null)
          {
            openFile(file);
          }
          else
          {
            JOptionPane.showMessageDialog(null, "Could not find the file for option "
              + option, "Option not found", JOptionPane.WARNING_MESSAGE);
          }
        }
      }

      @Override
      public void graphPressed(XabslNode v, MouseEvent me)
      {
      }

      @Override
      public void graphReleased(XabslNode v, MouseEvent me)
      {
      }
    };

    optionVisualizer.setGraphMouseListener(mouseListener);

    tabbedPanelEditor.addChangeListener(new ChangeListener()
    {

      @Override
      public void stateChanged(ChangeEvent e)
      {
        refreshGraph();
      }
    });

    panelOption.add(optionVisualizer, BorderLayout.CENTER);

    agentVisualizer = new AgentVisualizer();    
    agentVisualizer.setGraphMouseListener(mouseListener);
    panelAgent.add(agentVisualizer, BorderLayout.CENTER);

  }//end Main

  private void refreshGraph()
  {
    if (tabbedPanelEditor.getSelectedComponent() == null)
    {
      return;
    }

    XEditorPanel selectedEditorPanel = (XEditorPanel) tabbedPanelEditor.getSelectedComponent();

    String text = selectedEditorPanel.getText();

    // Option
    XParser p = new XParser(selectedEditorPanel.getXABSLContext());
    p.parse(new StringReader(text));
    optionVisualizer.setGraph(p.getOptionGraph());

    String optionName = tabbedPanelEditor.getTitleAt(tabbedPanelEditor.getSelectedIndex());
    optionName = optionName.replaceAll(".xabsl", "");
    agentVisualizer.setContext(selectedEditorPanel.getXABSLContext(), optionName);

    // refresh autocompetion
    DefaultCompletionProvider completionProvider = new DefaultCompletionProvider();

    for (State state : p.getStateMap().values())
    {
      completionProvider.addCompletion(
        new XABSLStateCompetion(completionProvider, state.name));
    }//end for

    ((XEditorPanel) tabbedPanelEditor.getSelectedComponent()).setLocalCompletionProvider(completionProvider);
  }//end refreshGraph

  /** Reconstruct the Projects menu entry */
  private void updateProjectMenu()
  {
    mProject.removeAll();

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

        for(String s : menuSubs.keySet())
        {
          miAgent.add(menuSubs.get(s));
        }

        mProject.add(miAgent);

        foundAgents.add(agentFile);
      }
    }
  }//end updateProjectMenu


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

        if(miChild.getMenuComponentCount() > 0)
          miParent.add(miChild);
      }
      else if (file.getName().toLowerCase().endsWith(XABSL_FILE_ENDING))
      {
        // remove the file ending
        int dotIndex = file.getName().length() - XABSL_FILE_ENDING.length();
        final String name = file.getName().substring(0, dotIndex);

        if(!context.getOptionPathMap().containsKey(name))
          continue;

        // create new item
        JMenuItem miOptionOpener = new JMenuItem(name);
        miOptionOpener.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            File f = context.getOptionPathMap().get(name);
            openFile(f);
          }
        });
        miParent.add(miOptionOpener);
      }
    }//end for
  }//end addFilesToMenu


  /** Reconstruct the Projects menu entry */
  private void updateProjectDirectoryMenu()
  {
    mProject.removeAll();

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

        addFilesToMenu(miAgent, agentFile.getParentFile(), context);

        mProject.add(miAgent);

        foundAgents.add(agentFile);
      }
    }//end for
  }//end updateProjectMenu



  private XABSLContext loadXABSLContext(File folder, XABSLContext context)
  {
    if (context == null)
    {
      context = new XABSLContext();
    }

    final String XABSL_FILE_ENDING = ".xabsl";

    File[] fileList = folder.listFiles();
    for (File file : fileList)
    {
      if (file.isDirectory())
      {
        loadXABSLContext(file, context);
      }
      else if (file.getName().toLowerCase().endsWith(XABSL_FILE_ENDING))
      {
        // remove the file ending
        int dotIndex = file.getName().length() - XABSL_FILE_ENDING.length();
        String name = file.getName().substring(0, dotIndex);
        context.getOptionPathMap().put(name, file);

        // parse XABSL file
        try
        {
          //System.out.println("parse: " + file.getName()); // debug stuff
          XParser p = new XParser(context);
          p.parse(new FileReader(file), file.getAbsolutePath());

        }
        catch (Exception e)
        {
          System.err.println("Couldn't read the XABSL file " + file.getAbsolutePath());
        }
      }
    }//end for

    return context;
  }//end loadXABSLContext

  private DefaultCompletionProvider createCompletitionProvider(XABSLContext context)
  {
    DefaultCompletionProvider provider = new DefaultCompletionProvider()
    {

      @Override
      protected boolean isValidChar(char ch)
      {
        return super.isValidChar(ch) || ch == '.';
      }
    };

    provider.setParameterizedCompletionParams('(', ", ", ')');

    if (context != null)
    {
      for (XABSLContext.XABSLSymbol symbol : context.getSymbolMap().values())
      {
        if (symbol.getParameter().size() == 0)
        {
          provider.addCompletion(new XABSLSymbolSimpleCompletion(provider, symbol));
        }
        else
        {
          provider.addCompletion(new XABSLSymbolCompletion(provider, symbol));
        }
        //System.out.println(symbol); // debug stuff
      }//end for

      for (XABSLContext.XABSLOption option : context.getOptionMap().values())
      {
        provider.addCompletion(new XABSLOptionCompletion(provider, option));
      }//end for

      for (XABSLContext.XABSLEnum xabslEnum : context.getEnumMap().values())
      {
        for (String param : xabslEnum.getElements())
        {
          provider.addCompletion(new XABSLEnumCompletion(provider, xabslEnum.name, param));
        }//end for
      }//end for
    }//end if

    // add some default macros
    provider.addCompletion(new ShorthandCompletion(provider,
      "state",
      "state <name> {\n\tdecision {\n\t}\n\taction {\n\t}\n}",
      "behavior state",
      "behavior state"));

    return provider;
  }//end createCompletitionProvider

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
        panelAgent = new javax.swing.JPanel();
        compilerOutputPanel = new de.hu_berlin.informatik.ki.jxabsleditor.compilerconnection.CompilerOutputPanel();
        toolbarMain = new javax.swing.JToolBar();
        btNew = new javax.swing.JButton();
        btOpen = new javax.swing.JButton();
        btSave = new javax.swing.JButton();
        seperator1 = new javax.swing.JToolBar.Separator();
        btCompile = new javax.swing.JButton();
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

        panelAgent.setLayout(new java.awt.BorderLayout());
        tabbedPanelView.addTab("Agent", panelAgent);
        tabbedPanelView.addTab("Compiler", compilerOutputPanel);

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

        miOpenFile.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        miOpenFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/hu_berlin/informatik/ki/jxabsleditor/res/fileopen16.png"))); // NOI18N
        miOpenFile.setMnemonic('O');
        miOpenFile.setText("Open File");
        miOpenFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openFileAction(evt);
            }
        });
        mFile.add(miOpenFile);

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

        miSearchProject.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        miSearchProject.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/hu_berlin/informatik/ki/jxabsleditor/res/find.png"))); // NOI18N
        miSearchProject.setText("Search in Project");
        miSearchProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miSearchProjectActionPerformed(evt);
            }
        });
        mEdit.add(miSearchProject);
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
        miRefreshGraph.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/hu_berlin/informatik/ki/jxabsleditor/res/reload.png"))); // NOI18N
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

        mProject.setMnemonic('p');
        mProject.setText("Project");

        jMenuItem1.setFont(jMenuItem1.getFont().deriveFont((jMenuItem1.getFont().getStyle() | java.awt.Font.ITALIC)));
        jMenuItem1.setText("empty");
        mProject.add(jMenuItem1);

        mbMain.add(mProject);

        mHelp.setMnemonic('H');
        mHelp.setText("Help");

        miHelp.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.CTRL_MASK));
        miHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/hu_berlin/informatik/ki/jxabsleditor/res/help.png"))); // NOI18N
        miHelp.setText("Help");
        miHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miHelpActionPerformed(evt);
            }
        });
        mHelp.add(miHelp);

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
    createDocumentTab(null, null);
}//GEN-LAST:event_newFileAction

    private void miCloseActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_miCloseActionPerformed
    {//GEN-HEADEREND:event_miCloseActionPerformed
      // close current tab

      XEditorPanel editor = ((XEditorPanel) tabbedPanelEditor.getSelectedComponent());
      if (!editor.isChanged())
      {
        tabbedPanelEditor.remove(editor);
        return;
      }

      int result = JOptionPane.showConfirmDialog(this, "Save changes?", "File was modified.",
        JOptionPane.YES_NO_CANCEL_OPTION);

      if (result == JOptionPane.CANCEL_OPTION)
      {
        return;
      }
      else if (result == JOptionPane.NO_OPTION)
      {
        tabbedPanelEditor.remove(editor);
        return;
      }

      String text = editor.getText();
      File selectedFile = editor.getFile();
      try
      {
        File file = saveStringToFile(selectedFile, text);
        if (file != null)
        {
          editor.setChanged(false);
          editor.setFile(file);
          tabbedPanelEditor.setTitleAt(tabbedPanelEditor.getSelectedIndex(), file.getName());
        }//end if
      }
      catch (IOException e)
      {
        JOptionPane.showMessageDialog(this,
          e.toString(), "The file could not be written.", JOptionPane.ERROR_MESSAGE);
      }
      catch (Exception e)
      {
        JOptionPane.showMessageDialog(this,
          e.toString(), "Could not save the file.", JOptionPane.ERROR_MESSAGE);
      }//end catch

      if (!editor.isChanged())
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
        if (file != null)
        {
          editor.setChanged(false);
          editor.setFile(file);
          tabbedPanelEditor.setTitleAt(tabbedPanelEditor.getSelectedIndex(), file.getName());
          refreshGraph();
        }
      }
      catch (IOException e)
      {
        JOptionPane.showMessageDialog(this,
          e.toString(), "The file could not be written.", JOptionPane.ERROR_MESSAGE);
      }
      catch (Exception e)
      {
        JOptionPane.showMessageDialog(this,
          e.toString(), "Could not save the file.", JOptionPane.ERROR_MESSAGE);
      }//end catch

}//GEN-LAST:event_saveFileAction

    private void miRefreshGraphActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_miRefreshGraphActionPerformed
    {//GEN-HEADEREND:event_miRefreshGraphActionPerformed
      refreshGraph();
}//GEN-LAST:event_miRefreshGraphActionPerformed

    private void miSaveAsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_miSaveAsActionPerformed
    {//GEN-HEADEREND:event_miSaveAsActionPerformed
      XEditorPanel editor = ((XEditorPanel) tabbedPanelEditor.getSelectedComponent());
      String text = editor.getText();

      try
      {
        // save as a new file
        File file = saveStringToFile(null, text);
        if (file != null)
        {
          int idx = tabbedPanelEditor.getSelectedIndex();
          editor.setChanged(false);
          editor.setFile(file);
          tabbedPanelEditor.setTitleAt(idx, file.getName());
          tabbedPanelEditor.setToolTipTextAt(idx, file.getAbsolutePath());
        }
      }
      catch (IOException e)
      {
        JOptionPane.showMessageDialog(this,
          e.toString(), "The file could not be written.", JOptionPane.ERROR_MESSAGE);
      }
      catch (Exception e)
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
      if (JFileChooser.APPROVE_OPTION != result)
      {
        return;
      }

      File selectedFile = fileChooser.getSelectedFile();
      if (selectedFile == null)
      {
        return;
      }

      if (!selectedFile.exists())
      {
        JOptionPane.showMessageDialog(this,
          "File " + selectedFile.getAbsolutePath() + " doesn't exist.", "Error",
          JOptionPane.ERROR_MESSAGE);
        return;
      }//end if

      openFile(selectedFile);

    }//GEN-LAST:event_openFileAction

  public XEditorPanel openFile(File selectedFile)
  {
    if (selectedFile == null)
    {
      return null;
    }


    // test if the file is allready opened
    for (int i = 0; i < tabbedPanelEditor.getTabCount(); i++)
    {
      Component c = tabbedPanelEditor.getComponentAt(i);
      if (c instanceof XEditorPanel)
      {
        XEditorPanel editor = (XEditorPanel) c;
        if (c != null
          && editor.getFile() != null
          && selectedFile.compareTo(editor.getFile()) == 0)
        {
          tabbedPanelEditor.setSelectedComponent(c);
          return editor;
        }//end if
      }//end if
    }//end for

    configuration.setProperty("lastOpenedFolder",
      fileChooser.getCurrentDirectory().getAbsolutePath());
    saveConfiguration();

    File agentsFile = Tools.getAgentFileForOption(selectedFile);

    if (agentsFile != null)
    {
      file2Agent.put(selectedFile, agentsFile);
      XABSLContext newContext = loadXABSLContext(agentsFile.getParentFile(), null);

      return createDocumentTab(selectedFile, newContext);
    }
    else
    {
      return null;
    }
  }//end openFile

    private void compileAction(java.awt.event.ActionEvent evt)//GEN-FIRST:event_compileAction
    {//GEN-HEADEREND:event_compileAction
      if (tabbedPanelEditor.getSelectedComponent() != null)
      {

        XEditorPanel editor = ((XEditorPanel) tabbedPanelEditor.getSelectedComponent());
        File optionFile = editor.getFile();

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

      if (tabbedPanelEditor.getSelectedComponent() != null)
      {
        XEditorPanel editor = ((XEditorPanel) tabbedPanelEditor.getSelectedComponent());
        editor.getSearchPanel().setVisible(false);
        editor.getSearchPanel().setVisible(true);
      }//end if
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


  private XEditorPanel createDocumentTab(File file, XABSLContext context)
  {
    try
    {
      // create new document
      XEditorPanel editor = null;
      if (file == null)
      {
        editor = new XEditorPanel();
        editor.setXABSLContext(context);
        editor.setCompletionProvider(createCompletitionProvider(editor.getXABSLContext()));

        int tabCount = tabbedPanelEditor.getTabCount();
        tabbedPanelEditor.addTab("New " + tabCount, editor);
      }
      else
      {
        String content = Tools.readFileToString(file);
        editor = new XEditorPanel(content);
        editor.setFile(file);
        editor.setXABSLContext(context);
        editor.setCompletionProvider(createCompletitionProvider(editor.getXABSLContext()));

        // create a tab
        tabbedPanelEditor.addTab(editor.getFile().getName(), null, editor, file.getAbsolutePath());
      }

      tabbedPanelEditor.setSelectedComponent(editor);

      // update the other openend editors
      for (int i = 0; i < tabbedPanelEditor.getTabCount(); i++)
      {
        XEditorPanel p = (XEditorPanel) tabbedPanelEditor.getTabComponentAt(i);
        if (p != editor && p != null)
        {
          p.setXABSLContext(context);
        }
      }

      //updateProjectMenu();
      updateProjectDirectoryMenu();

      editor.addDocumentChangedListener(new DocumentChangedListener()
      {

        @Override
        public void documentChanged(XEditorPanel document)
        {
          tabbedPanelEditor.setSelectedComponent(document);
          int i = tabbedPanelEditor.getSelectedIndex();
          if (document.isChanged())
          {
            String title = tabbedPanelEditor.getTitleAt(i) + " *";
            tabbedPanelEditor.setTitleAt(i, title);
          }//end if
        }
      });

      final XEditorPanel editorFinal = editor;
      editor.addHyperlinkListener(new HyperlinkListener()
      {

        @Override
        public void hyperlinkUpdate(HyperlinkEvent e)
        {
          String element = e.getDescription();
          element = element.replace("no protocol: ", "");


          File file = null;
          if (editorFinal.getXABSLContext() != null)
          {
            file = editorFinal.getXABSLContext().getOptionPathMap().get(element);
          }
          int position = 0;

          // try to open symbol
          boolean symbolWasFound = false;

          if (file == null)
          {
            XABSLSymbol symbol = editorFinal.getXABSLContext().getSymbolMap().get(element);
            if (symbol != null && symbol.getDeclarationSource() != null)
            {
              file = new File(symbol.getDeclarationSource().fileName);
              position = symbol.getDeclarationSource().offset;
              symbolWasFound = true;
            }
          }//end if

          if (file == null)
          {
            XEditorPanel editor = ((XEditorPanel) tabbedPanelEditor.getSelectedComponent());
            State state = editor.getStateMap().get(element);
            if (state != null)
            {
              editor.setCarretPosition(state.offset);
              symbolWasFound = true;
            }
          }//end if

          if (file != null)
          {
            XEditorPanel editor = openFile(file);
            if (editor != null)
            {
              editor.setCarretPosition(position);
            }
          }

          if (file == null && !symbolWasFound)
          {
            JOptionPane.showMessageDialog(null, "Could not find the file for option, symbol or state",
              "Not found", JOptionPane.WARNING_MESSAGE);
          }
          //end if

          //System.out.println(option);
        }
      });

      return editor;
    }
    catch (Exception e)
    {
      JOptionPane.showMessageDialog(this,
        e.toString(), "The file could not be read.", JOptionPane.ERROR_MESSAGE);

      e.printStackTrace();
    }

    return null;
  }//end createDocumentTab

  @Override
  public void jumpTo(JumpTarget target)
  {
    if(target.getFileName() == null) return;

    final String XABSL_FILE_ENDING = ".xabsl";
    int dotIndex = target.getFileName().length() - XABSL_FILE_ENDING.length();
    String name = target.getFileName().substring(0, dotIndex);

    XEditorPanel editor = openFile(getOptionPathMap().get(name));
    if(editor != null)
    {
      editor.jumpToLine(target.getLineNumber());
    }
    else
    {
      System.err.println("Couldn't jump to taget " + target);
    }
  }//end jumpTo

  private File saveStringToFile(File selectedFile, String text) throws Exception
  {
    if (selectedFile == null)
    {
      fileChooser.setFileFilter(xabslFilter);
      int result = fileChooser.showSaveDialog(this);
      if (JFileChooser.APPROVE_OPTION != result)
      {
        return null;
      }
      selectedFile = fileChooser.getSelectedFile();
      selectedFile = Tools.validateFileName(selectedFile, fileChooser.getFileFilter());
    }

    if (selectedFile == null)
    {
      return null;
    }

    FileWriter writer = new FileWriter(selectedFile);
    writer.write(text);
    writer.close();

    return selectedFile;
  }//end saveStringToFile

  public Map<String, File> getOptionPathMap()
  {
    XEditorPanel selectedEditorPanel = (XEditorPanel) tabbedPanelEditor.getSelectedComponent();
    if (selectedEditorPanel.getXABSLContext() != null)
    {
      return selectedEditorPanel.getXABSLContext().getOptionPathMap();
    }
    else
    {
      return null;
    }
  }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btCompile;
    private javax.swing.JButton btNew;
    private javax.swing.JButton btOpen;
    private javax.swing.JButton btSave;
    private de.hu_berlin.informatik.ki.jxabsleditor.compilerconnection.CompilerOutputPanel compilerOutputPanel;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSplitPane jSplitPane;
    private javax.swing.JMenu mEdit;
    private javax.swing.JMenu mFile;
    private javax.swing.JMenu mHelp;
    private javax.swing.JMenu mProject;
    private javax.swing.JMenuBar mbMain;
    private javax.swing.JMenuItem miClose;
    private javax.swing.JMenuItem miCompile;
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
    private javax.swing.JPanel panelAgent;
    private javax.swing.JPanel panelOption;
    private javax.swing.JToolBar.Separator seperator1;
    private javax.swing.JTabbedPane tabbedPanelEditor;
    private javax.swing.JTabbedPane tabbedPanelView;
    private javax.swing.JToolBar toolbarMain;
    // End of variables declaration//GEN-END:variables

  @Override
  public void compilationFinished(CompileResult result)
  {
    //txtCompilerOutput.setText(result.messages);
    compilerOutputPanel.setCompilerResult(result);
    if (result.errors || result.warnings)
    {
      tabbedPanelView.setSelectedIndex(tabbedPanelView.getTabCount() - 1);
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

  private class XABSLFileFilter extends javax.swing.filechooser.FileFilter
  {

    @Override
    public boolean accept(File file)
    {
      if (file.isDirectory())
      {
        return true;
      }
      String filename = file.getName();

      return filename.endsWith(".xabsl") || filename.endsWith(".XABSL");
    }

    @Override
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

    @Override
    public boolean accept(File file)
    {
      if (file.isDirectory())
      {
        return true;
      }
      String filename = file.getName();
      return filename.endsWith(".dot") || filename.endsWith(".DOT");
    }

    @Override
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
      if (splStr.length == 4)
      {
        fileName = splStr[0];
        row = Integer.parseInt(splStr[1]);
        col = Integer.parseInt(splStr[2]);
        message = splStr[3];
      }//end if
    }//end parseMessage
  }//end class XABSLErrorOutputStream
}//end class Main

