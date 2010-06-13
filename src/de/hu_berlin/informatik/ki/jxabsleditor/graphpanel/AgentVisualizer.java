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
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.algorithms.shortestpath.MinimumSpanningForest2;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbsoluteCrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JLabel;
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
  private StaticLayout<XabslNode, XabslEdge> layout;
  private TreeLayout<XabslNode, XabslEdge> treeLayout;

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
    DirectedGraph<XabslNode, XabslEdge> g, String selectedNodeName)
  {

    XabslNode result = null;
    // add vertices
    Map<String, XabslNode> nodeByName = new HashMap<String, XabslNode>();

    for (XABSLContext.XABSLOption option : context.getOptionMap().values())
    {
      if (!nodeByName.containsKey(option.getName()))
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
    for (XABSLContext.XABSLOption option : context.getOptionMap().values())
    {
      XabslNode n1 = nodeByName.get(option.getName());

      for (String action : option.getActions())
      {
        XabslNode n2 = nodeByName.get(action);

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
    if (context == null)
    {
      return;
    }

    if (this.graphLoader != null && !this.graphLoader.isDone())
    {
      this.graphLoader.cancel(true);
    }

    this.graphLoader = new GraphLoader(context, selectedNodeName);
    this.graphLoader.execute();

  }

  private void doSetGraph(XABSLContext context, final String selectedNodeName)
  {
    // build graph
    final DirectedGraph<XabslNode, XabslEdge> graph =
      new DirectedSparseGraph<XabslNode, XabslEdge>();
    selectedNode = createAgentGraph(context, graph, selectedNodeName);


    // calculate the minimum spanning tree in order to be able to use the TreeLayout
    Transformer<XabslEdge, Double> weightTransformer = new Transformer<XabslEdge, Double>()
    {

      @Override
      public Double transform(XabslEdge i)
      {
        XabslNode source = graph.getSource(i);
        if (graph.getInEdges(source).size() == 0)
        {
          return new Double(10.0);
        }
        XabslNode dest = graph.getDest(i);
        if (graph.getOutEdges(dest).size() == 0)
        {
          return new Double(10.0);
        }
        return new Double(1.0);
      }
    };
    MinimumSpanningForest2<XabslNode, XabslEdge> prim = new MinimumSpanningForest2<XabslNode, XabslEdge>(graph,
      new DelegateForest<XabslNode, XabslEdge>(), DelegateTree.<XabslNode, XabslEdge>getFactory(),
      weightTransformer);

    Forest<XabslNode, XabslEdge> graphAsForest = prim.getForest();

    treeLayout =
      new TreeLayout<XabslNode, XabslEdge>(graphAsForest, 200, 200);

    // display graph
    layout = new StaticLayout<XabslNode, XabslEdge>(graph, treeLayout);

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
    add(scrollPane, BorderLayout.CENTER);

    validate();
    
    fitGraphinPanel();
  }

  private void fitGraphinPanel()
  {
    double panelW = scrollPane.getWidth();
    double graphW = treeLayout.getSize().getWidth();

    double panelH = scrollPane.getHeight();
    double graphH = treeLayout.getSize().getHeight();

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

    setLayout(new java.awt.BorderLayout());
  }// </editor-fold>//GEN-END:initComponents

  // Variables declaration - do not modify//GEN-BEGIN:variables
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

    public GraphLoader(XABSLContext context, String selectedNode)
    {
      this.selectedNode = selectedNode;
      this.context = context;
    }

    @Override
    protected String doInBackground() throws Exception
    {
      removeAll();
      add(lblLoading, BorderLayout.CENTER);
      doSetGraph(context, selectedNode);
      remove(lblLoading);

      return "";
    }
  }//end GraphLoader

  /** Add a listener for mouse events (clicking on a node) */
  public void setGraphMouseListener(GraphMouseListener<XabslNode> listener)
  {
    externalMouseListener = listener;
  }
}
