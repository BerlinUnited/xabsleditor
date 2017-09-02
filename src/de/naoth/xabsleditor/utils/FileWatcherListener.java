package de.naoth.xabsleditor.utils;

import java.io.File;
import java.nio.file.WatchEvent;

/**
 *
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public interface FileWatcherListener
{
    public void xabslFileChanged(File toFile, WatchEvent.Kind kind);
    
    public File getFile();
}
