/*
 *
 */

package de.hu_berlin.informatik.ki.jxabsleditor.parser;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Heinrich Mellmann
 */
public class XABSLContext
{
  private ArrayList<XABSLSymbol> symbolsList = new ArrayList<XABSLSymbol>();
  private HashMap<String, XABSLEnum> enumMap = new HashMap<String, XABSLEnum>();
  private HashMap<String, XABSLOption> optionMap = new HashMap<String, XABSLOption>();

  public HashMap<String, XABSLOption> getOptionMap() {
    return optionMap;
  }

  public HashMap<String, XABSLEnum> getEnumMap() {
    return enumMap;
  }

  public ArrayList<XABSLSymbol> getSymbolsList() {
    return symbolsList;
  }

  public boolean add(XABSLSymbol e) {
    return symbolsList.add(e);
  }

  public void add(XABSLEnum e) {
    enumMap.put(e.name, e);
  }

  public void add(XABSLOption e) {
    optionMap.put(e.name, e);
  }

  
  public static class XABSLEnum
  {
    public final String name;
    private ArrayList<String> elements;

    public XABSLEnum(String name) {
      this.name = name;
      this.elements = new ArrayList<String>();
    }

    public boolean add(String element) {
      return elements.add(element);
    }

    public ArrayList<String> getElements() {
      return elements;
    }
  }//end class XABSLEnum



  public static class XABSLBasicSymbol
  {
    protected String name;
    protected String type;
    protected String comment;

    // optional parameter for float symbols
    protected String unit;
    protected String range;

    // if the tyme is an enum
    protected XABSLEnum enumDeclaration;

    public XABSLBasicSymbol(){}

    public XABSLBasicSymbol(String type, String name)
    {
      this.type = type;
      this.name = name;
      this.comment = "";
      this.enumDeclaration = null;
    }

    public String getComment() {
      return comment;
    }

    public void setComment(String comment) {
      this.comment = comment;
    }

    public XABSLEnum getEnumDeclaration() {
      return enumDeclaration;
    }

    public void setEnumDeclaration(XABSLEnum enumDeclaration) {
      this.enumDeclaration = enumDeclaration;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getRange() {
      return range;
    }

    public void setRange(String range) {
      this.range = range;
    }

    public String getType() {
      return type;
    }

    public void setType(XABSLEnum enumDeclaration)
    {
      this.enumDeclaration = enumDeclaration;
      this.type = "enum";
    }

    public void setType(String type) {
      this.type = type;
    }

    public String getUnit() {
      return unit;
    }

    public void setUnit(String unit) {
      this.unit = unit;
    }

    @Override
    public String toString()
    {
      return type + " " + name;
    }
  }//end class XABSLBasicSymbol


  public static class XABSLSymbol extends XABSLBasicSymbol
  {
    private SecondaryType secondaryType;
    private ArrayList<XABSLBasicSymbol> parameter;

    public enum SecondaryType
    {
      output,
      input,
      internal
    }

    public XABSLSymbol()
    {
      super();
      this.parameter = new ArrayList<XABSLBasicSymbol>();
    }

    public XABSLSymbol(String type, String name)
    {
      super(type, name);
      this.parameter = new ArrayList<XABSLBasicSymbol>();
    }

    public SecondaryType getSecondaryType() {
      return secondaryType;
    }

    public void setSecondaryType(SecondaryType secondaryType) {
      this.secondaryType = secondaryType;
    }

    public ArrayList<XABSLBasicSymbol> getParameter() {
      return parameter;
    }

    public boolean addParameter(XABSLBasicSymbol e) {
      return parameter.add(e);
    }

    @Override
    public String toString()
    {
        return type + " " + secondaryType.name() + " " + name;
    }
  }//end class XABSLSymbol


  public static class XABSLOption
  {
    private String name;
    private String comment;
    private ArrayList<XABSLBasicSymbol> parameter;

    public XABSLOption(String name) {
      this.name = name;
      this.comment = "Option " + name;
      this.parameter = new ArrayList<XABSLBasicSymbol>();
    }

    public boolean addParameter(XABSLBasicSymbol e) {
      return parameter.add(e);
    }

    public ArrayList<XABSLBasicSymbol> getParameter() {
      return parameter;
    }

    public String getName() {
      return name;
    }

    public String getComment() {
      return comment;
    }

    public void setComment(String comment) {
      this.comment = comment;
    }

  }//end class XABSLOption
}//end XABSLContext
