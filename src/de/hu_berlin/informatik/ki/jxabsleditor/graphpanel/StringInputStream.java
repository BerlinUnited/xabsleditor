/*
 * 
 */

package de.hu_berlin.informatik.ki.jxabsleditor.graphpanel;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Heinrich Mellmann
 */
public class StringInputStream extends InputStream
{
    int pos;
    char[] buffer;

    public StringInputStream(String string)
    {
        this.pos = 0;
        this.buffer = string.toCharArray();
    }

    @Override
    public int read() throws IOException
    {
        //System.out.print(buffer.charAt(pos));
        if(pos < buffer.length)
            return buffer[pos++];
        else
            return -1;
    }//end read

}//end class StringInputStream
