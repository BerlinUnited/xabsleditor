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
import edu.uci.ics.jung.algorithms.layout.DAGLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.algorithms.shortestpath.MinimumSpanningForest2;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.renderers.VertexLabelAsShapeRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Paint;
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

  private VisualizationViewer<String, String> vv;
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

  private void createSubGraph(XABSLContext.XABSLOption o,
    DirectedGraph<String, String> g, Map<String, XABSLContext.XABSLOption> optionMap)
  {
    if(!g.containsVertex(o.getName()))
    {
      g.addVertex(o.getName());
    }

    for(String a : o.getActions())
    {

      if(!g.containsVertex(a))
      {
        g.addVertex(a);
      }

      if(g.findEdge(o.getName(), a) == null)
      {
        g.addEdge(o.getName() + "->" + a, o.getName(), a);
      }
      else
      {
        boolean b = true;
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
    DirectedGraph<String, String> g =
      new DirectedSparseGraph<String, String>();
    for(XABSLContext.XABSLOption o : context.getOptionMap().values())
    {
      createSubGraph(o, g, context.getOptionMap());
    }

    // calculate the minimum spanning tree in order to be able to use the TreeLayout
    Transformer<String,Double> weightTransformer = new Transformer<String, Double>()
    {

      @Override
      public Double transform(String i)
      {
        return new Double(1.0);
      }
    };
    MinimumSpanningForest2<String,String> prim
      = new MinimumSpanningForest2<String, String>(g,
        new DelegateForest<String, String>(), DelegateTree.<String,String>getFactory(),
        weightTransformer);

    Forest<String,String> graphAsForest = prim.getForest();

    TreeLayout<String,String> treeLayout = new TreeLayout<String, String>(graphAsForest);

    // display graph
    StaticLayout<String,String> layout = new StaticLayout<String, String>(g, treeLayout);
    layout.initialize();

    if(scrollPane != null)
    {
      remove(scrollPane);
      scrollPane = null;
    }

    vv = new VisualizationViewer<String, String>(layout);
    DefaultModalGraphMouse<XabslNode, XabslEdge> mouse =
      new DefaultModalGraphMouse<XabslNode, XabslEdge>();
    vv.setGraphMouse(mouse);

    vv.getRenderContext().setVertexShapeTransformer(
      new VertexLabelAsShapeRenderer<String, String>(vv.getRenderContext()));
    vv.getRenderContext().setVertexLabelTransformer(
      new Transformer<String, String>()
      {

        @Override
        public String transform(String s)
        {
          return s;
        }
      });
    vv.getRenderContext().setVertexFillPaintTransformer(new Transformer<String, Paint>()
    {

      @Override
      public Paint transform(String arg0)
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

}
