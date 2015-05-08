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
package de.naoth.xabsleditor;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Collection of static tool functions.
 * 
 * @author Thomas Krause
 * @author Heinrich Mellmann
 */
public class Tools
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
  }//end handleException

  /**
   * TODO: currently its a kind of a hack ... make it better
   * Returns the agent-file of an option
   * @param optionFile
   * @return The agents.xml or null if not found.
   */
  public static File getAgentFileForOption(File optionFile)
  {
    File current = optionFile;

    if(current == null)
      return null;

    // get parent directory
    if(current.isFile())
      current = current.getParentFile();

    while(current != null)
    {
      // check if agents.xabsl is in this folder
      File agentFile = new File(current.getAbsolutePath() + "/agents.xabsl");
      if(agentFile.exists() && agentFile.isFile())
      {
        return agentFile;
      }
      current = current.getParentFile();
    }//end while

    return null;
  }//end getAgentFileForOption
  

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

  /**
   * Reads a resource file as string.
   */
  public static String getResourceAsString(String name)
  {
    InputStream stream = (new Object()).getClass().getResourceAsStream(name);
    StringBuilder builder = new StringBuilder();

    if(stream != null)
    {
      try{
        while (stream.available() > 0)
        {
          builder.append((char)stream.read());
        }
      }catch(IOException e)
      {
        handleException(e);
      }
      return builder.toString();
    }//end if

    return null;
  }//end getResourceAsString

  
  /**
   * Validates the name of a given file, i.e., if the file name doesn't have
   * the proper ending, it will be appended.
   * @param file the file which name has to be validated
   * @param filter a file filter containing the definition of the file ending.
   * @return a file with validated file name
   */
  public static File validateFileName(final File file, final javax.swing.filechooser.FileFilter filter)
  {
    if(file == null || filter == null || filter.accept(file))
    {
      return file;
    }
    // remove wrong file extension if any
    String fileName = file.getName();
    int index = fileName.lastIndexOf(".");
    if(index > 0)
    {
      fileName = fileName.substring(0, index);
    }

    final String extension = filter.toString();

    if(extension.matches("(\\w)*"))
    {
      String newFileName = fileName + "." + extension;
      File newFile = new File(file.getParent(), newFileName);
      return newFile;
    }//end if

    return file;
  }//end validateFileName

}//end class Tools
