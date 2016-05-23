package net.nashlegend.anypref;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import net.nashlegend.anypref.annotations.Pref;
import net.nashlegend.anypref.annotations.PrefIgnore;
import net.nashlegend.anypref.annotations.PrefModel;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by NashLegend on 16/5/20.
 * 应该满足动态定义PrefName的需求，比如为不同的用户添加不同的名字
 */
@SuppressWarnings("unchecked")
public class AnyPref {

    private static ConcurrentHashMap<String, Field[]> fieldsMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Boolean> strSetMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, String> classKeyMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, String> fieldKeyMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Boolean> ignoreMap = new ConcurrentHashMap<>();

    private static Field[] getFields(Class clazz) {
        String key = getKeyForClazz(clazz);
        Field[] fields = fieldsMap.get(key);
        if (fields == null) {
            fields = clazz.getFields();
            fieldsMap.put(key, fields);
        }
        return fields;
    }

    public static <T> T get(Class<T> clazz, Context context) throws IllegalAccessException, InstantiationException {
        T obj = clazz.newInstance();
        Field[] fields = getFields(clazz);
        for (Field field : fields) {
            int modifier = field.getModifiers();
            if (Modifier.isFinal(modifier) || Modifier.isStatic(modifier) || isFieldIgnored(field)) {
                continue;
            }
            get(context.getSharedPreferences(getKeyForClazz(clazz), Context.MODE_PRIVATE), field, obj);
        }
        return obj;
    }

    public static void apply(Object object, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(getKeyForClazz(object.getClass()),
                Context.MODE_PRIVATE).edit();
        for (Field field : getFields(object.getClass())) {
            int modifier = field.getModifiers();
            if (Modifier.isFinal(modifier) || Modifier.isStatic(modifier) || isFieldIgnored(field)) {
                continue;
            }
            put(editor, field, object);
        }
        editor.apply();
    }

    private static void put(SharedPreferences.Editor editor, Field field, Object object) {
        String key = getKeyForField(field);
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
                    String mapKey = getKeyForClazz(object.getClass()) + "$$" + key;
                    Boolean result = strSetMap.get(mapKey);
                    if (result == null) {
                        boolean isSet = isFieldStringSet(field);
                        if (isSet) {
                            editor.putStringSet(key, (Set<String>) field.get(object));
                        }
                        strSetMap.put(mapKey, isSet);
                    } else if (result) {
                        editor.putStringSet(key, (Set<String>) field.get(object));
                    }
                    break;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static void get(SharedPreferences preferences, Field field, Object object) {
        String key = getKeyForField(field);
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
                    String mapKey = getKeyForClazz(object.getClass()) + "$$" + key;
                    Boolean result = strSetMap.get(mapKey);
                    if (result == null) {
                        boolean isSet = isFieldStringSet(field);
                        if (isSet) {
                            field.set(object, preferences.getStringSet(key, null));
                        }
                        strSetMap.put(mapKey, isSet);
                    } else if (result) {
                        field.set(object, preferences.getStringSet(key, null));
                    }
                    break;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static boolean isFieldStringSet(Field field) {
        if (field.getType().isAssignableFrom(Set.class)) {
            Type tp = field.getGenericType();
            if (tp != null && tp instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) tp;
                Type[] types = pt.getActualTypeArguments();
                if (types.length == 1) {
                    Class genericClazz = (Class) pt.getActualTypeArguments()[0];
                    if (genericClazz.getCanonicalName().equals(String.class.getCanonicalName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static String getKeyForClazz(Class clazz) {
        String key = classKeyMap.get(clazz.getCanonicalName());
        if (key == null) {
            if (clazz.isAnnotationPresent(PrefModel.class)) {
                PrefModel model = (PrefModel) clazz.getAnnotation(PrefModel.class);
                String value = model.value();
                if (!TextUtils.isEmpty(value)) {
                    classKeyMap.put(clazz.getCanonicalName(), value);
                    return value;
                }
            }
            classKeyMap.put(clazz.getCanonicalName(), clazz.getCanonicalName());
            return clazz.getCanonicalName();
        } else {
            return key;
        }
    }


    private static String getKeyForField(Field field) {
        String keyMap = field.getDeclaringClass().getCanonicalName() + "$$" + field.getName();
        String key = fieldKeyMap.get(keyMap);
        if (key == null) {
            if (field.isAnnotationPresent(Pref.class)) {
                Pref pref = field.getAnnotation(Pref.class);
                String value = pref.value();
                if (!TextUtils.isEmpty(value)) {
                    fieldKeyMap.put(keyMap, value);
                    return value;
                }
            }
            fieldKeyMap.put(keyMap, field.getType().getCanonicalName());
            return field.getType().getCanonicalName();
        } else {
            return key;
        }
    }


    private static boolean isFieldIgnored(Field field) {
        String keyMap = field.getDeclaringClass().getCanonicalName() + "$$" + field.getName();
        Boolean result = ignoreMap.get(keyMap);
        if (result == null) {
            result = field.isAnnotationPresent(PrefIgnore.class);
            ignoreMap.put(keyMap, result);
        }
        return result;

    }
}