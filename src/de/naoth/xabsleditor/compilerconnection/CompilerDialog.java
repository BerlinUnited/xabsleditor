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
package de.naoth.xabsleditor.compilerconnection;

import de.naoth.xabsleditor.Tools;
import de.naoth.xabsleditor.OptionsDialog;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.security.CodeSource;
import java.util.Properties;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 *
 * @author thomas
 */
public class CompilerDialog extends javax.swing.JDialog
{

  private SwingWorker<CompileResult, Object> worker;
  private File optionFile;
  private File outputFile;
  private CompilationFinishedReceiver receiver;
  private final CompilerDialog finalThis;
  private final Properties configuration;

  /** Creates new form CompilerDialog */
  public CompilerDialog(java.awt.Frame parent, boolean modal,
    File optionFile, File outputFile,
    CompilationFinishedReceiver receiver, Properties configuration)
  {
    super(parent, modal);
    finalThis = this;
    this.configuration = configuration;

    this.optionFile = optionFile;
    this.outputFile = outputFile;
    this.receiver = receiver;

    worker = new CompiliationWorker();

    initComponents();

    pbCompiling.setIndeterminate(true);
    this.validate();

    worker.execute();
  }

  public class CompiliationWorker extends SwingWorker<CompileResult, Object>
  {

    @Override
    protected CompileResult doInBackground() throws Exception
    {
      CompileResult result = new CompileResult();

      File agentsFile = Tools.getAgentFileForOption(optionFile);
      if(agentsFile == null)
      {
        JOptionPane.showMessageDialog(null, "Could not find agents.xabsl",
          "ERROR", JOptionPane.ERROR_MESSAGE);
        result = null;
      }
      else
      {

        try
        {
          // create the compiler process
          Process compilerProcess = null;

          String compilerCommand = configuration.getProperty(OptionsDialog.XABSL_COMPILER_COMMAND);     
          
          // use the custom compiler command if specified
          if(compilerCommand != null && !compilerCommand.strip().isEmpty())
          {
            String[] cmd = new String[]
            {
              compilerCommand,
              agentsFile.getAbsolutePath(),
              "-i", // intermediate code
              outputFile.getAbsolutePath()
            };
            
            result.addMessage(String.format("Use custom command:\n%s\n",String.join(" ", cmd)));
            compilerProcess = Runtime.getRuntime().exec(cmd);
          }
          else
          {
            final String compilerDirectoryPath = getCompilerDirectory();
            
            Boolean useRuby = Boolean.parseBoolean(configuration.getProperty(OptionsDialog.USE_INSTALLED_RUBY));

            if(useRuby)
            {
              // try to use the installed ruby
              try {
                String[] cmd = new String[]
                {
                  "ruby2",
                  compilerDirectoryPath + "/xabsl.rb",
                  agentsFile.getAbsolutePath(),
                  "-i", // intermediate code
                  outputFile.getAbsolutePath()
                };
                
                result.addMessage(String.format("Use installed ruby:\n%s\n",String.join(" ", cmd)));
                compilerProcess = Runtime.getRuntime().exec(cmd);
              } 
              catch(IOException ex) 
              {
                  result.addMessage("INFO: using installed ruby failed:");
                  result.addMessage("EXCEPTION: " + ex.getMessage() + "\n");
                  
                  System.out.println("INFO: using installed ruby failed:");
                  System.out.println(ex.getMessage());
              }
            }//end if

            // Use the jruby as default
            if(compilerProcess == null)
            {
                String[] cmd = new String[]
                {
                  "java",
                  "-jar",
                  compilerDirectoryPath + "/jruby-complete-1.4.0.jar",
                  compilerDirectoryPath + "/xabsl.rb",
                  agentsFile.getAbsolutePath(),
                  "-i", // intermediate code
                  outputFile.getAbsolutePath()
                };
                
              result.addMessage(String.format("Use jruby:\n%s\n\n",String.join(" ", cmd)));
              compilerProcess = Runtime.getRuntime().exec(cmd);
            }
          }


          DebugProcessOutpuObserver debugProcessOutpuObserver = new DebugProcessOutpuObserver(compilerProcess);
          // read the compiler output during the compilation
          new Thread(debugProcessOutpuObserver).start();
          
          // wait until compiler finishes
          compilerProcess.waitFor();
          debugProcessOutpuObserver.stop();

          BufferedReader rIn = new BufferedReader(new InputStreamReader(compilerProcess.getInputStream()));
          BufferedReader rErr = new BufferedReader(new InputStreamReader(compilerProcess.getErrorStream()));
          
          // parse the compiler output for errors
          if(compilerProcess.exitValue() != 0)
          {
            result.errors = true;
          }

          String errStr = debugProcessOutpuObserver.getStderr();
          String outStr = debugProcessOutpuObserver.getStdout();
          
          String[] errorMsgLines = errStr.split("\n");
          int lineOffset = 0;
          for(String line: errorMsgLines)
          {
            if(line.startsWith("ERROR "))
            {
              // parse the error
              String[] splitted = line.split("((\\s)+)|((\\s)*):((\\s)*)");
              if(splitted.length == 4)
              {
                String fileName = splitted[2];
                int lineNumber = 0;
                try {
                  lineNumber = Integer.parseInt(splitted[3],10);
                } catch(NumberFormatException ex)
                {
                  // its not a number ?
                  System.err.println("Couldnt parse the line number of the error message: '" + splitted[3] + "'");
                }
                // TODO: do something with it
                //System.out.println(fileName + " - " + lineNumeber);
                result.addNotice(new CompileResult.CompilerNotice(lineOffset,
                        CompileResult.CompilerNotice.Level.ERROR, fileName, fileName, lineNumber-1));
              }
              result.errors = true;
            }
            else if(line.startsWith("WARNING "))
            {
              result.warnings = true;
            }

            // check for syntax errors
            String[] splitted = line.split(":");
            if(splitted.length >= 2 && splitted[1].trim().startsWith("error"))
            {
              result.errors = true;
            }
            lineOffset++;
          }//end for

          System.err.println(outStr);
          System.err.println(errStr);

          result.addMessage(outStr);
          result.addMessage(errStr);
          
                  
          pbCompiling.setIndeterminate(false);
          pbCompiling.setValue(pbCompiling.getMaximum());

          if(result.errors)
          {
            JOptionPane.showMessageDialog(null,
              "Compilation failed!", "ERROR", JOptionPane.ERROR_MESSAGE);
          }
          else if(result.warnings)
          {
            JOptionPane.showMessageDialog(null,
              "There were warnings during compiliation.", "WARNING", JOptionPane.WARNING_MESSAGE);
          }
          else
          {
            JOptionPane.showMessageDialog(null, "Intermediate code successfully " +
              "compiled and saved.");
          }
          
        }
        catch(Exception ex)
        {
          finalThis.setVisible(false);
          Tools.handleException(ex);
        }
      }

      finalThis.setVisible(false);

      if(receiver != null)
      {
        receiver.compilationFinished(result);
      }
      return result;
    }//end doInBackground
  }//end class CompiliationWorker

  
  // TODO: can the jruby jar and xabsl.rb be resources?
  //       why do we have to search for them?
  private String getCompilerDirectory() throws Exception
  {
    // search the installation directory of the xabsl-editor
    File install = new File(System.getProperty("user.dir"));

    CodeSource source = CompilerDialog.class.getProtectionDomain().getCodeSource();
    if(source != null)
    {
      URL url = source.getLocation();
      install = new File(url.toURI()).getParentFile();
    }

    while(install != null && install.isDirectory())
    {
      // filter: select directories named "xabsl-compiler" (should be only one :)
      File[] xabsl_compiler_dirs = install.listFiles((File pathname) -> 
              pathname.isDirectory() && pathname.getName().equals("xabsl-compiler")
      );
      
      // nothing found => move a level up and look in the parent directory
      if(xabsl_compiler_dirs.length == 0) {
          install = install.getParentFile();
      } else {
          return xabsl_compiler_dirs[0].getAbsolutePath();
      }
    }

    // no compiler was found
    throw new Exception("Could not find \"xabsl-compiler\"-directory! Aborting. " +
      "Please specify your custom path in the options. " +
      "Assumed installation path was \"" + (install == null ? "null" :  install.getAbsolutePath()) + "\"");

    // TODO: maybe better to return null and throw an exceptions at a top level?
    // return null
  }//end getCompilerDirectory



  private class DebugProcessOutpuObserver implements Runnable
  {
    private Process compilerProcess;
    private boolean running;

    private StringBuilder stdout = new StringBuilder();
    private StringBuilder stderr = new StringBuilder();
    
    public DebugProcessOutpuObserver(Process compilerProcess)
    {
      this.compilerProcess = compilerProcess;
      this.running = true;
    }
    
    @Override
    public void run()
    {
      InputStream inputStream = compilerProcess.getInputStream();
      InputStream errorStream = compilerProcess.getErrorStream();

      try
      {
        while (compilerProcess != null && this.running)
        {
          //readStream(inputStream, System.out);
          //readStream(errorStream, System.err);
          readStream(inputStream, stdout);
          readStream(errorStream, stderr);
        }//end while
      }
      catch(Exception ex)
      {
        Tools.handleException(ex);
      }
    }//end run

    // TODO: make it better
    // redirect streams
    private void readStream(InputStream is, OutputStream os) throws IOException
    {
      while(is.available() > 0)
      {
        os.write(is.read());
      }//end while
    }//end readStream

    private void readStream(InputStream is, StringBuilder os) throws IOException
    {
      while(is.available() > 0)
      {
        os.append((char)is.read());
      }//end while
    }//end readStream

    public void stop()
    {
      this.running = false;
    }//end stop

    public String getStderr() {
      return stderr.toString();
    }

    public String getStdout() {
      return stdout.toString();
    }

  }//end class DebugProcessOutpuObserver
  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pbCompiling = new javax.swing.JProgressBar();
        lblCompiling = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setLocationByPlatform(true);

        lblCompiling.setText("Compiling...");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pbCompiling, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                    .addComponent(lblCompiling))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblCompiling)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pbCompiling, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblCompiling;
    private javax.swing.JProgressBar pbCompiling;
    // End of variables declaration//GEN-END:variables
}
