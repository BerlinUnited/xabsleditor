package de.naoth.xabsleditor.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 *
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class FileWatcher extends Thread
{
    public BooleanProperty running = new SimpleBooleanProperty(false);
    
    private final WatchService watcher;
    private final Map<WatchKey,Path> keys;
    private final Map<File, Entry> listener;

    public FileWatcher() throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();
        this.listener = new HashMap<>();
    }

    @Override
    public void run() {
        running.set(true);
        WatchKey key;
        
        while(running.get()) {
            // wait for key to be signalled
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();
                
                if (kind == OVERFLOW) {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);
                
                // watch/notify only on xabsl files
                if(child.toFile().getName().endsWith(".xabsl")) {
                    // only on files with a listener
                    Entry e = this.listener.get(child.toFile());
                    if(e != null) {
                        // we're always getting 2 modify events: content & file attributes
                        // notify listeners only on "real" changes
                        if(kind == ENTRY_MODIFY && e.lastmodified == child.toFile().lastModified()) {
                            continue;
                        }
                        
                        e.listeners.forEach(l->l.xabslFileChanged(child.toFile(), kind));
                        e.lastmodified = child.toFile().lastModified();
                    }
                }

                // if directory is created, then register it and its sub-directories
                if (kind == ENTRY_CREATE) {
                    try {
                        if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                            register(child);
                        }
                    } catch (IOException x) {
                        // ignore to keep sample readbale
                    }
                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);
            }
        }
    }
    
    public void register(Path dir) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
            {
                WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                keys.put(key, dir);

                return FileVisitResult.CONTINUE;
            }
        });
    }
    
    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }
    
    public void addListener(FileWatcherListener l) {
        // add the file listener itself
        if(!this.listener.containsKey(l.getFile())) {
            this.listener.put(l.getFile(), new Entry(l.getFile().lastModified()));
        }
        this.listener.get(l.getFile()).add(l);
        // register path
        try {
            register(l.getFile().getParentFile().toPath());
        } catch (IOException ex) {
            Logger.getLogger(FileWatcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void removeListener(FileWatcherListener l) {
        if(this.listener.containsKey(l.getFile())) {
            this.listener.get(l.getFile()).remove(l);
        }
        if(this.listener.get(l.getFile()).isEmpty()) {
            this.listener.remove(l.getFile());
        }
    }
    
    class Entry {
        public Set<FileWatcherListener> listeners = new HashSet<>();
        public long lastmodified;
        
        public Entry(long m) {
            lastmodified = m;
        }
        
        public boolean add(FileWatcherListener l) {
            return listeners.add(l);
        }
        
        public boolean remove(FileWatcherListener l) {
            return listeners.remove(l);
        }
        
        public boolean isEmpty() {
            return listeners.isEmpty();
        }
    }
}
