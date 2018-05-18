package de.naoth.xabsleditor.events;

import de.naoth.xabsleditor.compilerconnection.CompileResult;
import java.util.EventObject;

/**
 *
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class CompilationFinishedEvent extends EventObject
{
    public final CompileResult result;
    public CompilationFinishedEvent(Object source, CompileResult result) {
        super(source);
        this.result = result;
    }
}
