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
