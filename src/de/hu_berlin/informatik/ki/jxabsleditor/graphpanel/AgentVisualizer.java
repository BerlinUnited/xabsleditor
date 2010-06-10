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
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.renderers.VertexLabelAsShapeRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Paint;
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
  private VisualizationViewer<XabslNode,XabslEdge> vv;
  private GraphZoomScrollPane scrollPane;
  private JLabel lblLoading;
  GraphLoader graphLoader;

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

  private void createAgentGraph(XABSLContext context,
    DirectedGraph<XabslNode,XabslEdge> g, Map<String, XABSLContext.XABSLOption> optionMap)
  {

    // add vertices
    Map<String, XabslNode> nodeByName = new HashMap<String, XabslNode>();

    for(XABSLContext.XABSLOption option : context.getOptionMap().values())
    {
      if(!nodeByName.containsKey(option.getName()))
      {
        XabslNode n = new XabslNode();
        n.setName(option.getName());
        n.setType(XabslNode.Type.Option);

        g.addVertex(n);
        nodeByName.put(n.getName(), n);
      }
    }
    for(XABSLContext.XABSLOption option : context.getOptionMap().values())
    {
      XabslNode n1 = nodeByName.get(option.getName());

      for(String action : option.getActions())
      {
        XabslNode n2 = nodeByName.get(action);

        if(n1 != null && n2 != null)
        {
          XabslEdge e = new XabslEdge(XabslEdge.Type.Outgoing);
          g.addEdge(e, n1, n2);
        }

      }
    }
  }

  /** (Re-) set to a new context and display it */
  public void setContext(XABSLContext context)
  {
    if(context == null)
    {
      return;
    }
    
    if (this.graphLoader != null && !this.graphLoader.isDone())
    {
      this.graphLoader.cancel(true);
    }

    this.graphLoader = new GraphLoader(context);
    this.graphLoader.execute();

  }

  private void doSetGraph(XABSLContext context)
  {
    // build graph
    final DirectedGraph<XabslNode, XabslEdge> g =
      new DirectedSparseGraph<XabslNode, XabslEdge>();
    createAgentGraph(context, g, null);

    // calculate the minimum spanning tree in order to be able to use the TreeLayout
    Transformer<XabslEdge,Double> weightTransformer = new Transformer<XabslEdge, Double>()
    {

      @Override
      public Double transform(XabslEdge i)
      {
        XabslNode source = g.getSource(i);
        if(g.getInEdges(source).size() == 0)
        {
          return new Double(1.0);
        }
        XabslNode dest = g.getDest(i);
        if(g.getOutEdges(dest).size() == 0)
        {
          return new Double(1.0);
        }
        return new Double(10.0);
      }
    };
    MinimumSpanningForest2<XabslNode,XabslEdge> prim
      = new MinimumSpanningForest2<XabslNode,XabslEdge>(g,
        new DelegateForest<XabslNode,XabslEdge>(), DelegateTree.<XabslNode,XabslEdge>getFactory(),
        weightTransformer);

    Forest<XabslNode,XabslEdge> graphAsForest = prim.getForest();

    TreeLayout<XabslNode,XabslEdge> treeLayout = new TreeLayout<XabslNode,XabslEdge>(graphAsForest);

    // display graph
    StaticLayout<XabslNode,XabslEdge> layout = new StaticLayout<XabslNode,XabslEdge>(g, treeLayout);
    //DAGLayout<XabslNode,XabslEdge> layout = new DAGLayout<XabslNode, XabslEdge>(g);
    layout.initialize();

    if(scrollPane != null)
    {
      remove(scrollPane);
      scrollPane = null;
    }

    vv = new VisualizationViewer<XabslNode,XabslEdge>(layout);
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

    vv.getRenderContext().setVertexShapeTransformer(
      new VertexLabelAsShapeRenderer<XabslNode,XabslEdge>(vv.getRenderContext()));
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
        return Color.white;
      }
    });

    // label is placed in the center of the node
    vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);

    // add to a zoomable container
    scrollPane = new GraphZoomScrollPane(vv);
    add(scrollPane, BorderLayout.CENTER);

    validate();
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

  private class GraphLoader extends SwingWorker<String, Void>
  {

    XABSLContext context;

    public GraphLoader(XABSLContext context)
    {
      this.context = context;
    }

    @Override
    protected String doInBackground() throws Exception
    {
      removeAll();
      add(lblLoading, BorderLayout.CENTER);
      doSetGraph(context);
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
