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

import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Enumeration;
import java.util.Properties;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

/**
 *
 * @author Heinrich Mellmann
 */
public class OptionsDialog extends javax.swing.JDialog
{

  public static final String DEFAULT_COMPILATION_PATH = "defaultCompilationPath";
  public static final String XABSL_COMPILER_COMMAND = "xabslCompilerCommand";
  public static final String USE_INSTALLED_RUBY = "useInstalledRuby";
  public static final String OPEN_LAST = "openLast";
  public static final String[] OPEN_LAST_OPTIONS = { "nothing", "agent", "files" };
  public static final String[] OPEN_LAST_VALUES = { "openLastAgent", "openLastFiles" };
  public static final String START_POSITION = "startPosition";
  public static final String[] START_POSITION_OPTIONS = { "default", "last", "maximized" };
  public static final String[] START_POSITION_VALUE = { "startPositionX", "startPositionY", "startPositionWidth", "startPositionHeight" };

  public static final String EDITOR_TAB_SIZE = "editorTabSize";
  public static final String EDITOR_FONT_SIZE = "fontSize";
  public static final String EDITOR_TAB_CLOSE_BTN = "editorTabCloseButton";
  public static final String EDITOR_TAB_LAYOUT = "editorTabLayout";
  public static final String EDITOR_TAB_LAST_USED = "editorTabLastUsed";
  public static final String EDITOR_SAVE_BEFOR_COMPILE = "editorSaveBeforeCompile";
  public static final String EDITOR_SHOW_WHITESPACES = "editorShowWhitespaces";
  
  public static final String APPLICATION_FONT_SIZE = "applicationFontSize";
  
  private Properties configuration;

  /** Creates new form Options */
  public OptionsDialog(java.awt.Frame parent, boolean modal, Properties configuration)
  {
    super(parent, modal);
    initComponents();
    
    // close preference/options dialog with esc key!
    this.getRootPane().registerKeyboardAction(new java.awt.event.ActionListener() {
        @Override
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButtonCancelActionPerformed(evt);
        }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

    this.configuration = configuration;
    loadOptions();
  }

  private void loadOptions()
  {

    if(configuration.containsKey(DEFAULT_COMPILATION_PATH))
    {
      this.txtDefaultCompilationPath.setText(configuration.getProperty(DEFAULT_COMPILATION_PATH));
    }

    if(configuration.containsKey(XABSL_COMPILER_COMMAND))
    {
      this.txtXabslCompilerCommand.setText(configuration.getProperty(XABSL_COMPILER_COMMAND));
    }

    if(configuration.containsKey(USE_INSTALLED_RUBY))
    {
      if(Boolean.parseBoolean(configuration.getProperty(USE_INSTALLED_RUBY)) == Boolean.TRUE)
        this.cbUseRuby.setSelected(true);
      else
        this.cbUseRuby.setSelected(false);
    }

    if(configuration.containsKey(EDITOR_TAB_SIZE))
    {
      int n = Integer.parseInt(configuration.getProperty(EDITOR_TAB_SIZE));
      this.spTabSize.setValue(n);
    }
    
    if(configuration.containsKey(EDITOR_FONT_SIZE))
    {
      this.spFontSize.setValue(Integer.parseInt(configuration.getProperty(EDITOR_FONT_SIZE)));
    }
    appFontSize.setValue(Integer.parseInt(configuration.getProperty(APPLICATION_FONT_SIZE, String.valueOf(UIManager.getFont("defaultFont").getSize()))));
    cbTabCloseBtn.setSelected(Boolean.parseBoolean(configuration.getProperty(EDITOR_TAB_CLOSE_BTN)));
    cbTabLayout.setSelected(Boolean.parseBoolean(configuration.getProperty(EDITOR_TAB_LAYOUT)));
    cbTabLastUsed.setSelected(Boolean.parseBoolean(configuration.getProperty(EDITOR_TAB_LAST_USED)));
    cbSaveBeforCompile.setSelected(Boolean.parseBoolean(configuration.getProperty(EDITOR_SAVE_BEFOR_COMPILE)));
    cbShowWhitespaces.setSelected(Boolean.parseBoolean(configuration.getProperty(EDITOR_SHOW_WHITESPACES)));
    
    // iterate through elements of btn-group and select the one in the config
    String openLast = configuration.getProperty(OPEN_LAST, "");
    Enumeration<AbstractButton> openItems = this.bgOpenLast.getElements();
    while(openItems.hasMoreElements()){
        AbstractButton btn = openItems.nextElement();
        if(openLast.equals(btn.getActionCommand())) {
            this.bgOpenLast.setSelected(btn.getModel(), true);
        }
    }

    // iterate through elements of btn-group and select the one in the config
    String startPosition = configuration.getProperty(START_POSITION, "");
    Enumeration<AbstractButton> startItems = this.bgStartBehavior.getElements();
    while(startItems.hasMoreElements()){
        AbstractButton btn = startItems.nextElement();
        if(startPosition.equals(btn.getActionCommand())) {
            this.bgStartBehavior.setSelected(btn.getModel(), true);
        }
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

        fileChooserCompilationPath = new javax.swing.JFileChooser();
        bgStartBehavior = new javax.swing.ButtonGroup();
        bgOpenLast = new javax.swing.ButtonGroup();
        jButtonOK = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        optionPanel = new javax.swing.JTabbedPane();
        jpCompiler = new javax.swing.JPanel();
        cbUseRuby = new javax.swing.JCheckBox();
        txtDefaultCompilationPath = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtXabslCompilerCommand = new javax.swing.JTextField();
        btBrowseCompilation = new javax.swing.JButton();
        jpEditor = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        spTabSize = new javax.swing.JSpinner();
        cbTabCloseBtn = new javax.swing.JCheckBox();
        cbTabLayout = new javax.swing.JCheckBox();
        cbSaveBeforCompile = new javax.swing.JCheckBox();
        jpStartBehavior = new javax.swing.JPanel();
        startPosition_default = new javax.swing.JRadioButton();
        startPosition_last = new javax.swing.JRadioButton();
        startPosition_maximized = new javax.swing.JRadioButton();
        jpStartOpen = new javax.swing.JPanel();
        startOpen_nothing = new javax.swing.JRadioButton();
        startOpen_agent = new javax.swing.JRadioButton();
        startOpen_lastFiles = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        spFontSize = new javax.swing.JSpinner();
        cbTabLastUsed = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        appFontSize = new javax.swing.JSpinner();
        cbShowWhitespaces = new javax.swing.JCheckBox();

        fileChooserCompilationPath.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Preferences");
        setModal(true);
        setName("Preferences"); // NOI18N
        setResizable(false);

        jButtonOK.setText("OK");
        jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOKActionPerformed(evt);
            }
        });

        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });

        cbUseRuby.setSelected(true);
        cbUseRuby.setText("try to use installed ruby");

        jLabel2.setText("Default compilation path");

        jLabel3.setText("XABSL Compiler command");

        btBrowseCompilation.setText("Browse...");
        btBrowseCompilation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btBrowseCompilationActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jpCompilerLayout = new javax.swing.GroupLayout(jpCompiler);
        jpCompiler.setLayout(jpCompilerLayout);
        jpCompilerLayout.setHorizontalGroup(
            jpCompilerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpCompilerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpCompilerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpCompilerLayout.createSequentialGroup()
                        .addGroup(jpCompilerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(txtDefaultCompilationPath, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btBrowseCompilation))
                    .addGroup(jpCompilerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(txtXabslCompilerCommand, javax.swing.GroupLayout.DEFAULT_SIZE, 573, Short.MAX_VALUE)
                        .addComponent(jLabel3)
                        .addComponent(cbUseRuby)))
                .addGap(20, 20, 20))
        );
        jpCompilerLayout.setVerticalGroup(
            jpCompilerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpCompilerLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jpCompilerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtDefaultCompilationPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btBrowseCompilation))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtXabslCompilerCommand, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cbUseRuby)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        optionPanel.addTab("Compiler", jpCompiler);

        jLabel4.setText("Tab Size");

        spTabSize.setModel(new javax.swing.SpinnerNumberModel(2, 0, 13, 1));
        spTabSize.setToolTipText("");

        cbTabCloseBtn.setText("Show close button");
        cbTabCloseBtn.setToolTipText("Whether or not the close button for an editor tab should be shown.");

        cbTabLayout.setText("Show tabs in multiple rows");
        cbTabLayout.setToolTipText("Shows opened tabs in multiple rows (instead of only one row).");

        cbSaveBeforCompile.setText("Always save changes before compile");
        cbSaveBeforCompile.setToolTipText("Unsaved changes are saved before compiling.");

        jpStartBehavior.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Positioning on startup"));
        jpStartBehavior.setLayout(new javax.swing.BoxLayout(jpStartBehavior, javax.swing.BoxLayout.Y_AXIS));

        bgStartBehavior.add(startPosition_default);
        startPosition_default.setSelected(true);
        startPosition_default.setText("Default");
        startPosition_default.setActionCommand(START_POSITION_OPTIONS[0]);
        jpStartBehavior.add(startPosition_default);

        bgStartBehavior.add(startPosition_last);
        startPosition_last.setText("Remember last position & size");
        startPosition_last.setActionCommand(START_POSITION_OPTIONS[1]);
        jpStartBehavior.add(startPosition_last);

        bgStartBehavior.add(startPosition_maximized);
        startPosition_maximized.setText("Start maximized");
        startPosition_maximized.setActionCommand(START_POSITION_OPTIONS[2]);
        jpStartBehavior.add(startPosition_maximized);

        jpStartOpen.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Open on startup"));
        jpStartOpen.setLayout(new javax.swing.BoxLayout(jpStartOpen, javax.swing.BoxLayout.Y_AXIS));

        bgOpenLast.add(startOpen_nothing);
        startOpen_nothing.setSelected(true);
        startOpen_nothing.setText("nothing");
        startOpen_nothing.setActionCommand(OPEN_LAST_OPTIONS[0]);
        jpStartOpen.add(startOpen_nothing);

        bgOpenLast.add(startOpen_agent);
        startOpen_agent.setText("last agent only");
        startOpen_agent.setActionCommand(OPEN_LAST_OPTIONS[1]);
        jpStartOpen.add(startOpen_agent);

        bgOpenLast.add(startOpen_lastFiles);
        startOpen_lastFiles.setText("last opened files");
        startOpen_lastFiles.setActionCommand(OPEN_LAST_OPTIONS[2]);
        jpStartOpen.add(startOpen_lastFiles);

        jLabel1.setText("Editor Font Size");

        spFontSize.setModel(new javax.swing.SpinnerNumberModel(14, 8, 120, 1));

        cbTabLastUsed.setText("Switch through tabs in the order of last used");
        cbTabLastUsed.setToolTipText("Shows opened tabs in multiple rows (instead of only one row).");

        jLabel5.setText("Application Font Size");

        appFontSize.setModel(new javax.swing.SpinnerNumberModel(14, 8, 120, 1));
        appFontSize.setToolTipText("A restart is needed for this setting!");

        cbShowWhitespaces.setText("Whitespace should be visible");
        cbShowWhitespaces.setToolTipText("Sets whether whitespace is visible.");

        javax.swing.GroupLayout jpEditorLayout = new javax.swing.GroupLayout(jpEditor);
        jpEditor.setLayout(jpEditorLayout);
        jpEditorLayout.setHorizontalGroup(
            jpEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpEditorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbShowWhitespaces)
                    .addGroup(jpEditorLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(spTabSize, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jpEditorLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spFontSize, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jpEditorLayout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(appFontSize, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(cbTabLastUsed)
                    .addComponent(jpStartOpen, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jpStartBehavior, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbTabLayout)
                    .addComponent(cbSaveBeforCompile)
                    .addComponent(cbTabCloseBtn))
                .addContainerGap(315, Short.MAX_VALUE))
        );
        jpEditorLayout.setVerticalGroup(
            jpEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpEditorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(spTabSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jpEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(spFontSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jpEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(appFontSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbTabCloseBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbTabLayout)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbTabLastUsed)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbSaveBeforCompile)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbShowWhitespaces)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jpStartOpen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jpStartBehavior, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27))
        );

        optionPanel.addTab("Editor", jpEditor);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(239, 239, 239)
                        .addComponent(jButtonOK, javax.swing.GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonCancel, javax.swing.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(optionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(optionPanel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonCancel)
                    .addComponent(jButtonOK))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOKActionPerformed

      // save settings
      if("".equals(txtDefaultCompilationPath.getText()) && configuration.containsKey(DEFAULT_COMPILATION_PATH))
      {
        configuration.remove(DEFAULT_COMPILATION_PATH);
      }
      else
      {
        configuration.setProperty(DEFAULT_COMPILATION_PATH, txtDefaultCompilationPath.getText());
      }

      if("".equals(txtXabslCompilerCommand.getText()) && configuration.containsKey(XABSL_COMPILER_COMMAND))
      {
        configuration.remove(XABSL_COMPILER_COMMAND);
      }
      else
      {
        configuration.setProperty(XABSL_COMPILER_COMMAND,
          txtXabslCompilerCommand.getText());
      }

      configuration.setProperty(USE_INSTALLED_RUBY, Boolean.toString(this.cbUseRuby.isSelected()));
      
      configuration.setProperty(OPEN_LAST, this.bgOpenLast.getSelection().getActionCommand());

      configuration.setProperty(EDITOR_TAB_SIZE, this.spTabSize.getValue().toString());
      configuration.setProperty(APPLICATION_FONT_SIZE, this.appFontSize.getValue().toString());
      configuration.setProperty(EDITOR_FONT_SIZE, this.spFontSize.getValue().toString());
      configuration.setProperty(EDITOR_TAB_CLOSE_BTN, Boolean.toString(this.cbTabCloseBtn.isSelected()));
      configuration.setProperty(EDITOR_TAB_LAYOUT, Boolean.toString(this.cbTabLayout.isSelected()));
      configuration.setProperty(EDITOR_TAB_LAST_USED, Boolean.toString(this.cbTabLastUsed.isSelected()));
      configuration.setProperty(EDITOR_SAVE_BEFOR_COMPILE, Boolean.toString(this.cbSaveBeforCompile.isSelected()));
      configuration.setProperty(EDITOR_SHOW_WHITESPACES, Boolean.toString(this.cbShowWhitespaces.isSelected()));

      configuration.setProperty(START_POSITION, this.bgStartBehavior.getSelection().getActionCommand());
      
      this.setVisible(false);
      this.dispose();
    }//GEN-LAST:event_jButtonOKActionPerformed

    private void btBrowseCompilationActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btBrowseCompilationActionPerformed
    {//GEN-HEADEREND:event_btBrowseCompilationActionPerformed

      File fPath = new File(System.getProperty("user.dir"));
      if(!"".equals(txtDefaultCompilationPath.getText()))
      {
        fPath = new File(txtDefaultCompilationPath.getText());
        
      }

      fileChooserCompilationPath.setSelectedFile(fPath);
      if(fileChooserCompilationPath.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
      {
        txtDefaultCompilationPath.setText(fileChooserCompilationPath.getSelectedFile().getAbsolutePath());
      }

}//GEN-LAST:event_btBrowseCompilationActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
      this.setVisible(false);
      this.dispose();
    }//GEN-LAST:event_jButtonCancelActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSpinner appFontSize;
    private javax.swing.ButtonGroup bgOpenLast;
    private javax.swing.ButtonGroup bgStartBehavior;
    private javax.swing.JButton btBrowseCompilation;
    private javax.swing.JCheckBox cbSaveBeforCompile;
    private javax.swing.JCheckBox cbShowWhitespaces;
    private javax.swing.JCheckBox cbTabCloseBtn;
    private javax.swing.JCheckBox cbTabLastUsed;
    private javax.swing.JCheckBox cbTabLayout;
    private javax.swing.JCheckBox cbUseRuby;
    private javax.swing.JFileChooser fileChooserCompilationPath;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jpCompiler;
    private javax.swing.JPanel jpEditor;
    private javax.swing.JPanel jpStartBehavior;
    private javax.swing.JPanel jpStartOpen;
    private javax.swing.JTabbedPane optionPanel;
    private javax.swing.JSpinner spFontSize;
    private javax.swing.JSpinner spTabSize;
    private javax.swing.JRadioButton startOpen_agent;
    private javax.swing.JRadioButton startOpen_lastFiles;
    private javax.swing.JRadioButton startOpen_nothing;
    private javax.swing.JRadioButton startPosition_default;
    private javax.swing.JRadioButton startPosition_last;
    private javax.swing.JRadioButton startPosition_maximized;
    private javax.swing.JTextField txtDefaultCompilationPath;
    private javax.swing.JTextField txtXabslCompilerCommand;
    // End of variables declaration//GEN-END:variables
}
