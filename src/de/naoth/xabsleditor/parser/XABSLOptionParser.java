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

import de.naoth.xabsleditor.parser.XABSLContext.XABSLBasicSymbol;
import de.naoth.xabsleditor.parser.XABSLContext.XABSLOption;
import de.naoth.xabsleditor.parser.XParser.XABSLAbstractParser;
import java.util.HashSet;
import java.util.Set;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rsyntaxtextarea.Token;

/**
 *
 * @author Heinrich Mellmann
 */
public class XABSLOptionParser extends XABSLAbstractParser
{

  public XABSLOptionParser(XParser parent)
  {
    parent.super(parent);
    parent.xabslOptionContext = new XABSLOptionContext();
  }

    // PARSE OPTION
  private String currentStateName;
  private Set<String> currentOutgoingOptions;
  private Set<String> allOutgoingOptions;
  private boolean currentStateInitial;
  private boolean currentStateTarget;
  private XABSLOption currentOption;

  public XABSLOption getOption() {
      return currentOption;
  }

  @Override
  public void parse() throws Exception
  {
    currentOutgoingOptions = new HashSet<String>();
    allOutgoingOptions = new HashSet<String>();

    isTokenAndEat("option");
    currentOption = new XABSLOption(parseIdentifier());
    currentOption.setComment(getCurrentComment());
    isTokenAndEat("{");

    while(isToken("float") || isToken("bool") || isToken("enum"))
    {
      parseSymbolDefinition();
    }

    if(isToken("common"))
    {
      parseCommonDecision();
    }

    while(!isToken("}"))
    {
      parseState();
      allOutgoingOptions.addAll(currentOutgoingOptions);
    }//end while
    isTokenAndEat("}");

    
    // expect end of file
    isEOF();

    for(String s : allOutgoingOptions)
    {
      currentOption.addAction(s);
    }
    getXABSLContext().add(currentOption);
  }//end parseOption


  private void parseSymbolDefinition() throws Exception
  {
    XABSLBasicSymbol parameter = new XABSLBasicSymbol();
    parameter.setComment(getCurrentComment());

    if(isToken("bool"))
    {
      parameter.setType("bool");
      isTokenAndEat("bool");
    }
    else if(isToken("float"))
    {
      parameter.setType("float");
      isTokenAndEat("float");
    }else
    {
      // try to parse enum
      isTokenAndEat("enum");
      String type = parseIdentifier();
      parameter.setType(getXABSLContext().getEnumMap().get(type));
    }

    isTokenAndEat("@");

    // eat name
    parameter.setName(parseIdentifier());

    if(isToken("["))
    {
      eat();
      eat();
      isTokenAndEat("]");
    }//end if

    isTokenAndEat(";");
    this.currentOption.addParameter(parameter);
  }//end parseSymbolDefinition


  private void parseCommonDecision() throws Exception
  {
    isTokenAndEat("common");
    isTokenAndEat("decision");
    parseBlock();
  }//end parseCommonDecision

    private void eatInitialOrTarget() throws Exception
  {
    if(!currentStateInitial && isToken("initial"))
    {
      eat();
      currentStateInitial = true;
    }
    else if(!currentStateTarget && isToken("target"))
    {
      eat();
      currentStateTarget = true;
    }
  }//end eatInitialOrTarget

  private void parseState() throws Exception
  {
    currentStateInitial = false;
    currentStateTarget = false;
    currentOutgoingOptions = new HashSet<String>();

    eatInitialOrTarget();
    // we can have initial *and* target state
    eatInitialOrTarget();

    isTokenAndEat("state");

    int offset = getCurrentToken().getOffset();
    currentStateName = parseIdentifier();


    isTokenAndEat("{");
    if(isToken("decision"))
    {
      parseDecision();
    }
    parseAction();

    addState(currentStateName,
             getCurrentComment(),
             offset,
             currentStateTarget,
             currentStateInitial,
             currentOutgoingOptions);

    isTokenAndEat("}");
  }//end parseState

  private void parseDecision() throws Exception
  {
    isTokenAndEat("decision");
    /*
    isTokenAndEat("{");

    boolean resume = true;

    while(resume)
    {
    if(isToken("if"))
    {
    isTokenAndEat("if");
    isTokenAndEat("(");
    parseBooleanExpression();
    isTokenAndEat(")");
    isTokenAndEat("{");
    parseSingleDecision();
    isTokenAndEat("}");
    }
    else if(isToken("else"))
    {
    isTokenAndEat("else");
    // "else if" ?
    if(isToken("if"))
    {
    isTokenAndEat("if");
    isTokenAndEat("(");
    parseBooleanExpression();
    isTokenAndEat(")");
    }
    else
    {
    // only one else allowed, and only at the end
    resume = false;
    }

    isTokenAndEat("{");
    parseSingleDecision();
    isTokenAndEat("}");
    }
    else
    {
    parseSingleDecision();
    // nothing found
    resume = false;
    }
    }
     */

    //isTokenAndEat("}");
    parseBlock();
  }//end parseDecision

  private void parseSingleDecision() throws Exception
  {
    if(isToken("goto"))
    {
      parseGoto();
    }
    else if(isToken("stay"))
    {
      isTokenAndEat("stay");
      addTransition(currentStateName, currentStateName, getCurrentToken().getOffset());
      isTokenAndEat(";");
    }
    else
    {
      addNotice(new DefaultParserNotice( this.parent,
        "Either \"stay\" or \"goto\" needed in decision",
        getCurrentLine(), getCurrentToken().getOffset(), Math.max(getCurrentToken().getEndOffset(), 2)));

    }
  }//end parseSingleDecision


  private void parseAction() throws Exception
  {
    isTokenAndEat("action");

    isTokenAndEat("{");
    while(!isToken("}"))
    {
      parseExpression();
    }//end while
    isTokenAndEat("}");

  }//end parseAction

  private void parseGoto() throws Exception
  {
    isTokenAndEat("goto");

    int offset = getCurrentToken().getOffset();
    String targetStateName = parseIdentifier();

    addTransition(currentStateName, targetStateName, offset);
    isTokenAndEat(";");

  //System.out.println(currentStateName + " -> " + targetStateName);
  }//end parseGoto

  private void parseExpression() throws Exception
  {
    if(isToken(Token.FUNCTION))
    {
      parseFunction();
    }
    else
    {
      parseAssignment();
    }
  }//end parseExpression

  private void parseFunction() throws Exception
  {
    if(isToken(Token.FUNCTION))
    {
      currentOutgoingOptions.add(getCurrentToken().getLexeme());
    }
    parseFunctionSingle();
    isTokenAndEat(";");
  }//end parseFunction

  private void parseFunctionSingle() throws Exception
  {
    isTokenAndEat(Token.FUNCTION);
    parseFunctionParameter();
  }

  private void parseFunctionParameter() throws Exception
  {
    isTokenAndEat("(");
    int bracketCount = 1;

    while(bracketCount > 0)
    {
      if(isToken(Token.LITERAL_NUMBER_DECIMAL_INT) || isToken(Token.LITERAL_NUMBER_FLOAT))
      {
        eat();
      }
      else if(isToken(Token.FUNCTION))
      {
        parseFunctionSingle();
      }
      else if(isToken(Token.IDENTIFIER))
      {
        parseIdentifier();
      }
      else if(isToken("="))
      {
        isTokenAndEat("=");
      }
      else if(isToken("("))
      {
        isTokenAndEat("(");
        bracketCount++;
      }
      else if(isToken(")"))
      {
        isTokenAndEat(")");
        bracketCount--;
      }
      else
      {
        // skip
        eat();
      }

      if(isToken(","))
      {
        isTokenAndEat(",");
      }
    }//end while
  }//end parseFunctionParameter

  private void parseBooleanExpression() throws Exception
  {
    if(isToken("true"))
    {
      isTokenAndEat("true");
    }
    else if(isToken("false"))
    {
      isTokenAndEat("false");
    }
    else if(isToken("&&"))
    {
      isTokenAndEat("&&");
      parseBooleanExpression();
    }
    else if(isToken("||"))
    {
      isTokenAndEat("|");
      parseBooleanExpression();
    }
    else if(isToken("<="))
    {
      isTokenAndEat("<=");
      parseBooleanExpression();
    }
    else if(isToken(">="))
    {
      isTokenAndEat(">=");
      parseBooleanExpression();
    }
    else if(isToken("<"))
    {
      isTokenAndEat("<");
      parseBooleanExpression();
    }
    else if(isToken(">"))
    {
      isTokenAndEat(">");
      parseBooleanExpression();
    }
    else if(isToken("=="))
    {
      isTokenAndEat("==");
      parseBooleanExpression();
    }
    else if(isToken("("))
    {
      isTokenAndEat("(");
      parseBooleanExpression();
      isTokenAndEat(")");
    }
    else
    {
      parseIdentifier();
      if(!isToken(")"))
      {
        parseBooleanExpression();
      }
    }
  }//end parseBooleanExpression


  // parse a block surounded by {}
  private void parseBlock() throws Exception
  {
    isTokenAndEat("{");

    while(!isToken("}"))
    {
      if(isToken("{"))
      {
        parseBlock();
      }
      else if(isToken("goto") || isToken("stay"))
      {
        parseSingleDecision();
      }
      else
      {
        eat();
      }
    }//end while

    isTokenAndEat("}");
  }//end parseBlock

  private void parseAssignment() throws Exception
  {
    parseIdentifier();
    if(!isTokenAndEat("="))
    {
      return;
    }

    int parenthesisCount = 0;

    while(isToken(Token.IDENTIFIER) || isToken(Token.LITERAL_BOOLEAN)
      || isToken(Token.LITERAL_NUMBER_DECIMAL_INT)
      || isToken(Token.LITERAL_NUMBER_FLOAT) || isToken(Token.OPERATOR)
      || isToken("(") || isToken(")") || isToken("@") || isToken(Token.FUNCTION))
    {

      if(isToken("@"))
      {
        isTokenAndEat("@");
      }
      else if(isToken(Token.IDENTIFIER))
      {
        parseIdentifier();
      }
      else if(isToken("("))
      {
        eat();
        parenthesisCount++;
      }
      else if(isToken(")"))
      {
        eat();
        parenthesisCount--;
      }
      else if(isToken(Token.FUNCTION))
      {
        parseFunctionSingle();
      }
      else
      {
        eat();
      }
    }

    if(parenthesisCount < 0)
    {
      addNotice(new DefaultParserNotice(this.parent,
        "More right paranthesis than left ones (" + Math.abs(parenthesisCount) + ")",
        getCurrentLine(), getCurrentToken().getOffset(), Math.max(getCurrentToken().getEndOffset(), 2)));
    }
    else if(parenthesisCount > 0)
    {
      addNotice(new DefaultParserNotice(this.parent,
        "More left paranthesis than right ones (" + Math.abs(parenthesisCount) + ")",
        getCurrentLine(), getCurrentToken().getOffset(), Math.max(getCurrentToken().getEndOffset(), 2)));
    }
    isTokenAndEat(";");
  }//end parseAssignment



  private void addState(
          String name,
          String comment,
          int offset,
          boolean target,
          boolean initial,
          Set<String> outgoingOptions
          ) throws Exception
  {
    if(this.parent.xabslOptionContext.getStateMap().containsKey(name))
    {
      addNotice(new DefaultParserNotice(this.parent, "State " + name + " already defined.", getCurrentLine(), offset, name.length()));
      throw new Exception("State " + name + " already defined.");
    }//end if

    // create new state :)
    this.parent.xabslOptionContext.new State(
            name,
            comment,
            offset,
            target,
            initial,
            outgoingOptions);

  }//end addState


  private void addTransition(String from, String to, int offset)
  {
    // create new transition
    this.parent.xabslOptionContext.new Transition(from, to, offset);
  }//end addTransition
}//end class XABSLOptionParser
