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
package de.hu_berlin.informatik.ki.jxabsleditor.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Heinrich Mellmann
 */
public class XABSLContext
{
  private Map<String, XABSLSymbol> symbolMap = new TreeMap<String, XABSLSymbol>();
  private Map<String, XABSLEnum> enumMap = new TreeMap<String, XABSLEnum>();
  private Map<String, XABSLOption> optionMap = new TreeMap<String, XABSLOption>();
  private Map<String, File> optionPathMap = new TreeMap<String, File>();

  public Map<String, XABSLOption> getOptionMap() {
    return optionMap;
  }

  public Map<String, XABSLEnum> getEnumMap() {
    return enumMap;
  }

  public Map<String, XABSLSymbol> getSymbolMap() {
    return symbolMap;
  }

  public Map<String, File> getOptionPathMap()
  {
    return optionPathMap;
  }
  
  public void add(XABSLSymbol e) {
    // TODO: symbols cannot be overwritten...
    if(e != null && !symbolMap.containsKey(e.name))
      symbolMap.put(e.name, e);
  }

  public void add(XABSLEnum e) {
    // TODO: symbols cannot be overwritten...
    if(e != null && !enumMap.containsKey(e.name))
      enumMap.put(e.name, e);
  }

  public void add(XABSLOption e) {
    // TODO: symbols cannot be overwritten...
    if(e != null && !optionMap.containsKey(e.name))
      optionMap.put(e.name, e);
  }


  public static class DeclarationSource
  {
    public final String fileName;
    public final int offset;

    public DeclarationSource(String fileName, int offset) {
      this.fileName = fileName;
      this.offset = offset;
    }
  }//end class DeclarationSource

  
  public static class XABSLEnum
  {
    public final String name;
    private ArrayList<String> elements;
    private DeclarationSource declarationSource;

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

    public DeclarationSource getDeclarationSource() {
      return declarationSource;
    }

    public void setDeclarationSource(DeclarationSource declarationSource) {
      this.declarationSource = declarationSource;
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
    private DeclarationSource declarationSource;

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

    public DeclarationSource getDeclarationSource() {
      return declarationSource;
    }

    public void setDeclarationSource(DeclarationSource declarationSource) {
      this.declarationSource = declarationSource;
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
    private ArrayList<String> actions;

    public XABSLOption(String name) {
      this.name = name;
      this.comment = "Option " + name;
      this.parameter = new ArrayList<XABSLBasicSymbol>();
      this.actions = new ArrayList<String>();
    }

    public boolean addAction(String action)
    {
      return actions.add(action);
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

    public ArrayList<String> getActions()
    {
      return actions;
    }

  }//end class XABSLOption
}//end XABSLContext
