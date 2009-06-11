package de.hu_berlin.informatik.ki.jxabsleditor.parser;

/**
 *
 * @author thomas
 */
public class XabslEdge
{

  public enum Type
  {
    Normal,
    CommonDecision,
    Outgoing
  }

  private Type type;
  
  public XabslEdge(Type type)
  {
    this.type = type;
  }

  public Type getType()
  {
    return type;
  }




}
