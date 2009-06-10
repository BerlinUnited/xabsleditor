/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.hu_berlin.informatik.ki.jxabsleditor;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Static helper functions
 * @author thomas
 */
public class Helper
{
  /**
   * Prints stacktrace and shows a dialog to the user.
   * @param ex
   */
  public static void handleException(Exception ex)
  {
    // log
    Logger.getLogger("XabslEditor").log(Level.SEVERE, null, ex);
    // show
    ExceptionDialog dlg = new ExceptionDialog(null, ex);
    dlg.setVisible(true);
  }

  /**
   * Returns the agent-file of an option
   * @param optionFile
   * @return The agents.xml or null if not found.
   */
  public static File getAgentFileForOption(File optionFile)
  {
    File agent = null;
    File current = optionFile;

    if(current != null)
    {
      // check if this is already the root file
      if("agents.xabsl".equalsIgnoreCase(current.getName())
        && current.isFile())
      {
        File optionsDir = new File(current.getParentFile().getAbsolutePath() + "/Options");
        if(optionsDir.exists() && optionsDir.isDirectory())
        {
          return current;
        }
      }

      // find options- or symbols-folder
      current = current.getParentFile();
      while(current != null && !current.getName().equalsIgnoreCase("Options")
            && !current.getName().equalsIgnoreCase("Symbols"))
      {
        current = current.getParentFile();
      }

      if(current != null)
      {
        // the agents.xabsl should be in the parent folder
        current = current.getParentFile();
        if(current != null)
        {
          // check if agents.xabsl is in this folder
          File tmpAgent = new File(current.getAbsolutePath() + "/agents.xabsl");
          if(tmpAgent.exists() && tmpAgent.isFile())
          {
            agent = tmpAgent;
          }
        }
      }
    }

    return agent;
  }
  

  public static String readFileToString(File file) throws IOException
  {
    FileReader reader = new FileReader(file);
    StringBuilder buffer = new StringBuilder();

    int c = reader.read();
    while(c != -1)
    {
      buffer.append((char) c);
      c = reader.read();
    }//end while

    reader.close();
    return buffer.toString();
  }//end readFileToString

  

}
