package de.naoth.xabsleditor.events;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation interface for an event subscriber/listener object method.
 * 
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventListener {
}
