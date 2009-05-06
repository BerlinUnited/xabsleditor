/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hu_berlin.informatik.ki.jxabsleditor.parser;

/**
 *
 * @author thomas
 */
public class XabslEdge
{
  private boolean commonDecision;

  public XabslEdge(boolean commonDecision)
  {
    this.commonDecision = commonDecision;
  }

  

  public boolean isCommonDecision()
  {
    return commonDecision;
  }

}
