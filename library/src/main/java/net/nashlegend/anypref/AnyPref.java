package net.nashlegend.anypref;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import net.nashlegend.anypref.annotations.PrefField;
import net.nashlegend.anypref.annotations.PrefIgnore;
import net.nashlegend.anypref.annotations.PrefModel;
import net.nashlegend.anypref.annotations.PrefSub;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by NashLegend on 16/5/20.
 */
@SuppressWarnings("unchecked")
public class AnyPref {
    private static final String EXISTS_KEY = "key.i.am.here";

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
        clear(clazz, getKeyForClazz(clazz));
    }

    /**
     * 从SharedPreferences中清除一个实例
     */
    private static void clear(Class clazz, String prefKey) {
        for (Field field : getFields(clazz)) {
            if (isSubPref(field)) {
                clear(field.getType(), prefKey + "$$$" + getKeyForField(field));
            }
        }
        getPrefs(prefKey).clear();
    }

    /**
     * 从SharedPreferences中读取一个实例
     */
    public static <T> T read(Class<T> clazz) {
        return read(clazz, getKeyForClazz(clazz));
    }

    /**
     * 从SharedPreferences中读取一个实例
     */
    private static <T> T read(Class<T> clazz, String customKey) {
        T obj;
        try {
            obj = clazz.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage());
        }
        SharedPreferences preferences = mContext.getSharedPreferences(customKey, Context.MODE_PRIVATE);
        for (Field field : getFields(clazz)) {
            read(preferences, field, obj, customKey);
        }
        return obj;
    }

    /**
     * 将一个对象实例保存到SharedPreferences中
     */
    public static void save(Object object) {
        if (object == null) {
            return;
        }
        save(object, getKeyForClazz(object.getClass()));
    }

    /**
     * 将一个对象实例保存到SharedPreferences中
     */
    private static void save(Object object, String customKey) {
        if (object == null || customKey == null) {
            return;
        }
        SharedPreferences.Editor editor = mContext.getSharedPreferences(customKey, Context.MODE_PRIVATE).edit();
        for (Field field : getFields(object.getClass())) {
            put(editor, field, object, customKey);
        }
        editor.apply();
    }

    /**
     * 将一个变量保存到SharedPreferences中
     */
    private static void put(SharedPreferences.Editor editor, Field field, Object object, String prefKey) {
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
                    if (isSubPref(field)) {
                        save(field.get(object), prefKey + "$$$" + key);
                    } else {
                        String mapKey = object.getClass().getCanonicalName() + "$$" + key;
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
    private static void read(SharedPreferences preferences, Field field, Object object, String prefKey) {
        String cacheKey = getCacheKeyForField(field);
        String key = getKeyForField(field);
        String type = field.getType().getCanonicalName();
        try {
            switch (type) {
                case "int":
                    field.setInt(object, preferences.getInt(key, (int) getDefaultNumber(cacheKey)));
                    break;
                case "float":
                    field.setFloat(object, preferences.getFloat(key, (float) getDefaultNumber(cacheKey)));
                    break;
                case "long":
                    field.setLong(object, preferences.getLong(key, (long) getDefaultNumber(cacheKey)));
                    break;
                case "boolean":
                    field.setBoolean(object, preferences.getBoolean(key, getDefaultBoolean(cacheKey)));
                    break;
                case "java.lang.String":
                    field.set(object, preferences.getString(key, getDefaultString(cacheKey)));
                    break;
                default:
                    if (isSubPref(field)) {
                        field.set(object, read(field.getType(), prefKey + "$$$" + key));
                    } else {
                        String mapKey = object.getClass().getCanonicalName() + "$$" + key;
                        Boolean result = strSetMap.get(mapKey);
                        if (result == null) {
                            boolean isSet = isFieldStringSet(field);
                            if (isSet) {
                                field.set(object, preferences.getStringSet(key, getDefaultStringSet(cacheKey)));
                            }
                            strSetMap.put(mapKey, isSet);
                        } else if (result) {
                            field.set(object, preferences.getStringSet(key, getDefaultStringSet(cacheKey)));
                        }
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
     * 判断变量是否是一个要读取其他Preference的对象
     */
    private static boolean isSubPref(Field field) {
        return field.isAnnotationPresent(PrefSub.class);
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
        String cacheKey = getCacheKeyForField(field);
        String key = fieldKeyMap.get(cacheKey);
        if (key == null) {
            if (field.isAnnotationPresent(PrefField.class)) {
                PrefField pref = field.getAnnotation(PrefField.class);
                String value = pref.value();
                if (!TextUtils.isEmpty(value)) {
                    fieldKeyMap.put(cacheKey, value);
                    return value;
                }
            }
            fieldKeyMap.put(cacheKey, field.getName());
            return field.getName();
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
                getDefaultValue(field);
                fields.add(field);
            }
            fieldsMap.put(key, fields);
        }
        return fields;
    }

    private static void getDefaultValue(Field field) {
        if (field.isAnnotationPresent(PrefField.class)) {
            PrefField pref = field.getAnnotation(PrefField.class);
            String cacheKey = getCacheKeyForField(field);
            String type = field.getType().getCanonicalName();
            switch (type) {
                case "int":
                case "float":
                case "long":
                    defaultNumberMap.put(cacheKey, pref.number());
                    break;
                case "boolean":
                    defaultBooleanMap.put(cacheKey, pref.bool());
                    break;
                case "java.lang.String":
                    String[] ds = pref.string();
                    if (ds != null && ds.length > 0) {
                        defaultStringMap.put(cacheKey, pref.string()[0]);
                    }
                    break;
                default:
                    if (isFieldStringSet(field)) {
                        String[] sets = pref.string();
                        if (sets != null) {
                            HashSet<String> hashSet = new HashSet<>();
                            hashSet.addAll(Arrays.asList(pref.string()));
                            defaultStringSetMap.put(cacheKey, hashSet);
                        }
                    }
                    break;
            }
        }
    }

    private static String getCacheKeyForField(Field field) {
        return field.getDeclaringClass().getCanonicalName() + "$$" + field.getName();
    }

    ///////下面这一片可以再封装一下
    /**
     * 字段缓存
     */
    private static ConcurrentHashMap<String, ArrayList<Field>> fieldsMap = new ConcurrentHashMap<>();
    /**
     * 判断字段是不是Set<String>的缓存
     */
    private static ConcurrentHashMap<String, Boolean> strSetMap = new ConcurrentHashMap<>();
    /**
     * 保存实例的SharedPreferences的name缓存
     */
    private static ConcurrentHashMap<String, String> classKeyMap = new ConcurrentHashMap<>();
    /**
     * 保存字段时使用的Key的缓存
     */
    private static ConcurrentHashMap<String, String> fieldKeyMap = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<String, Double> defaultNumberMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Boolean> defaultBooleanMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, String> defaultStringMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Set<String>> defaultStringSetMap = new ConcurrentHashMap<>();

    private static double getDefaultNumber(String key) {
        Double num = defaultNumberMap.get(key);
        if (num == null) {
            return 0;
        } else {
            return num;
        }
    }

    private static boolean getDefaultBoolean(String key) {
        Boolean bool = defaultBooleanMap.get(key);
        if (bool == null) {
            return false;
        } else {
            return bool;
        }
    }

    private static String getDefaultString(String key) {
        return defaultStringMap.get(key);
    }

    private static Set<String> getDefaultStringSet(String key) {
        return defaultStringSetMap.get(key);
    }
}