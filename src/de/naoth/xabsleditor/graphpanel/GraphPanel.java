package de.naoth.xabsleditor.graphpanel;

import de.naoth.xabsleditor.compilerconnection.CompileResult;
import de.naoth.xabsleditor.compilerconnection.CompilerOutputPanel;
import de.naoth.xabsleditor.editorpanel.XABSLStateCompetion;
import de.naoth.xabsleditor.editorpanel.XEditorPanel;
import de.naoth.xabsleditor.events.CompilationFinishedEvent;
import de.naoth.xabsleditor.events.EventListener;
import de.naoth.xabsleditor.events.EventManager;
import de.naoth.xabsleditor.events.OpenFileEvent;
import de.naoth.xabsleditor.events.RefreshGraphEvent;
import de.naoth.xabsleditor.parser.XABSLContext;
import de.naoth.xabsleditor.parser.XABSLOptionContext;
import de.naoth.xabsleditor.parser.XParser;
import de.naoth.xabsleditor.parser.XabslEdge;
import de.naoth.xabsleditor.parser.XabslNode;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.StringReader;
import javax.swing.JOptionPane;
import org.fife.ui.autocomplete.DefaultCompletionProvider;

/**
 *
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class GraphPanel extends javax.swing.JPanel
{
    private final EventManager evtManager = EventManager.getInstance();
    
    private OptionVisualizer optionVisualizer;
    private AgentVisualizer agentVisualizer;
    private XEditorPanel currentEditor;
  
    /**
     * Creates new form GraphPanel
     */
    public GraphPanel() {
        initComponents();
        // register event handler
        evtManager.add(this);
        XabslGraphMouseListener mouseListener = new XabslGraphMouseListener();
        initAgentTab(mouseListener);
        initOptionTab(mouseListener);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        panelOption = new javax.swing.JPanel();
        panelAgent = new javax.swing.JPanel();
        panelCompiler = new de.naoth.xabsleditor.compilerconnection.CompilerOutputPanel();

        setLayout(new java.awt.BorderLayout());

        panelOption.setLayout(new java.awt.BorderLayout());
        jTabbedPane1.addTab("Option", panelOption);

        panelAgent.setLayout(new java.awt.BorderLayout());
        jTabbedPane1.addTab("Agent", panelAgent);
        jTabbedPane1.addTab("Compiler", panelCompiler);

        add(jTabbedPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void initAgentTab(XabslGraphMouseListener mouseListener) {
        agentVisualizer = new AgentVisualizer();    
        agentVisualizer.setGraphMouseListener(mouseListener);
        panelAgent.add(agentVisualizer, BorderLayout.CENTER);
    }
    
    private void initOptionTab(XabslGraphMouseListener mouseListener) {
        optionVisualizer = new OptionVisualizer();
        optionVisualizer.setGraphMouseListener(mouseListener);
        panelOption.add(optionVisualizer, BorderLayout.CENTER);
    }
    
    public void addJumpListener(CompilerOutputPanel.JumpListener j) {
        panelCompiler.addJumpListener(j);
    }

    public void selectTab(String tab) {
        for (int i = 0; i < jTabbedPane1.getTabCount(); i++) {
            if(jTabbedPane1.getTitleAt(i).equals(tab)) {
                jTabbedPane1.setSelectedIndex(i);
                break;
            }
        }
    }
    
    public void updateAgentContext(XABSLContext context, String selectedNodeName) {
        agentVisualizer.setContext(context, selectedNodeName);
    }
    
    public void updateOptionGraph(Graph<XabslNode, XabslEdge> g) {
        optionVisualizer.setGraph(g);
    }
    
    public void updateCompilerResult(CompileResult result) {
        panelCompiler.setCompilerResult(result);
    }
    
    @EventListener
    public void compileResult(CompilationFinishedEvent e) {
        
        updateCompilerResult(e.result);
        if (e.result.errors || e.result.warnings) {
            selectTab("Compiler");
        }
    }
    
    @EventListener
    public void refreshGraph(RefreshGraphEvent e) {
        if (e.getSource() == null || !(e.getSource() instanceof XEditorPanel) || ((XEditorPanel)e.getSource()).getFile() == null) {
            return;
        }
        currentEditor = (XEditorPanel)e.getSource();

        String text = currentEditor.getContent();

        // Option
        XParser p = new XParser(currentEditor.getXABSLContext());
        p.parse(new StringReader(text));
        updateOptionGraph(p.getOptionGraph());

        String optionName = currentEditor.getFile().getName();
        optionName = optionName.replaceAll(".xabsl", "");
        updateAgentContext(currentEditor.getXABSLContext(), optionName);

        // refresh autocompetion
        DefaultCompletionProvider completionProvider = new DefaultCompletionProvider();

        for (XABSLOptionContext.State state : p.getStateMap().values()) {
            completionProvider.addCompletion(
                    new XABSLStateCompetion(completionProvider, state.name));
        }//end for

        currentEditor.setCompletionProvider(completionProvider);
    }
    
    class XabslGraphMouseListener implements GraphMouseListener<XabslNode>
    {
        @Override
        public void graphClicked(XabslNode v, MouseEvent me) {
            if (currentEditor != null && v.getType() == XabslNode.Type.State && v.getPosInText() > -1) {
                currentEditor.setCarretPosition(v.getPosInText());
            } else if (v.getType() == XabslNode.Type.Option) {
                String option = v.getName();
                File file = null;
                if (currentEditor.getXABSLContext()!= null) {
                    file = currentEditor.getXABSLContext().getOptionPathMap().get(option);
                }

                if (file != null) {
                    evtManager.publish(new OpenFileEvent(this, file));
                } else {
                    JOptionPane.showMessageDialog(null, "Could not find the file for option "
                            + option, "Option not found", JOptionPane.WARNING_MESSAGE);
                }
            }
        }

        @Override
        public void graphPressed(XabslNode v, MouseEvent me) {
        }

        @Override
        public void graphReleased(XabslNode v, MouseEvent me) {
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel panelAgent;
    private de.naoth.xabsleditor.compilerconnection.CompilerOutputPanel panelCompiler;
    private javax.swing.JPanel panelOption;
    // End of variables declaration//GEN-END:variables
}
