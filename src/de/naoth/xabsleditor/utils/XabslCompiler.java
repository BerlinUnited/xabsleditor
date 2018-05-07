package de.naoth.xabsleditor.utils;

import de.naoth.xabsleditor.OptionsDialog;
import de.naoth.xabsleditor.compilerconnection.CompilationFinishedReceiver;
import de.naoth.xabsleditor.compilerconnection.CompileResult;
import de.naoth.xabsleditor.compilerconnection.CompilerDialog;
import de.naoth.xabsleditor.events.EventManager;
import de.naoth.xabsleditor.events.CompilationFinishedEvent;
import java.io.File;
import java.util.Properties;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Wrapper for handling the compile process.
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class XabslCompiler implements CompilationFinishedReceiver
{
    /** Manager for distributing events. */
    private final EventManager evtManager = EventManager.getInstance();
    /** The configuration of the editor. */
    private Properties configuration = new Properties();
    /** The default file name for the compilation result. */
    private final String defaultFileName = "behavior-ic.dat";
    /** The default path/file, where the compilation result should be stored */
    private String defaultCompilationPath = null;
    /** A file chooser for selecting the file, where the compilation result is saved. */
    private final JFileChooser fileChooser = new JFileChooser();
    /** The filter for the file chooser. */
    private final FileFilter icFilter = new FileNameExtensionFilter("Intermediate code (*.dat)", "dat");
    /** The current file where the compilation is stored. */
    private File compilationFile;

    /**
     * Constructor, sets the default values for the file chooser.
     */
    public XabslCompiler() {
        fileChooser.setFileFilter(icFilter);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setSelectedFile(new File(defaultFileName));
        
        evtManager.add(this);
    }
    
    /**
     * Updates the configuration and sets the default compilation path - if available.
     * 
     * @param c the new configuration
     */
    public void setConfiguration(Properties c) {
        configuration = c;
        
        if (configuration.containsKey(OptionsDialog.DEFAULT_COMPILATION_PATH)) {
            String path = configuration.getProperty(OptionsDialog.DEFAULT_COMPILATION_PATH);
            if (new File(path).exists()) {
                this.defaultCompilationPath = path;
            } else {
                this.defaultCompilationPath = null;
            }
        } else {
            this.defaultCompilationPath = null;
        }
    }
    
    /**
     * Starts the compilation process.
     * 
     * @param file a file of the project which should be compiled
     */
    public void compile(File file) {
        File fout = null;

        if (defaultCompilationPath == null) {
            if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                fout = fileChooser.getSelectedFile();
            }
        } else {
            fout = new File(defaultCompilationPath + "/" + defaultFileName);
        }

        if (fout == null) {
            JOptionPane.showMessageDialog(null, "No file selected");
            return;
        } else if (fout.exists()) {
            // 'create' backup file, in order to be able to restore this backup if compilation fails
            if (!fout.renameTo(new File(fout.getAbsolutePath()+".bak"))) {
                JOptionPane.showMessageDialog(null, "Can not overwrite the file "
                        + fout.getAbsolutePath());
                return;
            }
        }
        compilationFile = fout;
        CompilerDialog frame = new CompilerDialog(null, true, file, fout, this, configuration);
        frame.setVisible(true);
    }

    /**
     * Event listener when the compilation finished.
     * Schedules the CompilationFinishedEvent for interested other editor components.
     * 
     * @param result the result of the compilation
     */
    @Override
    public void compilationFinished(CompileResult result) {
        // delete or restore backup file, if compilation fails
        if(compilationFile != null) {
            File backup = new File(compilationFile.getAbsolutePath() + ".bak");
            if(backup.exists()) {
                if(result.errors) {
                    backup.renameTo(new File(backup.getAbsolutePath().substring(0, backup.getAbsolutePath().length()-4)));
                } else {
                    backup.delete();
                }
            }
        }
        evtManager.publish(new CompilationFinishedEvent(this, result));
    }
}
