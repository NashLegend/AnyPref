package net.nashlegend.anypref;

import android.text.TextUtils;

import net.nashlegend.anypref.annotations.PrefArrayList;
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
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by NashLegend on 16/6/7.
 */
public class PrefUtil {
    private static ConcurrentHashMap<String, Class> arraylistType = new ConcurrentHashMap<>();
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

    public static boolean isFieldStringSet(Field field) {
        String mapKey = getCacheKeyForField(field);
        Boolean result = strSetMap.get(mapKey);
        if (result == null) {
            boolean isSet = checkIsFieldStringSet(field);
            strSetMap.put(mapKey, isSet);
            return isSet;
        } else {
            return result;
        }
    }

    /**
     * 判断变量是否是Set<String>
     */
    public static boolean checkIsFieldStringSet(Field field) {
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
    public static boolean isSubPref(Field field) {
        return field.isAnnotationPresent(PrefSub.class);
    }

    /**
     * 判断变量是否是一个要读取其他Preference的对象
     */
    public static boolean isArrayListPref(Field field) {
        return field.isAnnotationPresent(PrefArrayList.class);
    }

    /**
     * 返回保存此类的SharedPreferences的name，如果有注解则是注解中的值，否则就是类全名
     */
    public static String getKeyForClazz(Class clazz) {
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
    public static String getKeyForField(Field field) {
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
    public static boolean isFieldIgnored(Field field) {
        return field.isAnnotationPresent(PrefIgnore.class);
    }

    /**
     * 获取类的所有变量
     */
    public static ArrayList<Field> getFields(Class clazz) {
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

    /**
     * 初始化时读取默认值
     *
     * @param field
     */
    public static void getDefaultValue(Field field) {
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
                            LinkedHashSet<String> hashSet = new LinkedHashSet<>();
                            hashSet.addAll(Arrays.asList(pref.string()));
                            defaultStringSetMap.put(cacheKey, hashSet);
                        }
                    }
                    break;
            }
        }
    }

    public static String getCacheKeyForField(Field field) {
        return field.getDeclaringClass().getCanonicalName() + "$$" + field.getName();
    }

    public static double getDefaultNumber(String key) {
        Double num = defaultNumberMap.get(key);
        if (num == null) {
            return 0;
        } else {
            return num;
        }
    }

    public static boolean getDefaultBoolean(String key) {
        Boolean bool = defaultBooleanMap.get(key);
        if (bool == null) {
            return false;
        } else {
            return bool;
        }
    }

    public static String getDefaultString(String key) {
        return defaultStringMap.get(key);
    }

    public static Set<String> getDefaultStringSet(String key) {
        return defaultStringSetMap.get(key);
    }

    /**
     * 返回不为空
     */
    public static ArrayList<String> getArrayListKeys(Field field, String prefKey) {
        String lengthKey = getArrayListLengthKey(field, prefKey);
        int length = AnyPref.getPrefs(prefKey).getInt(lengthKey, 0);
        ArrayList<String> keyList = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            keyList.add(getArrayListItemKey(field, prefKey, i));
        }
        return keyList;
    }

    public static Class getArrayListType(Field field) {
        String mapKey = getCacheKeyForField(field);
        Class result = arraylistType.get(mapKey);
        if (result == null) {
            if (field.getType().isAssignableFrom(ArrayList.class)) {
                Type tp = field.getGenericType();
                if (tp != null && tp instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType) tp;
                    Type[] types = pt.getActualTypeArguments();
                    if (types.length == 1) {
                        Class genericClazz = (Class) pt.getActualTypeArguments()[0];
                        arraylistType.put(mapKey, genericClazz);
                        return genericClazz;
                    }
                }
            }
        } else {
            return result;
        }
        return null;
    }

    public static String getArrayListItemKey(Field field, String prefKey, int index) {
        return prefKey + "$$$$" + PrefUtil.getKeyForField(field) + "_arraylist_" + index;
    }

    public static String getArrayListLengthKey(Field field, String prefKey) {
        return prefKey + "$$$$" + PrefUtil.getKeyForField(field) + "_arraylist_length";
    }

}
