package de.naoth.xabsleditor.utils;

import de.naoth.xabsleditor.events.EventListener;
import de.naoth.xabsleditor.events.EventManager;
import de.naoth.xabsleditor.events.LocateFileEvent;
import de.naoth.xabsleditor.events.NewFileEvent;
import de.naoth.xabsleditor.events.OpenFileEvent;
import de.naoth.xabsleditor.events.ReloadProjectEvent;
import de.naoth.xabsleditor.events.RenameFileEvent;
import de.naoth.xabsleditor.events.UpdateProjectEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class ProjectTree extends javax.swing.JPanel
{
    /** Manager for distributing events. */
    private final EventManager evtManager = EventManager.getInstance();
    
    /**
     * Creates new form ProjectTree
     */
    public ProjectTree() {
        initComponents();
        
        evtManager.add(this);
        
        // hide open option, if not supported!
        if(!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            fileTreePopup.remove(fileTreePopupOpen);
        }

        // set the cell renderer for the projects treeview
        fileTree.setCellRenderer(new DefaultTreeCellRenderer(){
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                if(value instanceof DefaultMutableTreeNode && ((DefaultMutableTreeNode)value).getUserObject() instanceof File) {
                    // leaf nodes (xabsl files) are shown without the '.xabsl' extension; directories are shonw as-is.
                    File file = (File)((DefaultMutableTreeNode)value).getUserObject();
                    if(file.isDirectory()) {
                        value = file.getName();
                    } else {
                        value = file.getName().substring(0, file.getName().length()-6);
                    }
                }
                Component c = super.getTreeCellRendererComponent(tree, value, leaf, expanded, leaf, row, hasFocus);
                c.setForeground(Color.BLACK);
                return c;
            }
        });
    
        // add mouse listener for selecting element in tree
        fileTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    // double click opens the selected file
                    TreePath selPath = fileTree.getPathForLocation(e.getX(), e.getY());
                    openFileFromTree(selPath);
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    // right click shows a context menu of the file tree
                    TreePath selPath = fileTree.getPathForLocation(e.getX(), e.getY());
                    if(selPath != null) {
                        fileTree.setSelectionPath(selPath);
                    }
                    showPopup(e.getX(), e.getY(), selPath != null);
                }
            }
        });
    }
    
    /**
     * Opens the context menu of the file tree.
     * 
     * @param x location on the screen
     * @param y location on the screen
     * @param rename whether or not the 'rename' entry of the menu should be enabled or not
     */
    private void showPopup(int x, int y, boolean rename) {
        fileTreePopupRename.setEnabled(rename);
        fileTreePopupOpen.setEnabled(rename);
        fileTreePopup.show(fileTree, x, y);
    }
    
    /**
     * If the given tree path ends in a file node, a OpenFileEvent is scheduled.
     * 
     * @param selPath a tree path
     */
    private void openFileFromTree(TreePath selPath) {
        if (selPath != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
            if (node == null || !node.isLeaf() || !(node.getUserObject() instanceof File)) {
                return;
            }
            evtManager.publish(new OpenFileEvent(this, (File) node.getUserObject()));
        }
    }

    /**
     * Expands all tree nodes on the given path.
     * 
     * @param node a node of the file tree
     * @param val the node value to compare with
     */
    private void nodeExpander(TreeNode node, String val) {
        // we're only expanding nodes
        if (!node.isLeaf()) {
            // matching?
            if (node.toString().equals(val)) {
                fileTree.expandPath(new TreePath(((DefaultMutableTreeNode) node).getPath()));
            }
            // iterate through childs
            Enumeration childs = node.children();
            while (childs.hasMoreElements()) {
                nodeExpander((TreeNode) childs.nextElement(), val);
            }
        }
    }
    
    /**
     * The event listener for the UpdateProjectEvent.
     * If the project structure/hierarchy changes, the project tree gets updated.
     * 
     * @param e the event containing the projects
     */
    @EventListener
    public void setProjectTreeItems(UpdateProjectEvent e) {
        // get expanded nodes
        Enumeration<TreePath> expendedNodes = fileTree.getExpandedDescendants(new TreePath(((DefaultMutableTreeNode) fileTree.getModel().getRoot()).getPath()));
        
        if(e.projects.isEmpty()) {
            // if projects are empty, set default tree node
            DefaultMutableTreeNode treeNode1 = new DefaultMutableTreeNode("<no project>");
            fileTree.setModel(new DefaultTreeModel(treeNode1));
            fileTree.setRootVisible(true);
        } else {
            // iterate through projects and append to project tree
            fileTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
            e.projects.values().forEach((project) -> {
                ((DefaultMutableTreeNode)fileTree.getModel().getRoot()).add(project.tree());
            });

            // previously expended nodes ...
            if (expendedNodes != null) {
                // get "restored"
                while (expendedNodes.hasMoreElements()) {
                    TreePath param = expendedNodes.nextElement();
                    nodeExpander(((DefaultMutableTreeNode)fileTree.getModel().getRoot()), param.getLastPathComponent().toString());
                }
            }
            
            fileTree.expandRow(0);
            fileTree.expandRow(1);
            fileTree.setRootVisible(false);
        }
    }

    /**
     * Event listener for the LocateFileEvent.
     * When a file location request was submitted, the given file is searched in
     * the project file tree.
     * 
     * @param evt the event containing the file to search for
     */
    @EventListener
    public void locateProjectFiles(LocateFileEvent evt) {
        Enumeration e = ((DefaultMutableTreeNode) fileTree.getModel().getRoot()).breadthFirstEnumeration();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) e.nextElement();
            if (n.getUserObject() instanceof File && n.getUserObject().equals(evt.file)) {
                fileTree.setSelectionPath(new TreePath(n.getPath()));
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fileTreePopup = new javax.swing.JPopupMenu();
        fileTreePopupRefresh = new javax.swing.JMenuItem();
        fileTreePopupNewFile = new javax.swing.JMenuItem();
        fileTreePopupRename = new javax.swing.JMenuItem();
        fileTreePopupOpen = new javax.swing.JMenuItem();
        jScrollPaneFileTree = new javax.swing.JScrollPane();
        fileTree = new javax.swing.JTree();

        fileTreePopupRefresh.setMnemonic('R');
        fileTreePopupRefresh.setText("Refresh (F5)");
        fileTreePopupRefresh.setToolTipText("Reloads the project file tree.");
        fileTreePopupRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileTreePopupRefreshActionPerformed(evt);
            }
        });
        fileTreePopup.add(fileTreePopupRefresh);

        fileTreePopupNewFile.setMnemonic('N');
        fileTreePopupNewFile.setText("New File");
        fileTreePopupNewFile.setToolTipText("Add a new xabsl file");
        fileTreePopupNewFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileTreePopupNewFileActionPerformed(evt);
            }
        });
        fileTreePopup.add(fileTreePopupNewFile);

        fileTreePopupRename.setMnemonic('N');
        fileTreePopupRename.setText("Rename");
        fileTreePopupRename.setToolTipText("Renames selected file.");
        fileTreePopupRename.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileTreePopupRenameActionPerformed(evt);
            }
        });
        fileTreePopup.add(fileTreePopupRename);

        fileTreePopupOpen.setText("Open folder");
        fileTreePopupOpen.setEnabled(false);
        fileTreePopupOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileTreePopupOpenActionPerformed(evt);
            }
        });
        fileTreePopup.add(fileTreePopupOpen);

        setMinimumSize(new java.awt.Dimension(200, 22));
        setLayout(new java.awt.BorderLayout());

        jScrollPaneFileTree.setPreferredSize(new java.awt.Dimension(300, 322));

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("<no project>");
        fileTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        fileTree.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                fileTreeKeyReleased(evt);
            }
        });
        jScrollPaneFileTree.setViewportView(fileTree);

        add(jScrollPaneFileTree, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void fileTreeKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fileTreeKeyReleased
        if(evt.getKeyCode() == KeyEvent.VK_ENTER) {
            openFileFromTree(fileTree.getSelectionPath());
        } else if(evt.getKeyCode() == KeyEvent.VK_F5) {
            evtManager.publish(new ReloadProjectEvent(this));
        }
    }//GEN-LAST:event_fileTreeKeyReleased

    private void fileTreePopupRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileTreePopupRefreshActionPerformed
        evtManager.publish(new ReloadProjectEvent(this));
    }//GEN-LAST:event_fileTreePopupRefreshActionPerformed

    private void fileTreePopupNewFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileTreePopupNewFileActionPerformed
        // reset filechooser config
        if(fileTree.getSelectionPath() != null) {
            evtManager.publish(new NewFileEvent(this, fileTree.getSelectionPath().getLastPathComponent().toString()));
        } else {
            evtManager.publish(new NewFileEvent(this));
        }
    }//GEN-LAST:event_fileTreePopupNewFileActionPerformed

    private void fileTreePopupRenameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileTreePopupRenameActionPerformed
        if(fileTree.getSelectionPath() != null) {
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) fileTree.getSelectionPath().getLastPathComponent();
            if(n.getUserObject() instanceof File) {
                evtManager.publish(new RenameFileEvent(this, (File) n.getUserObject()));
            }
        }
    }//GEN-LAST:event_fileTreePopupRenameActionPerformed

    private void fileTreePopupOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileTreePopupOpenActionPerformed
        if(fileTree.getSelectionPath() != null) {
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) fileTree.getSelectionPath().getLastPathComponent();
            if(n.getUserObject() instanceof File) {
                try {
                    File f = (File) n.getUserObject();
                    if(f.isFile()) { f = f.getParentFile(); }
                    Desktop.getDesktop().open(f);
                } catch (IOException ex) {
                    Logger.getLogger(ProjectTree.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }//GEN-LAST:event_fileTreePopupOpenActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTree fileTree;
    private javax.swing.JPopupMenu fileTreePopup;
    private javax.swing.JMenuItem fileTreePopupNewFile;
    private javax.swing.JMenuItem fileTreePopupOpen;
    private javax.swing.JMenuItem fileTreePopupRefresh;
    private javax.swing.JMenuItem fileTreePopupRename;
    private javax.swing.JScrollPane jScrollPaneFileTree;
    // End of variables declaration//GEN-END:variables
}
