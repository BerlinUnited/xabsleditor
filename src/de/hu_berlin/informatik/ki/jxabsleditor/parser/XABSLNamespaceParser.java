/*
 * 
 */

package de.hu_berlin.informatik.ki.jxabsleditor.parser;

import de.hu_berlin.informatik.ki.jxabsleditor.parser.XABSLContext.XABSLBasicSymbol;
import de.hu_berlin.informatik.ki.jxabsleditor.parser.XABSLContext.XABSLEnum;
import de.hu_berlin.informatik.ki.jxabsleditor.parser.XABSLContext.XABSLSymbol;
import de.hu_berlin.informatik.ki.jxabsleditor.parser.XParser.XABSLAbstractParser;
import org.fife.ui.rsyntaxtextarea.ParserNotice;
import org.fife.ui.rsyntaxtextarea.Token;

/**
 *
 * @author Heinrich Mellmann
 */
public class XABSLNamespaceParser extends XABSLAbstractParser
{

  public XABSLNamespaceParser(XParser parent)
  {
    parent.super(parent);
  }

  // PARSE SYMBOLS
  XABSLSymbol currentSymbol;
  XABSLEnum currentEnumDeclaration;

  @Override
  public void parse() throws Exception
  {
    isTokenAndEat("namespace");
    isTokenAndEat(Token.FUNCTION);
    isTokenAndEat("(");
    isTokenAndEat(Token.LITERAL_STRING_DOUBLE_QUOTE);
    isTokenAndEat(")");
    isTokenAndEat("{");

    while(!isToken("}"))
    {
      parseSymbolsEntry();
    }//end while

    isTokenAndEat("}");
  }//end parseNamespace

  private void parseSymbolsEntry() throws Exception
  {
    String type;
    String comment = getCurrentComment();

    // enum
    if(isToken("enum"))
    {
      boolean isInternal = false;
      eat();
      type = parseIdentifier();
      if(isToken("internal"))
      {
        isTokenAndEat("internal");
        isInternal = true;
      }

      if(isToken("{"))
      {
        currentEnumDeclaration = new XABSLEnum(type);
        parseEnumDeclaration();
        getXABSLContext().add(currentEnumDeclaration);
      }
      else // it's a enum symbol definition :)
      {
        currentSymbol = new XABSLSymbol();

        XABSLEnum enumType = getXABSLContext().getEnumMap().get(type);
        if(enumType != null)
          currentSymbol.setType(enumType);
        else // enum type is not declared
          currentSymbol.setType(type);
        currentSymbol.setComment(comment);

        if(!isInternal)
        {
          if(isToken("output"))
          {
            currentSymbol.setSecondaryType(XABSLSymbol.SecondaryType.output);
            isTokenAndEat("output");
          }
          else
          {
            currentSymbol.setSecondaryType(XABSLSymbol.SecondaryType.input);
            isTokenAndEat("input");
          }
        }else
        {
          currentSymbol.setSecondaryType(XABSLSymbol.SecondaryType.internal);
        }

        currentSymbol.setName(parseIdentifier());
        isTokenAndEat(";");

        getXABSLContext().add(currentSymbol);
      }
    }
    else if(isToken("float") || isToken("bool"))
    {
      currentSymbol = new XABSLSymbol();
      currentSymbol.setType(getCurrentToken().getLexeme());
      currentSymbol.setComment(comment);

      eat();

      parseSymbolDeclaration();

      getXABSLContext().add(currentSymbol);
    }
    else
    {
      eat();
      addNotice(new ParserNotice("A symbol declaration or enum definition expected.",
              getCurrentToken().offset, getCurrentToken().getLexeme().length()));
    }

  }//end parseSymbolsEntry


  private void parseEnumDeclaration() throws Exception
  {
    isTokenAndEat("{");
    currentEnumDeclaration.add(parseIdentifier());

    while(isToken(","))
    {
      eat(); // eat ","
      currentEnumDeclaration.add(parseIdentifier());
    }//end while

    isTokenAndEat("}");
    isTokenAndEat(";");
  }//end parseEnumDeclaration


    private void parseSymbolDeclaration() throws Exception
  {
    if(isToken("output"))
    {
      currentSymbol.setSecondaryType(XABSLSymbol.SecondaryType.output);
      isTokenAndEat("output");
    }
    else if(isToken("input"))
    {
      currentSymbol.setSecondaryType(XABSLSymbol.SecondaryType.input);
      isTokenAndEat("input");
    }
    else
    {
      currentSymbol.setSecondaryType(XABSLSymbol.SecondaryType.internal);
      isTokenAndEat("internal");
    }

    currentSymbol.setName(parseIdentifier());

    if(isToken("["))
    {
      do
      {
        eat();
      }
      while(!isToken("]"));

      isTokenAndEat("]");
    }//end if

    if(isToken(Token.LITERAL_STRING_DOUBLE_QUOTE))
    {
      isTokenAndEat(Token.LITERAL_STRING_DOUBLE_QUOTE);
    }


    // parse function symbol parameters
    if(isToken("("))
    {
      //this.currentComment = "";
      getCurrentComment();
      eat();// eat "("

      // parse symbol parameter
      while(!isToken(")"))
      {
        XABSLBasicSymbol parameter = new XABSLBasicSymbol();
        parameter.setComment(getCurrentComment());

        // parse parameter type if given (float by default)
        parameter.setType("float");
        if(isToken("bool"))
        {
          isTokenAndEat("bool");
          parameter.setType("bool");
        }else if(isToken("float"))
        {
          isTokenAndEat("float");
        }else if(isToken("enum"))
        {
          isTokenAndEat("enum");

          String enumName = parseIdentifier();
          XABSLEnum enumType = getXABSLContext().getEnumMap().get(enumName);
          if(enumType == null)
            parameter.setType(enumName);
          else
            parameter.setType(enumType);
        }

        parameter.setName(parseIdentifier()); // parameter name

        if(parameter.getType().equals("float"))
        {
          // parse range if defined
          String range = "";
          if(isToken("["))
          {
            eat();
            while(!isToken("]"))
            {
              range += getCurrentToken().getLexeme();
              eat();
            }//end while
            isTokenAndEat("]");
            parameter.setRange(range);
          }//end if

          // parse unit if defined
          if(isToken(Token.LITERAL_STRING_DOUBLE_QUOTE))
          {
            parameter.setUnit(getCurrentToken().getLexeme());
            isTokenAndEat(Token.LITERAL_STRING_DOUBLE_QUOTE);
          }//end if
          //System.out.println(parameter);
        }//end if type = float

        this.currentSymbol.addParameter(parameter);

        isTokenAndEat(";");
      }//end while

      isTokenAndEat(")");
    }//end if


    isTokenAndEat(";");
  }//end parseSymbolDeclaration
    
}//end class XABSLNamespaceParser
