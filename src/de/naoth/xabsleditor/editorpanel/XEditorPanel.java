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
package de.naoth.xabsleditor.editorpanel;

import de.naoth.xabsleditor.Tools;
import de.naoth.xabsleditor.completion.CCellRenderer;
import de.naoth.xabsleditor.parser.XABSLContext;
import de.naoth.xabsleditor.parser.XABSLOptionContext;
import de.naoth.xabsleditor.parser.XParser;
import de.naoth.xabsleditor.parser.XTokenMaker;
import de.naoth.xabsleditor.utils.XABSLFileFilter;
import java.awt.Color;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.rsyntaxtextarea.FileLocation;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Style;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.parser.Parser;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.ToolTipSupplier;

/**
 *
 * @author Heinrich Mellmann
 */
public class XEditorPanel extends javax.swing.JPanel
{

  private XSyntaxTextArea textArea;
  private RTextScrollPane scrollPane;
  private AutoCompletion ac;

  private File file;
  private XABSLContext context;
  private int hashCode;
  private String lastSearch;

  /** Create new panel and read text from file */
  public XEditorPanel()
  {
    this(null);
  }

  /** Create new panel and with the given text */
  public XEditorPanel(File file)
  {
    initComponents();
    InitTextArea();
    
    
    if(file != null) {
        loadFromFile(file);
    }
    // NOTE: this has to be after loadFromFile to remove all insertions while 
    //       opening the file
    resetUndos();
    
    // this is done inside loadFromFile
    //hashCode = textArea.getText().hashCode();
    
    // disable traversal keys; the tab panel should handle it
    textArea.setFocusTraversalKeysEnabled(false);
    textArea.setMarkOccurrences(true);
    textArea.setCloseCurlyBraces(true);
  }

  private void InitTextArea()
  {
    textArea = new XSyntaxTextArea();
    
    textArea.setCodeFoldingEnabled(true);
    textArea.getFoldManager().setCodeFoldingEnabled(true);
    
    /*
    if(str != null)
    {
      try {
        textArea.read(new StringReader(str), ac);
      } catch(IOException ex) {}
      //textArea.setText(str);
    }
    */
    textArea.setText("");
    textArea.setAutoIndentEnabled(true);
    
    textArea.setCaretPosition(0);
    //textArea.addHyperlinkListener(this);
    textArea.requestFocusInWindow();


    //textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
    // HACK: set a font, so that the default font is not null
    textArea.setFont(RSyntaxTextArea.getDefaultFont());

    // replace tabs with spaces
    textArea.setTabsEmulated(true);
    textArea.setTabSize(2);

    // set the color of the current line
    textArea.setCurrentLineHighlightColor(new Color(0.9f, 0.9f, 1.0f));

    // TODO: define new color scheme
    SyntaxScheme scheme = new SyntaxScheme(true);
    scheme.setStyle(Token.COMMENT_EOL, new Style(new Color(255, 128, 0), null));
    scheme.setStyle(Token.SEPARATOR, new Style());
    
    textArea.setSyntaxScheme(scheme);
    textArea.setVisible(true);

    //textArea.setHyperlinksEnabled(true);
    //textArea.setHyperlinkForeground(Color.blue);
    //textArea.setAutoIndentEnabled(true);
    
    // set parser
    textArea.setSyntaxEditingStyle(XParser.SYNTAX_STYLE_XABSL);
    // the tokenizer
    ((RSyntaxDocument) textArea.getDocument()).setSyntaxStyle(new XTokenMaker());
    
    textArea.addParser(new XParser());

    textArea.getDocument().addDocumentListener(new DocumentListener()
    {
      @Override
      public void insertUpdate(DocumentEvent e)
      {
        fireDocumentChangedEvent();
      }

      @Override
      public void removeUpdate(DocumentEvent e)
      {
        fireDocumentChangedEvent();
      }

      @Override
      public void changedUpdate(DocumentEvent e)
      {
        fireDocumentChangedEvent();
      }
    });
    
    // key listener for continue search and "stop" search, when textarea has focus
    textArea.addKeyListener(new KeyListener() {
        @Override
        public void keyTyped(KeyEvent e) {}

        @Override
        public void keyPressed(KeyEvent e) {}

        @Override
        public void keyReleased(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_F3) {
                // continue search
                search(lastSearch);
            } else if(e.getKeyCode() == KeyEvent.VK_ESCAPE && searchPanel.isVisible()) {
                // hide search panel
                searchPanel.setVisible(false);
            }
        }
    });

    this.scrollPane = new RTextScrollPane(textArea, true);
    scrollPane.setFoldIndicatorEnabled(true);
    scrollPane.setLineNumbersEnabled(true);
    
    // NOTE: currently not used
    // setup bookmarking capability
//    scrollPane.setIconRowHeaderEnabled(true);
//    scrolPane.getGutter().setBookmarkIcon(new ImageIcon(CCellRenderer.class.getResource("/de/naoth/xabsleditor/res/var.png")));
//    scrolPane.getGutter().setBookmarkingEnabled(true);

    // setup the error/warning/info notification bar on the right side (like netbeans)
//    ErrorStrip es = new ErrorStrip(textArea);
//    es.setShowMarkedOccurrences(true);
//    es.setLevelThreshold(ParserNotice.Level.INFO);
//    add(es, java.awt.BorderLayout.LINE_END);

    add(scrollPane, java.awt.BorderLayout.CENTER);

    searchPanel.setVisible(false);
    // react on hidding search panel
    searchPanel.addComponentListener(new ComponentAdapter() {
        @Override
        public void componentShown(ComponentEvent e) {
            textArea.setMarkAllOnOccurrenceSearches(true);
        }

        @Override
        public void componentHidden(ComponentEvent e) {
            textArea.setMarkAllOnOccurrenceSearches(false);
            textArea.clearAllHighlights();
            textArea.grabFocus();
        }
    });
  }//end InitTextArea
  
  private void resetUndos() {
    if(textArea.getDocument() instanceof RSyntaxDocument) {
        UndoableEditListener[] mgr = ((RSyntaxDocument)textArea.getDocument()).getUndoableEditListeners();
        for (UndoableEditListener undoers : mgr) {
            if(undoers instanceof UndoManager) {
                ((UndoManager)undoers).discardAllEdits();
            }
        }
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

    searchPanel = new SearchPanel(this);

    setLayout(new java.awt.BorderLayout());
    add(searchPanel, java.awt.BorderLayout.PAGE_END);
  }// </editor-fold>//GEN-END:initComponents

  /*
  public String getText()
  {
    //return this.textArea.getText();
    StringWriter sw = new StringWriter();
    try {
            this.textArea.write(sw);
        } catch(IOException ex) {}
    return sw.toString();
  }

  public void setText(String text)
  {
    //RSyntaxDocument document = new RSyntaxDocument(text);
    //document.setSyntaxStyle(new XTokenMaker());
    //this.textArea.setDocument(document);

    //this.textArea.setText(text);
      try {
        this.textArea.read(new StringReader(text), ac);
      } catch(IOException ex) {}
      
    this.textArea.revalidate();
  }
  
  
  public void setContent(String s) {
    setText(s);
  }
  */

  // NOTE: getText returns the text as it is saved internally, i.e., all line 
  //       breaks are represented by '\n'
  public String getContent() {
    return textArea.getText();
  }

  public boolean isChanged()
  {
    return hashCode != textArea.getText().hashCode();
  }
  
  public boolean isModifiedOutsideEditor() {
    return this.textArea.isModifiedOutsideEditor();
  }

  public File getFile()
  {
    return file;
  }

  public void setFile(File file)
  {
    this.file = file;
  }

  /*
   *  Attempt to center the line containing the caret at the center of the
   *  scroll pane.
   *
   *  @param component the text component in the sroll pane
   */
  public static void centerLineInScrollPane(JTextComponent component) {
    Container container = SwingUtilities.getAncestorOfClass(JViewport.class, component);

    // empty container or 'invalid' component size -> can not center on a 0,0 component!
    if (container == null || (component.getSize().getWidth() == 0 && component.getSize().getHeight() == 0)) {
      return;
    }

    try {
      Rectangle r = component.modelToView(component.getCaretPosition());
      JViewport viewport = (JViewport) container;
      int extentHeight = viewport.getExtentSize().height;
      int viewHeight = viewport.getViewSize().height;

      int y = Math.max(0, r.y - (extentHeight / 2));
      y = Math.min(y, viewHeight - extentHeight);

      viewport.setViewPosition(new Point(0, y));
    } catch (BadLocationException ble) {
    }
  }//end centerLineInScrollPane

  final public void setCarretPosition(int pos)
  {
    this.textArea.setCaretPosition(pos);

    try {
      centerLineInScrollPane(this.textArea);
    } catch(Exception e)
    {
      // TODO:
      // could not scroll to the right position
    }

    this.textArea.revalidate();
  }//end setCarretPosition
  
  public int getCarretPosition() {
      return this.textArea.getCaretPosition();
  }

  public void jumpToLine(int line)
  {
    try{
      int startOffs = textArea.getLineStartOffset(line);
      setCarretPosition(startOffs);
    }catch(BadLocationException ex)
    {
      System.err.println("Couldn't jump to the line " + ex.offsetRequested());
    }
  }//end jumpToLine

  
    private void loadFromFile(File file) 
    {    
        try {
            // NOTE: this is here for documentation purposes
            //https://docs.oracle.com/javase/7/docs/api/javax/swing/text/DefaultEditorKit.html
            //this.textArea.read(new FileReader(file), file);
            
            // NOTE: use the convenient method load, because it can handle
            //       the UTF-8 files with BOM identifyer
            //       https://en.wikipedia.org/wiki/Byte_order_mark
            this.textArea.load(FileLocation.create(file), null);
            
            //System.gc();
            Tools.releaseFileAsync(file);
            setFile(file);
            renewHashCode();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            JOptionPane.showMessageDialog(null, ioe.toString(), "Can't load file", JOptionPane.ERROR_MESSAGE);
        }
    }//end loadFromFile
    
    public void reloadFromFile() {
        reloadFromFile(true);
    }
    
    public void reloadFromFile(boolean updateTextArea) {

        if(updateTextArea) {
            //textArea.setText(content);
            try {
                //https://docs.oracle.com/javase/7/docs/api/javax/swing/text/DefaultEditorKit.html
                this.textArea.reload();
                //System.gc();
                Tools.releaseFileAsync(file);
                renewHashCode();
            } catch (IOException ioe) {
                ioe.printStackTrace();
                JOptionPane.showMessageDialog(null, ioe.toString(), "Can't load file", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            renewHashCode(readFileToString(file));
        }
    }
    
    private static String readFileToString(File file) {
        if(file == null) {
            return null;
        }
        
        try {
            String str = new String(Files.readAllBytes(file.toPath()));
            //System.gc();
            Tools.releaseFileAsync(file);
            return str;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            JOptionPane.showMessageDialog(null, ioe.toString(), "Can't load file", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }//end readFileToString
  
    
    public void renewHashCode() {
        renewHashCode(null);
    }
    
    public void renewHashCode(String content) {
        if(content == null) {
            hashCode = textArea.getText().hashCode();
        } else {
            hashCode = content.hashCode();
        }
        fireDocumentChangedEvent();
    }

  public void addHyperlinkListener(HyperlinkListener listener)
  {
    textArea.addHyperlinkListener(listener);
  }
  
  ArrayList<DocumentChangedListener> documentChangedListeners = new ArrayList<DocumentChangedListener>();

  public void addDocumentChangedListener(DocumentChangedListener listener)
  {
    documentChangedListeners.add(listener);
  }

  public void removeDocumentChangedListener(DocumentChangedListener listener)
  {
    documentChangedListeners.remove(listener);
  }

  public void fireDocumentChangedEvent()
  {
    for(DocumentChangedListener listener : documentChangedListeners)
    {
      listener.documentChanged(this);
    }
  }

  public SearchPanel getSearchPanel()
  {
    return searchPanel;
  }

  /**
   * Searching in the text. The search will begin at the beginning of text.
   * When this function is called the next time, it will search for the next
   * occurance. Search will be begun from the beginning if end of text reached.
   * 
   * @param s The string to search for
   * @return True if something was found, false else
   */
  public boolean search(String s)
  {
    lastSearch = s;
    if(s != null && !s.isEmpty()) {
          SearchContext sc = new SearchContext(s);
          sc.setMarkAll(textArea.getMarkAllOnOccurrenceSearches());
        return SearchEngine.find(textArea, sc).wasFound();
    }
    return false;
  }//end search

  /**
   * Sets the completion provider to this text area.
   * If the autocompletion object doesn't exist, one is created.
   * 
   * @param provider the (default) completion provider
   */
  public void setCompletionProvider(CompletionProvider provider) {
    if(ac == null) {
        ac = new AutoCompletion(provider);
        ac.setDescriptionWindowSize(300, 200);
        ac.setListCellRenderer(new CCellRenderer());
        ac.setShowDescWindow(true);
        ac.setParameterAssistanceEnabled(true);
        ac.install(textArea);
        
        textArea.setToolTipSupplier((ToolTipSupplier) provider);
        ToolTipManager.sharedInstance().registerComponent(textArea);
    } else {
        ac.setCompletionProvider(provider);
    }
  }//end setCompletionProvider
  
  /**
   * Returns the current completion provider, if already set.
   * 
   * @return current completion provider
   */
  public CompletionProvider getCompletionProvider() {
      return ac == null ? null : ac.getCompletionProvider();
  }

  // HACK: make it local...
  public Map<String, XABSLOptionContext.State> getStateMap()
  {
    try{
      return textArea.getXParser().getStateMap();
    }catch(Exception e)
    {
      //
    }
    return null;
  }//end getStateMap

  public void setXABSLContext(XABSLContext xabslContext)
  {
      this.context = xabslContext;
      // we just want to remove 'our' (X)parser!
      for (int i = 0; i < textArea.getParserCount(); i++) {
          Parser p = textArea.getParser(i);
          if (p instanceof XParser) {
              textArea.removeParser(p);
              i--;
          }
      }
      // 're-add' parser
      textArea.addParser(new XParser(xabslContext));
  }//end setXABSLContext

  public XABSLContext getXABSLContext()
  {
    return context;
  }

  public void setTabSize(int size)
  {
    this.textArea.setTabSize(size);
  }
  
  public void setFontSize(float size)
  {
    // update all the fonts with the new fonsize
    this.textArea.setFont(RSyntaxTextArea.getDefaultFont().deriveFont(size));
    
    scrollPane.getGutter().setLineNumberFont(scrollPane.getGutter().getLineNumberFont().deriveFont(size));
  }
  
  public void setShowWhitespaces(boolean show) {
    textArea.setWhitespaceVisible(show);
  }
  
  /**
   * Checks whether the tab/editor can be closed safely - without data loss.
   * @return true, if tab/editor can be closed without data loss, false otherwise
   */
  /*
    public boolean close() {
        if (this.isChanged()) {
            // something changed ...
            int result = JOptionPane.showConfirmDialog(this, "Save changes?", "File was modified.", JOptionPane.YES_NO_CANCEL_OPTION);
            // cancel or try to save
            if (result == JOptionPane.CANCEL_OPTION || (result == JOptionPane.YES_OPTION && !this.save())) {
                return false;
            }
        }
        return true;
    }
  */

    public boolean save() {
        return save(System.getProperty("user.home"));
    }
    
    public boolean save(String defaultDirectory) {
        // save as a new file
        if(this.file == null)
        {
            // set up file chooser
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setFileFilter(new XABSLFileFilter());
            fileChooser.setAcceptAllFileFilterUsed(true);
            fileChooser.setCurrentDirectory(new File(defaultDirectory));
            // get new file
            int result = fileChooser.showSaveDialog(this);
            if (result != JFileChooser.APPROVE_OPTION) {
                this.file = null;
            } else {
                this.file = Tools.validateFileName(fileChooser.getSelectedFile(), fileChooser.getFileFilter());
            }
        }
        // only if we have a valid file
        if(this.file != null) {
            try {
                // NOTE: don't use the convenient method saveAs, because it is writing 
                //       the UTF-8 files with BOM identifyer, which is not recomended 
                //       and might make problems
                //       https://en.wikipedia.org/wiki/Byte_order_mark
                //this.textArea.saveAs(FileLocation.create(file));
                
                // write data
                FileWriter writer = new FileWriter(this.file);
                this.textArea.write(writer);
                writer.close();
                Tools.releaseFileAsync(file);
               
                renewHashCode();

                // change UI (title, tooltip) of tab
                if(this.getParent() instanceof JTabbedPane) {
                    JTabbedPane pane = (JTabbedPane)this.getParent();
                    int idx = pane.indexOfComponent(this);
                    pane.setTitleAt(idx, this.file.getName());
                    pane.setToolTipTextAt(idx, file.getAbsolutePath());
                }
                // data saved
                return true;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, e.toString(), "The file could not be written.", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.toString(), "Could not save the file.", JOptionPane.ERROR_MESSAGE);
            }//end catch
        }
        // ... otherwise we're wasn't able to save!!
        return false;
    }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private de.naoth.xabsleditor.editorpanel.SearchPanel searchPanel;
  // End of variables declaration//GEN-END:variables
}//end class XEditorPanel
