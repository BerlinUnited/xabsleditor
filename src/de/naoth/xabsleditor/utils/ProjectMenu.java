package de.naoth.xabsleditor.utils;

import de.naoth.xabsleditor.events.EventListener;
import de.naoth.xabsleditor.events.EventManager;
import de.naoth.xabsleditor.events.OpenFileEvent;
import de.naoth.xabsleditor.events.UpdateProjectEvent;
import de.naoth.xabsleditor.parser.XABSLContext;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Enumeration;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class ProjectMenu extends JMenu
{
    /** Manager for distributing events. */
    private final EventManager evtManager = EventManager.getInstance();
    /** The default menu item, if no project is loaded. */
    private final JMenuItem emptyItem;
    /** xabsl files icons */
    private final ImageIcon icon_xabsl_agent = new ImageIcon(getClass().getResource("/de/naoth/xabsleditor/res/xabsl_agents_file.png"));
    private final ImageIcon icon_xabsl_option = new ImageIcon(getClass().getResource("/de/naoth/xabsleditor/res/xabsl_option_file.png"));
    private final ImageIcon icon_xabsl_symbol = new ImageIcon(getClass().getResource("/de/naoth/xabsleditor/res/xabsl_symbols_file.png"));

    /**
     * Contructor, initializes the default (empty) menu item.
     */
    public ProjectMenu() {
        evtManager.add(this);
        emptyItem = new javax.swing.JMenuItem();
        emptyItem.setFont(emptyItem.getFont().deriveFont((emptyItem.getFont().getStyle() | java.awt.Font.ITALIC)));
        emptyItem.setText("empty");
        add(emptyItem);
    }
    
    /**
     * Event listener for the 'UpdateProjectEvent'.
     * Updates the menu structure, if something change in the project hierarchy/structure.
     * 
     * @param e the event object containing the projects.
     */
    @EventListener
    public void updateProjectMenu(UpdateProjectEvent e) {
        removeAll();
        if(e.projects.isEmpty()) {
            // if projects are empty, set default tree node
            add(emptyItem);
        } else {
            // iterate through projects and append to project tree
            e.projects.values().forEach((Project project) -> {
                JMenu miAgent = new JMenu(project.agent().getName());
                addMenuItems(miAgent, project.tree(), project.context());
                add(miAgent);
            });
        }
    }
    
    /**
     * Adds recursivly the menu items based on the projects hierarchy.
     * 
     * @param parent the parent menu item for the current hierarchy level
     * @param node the parent hierarchy node of the current level
     * @param c XABSLContext
     */
    private void addMenuItems(JMenu parent, DefaultMutableTreeNode node, XABSLContext c) {
        if(node.getChildCount() > 0) {
            Enumeration e = node.children();
            while (e.hasMoreElements()) {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode) e.nextElement();
                if(child.getUserObject() instanceof File) {
                    File file = (File) child.getUserObject();
                    if (file.isDirectory()) {
                        JMenu childMenu = new JMenu(file.getName());
                        addMenuItems(childMenu, child, c);
                        if (childMenu.getMenuComponentCount() > 0) {
                            parent.add(childMenu);
                        }
                    } else {
                        // remove the file ending
                        int dotIndex = file.getName().length() - 6;
                        final String name = file.getName().substring(0, dotIndex);
                        // create new item
                        JMenuItem miOptionOpener = setJMenuItemXabslFont(new JMenuItem(name));
                        // agent, option or symbol file
                        String type = c.getFileTypeMap().get(name);
                        if(type != null)
                        {
                          if(type.equals("option")) {
                            miOptionOpener.setIcon(icon_xabsl_option);
                          } else if(type.equals("symbol")) {
                            miOptionOpener.setIcon(icon_xabsl_symbol);
                          } else if(type.equals("agent")) {
                            miOptionOpener.setIcon(icon_xabsl_agent);
                          }
                        }//end if

                        miOptionOpener.addActionListener(new ActionListener()
                        {
                          @Override
                          public void actionPerformed(ActionEvent e)
                          {
                            evtManager.publish(new OpenFileEvent(this, c.getOptionPathMap().get(name)));
                          }
                        });
                        parent.add(miOptionOpener);
                    }
                }
            }
        }
    }
    
    /**
     * Sets the font of a menu item.
     * 
     * @param jMenuItem the menu item, where the font should be changed
     * @return the modified menu item
     */
    private JMenuItem setJMenuItemXabslFont(JMenuItem jMenuItem) {
        jMenuItem.setFont(jMenuItem.getFont().deriveFont((jMenuItem.getFont().getStyle() | java.awt.Font.ITALIC)));
        return jMenuItem;
    }
}
