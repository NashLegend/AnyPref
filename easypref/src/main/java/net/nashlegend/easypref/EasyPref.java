package net.nashlegend.easypref;

import android.content.Context;
import android.content.SharedPreferences;

import net.nashlegend.annotations.Pref;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by NashLegend on 16/5/20.
 * 应该满足动态定义PrefName的需求，比如为不同的用户添加不同的名字
 */
public class EasyPref {
    private static Context context;
    private static HashMap<String, Object> prefMap = new HashMap<>();

    public static void main(String[] args) {

    }

    public static Object get(Class<?> clazz) {
        Object object = prefMap.get(clazz.getCanonicalName());
        if (object == null) {

        }
        return object;
    }

    public static Object getFromPrefs(Class<?> clazz, Context context) {
        SharedPreferences mSharedPreferences = context.getSharedPreferences(clazz.getCanonicalName(),
                Context.MODE_PRIVATE);
        Object object = null;
        // TODO: 16/5/20
        return object;
    }

    public static void applyPrefs(Object object, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(object.getClass().getCanonicalName(),
                Context.MODE_PRIVATE).edit();
        Class<?> clazz = object.getClass();
        Field[] fields = clazz.getFields();
        for (Field field : fields) {
            boolean isPrefs = field.isAnnotationPresent(Pref.class);
            if (isPrefs) {
                put(editor, field, object);
            }
        }
        editor.apply();
    }

    public static void apply(Object object, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(object.getClass().getCanonicalName(),
                Context.MODE_PRIVATE).edit();
        for (Field field : object.getClass().getFields()) {
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
}