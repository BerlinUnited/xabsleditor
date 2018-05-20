package de.naoth.xabsleditor.utils;

import de.naoth.xabsleditor.parser.XABSLContext;
import de.naoth.xabsleditor.parser.XParser;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Represents a xabsl project.
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class Project
{
    /** The (root) 'agents.xabsl' file of this project. */
    private final File agent;
    /** All project xabsl files. */
    private final HashMap<String, File> files = new HashMap<>();
    /** Hierarchy of the project xabsl files. */
    private final DefaultMutableTreeNode fileTree = new DefaultMutableTreeNode();
    /** The xabsl context (option, symbols, ...) of this project. */
    private final XABSLContext context = new XABSLContext();
    /** The xabsl extension. */
    private final String XABSL_FILE_ENDING = ".xabsl";

    /**
     * Constructor, reads the project of the given agent file and creates the
     * file hierarchy.
     * 
     * @param agent the root 'agents.xabsl' file
     */
    public Project(File agent) {
        this.agent = agent;
        fileTree.setUserObject(agent.getName());
        updateProject(fileTree, agent.getParentFile());
    }
    
    /**
     * Public method for updating (re-loading) the project.
     */
    public void update() {
        files.clear();
        fileTree.removeAllChildren();
        updateProject(fileTree, agent.getParentFile());
    }
    
    /**
     * Constructs the file hierarchy of this project (recursively) and caches the
     * project files. Also updates the projects xabsl context.
     * 
     * @param parent the current file hierarchy file node
     * @param dir the File object for the corresponding file hierarchy node
     */
    private void updateProject(DefaultMutableTreeNode parent, File dir) {
        // not a directory
        if (dir == null || !dir.isDirectory()) {
            return;
        }
        // order directory entries by name and directories first
        Arrays.stream(dir.listFiles()).sorted((File f1, File f2) -> {
            if(f1.isDirectory() && !f2.isDirectory()) {
                return -1;
            } else if(!f1.isDirectory() && f2.isDirectory()) {
                return 1;
            }
            return f1.compareTo(f2);
        }).forEachOrdered((f) -> {
            // add directories recursively to the file hierarchy
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
    
    /**
     * Returns the (root) 'agent.xabsl' file of this project.
     * 
     * @return the root 'agent.xabsl' file
     */
    public File agent() {
        return agent;
    }
    
    /**
     * Returns the xabsl context of this project.
     * 
     * @return the xabsl context
     */
    public XABSLContext context() {
        return context;
    }
    
    /**
     * Determines, whether the given file is part of the project.
     * 
     * @param f the (xabsl) file to check
     * @return true, if the file is part of the project, false otherwise
     */
    public boolean contains(File f) {
        return files.containsKey(f.getAbsolutePath());
    }
    
    /**
     * Returns the file hierarchy of this project.
     * 
     * @return file hierarchy
     */
    public DefaultMutableTreeNode tree() {
        return fileTree;
    }
}
