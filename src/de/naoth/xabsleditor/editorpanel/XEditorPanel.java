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
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Style;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.parser.Parser;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.ToolTipSupplier;

/**
 *
 * @author Heinrich Mellmann
 */
public class XEditorPanel extends javax.swing.JPanel
{

  private RSyntaxTextArea textArea;
  private RTextScrollPane scrolPane;
  private AutoCompletion ac;
  

  private File file;
  private XABSLContext context;
  private int hashCode;
  private int searchOffset;
  private String lastSearch;

  /** Creates new form XEditorPanel */
  public XEditorPanel()
  {
      this((String)null);
  }

  /** Create new panel and read text from file */
  public XEditorPanel(File file)
  {
    this(loadFromFile(file));
    setFile(file);
  }

  /** Create new panel and with the given text */
  public XEditorPanel(String str)
  {
    initComponents();
    InitTextArea(str);
    resetUndos();
    hashCode = textArea.getText().hashCode();
    // disable traversal keys; the tab panel should handle it
    textArea.setFocusTraversalKeysEnabled(false);
    textArea.setMarkOccurrences(true);
    textArea.setCloseCurlyBraces(true);
  }

  private void InitTextArea(String str)
  {
    searchOffset = 0;
    textArea = new RSyntaxTextArea()
    {
      /**
       * underline only when the hyperlink is activated
       */
      @Override
      public boolean getUnderlineForToken(Token t) {
        // HACK: using the color of token to identify if
        //       it is activated, since hoveredOverLinkOffset
        //       is private in RSyntaxTextArea
        if(t.isHyperlink())
        {
          return (getHyperlinksEnabled() &&
                  getForegroundForToken(t) == getHyperlinkForeground());
        }//end if

        return super.getUnderlineForToken(t);
      }//end getUnderlineForToken
      
    };//end new RSyntaxTextArea
    
    textArea.setCodeFoldingEnabled(true);
    textArea.getFoldManager().setCodeFoldingEnabled(true);

    if(str != null)
    {
      textArea.setText(str);
    }

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
    textArea.setWhitespaceVisible(true);
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

    this.scrolPane = new RTextScrollPane(textArea, true);
    scrolPane.setFoldIndicatorEnabled(true);
    scrolPane.setIconRowHeaderEnabled(true);
    scrolPane.setLineNumbersEnabled(true);
    scrolPane.getGutter().setBookmarkingEnabled(true);
    add(scrolPane);

    searchPanel.setVisible(false);
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

  public String getText()
  {
    return this.textArea.getText();
  }

  public void setText(String text)
  {
    //RSyntaxDocument document = new RSyntaxDocument(text);
    //document.setSyntaxStyle(new XTokenMaker());
    //this.textArea.setDocument(document);

    this.textArea.setText(text);
    this.textArea.revalidate();
  }
  
  public void setContent(String s) {
    setText(s);
  }

  public String getContent() {
    return getText();
  }

  public boolean isChanged()
  {
    return hashCode != textArea.getText().hashCode();
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

    try{
      centerLineInScrollPane(this.textArea);
    }catch(Exception e)
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

    private static String loadFromFile(File file) {
        if(file == null) {
            return null;
        }
        
        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException ioe) {
            ioe.printStackTrace();
            JOptionPane.showMessageDialog(null, ioe.toString(), "Can't load file", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }//end loadFromFile

    public void reloadFromFile() {
        reloadFromFile(true);
    }
    
    public void reloadFromFile(boolean updateTextArea) {
        if (this.file != null && this.file.exists()) {
            
            String content = loadFromFile(file);
            if(updateTextArea) {
                textArea.setText(content);
            }
            renewHashCode(content);
        }
    }
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
   * @param s The string to search for
   * @return True if something was found, false else
   */
  public boolean search(String s)
  {
    if(s != null)
    {
      if(lastSearch == null || !lastSearch.equals(s))
      {
        // reset if new search expression
        searchOffset = 0;
      }
      try
      {
        int textLength = textArea.getText().length();
        if(searchOffset >= textLength)
        {
          searchOffset = 0;
        }

        String text =
          textArea.getText(searchOffset, textLength - searchOffset);

        int found = text.toLowerCase().indexOf(s.toLowerCase());
        if(found > -1)
        {
          textArea.grabFocus();
          //textArea.setCaretPosition(searchOffset + found);
          setCarretPosition(searchOffset + found);
          textArea.moveCaretPosition(searchOffset + found + s.length());

          //Highlighter.HighlightPainter p = new ChangeableHighlightPainter(Color.BLUE, true, 0.5f);
          //textArea.getHighlighter().addHighlight(searchOffset + found, searchOffset + found + s.length(), p);
          
          searchOffset = searchOffset + found + 1;
          lastSearch = s;

          return true;
        }
      }
      catch(BadLocationException ex)
      {
      }
    }

    searchOffset = 0;
    lastSearch = null;
    // reset any selection
    textArea.grabFocus();
    int oldCaretPos = textArea.getCaretPosition();
    textArea.setCaretPosition(0);
    textArea.setCaretPosition(oldCaretPos);

    return false;
  }//end search

  public void setCompletionProvider(CompletionProvider provider) {
    if(ac == null) {
        ac = new AutoCompletion(provider);
        // TODO: setup some stuff, see above
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
  
  public CompletionProvider getCompletionProvider() {
      return ac == null ? null : ac.getCompletionProvider();
  }

  // HACK: make it local...
  public Map<String, XABSLOptionContext.State> getStateMap()
  {
    try{
      return ((XParser)textArea.getParser(0)).getStateMap();
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
    
    scrolPane.getGutter().setLineNumberFont(scrolPane.getGutter().getLineNumberFont().deriveFont(size));
  }
  
  /**
   * Checks whether the tab/editor can be closed savely - without data loss.
   * @return true, if tab/editor can be closed without data loss, false otherwise
   */
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
                // write data
                FileWriter writer = new FileWriter(this.file);
                writer.write(this.getText());
                writer.close();
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
