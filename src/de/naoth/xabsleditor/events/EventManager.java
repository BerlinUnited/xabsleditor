package de.naoth.xabsleditor.events;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manager, that handles the distribution of events to registered callbacks/listeners.
 * This is a implementation of the publish/subscribe pattern or signal/slot pattern, similiar
 * to the Qt concept.
 * 
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class EventManager
{
    /** Singleton instance of the manager. */
    private static EventManager instance;
    /** Registered listeners of the particular event. */
    private final Map<Class, List<DataSubscriberInfo>> map = new LinkedHashMap<>();
    
    /**
     * Disabled constructor of the event manager singleton.
     */
    private EventManager() {}
    
    /**
     * Returns the singleton instance of the event manager.
     * If it doesn't already exists, the instance is created.
     * 
     * @return instance of the event manager
     */
    public static synchronized EventManager getInstance() {
        if (EventManager.instance == null) {
            EventManager.instance = new EventManager();
        }
        return EventManager.instance;
    }
    
    /**
     * Adds a subscriber/listener to the registered event map.
     * The appropiate listener method and event type is determined at runtime by
     * inspecting each method for a 'EventListener' annotation and suitable event parameter.
     * 
     * @param o the subscriber/listener object, which should be registered
     */
    public void add(Object o) {
        // iterate through all object methods
        for (Method method : o.getClass().getMethods()) {
            // only the appropiate methods are regsitered ('EventListener' annotation and one event parameter
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (method.getAnnotation(EventListener.class) == null || parameterTypes.length != 1) {
                continue;
            }
            // register appropiate method to the event type
            Class subscribeTo = parameterTypes[0];
            List<DataSubscriberInfo> subscriberInfos = map.get(subscribeTo);
            if (subscriberInfos == null) {
                map.put(subscribeTo, subscriberInfos = new ArrayList<>());
            }
            subscriberInfos.add(new DataSubscriberInfo(method, o));
        }
    }

    /**
     * Removes all registered methods of the subscriber/listener object from the registered event map.
     * 
     * @param o the subscriber/listener object, which should be removed
     */
    public void remove(Object o) {
        for (List<DataSubscriberInfo> subscriberInfos : map.values()) {
            for (int i = subscriberInfos.size() - 1; i >= 0; i--) {
                if (subscriberInfos.get(i).object == o) {
                    subscriberInfos.remove(i);
                }
            }
        }
    }

    /**
     * Publish an event object.
     * All registered subscriber/listener methods are invoked.
     * 
     * @param o the event object
     * @return the number of called subscriber/listener
     */
    public int publish(Object o) {
        List<DataSubscriberInfo> subscriberInfos = map.get(o.getClass());
        if (subscriberInfos == null) {
            return 0;
        }
        int count = 0;
        for (DataSubscriberInfo subscriberInfo : subscriberInfos) {
            subscriberInfo.invoke(o);
            count++;
        }
        return count;
    }

    /**
     * Wrapper for the registered subscriber object and the method for a event type.
     */
    static class DataSubscriberInfo {

        final Method method;
        final Object object;

        DataSubscriberInfo(Method method, Object object) {
            this.method = method;
            this.object = object;
        }

        void invoke(Object o) {
            try {
                method.invoke(object, o);
            } catch (Exception e) {
                // NOTE: an exception in the handler method gets catched here too!
                //       always look at the root cause of the exception
                throw new AssertionError(e);
            }
        }
    }
}
