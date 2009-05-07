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
  private String from;
  private String to;

  public XabslEdge(boolean commonDecision, String from, String to)
  {
    this.commonDecision = commonDecision;
    this.from = from;
    this.to = to;
  }

  public boolean isCommonDecision()
  {
    return commonDecision;
  }

  public String getFrom()
  {
    return from;
  }

  public String getTo()
  {
    return to;
  }

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
    final XabslEdge other = (XabslEdge) obj;
    if(this.commonDecision != other.commonDecision)
    {
      return false;
    }
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
    int hash = 7;
    hash = 89 * hash + (this.commonDecision ? 1 : 0);
    hash = 89 * hash + (this.from != null ? this.from.hashCode() : 0);
    hash = 89 * hash + (this.to != null ? this.to.hashCode() : 0);
    return hash;
  }

  

}
