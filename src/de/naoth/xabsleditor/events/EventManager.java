package de.naoth.xabsleditor.events;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Philipp Strobel <philippstrobel@posteo.de>
 */
public class EventManager
{
    private static EventManager instance;

    private final Map<Class, List<DataSubscriberInfo>> map = new LinkedHashMap<>();

    private EventManager() {}
    
    public static synchronized EventManager getInstance() {
        if (EventManager.instance == null) {
            EventManager.instance = new EventManager();
        }
        return EventManager.instance;
    }
    
    public void add(Object o) {
        for (Method method : o.getClass().getMethods()) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (method.getAnnotation(EventListener.class) == null || parameterTypes.length != 1) {
                continue;
            }
            Class subscribeTo = parameterTypes[0];
//            System.out.println(subscribeTo);
            List<DataSubscriberInfo> subscriberInfos = map.get(subscribeTo);
            if (subscriberInfos == null) {
                map.put(subscribeTo, subscriberInfos = new ArrayList<>());
            }
            subscriberInfos.add(new DataSubscriberInfo(method, o));
        }
    }

    public void remove(Object o) {
        for (List<DataSubscriberInfo> subscriberInfos : map.values()) {
            for (int i = subscriberInfos.size() - 1; i >= 0; i--) {
                if (subscriberInfos.get(i).object == o) {
                    subscriberInfos.remove(i);
                }
            }
        }
    }

    public int publish(Object o) {
//        System.out.println(o.getClass());
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
                throw new AssertionError(e);
            }
        }
    }
}
