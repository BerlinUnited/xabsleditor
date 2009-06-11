/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hu_berlin.informatik.ki.jxabsleditor.parser;

/**
 *
 * @author thomas
 */
public class XabslNode
{

  public static enum Type
  {
    State,
    Option
  };

  private String name;
  private Type type;
  private boolean targetState;
  private boolean initialState;
  private int posInText = -1;

  public XabslNode()
  {
  }

  public XabslNode(String name, Type type)
  {
    this.name = name;
    this.type = type;
  }


  public boolean isInitialState()
  {
    return initialState;
  }

  public void setInitialState(boolean initialState)
  {
    this.initialState = initialState;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public Type getType()
  {
    return type;
  }

  public void setType(Type type)
  {
    this.type = type;
  }

  public boolean isTargetState()
  {
    return targetState;
  }

  public void setTargetState(boolean targetState)
  {
    this.targetState = targetState;
  }

  public int getPosInText()
  {
    return posInText;
  }

  public void setPosInText(int posInText)
  {
    this.posInText = posInText;
  }



  @Override
  public String toString()
  {
    return new String(name);
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
    final XabslNode other = (XabslNode) obj;
    if((this.name == null) ? (other.name != null) : !this.name.equals(other.name))
    {
      return false;
    }
    if(this.type != other.type)
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 17 * hash + (this.name != null ? this.name.hashCode() : 0);
    hash = 17 * hash + (this.type != null ? this.type.hashCode() : 0);
    return hash;
  }



  
}
