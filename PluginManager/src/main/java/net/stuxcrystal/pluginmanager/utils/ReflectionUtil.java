package net.stuxcrystal.pluginmanager.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Utility for reflections.
 */
public final class ReflectionUtil {

    /**
     * Invokes a method.
     *
     * @param cls    The class where to start.
     * @param object The object to start resolving.
     * @param names  The field names. (But the last name is the method name.)
     * @param values The values passed to the command.
     * @param <T>    The return type of the method.
     * @return The result of the object.
     */
    public static <T> T invoke(Class<?> cls, Object object, String[] names, Object... values) throws ReflectiveOperationException {
        Object lastObject;
        Class<?> lastClass;

        if (object != null || names.length > 1) {
            lastObject = getField(object.getClass(), object, Arrays.copyOfRange(names, 0, names.length - 1));
            lastClass = lastObject.getClass();
        } else {
            lastObject = object;
            lastClass = cls;
        }

        Class<?>[] types = new Class[values.length];

        for (int i = 0; i < types.length; i++)
            types[i] = values[i].getClass();

        Method method = lastClass.getDeclaredMethod(names[names.length - 1], types);
        return (T) method.invoke(lastObject, types);
    }

    /**
     * Invokes the function without throwing a {@link ReflectiveOperationException}.
     *
     * @param cls    The class containg the method.
     * @param object The instance of the class (or null if the method is a static function.)
     * @param names  The names of the field to get to the function.
     * @param values The values passed to the object.
     * @param <T>    The return type.
     * @return The result of the function.
     * @throws Throwable if the function threw an exception.
     */
    public static <T> T invokeQuiet(Class<?> cls, Object object, String[] names, Object... values) throws Throwable {
        try {
            return invoke(cls, object, names, values);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        } catch (ReflectiveOperationException ignored) {

        }

        return null;
    }

    /**
     * Returns the reference of the field.
     * @param cls    The class object.
     * @param object The instance of the class.
     * @param fields The path to the field.
     * @return A field object
     * @throws ReflectiveOperationException If a reflective operation fails.
     */
    private static Field getFieldRaw(Class<?> cls, Object object, String... fields) throws ReflectiveOperationException {
        Object current = object;
        Class<?> currentClass = cls;
        Field currentField = null;
        boolean first = true;

        for (String fieldName : fields) {
            if (object == null && !first)
                throw new NullPointerException("ReflectionUtility: " + fieldName);

            currentField = currentClass.getDeclaredField(fieldName);
            currentField.setAccessible(true);

            current = currentField.get(current);
            currentClass = current.getClass();

            first = false;
        }

        return currentField;
    }

    /**
     * Returns the value of a field.
     *
     * @param cls    The class of the field.
     * @param object The instance of the class. Null to access static fields.
     * @param fields The field names.
     * @param <T>    The result type.
     * @return The value of the field.
     * @throws ReflectiveOperationException The field name.
     */
    public static <T> T getField(Class<?> cls, Object object, String... fields) throws ReflectiveOperationException {
        Object current = object;
        Class<?> currentClass = cls;
        boolean first = true;

        for (String fieldName : fields) {
            if (object == null && !first)
                throw new NullPointerException("ReflectionUtility: " + fieldName);

            Field field = currentClass.getDeclaredField(fieldName);
            field.setAccessible(true);

            current = field.get(object);
            currentClass = current.getClass();

            first = false;
        }

        return (T) current;
    }

    /**
     * Returns the value of a field without throwing an exception.
     * @param cls    The class of the field.
     * @param object The instance of the class. Null to access static fields.
     * @param fields The field names.
     * @param <T>    The result type.
     * @return The value of the field.
     */
    public static <T> T getFieldQuiet(Class<?> cls, Object object, String... fields) {
        try {
            return getField(cls, object, fields);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    /**
     * Sets a field.
     * @param cls    The class to start.
     * @param object The object
     * @param value  The new value.
     * @param fields The path to the field.
     * @param <T>    The type of the object.
     * @throws ReflectiveOperationException If a Reflection-Operation fails.
     */
    public static <T> void setField(Class<?> cls, Object object, T value, String... fields)
            throws ReflectiveOperationException {

        Object pre = null;
        Object current = object;
        Class<?> currentClass = cls;
        Field currentField = null;
        boolean first = true;

        for (String fieldName : fields) {
            if (object == null && !first)
                throw new NullPointerException("ReflectionUtility: " + fieldName);

            currentField = currentClass.getDeclaredField(fieldName);
            currentField.setAccessible(true);

            pre = current;
            current = currentField.get(current);
            currentClass = current.getClass();

            first = false;
        }

        currentField.set(pre, value);
    }

    /**
     * Sets a field without throwing an exception.
     * @param cls    The class.
     * @param object The object
     * @param value  The new value.
     * @param fields The path.
     * @param <T>    The type.
     * @return Did the action succeed.
     */
    public static <T> boolean setFieldQuiet(Class<?> cls, Object object, T value, String... fields) {
        try {
            setField(cls, object, value, fields);
        } catch (ReflectiveOperationException e) {
            return false;
        }
        return true;
    }

    /**
     * Constructs a new instance of a class without throwing an exception.
     * @param cls The class object.
     * @param <T> The type of the class.
     * @return The instantiated class.
     */
    public static <T> T newInstance(Class<T> cls) {
        try {
            Constructor constructor = cls.getConstructor();
            constructor.setAccessible(true);
            return (T) constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

}
