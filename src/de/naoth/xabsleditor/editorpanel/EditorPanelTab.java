package de.naoth.xabsleditor.editorpanel;

import de.naoth.xabsleditor.parser.XABSLContext;
import de.naoth.xabsleditor.parser.XABSLOptionContext;
import java.awt.BorderLayout;
import java.io.File;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.TransferHandler;
import javax.swing.event.HyperlinkEvent;
import org.fife.ui.autocomplete.DefaultCompletionProvider;

/**
 *
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class EditorPanelTab extends JPanel
{
    private XEditorPanel editor;
    private File agent;
    
    public EditorPanelTab(File f) {
        // init editor
        if(f != null && f.exists()) {
            editor = new XEditorPanel(f);
        } else {
            editor = new XEditorPanel();
        }
        // setup ui
        setLayout(new BorderLayout());
        add(editor, BorderLayout.CENTER);
        // change tab title to indicate the modified content
        editor.addDocumentChangedListener((XEditorPanel document) -> {
            if (document.isChanged()) {
                JTabbedPane p = (JTabbedPane)getParent();
                int idx = p.indexOfComponent(this);
                p.setTitleAt(idx, p.getTitleAt(idx) + " *");
            }
        });
        
        editor.addHyperlinkListener((HyperlinkEvent e) -> {
            String element = e.getDescription();
            element = element.replace("no protocol: ", "");
            File file = null;
            if (editor.getXABSLContext() != null) {
                file = editor.getXABSLContext().getOptionPathMap().get(element);
            }
            int position = 0;
            // try to open symbol
            boolean symbolWasFound = false;
            if (file == null) {
                XABSLContext.XABSLSymbol symbol = editor.getXABSLContext().getSymbolMap().get(element);
                if (symbol != null && symbol.getDeclarationSource() != null) {
                    file = new File(symbol.getDeclarationSource().fileName);
                    position = symbol.getDeclarationSource().offset;
                    symbolWasFound = true;
                }
            }//end if
            if (file == null) {
                XABSLOptionContext.State state = editor.getStateMap().get(element);
                if (state != null) {
                    editor.setCarretPosition(state.offset);
                    symbolWasFound = true;
                }
            } //end if
            if (file != null) {
                ((EditorPanel)(getParent().getParent())).openFile(file, position);
            }
            if (file == null && !symbolWasFound) {
                JOptionPane.showMessageDialog(null, "Could not find the file for option, symbol or state",
                        "Not found", JOptionPane.WARNING_MESSAGE);
            }
            //end if
        });
    }
    
    public void setFile(File f) {
        editor.setFile(f);
    }
    
    public File getFile() {
        return editor.getFile();
    }
    
    public void setAgent(File f) {
        agent = f;
    }
    
    public File getAgent() {
        return agent;
    }
    
    public XABSLContext getXabslContext() {
        return editor.getXABSLContext();
    }
    
    public boolean isChanged() {
        return editor.isChanged();
    }
    
    public boolean save() {
        return editor.save();
    }
    
    public SearchPanel getSearchPanel() {
        return editor.getSearchPanel();
    }
    
    public void search(String s) {
        editor.search(s);
    }
    
    public void setXABSLContext(XABSLContext c) {
        editor.setXABSLContext(c);
    }
    
    public void setTabSize(int s) {
        editor.setTabSize(s);
    }
    
    public void setCompletionProvider(DefaultCompletionProvider cp) {
        editor.setCompletionProvider(cp);
    }
    
    public void setContent(String s) {
        editor.setText(s);
    }
    
    public String getContent() {
        return editor.getText();
    }
    
    public boolean close(boolean force) {
        if(editor.isChanged()) {
            JTabbedPane p = (JTabbedPane)getParent();
            
            int result = JOptionPane.showConfirmDialog(
                getParent(),
                "The file '"+p.getTitleAt(p.indexOfComponent(this))+"' was modified, save changes?",
                "File Not Saved", 
                force ? JOptionPane.YES_NO_OPTION : JOptionPane.YES_NO_CANCEL_OPTION, 
                JOptionPane.WARNING_MESSAGE
            );

            if(result == JOptionPane.YES_OPTION) {
                this.save();
            } else if(result == JOptionPane.CANCEL_OPTION) {
                return false;
            }
        }
        return true;
    }
    
    public void setCarretPosition(int pos) {
        editor.setCarretPosition(pos);
    }
    
    public void jumpToLine(int line) {
        editor.jumpToLine(line);
    }

    @Override
    public void setTransferHandler(TransferHandler newHandler) {
        super.setTransferHandler(newHandler);
        editor.setTransferHandler(newHandler);
    }
}
