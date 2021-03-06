/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * HelpDialog.java
 *
 * Created on 27.05.2011, 15:55:48
 */

package de.naoth.xabsleditor.help;

import de.naoth.xabsleditor.Tools;
import de.naoth.xabsleditor.utils.LinkListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.event.HyperlinkListener;

/**
 * The help dialog for the xabsleditor.
 * Shows some information about the xabsleditor and also about the xabsl language
 * itself.
 * 
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class HelpDialog extends javax.swing.JDialog
{
    /** The regex for replacing images in the html string with the appropiate resource of the jar file. */
    private final Pattern image = Pattern.compile("src=\"(file:(\\S+))\"");
    /** Link listener for handling html links (<a>) in the help documents */
    private final HyperlinkListener linkOpener;
    
    /** Creates new form HelpDialog */
    public HelpDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        
        // open links in the system browser - if possible
        linkOpener = new LinkListener();

        // load & set the html content
        this.helpPanel.setText(loadHtml("/de/naoth/xabsleditor/help/help.html"));
        this.about.setText(loadHtml("/de/naoth/xabsleditor/help/about.html"));
        this.compiler.setText(loadHtml("/de/naoth/xabsleditor/help/compiler.html"));
        this.engine.setText(loadHtml("/de/naoth/xabsleditor/help/engine.html"));
        this.reference.setText(loadHtml("/de/naoth/xabsleditor/help/reference.html"));
        // "jump" to the top
        this.helpPanel.setCaretPosition(0);
        this.about.setCaretPosition(0);
        this.compiler.setCaretPosition(0);
        this.engine.setCaretPosition(0);
        this.reference.setCaretPosition(0);
        // add the link handler
        this.helpPanel.addHyperlinkListener(linkOpener);
        this.about.addHyperlinkListener(linkOpener);
        this.compiler.addHyperlinkListener(linkOpener);
        this.engine.addHyperlinkListener(linkOpener);
        this.reference.addHyperlinkListener(linkOpener);

        ActionListener actionListener = new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent actionEvent) {
            dispose();
          }
        };

        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        this.getRootPane().registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    
    /**
     * Loads the given html file and replaces embeded images with the corresponding
     * resources in the jar file.
     * 
     * @param file the html file, which should be loaded
     * @return the loaded html string
     */
    private String loadHtml(String file) {
        StringBuffer html = new StringBuffer();
        Matcher m = image.matcher(Tools.getResourceAsString(file));
        // replace image resources
        while (m.find()) {
            URL res = getClass().getResource("/de/naoth/xabsleditor/help/"+m.group(2));
            if(res != null) {
                String replacement = m.group(0).replace(m.group(1), res.toExternalForm());
                m.appendReplacement(html, replacement);
            }
        }
        m.appendTail(html);
        return html.toString();
    } // END loadHtml()

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        helpPanel = new javax.swing.JEditorPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        about = new javax.swing.JEditorPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        reference = new javax.swing.JEditorPane();
        jScrollPane4 = new javax.swing.JScrollPane();
        engine = new javax.swing.JEditorPane();
        jScrollPane5 = new javax.swing.JScrollPane();
        compiler = new javax.swing.JEditorPane();

        setTitle("XabslEditor Help");
        setLocationByPlatform(true);

        jTabbedPane1.setToolTipText("");

        helpPanel.setEditable(false);
        helpPanel.setContentType("text/html"); // NOI18N
        helpPanel.setText("<html>\r\n  <head>\r\n\r\n  </head>\r\n  <body>\r\n    <p style=\"margin-top: 0\">\r\n      \r...could not load the help file...\n    </p>\r\n  </body>\r\n</html>\r\n");
        helpPanel.setCaretPosition(0);
        helpPanel.setPreferredSize(new java.awt.Dimension(480, 600));
        jScrollPane1.setViewportView(helpPanel);

        jTabbedPane1.addTab("Editor", jScrollPane1);

        about.setEditable(false);
        about.setContentType("text/html"); // NOI18N
        jScrollPane2.setViewportView(about);

        jTabbedPane1.addTab("About Xabsl", jScrollPane2);

        reference.setEditable(false);
        reference.setContentType("text/html"); // NOI18N
        jScrollPane3.setViewportView(reference);

        jTabbedPane1.addTab("Reference", jScrollPane3);

        engine.setEditable(false);
        engine.setContentType("text/html"); // NOI18N
        jScrollPane4.setViewportView(engine);

        jTabbedPane1.addTab("Engine", jScrollPane4);

        compiler.setEditable(false);
        compiler.setContentType("text/html"); // NOI18N
        jScrollPane5.setViewportView(compiler);

        jTabbedPane1.addTab("Compiler", jScrollPane5);

        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                HelpDialog dialog = new HelpDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane about;
    private javax.swing.JEditorPane compiler;
    private javax.swing.JEditorPane engine;
    private javax.swing.JEditorPane helpPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JEditorPane reference;
    // End of variables declaration//GEN-END:variables

}
