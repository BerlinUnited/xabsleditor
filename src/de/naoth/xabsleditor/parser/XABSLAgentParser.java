
package de.naoth.xabsleditor.parser;

import de.naoth.xabsleditor.parser.XParser.XABSLAbstractParser;
import org.fife.ui.rsyntaxtextarea.Token;

/**
 *
 * @author thomas
 */
public class XABSLAgentParser extends XABSLAbstractParser
{

  public XABSLAgentParser(XParser parent)
  {
    parent.super(parent);
  }

  @Override
  void parse() throws Exception
  {
    while(isToken("include"))
    {
      isTokenAndEat("include");
      isTokenAndEat(Token.LITERAL_STRING_DOUBLE_QUOTE);
      isTokenAndEat(";");
    }

    agentDefinition();
    while(isToken("agent"))
    {
      agentDefinition();
    }

  }

  private void agentDefinition() throws Exception
  {
    isTokenAndEat("agent");
    String agentID = parseIdentifier();
    isTokenAndEat("(");
    isTokenAndEat(Token.LITERAL_STRING_DOUBLE_QUOTE);
    isTokenAndEat(",");
    String rootOption = parseIdentifier();
    isTokenAndEat(")");
    isTokenAndEat(";");

    getXABSLContext().addAgent(agentID, rootOption);
  }

}
