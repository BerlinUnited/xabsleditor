/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.hu_berlin.informatik.ki.jxabsleditor;

import de.hu_berlin.informatik.ki.jxabsleditor.editorpanel.XParser;
import java.io.StringReader;
import java.util.HashSet;

/**
 *
 * @author thomas
 */
public class Xabsl2Dot
{

  /** Convert a XABSL-String to a dot-equivalent */
  public static String convert(String xabsl)
  {
    StringBuilder r = new StringBuilder();

    r.append("digraph optionGraph {\n");


    XParser p = new XParser();
    // let the parser do most of the job
    p.parse(new StringReader(xabsl));

    HashSet<XParser.Transition> alreadWrittenTransitions = new HashSet<XParser.Transition>();
    HashSet<XParser.State> alreadWrittenStates = new HashSet<XParser.State>();

    HashSet<XParser.Transition> commonDecisions = new HashSet<XParser.Transition>();

    for(XParser.State s : p.getStateMap().values())
    {
      if(!alreadWrittenStates.contains(s))
      {
        alreadWrittenStates.add(s);
        r.append(s);
        r.append("\n");
      }
    }

    for(XParser.Transition t : p.getStateTransitionList())
    {
      if(!alreadWrittenTransitions.contains(t))
      {
        alreadWrittenTransitions.add(t);
        if(t.from == null)
        {
          commonDecisions.add(t);
        }
        else
        {
          r.append(t);
          r.append("\n");
        }
      }
    }

    // common decisions
    for(XParser.Transition t : commonDecisions)
    {
      for(XParser.State s : alreadWrittenStates)
      {
        r.append("\"" + s.name + "\" -> \"" + t.to + "\" [style=dashed,color=lightgray]");
        r.append("\n");
      }
    }

    r.append("}\n");

    return r.toString();
  }

}
