package de.hu_berlin.informatik.ki.jxabsleditor.graphpanel;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.graph.Graph;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author thomas
 */
public class SuperDAGLayout<N,E> extends AbstractLayout<N, E>
{

  protected transient Point m_currentPoint = new Point();
  protected Map<N, Integer> basePositions = new HashMap<N, Integer>();
  protected transient Set<N> alreadyDone = new HashSet<N>();
  /**
   * The default horizontal vertex spacing.  Initialized to 50.
   */
  public final static int DEFAULT_DISTX = 80;
  /**
   * The default vertical vertex spacing.  Initialized to 50.
   */
  public final static int DEFAULT_DISTY = 250;
  /**
   * The horizontal vertex spacing.  Defaults to {@code DEFAULT_XDIST}.
   */
  protected int distX = DEFAULT_DISTX;
  /**
   * The vertical vertex spacing.  Defaults to {@code DEFAULT_YDIST}.
   */
  protected int distY = DEFAULT_DISTY;

  public SuperDAGLayout(Graph<N, E> graph)
  {
    super(graph);
  }

  @Override
  public void initialize()
  {
    if(size == null)
    {
       size = new Dimension(600,600);
    }
    buildTree();
  }

  @Override
  public void reset()
  {
    // TODO
  }

  private void buildTree()
  {
    this.m_currentPoint = new Point(0, 20);
    LinkedList<N> roots = getRoots(graph);
    if(roots.size() > 0 && graph != null)
    {
      calculateDimensionX(roots);
      for(N v : roots)
      {
        calculateDimensionX(v);
        m_currentPoint.x += this.basePositions.get(v) / 2 + distX;
        buildTree(v, this.m_currentPoint.x);
      }
    }
    
    // pull all token down to height of lowest token
    double lowestY = 0.0;
    for(N n : graph.getVertices())
    {
      if(graph.getOutEdges(n).size() == 0)
      {
        lowestY = Math.max(lowestY, locations.get(n).getY());
      }
    }
    for(N n : graph.getVertices())
    {
      if(graph.getOutEdges(n).size() == 0)
      {
        Point2D p = locations.get(n);
        p.setLocation(p.getX(), lowestY);
      }
    }

  }

  protected void buildTree(N v, int x)
  {

    if(!alreadyDone.contains(v))
    {
      alreadyDone.add(v);

      //go one level further down
      this.m_currentPoint.y += this.distY;
      this.m_currentPoint.x = x;

      this.setCurrentPositionFor(v);

      int sizeXofCurrent = basePositions.get(v);

      int lastX = x - sizeXofCurrent / 2;

      int sizeXofChild;
      int startXofChild;

      for(N element : graph.getSuccessors(v))
      {
        sizeXofChild = this.basePositions.get(element);
        startXofChild = lastX + sizeXofChild / 2;
        buildTree(element, startXofChild);
        lastX = lastX + sizeXofChild + distX;
      }
      this.m_currentPoint.y -= this.distY;
    }
  }

  /** Get all nodes without a incoming edge */
  private LinkedList<N> getRoots(Graph<N, E> graph)
  {
    LinkedList<N> result = new LinkedList<N>();

    if(graph != null)
    {
      for(N n : graph.getVertices())
      {
        if(graph.getInEdges(n).size() == 0)
        {
          result.add(n);
        }
      }
    }
    return result;
  }

  private int calculateDimensionX(N v)
  {

    int size = 0;
    int childrenNum = graph.getSuccessors(v).size();

    if(childrenNum != 0)
    {
      for(N element : graph.getSuccessors(v))
      {
        size += calculateDimensionX(element) + distX;
      }
    }
    size = Math.max(0, size - distX);
    basePositions.put(v, size);

    return size;
  }

  private int calculateDimensionX(Collection<N> roots)
  {

    int size = 0;
    for(N v : roots)
    {
      int childrenNum = graph.getSuccessors(v).size();

      if(childrenNum != 0)
      {
        for(N element : graph.getSuccessors(v))
        {
          size += calculateDimensionX(element) + distX;
        }
      }
      size = Math.max(0, size - distX);
      basePositions.put(v, size);
    }

    return size;
  }

  protected void setCurrentPositionFor(N vertex)
  {
    int x = m_currentPoint.x;
    int y = m_currentPoint.y;
    if(x < 0)
    {
      size.width -= x;
    }

    if(x > size.width - distX)
    {
      size.width = x + distX;
    }

    if(y < 0)
    {
      size.height -= y;
    }
    if(y > size.height - distY)
    {
      size.height = y + distY;
    }
    locations.get(vertex).setLocation(m_currentPoint);

  }
}
