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
import java.util.Iterator;
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
    private final EventManager evtManager = EventManager.getInstance();
    private final JMenuItem emptyItem;
    
    // xabsl files icons
    private final ImageIcon icon_xabsl_agent = new ImageIcon(this.getClass().getResource("../res/xabsl_agents_file.png"));
    private final ImageIcon icon_xabsl_option = new ImageIcon(this.getClass().getResource("../res/xabsl_option_file.png"));
    private final ImageIcon icon_xabsl_symbol = new ImageIcon(this.getClass().getResource("../res/xabsl_symbols_file.png"));


    public ProjectMenu() {
        evtManager.add(this);
        emptyItem = new javax.swing.JMenuItem();
        emptyItem.setFont(emptyItem.getFont().deriveFont((emptyItem.getFont().getStyle() | java.awt.Font.ITALIC)));
        emptyItem.setText("empty");
        add(emptyItem);
    }
    
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
//                addFilesToMenu(miAgent, agentFile.getParentFile(), context);
                add(miAgent);
            });
        }
    }
    
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
                        ///*
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
                        //*/
                        parent.add(miOptionOpener);
                    }
                }
            }
        }
    }
    
    private JMenuItem setJMenuItemXabslFont(JMenuItem jMenuItem) {
        jMenuItem.setFont(jMenuItem.getFont().deriveFont((jMenuItem.getFont().getStyle() | java.awt.Font.ITALIC)));
        return jMenuItem;
    }

/*
  private void addFilesToMenu(JMenu miParent, File folder, final XABSLContext context)
  {
    if(miParent == null || folder == null || context == null)
        return;

    File[] fileList = folder.listFiles();
    // sort entries alphabetically, with directory first
    Arrays.sort(fileList, (File f1, File f2)->{
        if(f1.isDirectory() && !f2.isDirectory()) {
            return -1;
        } else if(!f1.isDirectory() && f2.isDirectory()) {
            return 1;
        }
        return f1.getName().compareTo(f2.getName());
    });
    // iterate through files and add them to menu
    for (final File file : fileList)
    {
      if (file.isDirectory())
      {
        JMenu miChild = new JMenu(file.getName());
        addFilesToMenu(miChild, file, context);
        if(miChild.getMenuComponentCount() > 0) {
          miParent.add(miChild);
        }
      }
      else if (file.getName().toLowerCase().endsWith(XABSL_FILE_ENDING))
      {
        // remove the file ending
        int dotIndex = file.getName().length() - XABSL_FILE_ENDING.length();
        final String name = file.getName().substring(0, dotIndex);

        if(!context.getOptionPathMap().containsKey(name)) {
          continue;
        }

        // create new item
        JMenuItem miOptionOpener = setJMenuItemXabslFont(new JMenuItem(name));
      
        // agent, option or symbol file
        String type = context.getFileTypeMap().get(name);
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
            evtManager.publish(new OpenFileEvent(this, context.getOptionPathMap().get(name)));
          }
        });
        miParent.add(miOptionOpener);
      }
    }//end for
  }//end addFilesToMenu
*/
}
