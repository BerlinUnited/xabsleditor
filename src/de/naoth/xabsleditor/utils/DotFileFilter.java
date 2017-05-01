package de.naoth.xabsleditor.utils;

import java.io.File;

public class DotFileFilter extends javax.swing.filechooser.FileFilter {

    @Override
    public boolean accept(File file) {
        if (file.isDirectory()) {
            return true;
        }
        String filename = file.getName();
        return filename.endsWith(".dot") || filename.endsWith(".DOT");
    }

    @Override
    public String getDescription() {
        return "DOT (*.dot)";
    }

    @Override
    public String toString() {
        return "dot";
    }
}