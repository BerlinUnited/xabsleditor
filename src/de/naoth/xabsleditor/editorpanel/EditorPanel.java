package de.naoth.xabsleditor.editorpanel;

import de.naoth.xabsleditor.FileDrop;
import de.naoth.xabsleditor.Main;
import de.naoth.xabsleditor.events.EventManager;
import de.naoth.xabsleditor.events.LocateFileEvent;
import de.naoth.xabsleditor.events.RefreshGraphEvent;
import de.naoth.xabsleditor.parser.XABSLContext;
import de.naoth.xabsleditor.utils.FileWatcher;
import de.naoth.xabsleditor.utils.Project;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.plaf.TabbedPaneUI;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class EditorPanel extends javax.swing.JPanel implements Iterable<EditorPanelTab>
{
    /** Manager for distributing events. */
    EventManager evtManager = EventManager.getInstance();

    private FileWatcher watcher;
    private EditorPanelTab activeTab = null;
    
    private int tabSize = 2;
    private float fontSize = 14;
    private boolean showCloseButtons = false;
    private boolean showWhitespaces = false;

    /**
     * Creates new form EditorPanel
     */
    public EditorPanel() {
        initComponents();
        // register event handler
        evtManager.add(this);
        // add "tab-switch" listener
        tabs.addChangeListener((ChangeEvent e) -> {
            activeTab = (EditorPanelTab) tabs.getSelectedComponent();
            if(activeTab != null) {
                activeTab.select();
                evtManager.publish(new RefreshGraphEvent(activeTab));
            }
        });
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        // handle addition/removal of tabs
        tabs.addContainerListener(new ContainerListener() {
            @Override
            public void componentAdded(ContainerEvent e) {
                if(e.getChild() instanceof EditorPanelTab) {
                    // create a menu entry for the newly added tab
                    TabMenuItem item = new TabMenuItem((EditorPanelTab) e.getChild(), tabs.getTitleAt(tabs.indexOfComponent(e.getChild())));
                    // select tab of the selected menu entry
                    item.addActionListener((a) -> { tabs.setSelectedComponent(item.tab); });
                    // determine index, where to add the new menu entry so that the menu is sorted alphabetically
                    int i = 0;
                    for (; i < tabPopupMenu_Tabs.getMenuComponentCount(); i++) {
                        Component c = tabPopupMenu_Tabs.getMenuComponent(i);
                        if(c instanceof TabMenuItem && ((TabMenuItem)c).getText().compareTo(item.getText())>0) {
                            break;
                        }
                    }
                    // add menu entry at the appropiate menu index
                    tabPopupMenu_Tabs.add(item, i);
                }
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                if(e.getChild() instanceof EditorPanelTab) {
                    // get the menu entry associated with the removed tab
                    Optional<Component> item = Arrays.asList(tabPopupMenu_Tabs.getMenuComponents()).stream().filter((t) -> {
                        return ((TabMenuItem)t).getTab().equals(e.getChild());
                    }).findFirst();
                    // remove menu entry of the removed tab
                    if(item.isPresent()) { tabPopupMenu_Tabs.remove(item.get()); }
                }
            }
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
        
        // disable default traveral keys and setup "correct" ctrl-tab behavior
        tabs.setFocusTraversalKeysEnabled(false);

        // Add keys to the tab's input map
        InputMap inputMap = tabs.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke("ctrl TAB"), "navigateNext");
        inputMap.put(KeyStroke.getKeyStroke("ctrl shift TAB"), "navigatePrevious");
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
        tabPopupMenu_Sep = new javax.swing.JPopupMenu.Separator();
        tabPopupMenu_Locate = new javax.swing.JMenuItem();
        tabPopupMenu_Tabs = new javax.swing.JMenu();
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
        tabPopupMenu.add(tabPopupMenu_Sep);

        tabPopupMenu_Locate.setText("Locate file in project tree");
        tabPopupMenu_Locate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tabPopupMenu_LocateActionPerformed(evt);
            }
        });
        tabPopupMenu.add(tabPopupMenu_Locate);

        tabPopupMenu_Tabs.setText("Select Tab");
        tabPopupMenu.add(tabPopupMenu_Tabs);

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

    private void tabPopupMenu_LocateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tabPopupMenu_LocateActionPerformed
        if(activeTab != null) {
            locateTabInProjectTree(activeTab);
        }
    }//GEN-LAST:event_tabPopupMenu_LocateActionPerformed

    public void setTabSize(int size) {
        tabSize = size;
        for (EditorPanelTab tab : this) {
            tab.setTabSize(size);
        }
    }
    
    public int getTabSize() {
        return tabSize;
    }
    
    public void setTabLayout(int layout) {
        tabs.setTabLayoutPolicy(layout);
    }
    
    public int getTabLayout() {
        return tabs.getTabLayoutPolicy();
    }
    
    public void setTabUI(TabbedPaneUI ui) {
        tabs.setUI(ui);
    }
    
    public TabbedPaneUI getTabUI() {
        return tabs.getUI();
    }
    
    public void setFontSize(float size) {
        fontSize = size;
        for (EditorPanelTab tab : this) {
            tab.setFontSize(size);
        }
    }
    
    public float getFontSize() {
        return fontSize;
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
    
    public void setShowWhitespaces(boolean show) {
        showWhitespaces = show;
        for (EditorPanelTab tab : this) {
            tab.setShowWhitespaces(show);
        }
    }
    
    public boolean getShowWhitespaces() {
        return showWhitespaces;
    }

    public void openFile(File file, Project project, int carret, String search) {
        if (file == null) {
            createDocumentTab(null, null);
        } else if(project == null) {
            createDocumentTab(file, null);
        } else {
            // find and select already opened file
            for (EditorPanelTab tab : this) {
                if(tab.getFile() != null && tab.getFile().equals(file)) {
                    tabs.setSelectedComponent(tab);
                    return;
                }
            }
            // ... otherwise create new tab
            createDocumentTab(file, project);
            
            if(search == null) {
                activeTab.setCarretPosition(carret);
            } else {
                activeTab.search(search);
            }
        }
    }

    private EditorPanelTab createDocumentTab(File file, Project project) {
        try {
            // create new document
            EditorPanelTab tab = new EditorPanelTab(file);
            tab.setTabSize(tabSize);
            tab.setFontSize(fontSize);
            tab.setShowWhitespaces(showWhitespaces);
            tab.setFileWatcher(watcher);
            if(project != null) {
                tab.setXABSLContext(project.context());
                tab.setAgent(project.agent());
                tab.setCompletionProvider(project.completionProvider());
            }
            if (file == null) {
                tabs.addTab("New " + tabs.getTabCount(), null, tab, "New xabsl file");
            } else {
                tabs.addTab(file.getName(), null, tab, file.getAbsolutePath());
            }
            
            // install file drop on this tab
            Container frame = getTopLevelAncestor();
            if(frame != null && frame instanceof Main) {
                // don't show any "special" component border
                new FileDrop(tab, tab.getBorder(), true, ((Main)frame).dropHandler);
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
            // TODO: distinguish between different projects!?
            for (EditorPanelTab t : this) {
                if (t != tab && project != null) {
                    t.setXABSLContext(project.context());
                }
            }
            return tab;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.toString(), "The file could not be read.", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        return null;
    }//end createDocumentTab
  
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
    
    public void locateTabInProjectTree(EditorPanelTab t) {
        evtManager.publish(new LocateFileEvent(this, t.getFile()));
    }
    
    private final List<DefaultMutableTreeNode> getSearchNodes(DefaultMutableTreeNode root) {
            List<DefaultMutableTreeNode> searchNodes = new ArrayList<DefaultMutableTreeNode>();

            Enumeration<?> e = root.preorderEnumeration();
            while(e.hasMoreElements()) {
                searchNodes.add((DefaultMutableTreeNode)e.nextElement());
            }
            return searchNodes;
        }
    
    public void save() {
        save(System.getProperty("user.home"));
    }
    
    public void save(String defaultDirectory) {
        if(activeTab != null && activeTab.save(defaultDirectory)) {
            evtManager.publish(new RefreshGraphEvent(activeTab));
        }
    }

    public void saveAs() {
        saveAs(System.getProperty("user.home"));
    }
    
    public void saveAs(String defaultDirectory) {
        if(activeTab != null) {
            File old = activeTab.getFile();
            activeTab.setFile(null);
            if(activeTab.save(defaultDirectory)) {
                evtManager.publish(new RefreshGraphEvent(activeTab));
            } else {
                activeTab.setFile(old);
            }
        }
    }
    
    public boolean hasOpenFiles() {
        return tabs.getTabCount() > 0;
    }
    
    public ArrayList<EditorPanelTab> hasOpenUnsavedFiles() {
        ArrayList<EditorPanelTab> unsavedtabs = new ArrayList<>();
        for (EditorPanelTab tab : this) {
            if(tab.isChanged() && tab.getFile() != null) {
                unsavedtabs.add(tab);
            }
        }
        return unsavedtabs;
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
        return activeTab == null ? null : activeTab.getXABSLContext();
    }
    
    public File getActiveAgent() {
        return activeTab == null ? null : activeTab.getAgent();
    }

    public EditorPanelTab getActiveTab() {
        return activeTab;
    }
    
    public void searchInActiveTab() {
        if(activeTab != null) {
            activeTab.getSearchPanel().setVisible(true);
        }
    }
    
    public void setFileWatcher(FileWatcher w) {
        watcher = w;
        for (EditorPanelTab tab : this) {
            tab.setFileWatcher(w);
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPopupMenu tabPopupMenu;
    private javax.swing.JMenuItem tabPopupMenu_Close;
    private javax.swing.JMenuItem tabPopupMenu_CloseAll;
    private javax.swing.JMenuItem tabPopupMenu_CloseOthers;
    private javax.swing.JMenuItem tabPopupMenu_Locate;
    private javax.swing.JPopupMenu.Separator tabPopupMenu_Sep;
    private javax.swing.JMenu tabPopupMenu_Tabs;
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

    /**
     * Menu item with extra field holding reference to a associated tab.
     */
    class TabMenuItem extends JMenuItem
    {
        private final EditorPanelTab tab;

        public TabMenuItem(EditorPanelTab tab, String text) {
            super(text);
            this.tab = tab;
        }

        public EditorPanelTab getTab() {
            return tab;
        }
    }
}
