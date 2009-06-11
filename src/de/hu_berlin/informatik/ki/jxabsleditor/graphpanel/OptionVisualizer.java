/*
 * OptionVisualizer.java
 *
 * Created on 06.05.2009, 16:15:50
 */
package de.hu_berlin.informatik.ki.jxabsleditor.graphpanel;

import de.hu_berlin.informatik.ki.jxabsleditor.parser.XabslEdge;
import de.hu_berlin.informatik.ki.jxabsleditor.parser.XabslNode;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ChainedTransformer;

/**
 *
 * @author thomas
 */
public class OptionVisualizer extends javax.swing.JPanel
{

  /** The main visualization component */
  private VisualizationViewer<XabslNode, XabslEdge> vv;
  private GraphZoomScrollPane scrollPane;
  private GraphMouseListener<XabslNode> externalMouseListener;

  /** Creates new form OptionVisualizer */
  public OptionVisualizer()
  {
    initComponents();
  }

  /** (Re-) set to a new graph and display it */
  public void setGraph(Graph<XabslNode, XabslEdge> g)
  {
    KKLayout<XabslNode, XabslEdge> layout = new KKLayout<XabslNode, XabslEdge>(g);
    //FRLayout<XabslNode,XabslEdge> layout = new FRLayout<XabslNode,XabslEdge>(g);
    //SpringLayout2<XabslNode,XabslEdge> layout = new SpringLayout2<XabslNode, XabslEdge>(g);
    
    
    int w = 400;
    int h = 400;
    w = Math.max(w, this.getSize().width);
    h = Math.max(h, this.getSize().height);
    layout.setSize(new Dimension(w, h));

    if(scrollPane != null)
    {
      remove(scrollPane);
      scrollPane = null;
    }

    vv = new VisualizationViewer<XabslNode, XabslEdge>(layout);

    // zooming and selecting
    DefaultModalGraphMouse<XabslNode, XabslEdge> mouse =
      new DefaultModalGraphMouse<XabslNode, XabslEdge>();
    vv.setGraphMouse(mouse);
    vv.addKeyListener(mouse.getModeKeyListener());

    // enable selecting the nodes
    mouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);

    // add external mouse listener
    if(externalMouseListener != null)
    {
      vv.addGraphMouseListener(externalMouseListener);
    }

    // determine the shape of the nodes
    vv.getRenderContext().setVertexShapeTransformer(new VertexTransformer());

    // white background for nodes
    vv.getRenderContext().setVertexFillPaintTransformer(new Transformer<XabslNode, Paint>()
    {

      @Override
      public Paint transform(XabslNode n)
      {
        return Color.white;
      }
    });
    


    // howto render the edges (depending whether commond decision or not)
    vv.getRenderContext().setEdgeStrokeTransformer(new Transformer<XabslEdge, Stroke>()
    {

      @Override
      public Stroke transform(XabslEdge e)
      {
        if(e.isCommonDecision())
        {
          return vv.getRenderContext().DASHED;
        }
        else
        {
          return new BasicStroke();
        }
      }
    });
    vv.getRenderContext().setEdgeDrawPaintTransformer(new Transformer<XabslEdge, Paint>()
    {

      @Override
      public Paint transform(XabslEdge e)
      {
        if(e.isCommonDecision())
        {
          return Color.gray;
        }
        else
        {
          return Color.black;
        }
      }
    });

    // use toString() to draw label and do some extra transformations (splitting at "_")
    vv.getRenderContext().setVertexLabelTransformer(
      new ChainedTransformer<XabslNode, String>(new Transformer[]
      {
        new ToStringLabeller<XabslNode>(),
        new Transformer<String, String>()
        {

      @Override
          public String transform(String s)
          {
            return "<html><center>" + s.replaceAll("_", "_<br>") + "</center></html>";
          }
        }
      }));

    // label is placed in the center of the node
    vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);

    // add to a zoomable container
    scrollPane = new GraphZoomScrollPane(vv);
    add(scrollPane, BorderLayout.CENTER);

    validate();
  }

  /** Add a listener for mouse events (clicking on a node) */
  public void setGraphMouseListener(GraphMouseListener<XabslNode> listener)
  {
    externalMouseListener = listener;
  }

  private static class VertexTransformer
    implements Transformer<XabslNode, Shape>
  {

    public VertexTransformer()
    {
    }

    @Override
    public Shape transform(XabslNode n)
    {

      String lines[] = n.getName().split("_");
      int maxWidth = 1;
      for(int i=0; i < lines.length; i++)
      {
        maxWidth = Math.max(maxWidth, lines[i].length());
      }
      int maxHeight = lines.length;

      float s = (float) Math.max(20 * maxHeight, 11*maxWidth);

      GeneralPath result = null;

      if(n.getType() == XabslNode.Type.State)
      {
        result = new GeneralPath(getCircleFromSize(s));
      }
      else
      {
        result = new GeneralPath(getRectangleFromSize(s));
      }
      if(n.isTargetState())
      {
        // add bigger circle
        result.append(getCircleFromSize(s+5), false);
      }
      if(n.isInitialState())
      {
        // add two horizontal lines
        float h = s / 3.0f + 1.0f;
        float v = s / 3.0f + 1.0f;
        result.append(new Line2D.Float(-h, v, h, v), false);
        result.append(new Line2D.Float(-h, -v, h, -v), false);
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
}
