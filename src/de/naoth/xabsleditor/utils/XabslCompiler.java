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
 *
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class XabslCompiler implements CompilationFinishedReceiver
{
    private final EventManager evtManager = EventManager.getInstance();
    private Properties configuration = new Properties();
    private String defaultCompilationPath = null;
    private final JFileChooser fileChooser = new JFileChooser();
    private final FileFilter icFilter = new FileNameExtensionFilter("Intermediate code (*.dat)", "dat");

    public XabslCompiler() {
        fileChooser.setFileFilter(icFilter);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        evtManager.add(this);
    }
    
    public void setConfiguration(Properties c) {
        configuration = c;
        
        if (configuration.containsKey(OptionsDialog.DEFAULT_COMPILATION_PATH)) {
            String path = configuration.getProperty(OptionsDialog.DEFAULT_COMPILATION_PATH);
            if (new File(path).exists()) {
                this.defaultCompilationPath = path;
            }
        }
    }
    
    public void compile(File file) {
        File fout = null;

        if (defaultCompilationPath == null) {
            if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                fout = fileChooser.getSelectedFile();
            }
        } else {
            fout = new File(defaultCompilationPath + "/behavior-ic.dat");
        }

        if (fout == null) {
            JOptionPane.showMessageDialog(null, "No file selected");
            return;
        } else if (fout.exists()) {
            if (!fout.delete()) {
                JOptionPane.showMessageDialog(null, "Can not overwrite the file "
                        + fout.getAbsolutePath());
                return;
            }
        }

        CompilerDialog frame = new CompilerDialog(null, true, file, fout, this, configuration);
        frame.setVisible(true);
    }

    @Override
    public void compilationFinished(CompileResult result) {
        evtManager.publish(new CompilationFinishedEvent(this, result));
    }
}
