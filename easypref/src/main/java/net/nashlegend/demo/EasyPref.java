package net.nashlegend.demo;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * Created by NashLegend on 16/5/20.
 * 应该满足动态定义PrefName的需求，比如为不同的用户添加不同的名字
 */
public class EasyPref {

    public static void main(String[] args) {

    }

    public static <T> T get(Class<T> clazz, Context context) throws IllegalAccessException, InstantiationException {
        T obj = clazz.newInstance();
        Field[] fields = clazz.getFields();
        for (Field field : fields) {
            int modifier = field.getModifiers();
            if (Modifier.isFinal(modifier) || Modifier.isStatic(modifier)) {
                continue;
            }
            get(context.getSharedPreferences(clazz.getCanonicalName(), Context.MODE_PRIVATE), field, obj);
        }
        return obj;
    }

    public static void apply(Object object, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(object.getClass().getCanonicalName(),
                Context.MODE_PRIVATE).edit();
        for (Field field : object.getClass().getFields()) {
            int modifier = field.getModifiers();
            if (Modifier.isFinal(modifier) || Modifier.isStatic(modifier)) {
                continue;
            }
            put(editor, field, object);
        }
        editor.apply();
    }

    private static void put(SharedPreferences.Editor editor, Field field, Object object) {
        String key = field.getName();
        String type = field.getType().getCanonicalName();
        try {
            switch (type) {
                case "int":
                    editor.putInt(key, field.getInt(object));
                    break;
                case "float":
                    editor.putFloat(key, field.getFloat(object));
                    break;
                case "long":
                    editor.putLong(key, field.getLong(object));
                    break;
                case "boolean":
                    editor.putBoolean(key, field.getBoolean(object));
                    break;
                case "java.lang.String":
                    editor.putString(key, String.valueOf(field.get(object)));
                    break;
                default:
                    if (field.getType().isAssignableFrom(Set.class)) {
                        Type tp = field.getGenericType();
                        if (tp == null)
                            break;
                        if (tp instanceof ParameterizedType) {
                            ParameterizedType pt = (ParameterizedType) tp;
                            Type[] types = pt.getActualTypeArguments();
                            if (types.length == 1) {
                                Class genericClazz = (Class) pt.getActualTypeArguments()[0];
                                if (genericClazz.getCanonicalName().equals(String.class.getCanonicalName())) {
                                    Set<String> stringSet = (Set<String>) field.get(object);
                                    editor.putStringSet(key, stringSet);
                                }
                            }
                        }
                    }
                    break;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static void get(SharedPreferences preferences, Field field, Object object) {
        String key = field.getName();
        String type = field.getType().getCanonicalName();
        try {
            switch (type) {
                case "int":
                    field.setInt(object, preferences.getInt(key, 0));
                    break;
                case "float":
                    field.setFloat(object, preferences.getFloat(key, 0));
                    break;
                case "long":
                    field.setLong(object, preferences.getLong(key, 0));
                    break;
                case "boolean":
                    field.setBoolean(object, preferences.getBoolean(key, false));
                    break;
                case "java.lang.String":
                    field.set(object, preferences.getString(key, null));
                    break;
                default:
                    if (field.getType().isAssignableFrom(Set.class)) {
                        Type tp = field.getGenericType();
                        if (tp == null)
                            break;
                        if (tp instanceof ParameterizedType) {
                            ParameterizedType pt = (ParameterizedType) tp;
                            Type[] types = pt.getActualTypeArguments();
                            if (types.length == 1) {
                                Class genericClazz = (Class) pt.getActualTypeArguments()[0];
                                if (genericClazz.getCanonicalName().equals(String.class.getCanonicalName())) {
                                    field.set(object, preferences.getStringSet(key, null));
                                }
                            }
                        }
                    }
                    break;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}