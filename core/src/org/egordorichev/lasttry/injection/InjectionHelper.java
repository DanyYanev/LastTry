package org.egordorichev.lasttry.injection;

import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class InjectionHelper {
    private static final Logger logger = LoggerFactory.getLogger(ContextImpl.class);

    public static  <T> T inject(T object){
        return inject(object,CoreRegistry.getContext());
    }

    public static  <T> T inject(T object,Context context){
        for (Field field : ReflectionUtils.getAllFields(object.getClass(), ReflectionUtils.withAnnotation(In.class))) {
            Object value = context.get(field.getType());
            if (value != null) {
                try {
                    field.setAccessible(true);
                    field.set(object, value);
                } catch (IllegalAccessException e) {
                    logger.error("Failed to inject value {} into field {} of {}", value, field, object, e);
                }
            }
        }
        return object;
    }


    public static <T> T createWithConstructorInjection(Class<T> requestType, Context context) {
        try {

            final Constructor<T> constructor = InjectionHelper.findConstructor(requestType);
            if(constructor == null) {
                logger.error("Can't find usable constructor for {}", requestType);
                return null;
            }

            Object[] params = new Object[constructor.getParameterCount()];
            for (int i = 0; i < constructor.getParameterCount(); i++) {
                params[i] = CoreRegistry.get(constructor.getParameterTypes()[i]);
            }
            return  constructor.newInstance(params);

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            logger.error("Can't find usable constructor for {}", requestType,e);
            return null;
        }
    }

    private static  <T> Constructor<T> findConstructor(Class<T> type) {
        final Constructor<?>[] constructors = type.getConstructors();

        if(constructors.length == 0) {
            logger.error("Failed to Find Constructor for {}",type);
            return null;
        }

        if(constructors.length > 1)
        {
            final List<Constructor<?>> constructorsWithInject = Arrays.stream(constructors)
                    .filter(c -> c.isAnnotationPresent(In.class))
                    .collect(Collectors.toList());
            if(constructorsWithInject.size() != 1){
                return null;
            }

            return (Constructor<T>) constructorsWithInject.get(0);
        }

        return (Constructor<T>) constructors[0];
    }


}
