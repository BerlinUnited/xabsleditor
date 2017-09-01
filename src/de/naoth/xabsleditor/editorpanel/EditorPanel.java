package de.naoth.xabsleditor.editorpanel;

import de.naoth.xabsleditor.Tools;
import de.naoth.xabsleditor.graphpanel.GraphPanel;
import de.naoth.xabsleditor.parser.XABSLContext;
import de.naoth.xabsleditor.parser.XParser;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.ShorthandCompletion;

/**
 *
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class EditorPanel extends javax.swing.JPanel implements Iterable<EditorPanelTab>
{
    private GraphPanel graph;
    private EditorPanelTab activeTab = null;
    
    private int tabSize = 2;
    private boolean showCloseButtons = false;

    /**
     * Creates new form EditorPanel
     */
    public EditorPanel() {
        initComponents();
        // add "tab-switch" listener
        tabs.addChangeListener((ChangeEvent e) -> {
            activeTab = (EditorPanelTab) tabs.getSelectedComponent();
            graph.refreshGraph();
        });
        
        // add right-click-behavior of the tabs
        tabs.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {}

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {
                // right-click only
                if(e.getButton() == MouseEvent.BUTTON3) {
                    int activeTabIndex = tabs.getUI().tabForCoordinate(tabs, e.getX(), e.getY());
                    if(activeTabIndex >= 0) {
                        tabs.setSelectedIndex(activeTabIndex);
                        tabPopupMenu.show(tabs, e.getX(), e.getY());
                    }
                }

            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabPopupMenu = new javax.swing.JPopupMenu();
        tabPopupMenu_Close = new javax.swing.JMenuItem();
        tabPopupMenu_CloseAll = new javax.swing.JMenuItem();
        tabPopupMenu_CloseOthers = new javax.swing.JMenuItem();
        tabs = new javax.swing.JTabbedPane();

        tabPopupMenu_Close.setText("Close");
        tabPopupMenu_Close.setToolTipText("Closes active the tab");
        tabPopupMenu_Close.setFocusPainted(true);
        tabPopupMenu_Close.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tabPopupMenu_CloseActionPerformed(evt);
            }
        });
        tabPopupMenu.add(tabPopupMenu_Close);

        tabPopupMenu_CloseAll.setText("Close all");
        tabPopupMenu_CloseAll.setToolTipText("Closes all opened tabs.");
        tabPopupMenu_CloseAll.setFocusPainted(true);
        tabPopupMenu_CloseAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tabPopupMenu_CloseAllActionPerformed(evt);
            }
        });
        tabPopupMenu.add(tabPopupMenu_CloseAll);

        tabPopupMenu_CloseOthers.setText("Close others");
        tabPopupMenu_CloseOthers.setToolTipText("Closes all opened tabs, except for the active one.");
        tabPopupMenu_CloseOthers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tabPopupMenu_CloseOthersActionPerformed(evt);
            }
        });
        tabPopupMenu.add(tabPopupMenu_CloseOthers);

        setLayout(new java.awt.BorderLayout());
        add(tabs, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void tabPopupMenu_CloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tabPopupMenu_CloseActionPerformed
        closeActiveTab(false);
    }//GEN-LAST:event_tabPopupMenu_CloseActionPerformed

    private void tabPopupMenu_CloseAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tabPopupMenu_CloseAllActionPerformed
        closeAllTabs(false);
    }//GEN-LAST:event_tabPopupMenu_CloseAllActionPerformed

    private void tabPopupMenu_CloseOthersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tabPopupMenu_CloseOthersActionPerformed
        if(activeTab != null) {
            closeTabsExcept(activeTab, false);
        }
    }//GEN-LAST:event_tabPopupMenu_CloseOthersActionPerformed

    public void setGraph(GraphPanel g) {
        graph = g;
    }
    
    public void setTabSize(int size) {
        tabSize = size;
        for (EditorPanelTab tab : this) {
            tab.setTabSize(size);
        }
    }
    
    public int getTabSize() {
        return tabSize;
    }
    
    public void setShowCloseButtons(boolean b) {
        showCloseButtons = b;
        for (EditorPanelTab tab : this) {
            Component cbtn = tabs.getTabComponentAt(tabs.indexOfComponent(tab));
            if(cbtn instanceof ButtonTabComponent) {
                ((ButtonTabComponent)cbtn).setVisible(b);
            }
        }
    }
    
    public boolean getShowCloseButtons() {
        return showCloseButtons;
    }
    
    public void openFile(File f) {
        if (f == null) {
            createDocumentTab(null, null, null);
        } else {
            // find and select already opened file
            for (EditorPanelTab tab : this) {
                if(tab.getFile().equals(f)) {
                    tabs.setSelectedComponent(tab);
                    return;
                }
            }
            // ... otherwise create new tab
            File agentsFile = Tools.getAgentFileForOption(f);
            XABSLContext newContext = null;

            if (agentsFile != null) {
                newContext = loadXABSLContext(agentsFile.getParentFile(), null);
            }

            createDocumentTab(f, newContext, agentsFile);
        }
    }
    
    public void openFile(File f, int position) {
        openFile(f);
        activeTab.setCarretPosition(position);
    }

    private XABSLContext loadXABSLContext(File folder, XABSLContext context) {
        if (context == null) {
            context = new XABSLContext();
        }

        final String XABSL_FILE_ENDING = ".xabsl";

        File[] fileList = folder.listFiles();
        for (File file : fileList) {
            if (file.isDirectory()) {
                loadXABSLContext(file, context);
            } else if (file.getName().toLowerCase().endsWith(XABSL_FILE_ENDING)) {
                // remove the file ending
                int dotIndex = file.getName().length() - XABSL_FILE_ENDING.length();
                String name = file.getName().substring(0, dotIndex);
                context.getOptionPathMap().put(name, file);

                // parse XABSL file
                try {
                    //System.out.println("parse: " + file.getName()); // debug stuff
                    XParser p = new XParser(context);
                    p.parse(new FileReader(file), file.getAbsolutePath());

                    // HACK: problems with equal file names
                    context.getFileTypeMap().put(name, p.getFileType());
                } catch (Exception e) {
                    System.err.println("Couldn't read the XABSL file " + file.getAbsolutePath());
                }
            }
        }//end for

        return context;
    }//end loadXABSLContext

    private EditorPanelTab createDocumentTab(File file, XABSLContext context, File agentsFile) {
        try {
            // create new document
            EditorPanelTab tab = new EditorPanelTab(file);
            tab.setXABSLContext(context);
            tab.setAgent(agentsFile);
            tab.setTabSize(tabSize);
            tab.setCompletionProvider(createCompletitionProvider(context));
            tab.setTransferHandler(getTransferHandler());
            if (file == null) {
                tabs.addTab("New " + tabs.getTabCount(), null, tab, "New xabsl file");
            } else {
                tabs.addTab(file.getName(), null, tab, file.getAbsolutePath());
            }

            // NOTE: 'cause we're adding the close button, the tabpane has the double amount of components than tabs.
            //        To get the correct tab count, use "getTabCount()"!
            // adds the close button" to the tab
            tabs.setTabComponentAt(
                    tabs.indexOfComponent(tab),
                    new ButtonTabComponent(tabs, showCloseButtons)
            );
            tabs.setSelectedComponent(tab);

            // update the other openend editors
            for (EditorPanelTab t : this) {
                if (t != tab) {
                    t.setXABSLContext(context);
                }
            }
            return tab;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.toString(), "The file could not be read.", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        return null;
    }//end createDocumentTab
  
    private DefaultCompletionProvider createCompletitionProvider(XABSLContext context) {
        
        DefaultCompletionProvider provider = new DefaultCompletionProvider() {
            @Override
            protected boolean isValidChar(char ch) {
                return super.isValidChar(ch) || ch == '.';
            }
        };

        provider.setParameterizedCompletionParams('(', ", ", ')');

        if (context != null) {
            for (XABSLContext.XABSLSymbol symbol : context.getSymbolMap().values()) {
                if (symbol.getParameter().isEmpty()) {
                    provider.addCompletion(new XABSLSymbolSimpleCompletion(provider, symbol));
                } else {
                    provider.addCompletion(new XABSLSymbolCompletion(provider, symbol));
                }
                //System.out.println(symbol); // debug stuff
            }//end for

            for (XABSLContext.XABSLOption option : context.getOptionMap().values()) {
                provider.addCompletion(new XABSLOptionCompletion(provider, option));
            }//end for

            for (XABSLContext.XABSLEnum xabslEnum : context.getEnumMap().values()) {
                for (String param : xabslEnum.getElements()) {
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

    public void closeActiveTab(boolean force) {
        if(activeTab != null) {
            // something changed ...
            if(activeTab.close(force) || force) {
                tabs.remove(activeTab);
            }
        }
    }
    
    public ArrayList<File> closeAllTabs(boolean force) {
        ArrayList<File> closedFiles = new ArrayList<>();
        for (Iterator<EditorPanelTab> it = this.iterator(); it.hasNext();) {
            EditorPanelTab tab = it.next();
            if(tab.close(force) || force) {
                if(tab.getFile() != null) {
                    closedFiles.add(tab.getFile());
                }
                tabs.remove(tab);
            } else {
                tabs.setSelectedComponent(tab);
                break;
            }
        }
        return closedFiles;
    }
    
    public void closeTabsExcept(EditorPanelTab t, boolean force) {
        for (EditorPanelTab tab : this) {
            if(tab.equals(t)) {
                continue;
            } else if(tab.close(force) || force) {
                tabs.remove(tab);
            } else {
                tabs.setSelectedComponent(tab);
                break;
            }
        }
    }
    
    public void save() {
        if(activeTab != null && activeTab.save()) {
            graph.refreshGraph();
        }
    }

    public void saveAs() {
        if(activeTab != null) {
            File old = activeTab.getFile();
            activeTab.setFile(null);
            if(activeTab.save()) {
                graph.refreshGraph();
            } else {
                activeTab.setFile(old);
            }
        }
    }
    
    public boolean hasOpenFiles() {
        return tabs.getTabCount() > 0;
    }
    
    public ArrayList<File> getOpenFiles() {
        ArrayList<File> files = new ArrayList<>();
        for (EditorPanelTab tab : this) {
            files.add(tab.getFile());
        }
        return files;
    }
    
    public File getActiveFile() {
        return activeTab == null ? null : activeTab.getFile();
    }
    
    public String getActiveContent() {
        return activeTab == null ? null : activeTab.getContent();
    }
    
    public XABSLContext getActiveXABSLContext() {
        return activeTab == null ? null : activeTab.getXabslContext();
    }
    
    public File getActiveAgent() {
        return activeTab == null ? null : activeTab.getAgent();
    }
    
    public void setActiveCompletionProvider(DefaultCompletionProvider completionProvider) {
        if(activeTab != null) {
            activeTab.setCompletionProvider(completionProvider);
        }
    }
    
    public EditorPanelTab getActiveTab() {
        return activeTab;
    }
    
    public void searchInActiveTab() {
        if(activeTab != null) {
            activeTab.getSearchPanel().setVisible(true);
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPopupMenu tabPopupMenu;
    private javax.swing.JMenuItem tabPopupMenu_Close;
    private javax.swing.JMenuItem tabPopupMenu_CloseAll;
    private javax.swing.JMenuItem tabPopupMenu_CloseOthers;
    private javax.swing.JTabbedPane tabs;
    // End of variables declaration//GEN-END:variables

    @Override
    public Iterator<EditorPanelTab> iterator() {
        // get components
        Component[] t = this.tabs.getComponents();
        // filter for tabs
        ArrayList<EditorPanelTab> it_tabs = new ArrayList<>();
        for (int i = 0; i < t.length; i++) {
            if(t[i] instanceof EditorPanelTab) {
                it_tabs.add((EditorPanelTab) t[i]);
            }
        }
        // use arraylist-iterator
        return it_tabs.iterator();
    }
}
