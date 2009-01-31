/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.hu_berlin.informatik.ki.jxabsleditor;

import de.hu_berlin.informatik.ki.jxabsleditor.editorpanel.XParser;
import java.io.StringReader;

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

    for(XParser.State s : p.getStateMap().values())
    {
      r.append(s);
      r.append("\n");
    }

    for(XParser.Transition s : p.getStateTransitionList())
    {
      r.append(s);
      r.append("\n");
    }

    r.append("}\n");

    return r.toString();
  }

}
