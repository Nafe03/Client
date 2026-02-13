package dev.anarchy.waifuhax.api.events;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EventSystem {

    private static final Set<Object> listeners = new HashSet<>();

    private static final HashMap<Class<? extends Event>, HashMap<String, IEventLambda>> lambdas = new HashMap<>();

    public static String registerLambda(Class<? extends Event> event, IEventLambda lambda) {
        if (lambdas.containsKey(event)) {
            if (!lambdas.get(event).containsValue(lambda)) {
                final String uuid = UUID.randomUUID().toString();
                lambdas.get(event).put(uuid, lambda);
                return uuid;
            }
        }
        else {
            final String uuid = UUID.randomUUID().toString();
            lambdas.put(event, new HashMap<>());
            lambdas.get(event).put(uuid, lambda);
            return uuid;
        }
        return "";
    }

    public static void removeLambda(String _uuid) {
        lambdas.forEach((key, value) -> value.remove(_uuid));
    }

    public static void removeLambda(Class<? extends Event> event, String _uuid) {
        if (lambdas.containsKey(event)) {lambdas.get(event).remove(_uuid);}
    }

    public static void registerListener(Object instance) {
        listeners.add(instance);
    }

    public static void unregisterListener(Object instance) {
        listeners.remove(instance);
    }


    public static void fireEvent(Event event) {
        listeners.forEach((object) -> {

            for (Method declaredMethod : object.getClass().getDeclaredMethods()) {

                if (declaredMethod.isAnnotationPresent(EventListener.class)) {

                    if (declaredMethod.getAnnotation(EventListener.class).event() != null &&
                            declaredMethod.getAnnotation(EventListener.class).event().equals(event.getClass())) {

                        if (!declaredMethod.canAccess(object)) {
                            declaredMethod.setAccessible(true);
                        }

                        try {
                            declaredMethod.invoke(object, event);
                        } catch (IllegalAccessException |
                                 InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });

        if (lambdas.containsKey(event.getClass())) {
            lambdas.get(event.getClass()).forEach((uuid, lambda) -> lambda.onEvent(event));
        }
    }
}
