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
import de.naoth.xabsleditor.parser.XABSLContext;
import de.naoth.xabsleditor.parser.XABSLOptionContext;
import de.naoth.xabsleditor.parser.XParser;
import de.naoth.xabsleditor.parser.XTokenMaker;
import de.naoth.xabsleditor.utils.XABSLFileFilter;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CCompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Style;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Token;
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

  private File file;
  private XABSLContext context;
  private boolean changed;
  private int searchOffset;
  private String lastSearch;

  /** Creates new form XEditorPanel */
  public XEditorPanel()
  {
    initComponents();
    InitTextArea(null);
    changed = false;
  }

  public XEditorPanel(String str)
  {
    initComponents();
    InitTextArea(str);
    changed = false;
  }

  // create new panel and read text from file
  public XEditorPanel(File file)
  {
    initComponents();
    InitTextArea(null);


    loadFromFile(file);
    setFile(file);
    changed = false;
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


    if(str != null)
    {
      textArea.setText(str);
    }

    textArea.setAutoIndentEnabled(true);
    
    textArea.setCaretPosition(0);
    //textArea.addHyperlinkListener(this);
    textArea.requestFocusInWindow();


    textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));

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
        setChanged(true);
      }

      @Override
      public void removeUpdate(DocumentEvent e)
      {
        setChanged(true);
      }

      @Override
      public void changedUpdate(DocumentEvent e)
      {
        setChanged(true);
      }
    });

    this.scrolPane = new RTextScrollPane(textArea, true);
    add(scrolPane);

    searchPanel.setVisible(false);
  }//end InitTextArea

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
  }//end getText

  public void setText(String text)
  {
    //RSyntaxDocument document = new RSyntaxDocument(text);
    //document.setSyntaxStyle(new XTokenMaker());
    //this.textArea.setDocument(document);

    this.textArea.setText(text);
    this.textArea.revalidate();
  }//end getText

  public boolean isChanged()
  {
    return changed;
  }

  public void setChanged(boolean changed)
  {
    if(changed == this.changed)
    {
      return;
    }
    this.changed = changed;
    fireDocumentChangedEvent();
  }//end setChanged

  public File getFile()
  {
    return file;
  }

  public void setFile(File file)
  {
    this.file = file;
  }//end setFile


  /*
   *  Attempt to center the line containing the caret at the center of the
   *  scroll pane.
   *
   *  @param component the text component in the sroll pane
   */
  public static void centerLineInScrollPane(JTextComponent component) {
    Container container = SwingUtilities.getAncestorOfClass(JViewport.class, component);

    if (container == null) {
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

  public void setCarretPosition(int pos)
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

  public void loadFromFile(File file)
  {
    try
    {
      BufferedReader r = new BufferedReader(new FileReader(file));
      textArea.read(r, null);
      r.close();
    }
    catch(IOException ioe)
    {
      ioe.printStackTrace();
      UIManager.getLookAndFeel().provideErrorFeedback(textArea);
    }
  }//end loadFromFile

  public void addHyperlinkListener(HyperlinkListener listener)
  {
    textArea.addHyperlinkListener(listener);
  }//end addHyperlinkListener
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

  private CCompletionProvider completionProvider = null;
  
  
  private void createCompletionProvider()
  {
    this.completionProvider = new CCompletionProvider(new DefaultCompletionProvider());

    AutoCompletion ac = new AutoCompletion(this.completionProvider);
    ac.setDescriptionWindowSize(300, 200);
		ac.setListCellRenderer(new CCellRenderer());
		ac.setShowDescWindow(true);
		ac.setParameterAssistanceEnabled(true);
		ac.install(textArea);

    textArea.setToolTipSupplier((ToolTipSupplier)this.completionProvider);
		ToolTipManager.sharedInstance().registerComponent(textArea);
  }//end createCompletionProvider


  public void setCompletionProvider(DefaultCompletionProvider completionProvider) {
    if(this.completionProvider == null) createCompletionProvider();
    this.completionProvider.setDefaultCompletionProvider(completionProvider);
  }//end setCompletionProvider

  
  public void setLocalCompletionProvider(DefaultCompletionProvider completionProvider) {
    if(this.completionProvider == null) createCompletionProvider();
    this.completionProvider.setXabslLocalCompletionProvider(completionProvider);
  }//end setLocalCompletionProvider

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
    textArea.clearParsers();
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
        // save as a new file
        if(this.file == null)
        {
            // set up file chooser
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setFileFilter(new XABSLFileFilter());
            fileChooser.setAcceptAllFileFilterUsed(true);
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
                this.setChanged(false);

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

    @Override
    public void setTransferHandler(TransferHandler newHandler) {
        super.setTransferHandler(newHandler);
        textArea.setTransferHandler(newHandler);
    }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private de.naoth.xabsleditor.editorpanel.SearchPanel searchPanel;
  // End of variables declaration//GEN-END:variables
}//end class XEditorPanel
