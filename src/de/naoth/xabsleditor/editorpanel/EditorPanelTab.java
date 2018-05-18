package de.naoth.xabsleditor.editorpanel;

import de.naoth.xabsleditor.events.EventManager;
import de.naoth.xabsleditor.events.OpenFileEvent;
import de.naoth.xabsleditor.parser.XABSLContext;
import de.naoth.xabsleditor.parser.XABSLOptionContext;
import de.naoth.xabsleditor.utils.FileWatcher;
import de.naoth.xabsleditor.utils.FileWatcherListener;
import java.io.File;
import java.nio.file.WatchEvent;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;

/**
 *
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class EditorPanelTab extends XEditorPanel implements FileWatcherListener
{
    /** Manager for distributing events. */
    private final EventManager evtManager = EventManager.getInstance();
    private File agent;
    FileWatcher watcher;
    private boolean externalChange = false;
    
    public EditorPanelTab(File f) {
        super(f);

        setCarretPosition(0);
        
        // change tab title to indicate the modified content
        addDocumentChangedListener((XEditorPanel document) -> {
            JTabbedPane p = (JTabbedPane)getParent();
            if(p != null) {
                int idx = p.indexOfComponent(this);
                String title = p.getTitleAt(idx);
                String suffix = " *";
                if (document.isChanged() && !title.endsWith(suffix)) {
                    p.setTitleAt(idx, title + suffix);
                    p.getTabComponentAt(idx).revalidate();
                } else if(!document.isChanged() && title.endsWith(suffix)) {
                    p.setTitleAt(idx, p.getTitleAt(idx).substring(0, title.length()-2));
                    p.getTabComponentAt(idx).revalidate();
                }
            }
        });
        
        addHyperlinkListener((HyperlinkEvent e) -> {
            String element = e.getDescription();
            element = element.replace("no protocol: ", "");
            File file = null;
            if (getXABSLContext() != null) {
                file = getXABSLContext().getOptionPathMap().get(element);
            }
            int position = 0;
            // try to open symbol
            boolean symbolWasFound = false;
            if (file == null) {
                XABSLContext.XABSLSymbol symbol = getXABSLContext().getSymbolMap().get(element);
                if (symbol != null && symbol.getDeclarationSource() != null) {
                    file = new File(symbol.getDeclarationSource().fileName);
                    position = symbol.getDeclarationSource().offset;
                    symbolWasFound = true;
                }
            }//end if
            if (file == null) {
                XABSLOptionContext.State state = getStateMap().get(element);
                if (state != null) {
                    setCarretPosition(state.offset);
                    symbolWasFound = true;
                }
            } //end if
            if (file != null) {
                evtManager.publish(new OpenFileEvent(this, file, position));
            }
            if (file == null && !symbolWasFound) {
                JOptionPane.showMessageDialog(null, "Could not find the file for option, symbol or state",
                        "Not found", JOptionPane.WARNING_MESSAGE);
            }
            //end if
        });
    }
    
    @Override
    public void setFile(File f) {
        fileWatcherUnregister();
        super.setFile(f);
        fileWatcherRegister();
    }
    
    public void setFileWatcher(FileWatcher w) {
        fileWatcherUnregister();
        watcher = w;
        fileWatcherRegister();
    }
    
    private void fileWatcherRegister() {
        if(watcher != null && getFile() != null) {
            watcher.addListener(this);
        }
    }
    
    private void fileWatcherUnregister() {
        if(watcher != null && getFile() != null) {
            watcher.removeListener(this);
        }
    }
    
    public void setAgent(File f) {
        agent = f;
    }
    
    public File getAgent() {
        return agent;
    }
    
    @Override
    public boolean save(String defaultDirectory) {
        // unregister filewatcher - the file could be saved with a new name!
        fileWatcherUnregister();
        boolean result = super.save(defaultDirectory);
        // only if saveing was successfull, update tab title/tooltip
        if(result) {
            updateTabTitle();
        }
        // register filewatcher with the "new" name
        fileWatcherRegister();
        return result;
    }
    
    /**
     * Updates the title and tooltip of the parent JTabbedPane.
     */
    protected void updateTabTitle() {
        JTabbedPane p = (JTabbedPane)getParent();
        if(p != null) {
            int idx = p.indexOfComponent(this);
            if(idx != -1) {
                p.setTitleAt(idx, this.getFile().getName());
                p.setToolTipTextAt(idx, this.getFile().getAbsolutePath());
            }
        }
    }
    
    public boolean close(boolean force) {
        if(isChanged()) {
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
    
    private boolean isSelected() {
        return getParent() != null 
               && ((JTabbedPane)getParent()).getSelectedComponent() != null
               && ((JTabbedPane)getParent()).getSelectedComponent().equals(this);
    }

    @Override
    public void xabslFileChanged(File toFile, WatchEvent.Kind kind) {
        // if we already had an external-change-notification, it doesn't need to be notified again
        synchronized(this) {
            if(!externalChange) {
                externalChange = true;
                if(isSelected()) { externalChangeDialog(); }
            }
        }
    }
    
    public void resetExternalChangeFlag() {
        externalChange = false;
    }
    
    public void select() {
        externalChangeDialog();
    }
    
    private void externalChangeDialog() {
        // show dialog only if there were external changes
        if(externalChange) {
            // run in seperate thread
            // this has an advantage, that following external modification doesn't trigger additional dialogs
            // (before the first one isn't closed)
            SwingUtilities.invokeLater(() -> {
                int caret = getCarretPosition();
                int result;
                if (getFile().exists()) {
                    result = JOptionPane.showConfirmDialog(getParent(),
                            "The file " + getFile().getName() + " was modified externally. Reload?\nAll (other) changes are lost!",
                            "External modification", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    // reload file if requested
                    reloadFromFile(result == JOptionPane.YES_OPTION);
                } else {
                    result = JOptionPane.showConfirmDialog(getParent(),
                            "The file " + getFile().getName() + " was removed. Close tab?\nAll (other) changes are lost!",
                            "External deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (result == JOptionPane.YES_OPTION) {
                        renewHashCode();
                        ((JTabbedPane) getParent()).setSelectedComponent(this);
                        ((EditorPanel) (getParent().getParent())).closeActiveTab(true);
                    } else {
                        renewHashCode("");
                    }
                }

                resetExternalChangeFlag();
                setCarretPosition(caret);
            });
        }
    }
}
