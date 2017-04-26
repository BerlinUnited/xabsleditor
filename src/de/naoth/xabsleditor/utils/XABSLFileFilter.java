package de.naoth.xabsleditor.utils;

import java.io.File;

public class XABSLFileFilter extends javax.swing.filechooser.FileFilter {

    @Override
    public boolean accept(File file) {
        if (file.isDirectory()) {
            return true;
        }
        String filename = file.getName();
        return filename.endsWith(".xabsl") || filename.endsWith(".XABSL");
    }

    @Override
    public String getDescription() {
        return "Extensible Agent Behavior Language (*.xabsl)";
    }

    @Override
    public String toString() {
        return "xabsl";
    }
}
