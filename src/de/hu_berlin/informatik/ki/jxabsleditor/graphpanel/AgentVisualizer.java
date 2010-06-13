/*
 * Copyright 2009 NaoTeam Humboldt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.hu_berlin.informatik.ki.jxabsleditor.graphpanel;

import de.hu_berlin.informatik.ki.jxabsleditor.parser.XABSLContext;
import de.hu_berlin.informatik.ki.jxabsleditor.parser.XabslEdge;
import de.hu_berlin.informatik.ki.jxabsleditor.parser.XabslNode;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbsoluteCrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.apache.commons.collections15.Transformer;

/**
 *
 * @author thomas
 */
public class AgentVisualizer extends javax.swing.JPanel
{

  private GraphMouseListener<XabslNode> externalMouseListener;
  private VisualizationViewer<XabslNode, XabslEdge> vv;
  private GraphZoomScrollPane scrollPane;
  private JLabel lblLoading;
  private XabslNode selectedNode;
  private GraphLoader graphLoader;
  private Layout<XabslNode, XabslEdge> layout;
  private XABSLContext lastContext;
  private String lastSelectedNodeName;
  private String lastSelectedAgentName;
 
  /** Creates new form AgentVisualizer */
  public AgentVisualizer()
  {
    initComponents();

    graphLoader = null;

    lblLoading = new JLabel("loading graph...");
    lblLoading.setFont(new java.awt.Font("Tahoma", 0, 24));
    lblLoading.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    lblLoading.setVisible(true);

  }

  private XabslNode createAgentGraph(XABSLContext context,
    DirectedGraph<XabslNode, XabslEdge> g, String selectedNodeName, String selectedAgentName)
  {

    XabslNode result = null;

    // collect the current root options
    List<String> rootOptions = new LinkedList<String>();
    if(selectedAgentName != null && context.getAgentMap().containsKey(selectedAgentName))
    {
      rootOptions.add(context.getAgentMap().get(selectedAgentName));
    }
    else
    {
      // just add all nodes
      rootOptions.addAll(context.getOptionMap().keySet());
    }

    // add initial vertices
    Map<String, XabslNode> nodeByName = new HashMap<String, XabslNode>();

    for (String optionName : rootOptions)
    {
      XABSLContext.XABSLOption option = context.getOptionMap().get(optionName);

      if (option != null && !nodeByName.containsKey(option.getName()))
      {
        XabslNode n = new XabslNode();
        n.setName(option.getName());
        n.setType(XabslNode.Type.Option);

        if (n.getName().equals(selectedNodeName))
        {
          result = n;
        }

        g.addVertex(n);
        nodeByName.put(n.getName(), n);
      }
    }

    // add edges and connected (and not yet added) vertices

    List<XabslNode> optionQueue = new LinkedList<XabslNode>();
    optionQueue.addAll(g.getVertices());
    while(optionQueue.size() > 0)
    {
      // pop
      XabslNode n1 = optionQueue.remove(0);

      // get the corresponding option
      XABSLContext.XABSLOption option = context.getOptionMap().get(n1.getName());

      // go through all actions
      for (String action : option.getActions())
      {
        XabslNode n2 = nodeByName.get(action);
        if(n2 == null)
        {
          // not included yet, add it
          XABSLContext.XABSLOption actionAsOption = context.getOptionMap().get(action);
          n2 = new XabslNode();
          n2.setName(actionAsOption.getName());
          n2.setType(XabslNode.Type.Option);
          if (n2.getName().equals(selectedNodeName))
          {
            result = n2;
          }

          // remember this as already visited/added
          nodeByName.put(action, n2);
          
          // add to queue
          optionQueue.add(n2);

        }

        if (n1 != null && n2 != null)
        {
          XabslEdge e = new XabslEdge(XabslEdge.Type.Outgoing);
          g.addEdge(e, n1, n2);
        }
      }
    }
    return result;
  }

  /** (Re-) set to a new context and display it */
  public void setContext(XABSLContext context, String selectedNodeName)
  {
    lastContext = context;
    lastSelectedNodeName = selectedNodeName;

    if (context == null)
    {
      return;
    }

    if (this.graphLoader != null && !this.graphLoader.isDone())
    {
      this.graphLoader.cancel(true);
    }

    String agent = (String) cbAgentSelector.getSelectedItem();
    if("(all)".equals(agent))
    {
      agent = null;
    }

    this.graphLoader = new GraphLoader(context, selectedNodeName, agent);
    this.graphLoader.execute();

  }

  private void doSetGraph(final XABSLContext context, final String selectedNodeName,
    String selectedAgentName)
  {
    
    // build graph
    final DirectedGraph<XabslNode, XabslEdge> graph =
      new DirectedSparseGraph<XabslNode, XabslEdge>();
    selectedNode = createAgentGraph(context, graph, selectedNodeName, selectedAgentName);

    if(selectedAgentName == null)
    {
      layout = new SuperDAGLayout<XabslNode, XabslEdge>(graph);
    }
    else
    {
      layout = new SuperDAGLayout<XabslNode, XabslEdge>(graph, 80, 120);
    }

    layout.initialize();

    if (scrollPane != null)
    {
      remove(scrollPane);
      scrollPane = null;
    }

    vv = new VisualizationViewer<XabslNode, XabslEdge>(layout);
    DefaultModalGraphMouse<XabslNode, XabslEdge> mouse =
      new DefaultModalGraphMouse<XabslNode, XabslEdge>();
    vv.setGraphMouse(mouse);

    // enable selecting the nodes
    mouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);
    // add external mouse listener
    if (externalMouseListener != null)
    {
      vv.addGraphMouseListener(externalMouseListener);
    }

    vv.getRenderContext().setVertexShapeTransformer(new VertexTransformer(graph));
    vv.getRenderContext().setVertexLabelTransformer(
      new Transformer<XabslNode, String>()
      {

        @Override
        public String transform(XabslNode n)
        {
          return "<html><center>" + n.getName().replaceAll("_", "_<br>") + "</center></html>";
        }
      });
    vv.getRenderContext().setVertexFillPaintTransformer(new Transformer<XabslNode, Paint>()
    {

      @Override
      public Paint transform(XabslNode n)
      {
        if (n == selectedNode)
        {
          return Color.green;
        }
        if (selectedNode != null && graph.getSuccessors(selectedNode).contains(n))
        {
          return Color.red;
        }
        if (selectedNode != null && graph.getPredecessors(selectedNode).contains(n))
        {
          return Color.orange;
        }
        if (graph.getOutEdges(n).size() == 0)
        {
          return Color.lightGray;
        }
        return Color.white;
      }
    });

    // label is placed in the center of the node
    vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);

    vv.getRenderContext().setEdgeShapeTransformer(
      new EdgeShape.QuadCurve<XabslNode, XabslEdge>());
    vv.getRenderContext().setEdgeDrawPaintTransformer(new Transformer<XabslEdge, Paint>()
    {

      @Override
      public Paint transform(XabslEdge e)
      {
        if (selectedNode != null
          && graph.getSource(e) == selectedNode)
        {
          return Color.red;
        }
        else if (selectedNode != null
          && graph.getDest(e) == selectedNode)
        {
          return Color.ORANGE;
        }
        else
        {
          return Color.black;
        }
      }
    });

    // add to a zoomable container
    scrollPane = new GraphZoomScrollPane(vv);
    panelGraph.add(scrollPane, BorderLayout.CENTER);

    validate();
    
    fitGraphinPanel();
    recreateAgentSelector(context);
  }

  private void recreateAgentSelector(XABSLContext context)
  {
    DefaultComboBoxModel model = new DefaultComboBoxModel();

    model.addElement("(all)");
    for(String agent : context.getAgentMap().keySet())
    {
      model.addElement(agent);
    }

    cbAgentSelector.setModel(model);
    
    if(lastSelectedAgentName != null)
    {
      model.setSelectedItem(lastSelectedAgentName);
    }
  }

  private void fitGraphinPanel()
  {
    double panelW = scrollPane.getWidth();
    double graphW = layout.getSize().getWidth();

    double panelH = scrollPane.getHeight();
    double graphH = layout.getSize().getHeight();

    double scaleW = panelW / (graphW);
    double scaleH = panelH / (graphH);
    AbsoluteCrossoverScalingControl scaler = new AbsoluteCrossoverScalingControl();

    if (scaleW < scaleH)
    {
      scaler.scale(vv, (float) scaleW, new Point2D.Double(0, 0));
    }
    else
    {
      scaler.scale(vv, (float) scaleH, new Point2D.Double(0, 0));
    }
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    panelGraph = new javax.swing.JPanel();
    cbAgentSelector = new javax.swing.JComboBox();
    btFilter = new javax.swing.JButton();

    panelGraph.setLayout(new java.awt.BorderLayout());

    btFilter.setText("Filter");
    btFilter.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btFilterActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(panelGraph, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
        .addComponent(cbAgentSelector, 0, 315, Short.MAX_VALUE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(btFilter))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(cbAgentSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(btFilter))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(panelGraph, javax.swing.GroupLayout.DEFAULT_SIZE, 267, Short.MAX_VALUE))
    );
  }// </editor-fold>//GEN-END:initComponents

  private void btFilterActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btFilterActionPerformed
  {//GEN-HEADEREND:event_btFilterActionPerformed

    lastSelectedAgentName = (String) cbAgentSelector.getSelectedItem();
    setContext(lastContext, lastSelectedNodeName);

  }//GEN-LAST:event_btFilterActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton btFilter;
  private javax.swing.JComboBox cbAgentSelector;
  private javax.swing.JPanel panelGraph;
  // End of variables declaration//GEN-END:variables
  private static class VertexTransformer
    implements Transformer<XabslNode, Shape>
  {

    private DirectedGraph<XabslNode, XabslEdge> graph;

    public VertexTransformer(DirectedGraph<XabslNode, XabslEdge> graph)
    {
      this.graph = graph;
    }

    @Override
    public Shape transform(XabslNode n)
    {

      String lines[] = n.getName().split("_");
      int maxWidth = 1;
      for (int i = 0; i < lines.length; i++)
      {
        maxWidth = Math.max(maxWidth, lines[i].length());
      }
      int maxHeight = lines.length;

      float s = (float) Math.max(20 * maxHeight, 11 * maxWidth);

      GeneralPath result = null;

      if (graph.getInEdges(n).size() == 0)
      {
        result = new GeneralPath(getCircleFromSize(s));
      }
      else
      {
        result = new GeneralPath(getRectangleFromSize(s));
      }

      result.closePath();

      return result;
    }

    private Shape getCircleFromSize(float s)
    {
      float width = s;
      float height = width;
      float h_offset = -(width / 2);
      float v_offset = -(height / 2);

      Shape circle = new Ellipse2D.Float(h_offset, v_offset, width, height);
      return circle;
    }

    private Shape getRectangleFromSize(float s)
    {
      float width = s;
      float height = width;
      float h_offset = -(width / 2);
      float v_offset = -(height / 2);

      Shape circle = new Rectangle2D.Float(h_offset, v_offset, width, height);
      return circle;
    }
  }

  private class GraphLoader extends SwingWorker<String, Void>
  {

    private XABSLContext context;
    private String selectedNode;
    private String selectedAgentName;

    public GraphLoader(XABSLContext context, String selectedNode, String selectedAgentName)
    {
      this.selectedNode = selectedNode;
      this.context = context;
      this.selectedAgentName = selectedAgentName;
    }

    @Override
    protected String doInBackground() throws Exception
    {
      panelGraph.removeAll();
      panelGraph.add(lblLoading, BorderLayout.CENTER);
      doSetGraph(context, selectedNode, selectedAgentName);
      panelGraph.remove(lblLoading);

      return "";
    }
  }//end GraphLoader

  /** Add a listener for mouse events (clicking on a node) */
  public void setGraphMouseListener(GraphMouseListener<XabslNode> listener)
  {
    externalMouseListener = listener;
  }
}
