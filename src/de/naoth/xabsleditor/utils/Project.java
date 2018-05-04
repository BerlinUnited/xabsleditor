package de.naoth.xabsleditor.utils;

import de.naoth.xabsleditor.parser.XABSLContext;
import de.naoth.xabsleditor.parser.XParser;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class Project
{
    private final File agent;
    private final HashMap<String, File> files = new HashMap<>();
    private final DefaultMutableTreeNode fileTree = new DefaultMutableTreeNode();
    private final XABSLContext context = new XABSLContext();
    private final String XABSL_FILE_ENDING = ".xabsl";

    public Project(File agent) {
        this.agent = agent;
        fileTree.setUserObject(agent.getName());
        updateProject(fileTree, agent.getParentFile());
    }
    
    public void update() {
        files.clear();
        fileTree.removeAllChildren();
        updateProject(fileTree, agent.getParentFile());
    }
    
    private void updateProject(DefaultMutableTreeNode parent, File dir) {
        if (dir == null || !dir.isDirectory()) {
            return;
        }

        Arrays.stream(dir.listFiles()).sorted((File f1, File f2) -> {
            if(f1.isDirectory() && !f2.isDirectory()) {
                return -1;
            } else if(!f1.isDirectory() && f2.isDirectory()) {
                return 1;
            }
            return f1.compareTo(f2);
        }).forEachOrdered((f) -> {
            if(f.isDirectory()) {
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(f);
                updateProject(child, f);
                if(child.getChildCount() > 0) {
                    parent.add(child);
                }
            } else if (f.getName().toLowerCase().endsWith(XABSL_FILE_ENDING)) {
                // updates 'caches'
                files.put(f.getAbsolutePath(), f);
                parent.add(new DefaultMutableTreeNode(f));
                // remove the file ending
                int dotIndex = f.getName().length() - XABSL_FILE_ENDING.length();
                String name = f.getName().substring(0, dotIndex);
                context.getOptionPathMap().put(name, f);

                // parse XABSL file
                try {
                    XParser p = new XParser(context);
                    p.parse(new FileReader(f), f.getAbsolutePath());

                    // HACK: problems with equal file names
                    context.getFileTypeMap().put(name, p.getFileType());
                } catch (Exception e) {
                    System.err.println("Couldn't read the XABSL file " + f.getAbsolutePath());
                }
            }
        });
    } // END updateProject()
    
    public File agent() {
        return agent;
    }
    
    public XABSLContext context() {
        return context;
    }
    
    public boolean contains(File f) {
        return files.containsKey(f.getAbsolutePath());
    }
    
    public DefaultMutableTreeNode tree() {
        return fileTree;
    }
}
