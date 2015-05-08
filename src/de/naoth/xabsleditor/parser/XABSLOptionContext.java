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
package de.naoth.xabsleditor.parser;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author Heinrich Mellmann
 */
public class XABSLOptionContext
{

  private Map<String, State> stateMap = new TreeMap<String, State>();
  private ArrayList<Transition> stateTransitionList = new ArrayList<Transition>();


  public Graph<XabslNode, XabslEdge> getOptionGraph()
  {
    Graph<XabslNode, XabslEdge> optionGraph = new DirectedSparseGraph<XabslNode, XabslEdge>();

    // states
    for(State s : stateMap.values())
    {
      XabslNode n = new XabslNode();
      n.setName(s.name);
      n.setType(XabslNode.Type.State);
      n.setPosInText(s.offset);
      n.setInitialState(s.initial);
      n.setTargetState(s.target);

      optionGraph.addVertex(n);

      for(String o : s.outgoingOptions)
      {
        XabslNode outNode = new XabslNode();
        outNode.setName(o);
        outNode.setType(XabslNode.Type.Option);
        optionGraph.addEdge(new XabslEdge(XabslEdge.Type.Outgoing), n, outNode);
      }//end for
    }//end for

    Set<Transition> commonDecisions = new HashSet<Transition>();
    // transitions
    for(Transition t : stateTransitionList)
    {
      if(t.from == null)
      {
        // common decision, add later when all states are known
        commonDecisions.add(t);
      }
      else
      {
        // not a common decision
        XabslNode nFrom = new XabslNode(t.from, XabslNode.Type.State);
        XabslNode nTo = new XabslNode(t.to, XabslNode.Type.State);
        optionGraph.addEdge(new XabslEdge(XabslEdge.Type.Normal), nFrom, nTo);
      }
    }//end for

    // common decisions
    LinkedList<XabslNode> vertices = new LinkedList<XabslNode>(optionGraph.getVertices());
    for(Transition t : commonDecisions)
    {
      for(XabslNode nFrom : vertices)
      {
        if(nFrom.getType() == XabslNode.Type.State)
        {
          XabslNode nTo = new XabslNode(t.to, XabslNode.Type.State);

          optionGraph.addEdge(new XabslEdge(XabslEdge.Type.CommonDecision), nFrom, nTo);
        }
      }//end for
    }//end for

    return optionGraph;
  }//end getOptionGraph

  
  public Map<String, State> getStateMap() {
    return stateMap;
  }


  public ArrayList<Transition> getStateTransitionList() {
    return stateTransitionList;
  }


  public class State
  {

    public State(String name, String comment, int offset,
      boolean target, boolean initial, Set<String> outgoingOptions)
    {
      this.name = name;
      this.comment = comment;
      this.offset = offset;
      this.number = stateMap.size();
      this.target = target;
      this.initial = initial;
      this.outgoingOptions = outgoingOptions;

      // add the state to the map
      stateMap.put(this.name, this);
    }
    
    public final String name;
    public final String comment;
    public final int offset;
    public final boolean target;
    public final boolean initial;
    public final Set<String> outgoingOptions;
    private final int number;

    @Override
    public String toString()
    {
      return "\"" + name + "\" [shape=\"circle\" pos=\"10," + (number * 70) + "\" URL=\"" + offset + "\"];";
    }//end toString

    @Override
    public boolean equals(Object obj)
    {
      if(obj == null)
      {
        return false;
      }
      if(getClass() != obj.getClass())
      {
        return false;
      }
      final State other = (State) obj;
      if((this.name == null) ? (other.name != null) : !this.name.equals(other.name))
      {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode()
    {
      int hash = 7;
      hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
      return hash;
    }
  }//end class State

  public class Transition
  {

    public Transition(String from, String to, int offset)
    {
      this.from = from;
      this.to = to;
      this.offset = offset;

      stateTransitionList.add(this);
    }
    public final String from;
    public final String to;
    public final int offset;

    @Override
    public String toString()
    {
      return "\"" + from + "\" -> \"" + to + "\" [URL=\"" + offset + "\"]";
    }//end toString

    @Override
    public boolean equals(Object obj)
    {
      if(obj == null)
      {
        return false;
      }
      if(getClass() != obj.getClass())
      {
        return false;
      }
      final Transition other = (Transition) obj;
      if((this.from == null) ? (other.from != null) : !this.from.equals(other.from))
      {
        return false;
      }
      if((this.to == null) ? (other.to != null) : !this.to.equals(other.to))
      {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode()
    {
      int hash = 3;
      hash = 67 * hash + (this.from != null ? this.from.hashCode() : 0);
      hash = 67 * hash + (this.to != null ? this.to.hashCode() : 0);
      return hash;
    }
  }//end class Transition
  
}//end class XABSLOptionContext
