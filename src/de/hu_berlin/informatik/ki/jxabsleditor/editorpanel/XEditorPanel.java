/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * XEditorPanel.java
 *
 * Created on 08.01.2009, 02:57:09
 */
package de.hu_berlin.informatik.ki.jxabsleditor.editorpanel;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxHighlightingColorScheme;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rtextarea.RTextScrollPane;

/**
 *
 * @author Heinrich Mellmann
 */
public class XEditorPanel extends javax.swing.JPanel
{

  private RSyntaxTextArea textArea;
  private File file;
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

    textArea = new RSyntaxTextArea();
    if(str != null)
    {
      textArea.setText(str);
    }

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
    SyntaxHighlightingColorScheme s = new SyntaxHighlightingColorScheme(true);
    s.syntaxSchemes[Token.COMMENT_EOL] = new SyntaxScheme(new Color(255, 128, 0), null);
    textArea.setSyntaxHighlightingColorScheme(s);

    textArea.setWhitespaceVisible(true);
    textArea.setVisible(true);

    textArea.setHyperlinksEnabled(true);
    textArea.setHyperlinkForeground(Color.red);


    // the tokenizer
    ((RSyntaxDocument) textArea.getDocument()).setSyntaxStyle(new XTokenMaker());
    // set parser
    textArea.setParser(new XParser());

    textArea.getDocument().addDocumentListener(new DocumentListener()
    {

      public void insertUpdate(DocumentEvent e)
      {
        setChanged(true);
      }

      public void removeUpdate(DocumentEvent e)
      {
        setChanged(true);
      }

      public void changedUpdate(DocumentEvent e)
      {
        setChanged(true);
      }
    });

    RTextScrollPane scrolPane = new RTextScrollPane(500, 400, textArea, true);
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

  public void setCarretPosition(int pos)
  {
    this.textArea.setCaretPosition(pos);
    this.textArea.revalidate();
  }

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
          textArea.select(searchOffset + found, searchOffset + found + s.length());
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
    return false;
  }
  

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private de.hu_berlin.informatik.ki.jxabsleditor.editorpanel.SearchPanel searchPanel;
  // End of variables declaration//GEN-END:variables
}//end class XEditorPanel
