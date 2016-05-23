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
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by NashLegend on 16/5/20.
 */
@SuppressWarnings("unchecked")
public class AnyPref {

    private static Context mContext;

    public static void init(Context context) {
        mContext = context;
    }

    /**
     * 返回一个SharedPrefs对象，可进行指定名字的SharedPreferences的读写
     */
    public static SharedPrefs getPrefs(String key) {
        return new SharedPrefs(key, mContext);
    }

    /**
     * 返回一个SharedPrefs对象，可进行指定类的SharedPreferences的读写
     */
    public static SharedPrefs getPrefs(Class clazz) {
        return new SharedPrefs(getKeyForClazz(clazz), mContext);
    }

    /**
     * 从SharedPreferences中清除一个实例
     */
    public static void clear(Class clazz) {
        new SharedPrefs(getKeyForClazz(clazz), mContext).clear();
    }

    /**
     * 从SharedPreferences中读取一个实例
     */
    public static <T> T read(Class<T> clazz) {
        T obj;
        try {
            obj = clazz.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage());
        }
        for (Field field : getFields(clazz)) {
            read(mContext.getSharedPreferences(getKeyForClazz(clazz), Context.MODE_PRIVATE), field, obj);
        }
        return obj;
    }

    /**
     * 将一个对象实例保存到SharedPreferences中
     */
    public static void save(Object object) {
        SharedPreferences.Editor editor = mContext.getSharedPreferences(getKeyForClazz(object.getClass()),
                Context.MODE_PRIVATE).edit();
        for (Field field : getFields(object.getClass())) {
            put(editor, field, object);
        }
        editor.apply();
    }

    /**
     * 将一个变量保存到SharedPreferences中
     */
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

    /**
     * 从SharedPreferences读取一个变量并赋值
     */
    private static void read(SharedPreferences preferences, Field field, Object object) {
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

    /**
     * 判断变量是否是Set<String>
     */
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

    /**
     * 返回保存此类的SharedPreferences的name，如果有注解则是注解中的值，否则就是类全名
     */
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


    /**
     * 返回保存此变量到SharedPreferences使用的key，如果有注解则是注解中的值，否则就是变量名
     */
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

    /**
     * 判断一个变量是否被忽略而不被保存到SharedPreferences中
     */
    private static boolean isFieldIgnored(Field field) {
        return field.isAnnotationPresent(PrefIgnore.class);
    }

    /**
     * 获取类的所有变量
     */
    private static ArrayList<Field> getFields(Class clazz) {
        String key = getKeyForClazz(clazz);
        ArrayList<Field> fields = fieldsMap.get(key);
        if (fields == null) {
            fields = new ArrayList<>();
            Field[] fieldArray = clazz.getFields();
            for (Field field : fieldArray) {
                int modifier = field.getModifiers();
                if (Modifier.isFinal(modifier) || Modifier.isStatic(modifier) || isFieldIgnored(field)) {
                    continue;
                }
                fields.add(field);
            }
            fieldsMap.put(key, fields);
        }
        return fields;
    }

    private static ConcurrentHashMap<String, ArrayList<Field>> fieldsMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Boolean> strSetMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, String> classKeyMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, String> fieldKeyMap = new ConcurrentHashMap<>();
}