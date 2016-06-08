package net.nashlegend.anypref;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Set;

/**
 * Created by NashLegend on 16/5/20.
 * 保存PrefSub的key是父Object的key+$$$+此PrefSub自己的fieldKey，如"Sample$$$son1";三个$
 * <p/>
 * 保存PrefArray的key是父Object的key+$$$$+此PrefSub自己的fieldKey+_array_i，如 "Sample$$$$myArrays_array_3";
 * PrefArray的长度需要保存在一个Key里，key名为父Object的key+$$$$+此PrefSub自己的fieldKey+_array_length，如 "Sample$$$$myArrays_array_length";四个$
 * <p/>
 * 保存PrefArrayList的key是父Object的key+$$$$$+此PrefSub自己的fieldKey+_array_i，如 "Sample$$$$$myArrays_arraylist_3";
 * PrefArrayList的长度需要保存在一个Key里，key名为父Object的key+$$$$$+此PrefSub自己的fieldKey+_array_length，如 "Sample$$$$$myArrays_arraylist_length";五个$
 */
@SuppressWarnings("unchecked")
public class AnyPref {
    private static final String EXISTS_KEY = "key.i.am.here";

    private static Context mContext;

    public static void init(Context context) {
        mContext = context;
    }

    /**
     * 返回默认SharedPrefs对象
     */
    public static SharedPrefs getDefault() {
        return new SharedPrefs(mContext.getPackageName() + "_preferences", mContext);
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
        return new SharedPrefs(PrefUtil.getKeyForClazz(clazz), mContext);
    }

    /**
     * 从SharedPreferences中清除一个实例
     */
    public static void clear(Class clazz) {
        clear(clazz, PrefUtil.getKeyForClazz(clazz));
    }

    /**
     * 从SharedPreferences中清除一个实例
     */
    public static void clear(Class clazz, String prefKey) {
        for (Field field : PrefUtil.getFields(clazz)) {
            if (PrefUtil.isSubPref(field)) {
                clear(field.getType(), prefKey + "$$$" + PrefUtil.getKeyForField(field));
            } else if (PrefUtil.isArrayPref(field)) {
                // TODO: 16/6/8
            } else if (PrefUtil.isArrayListPref(field)) {
                // TODO: 16/6/8
            }
        }
        getPrefs(prefKey).clear();
    }

    /**
     * 从SharedPreferences中读取一个实例
     */
    public static <T> T get(Class<T> clazz) {
        return get(clazz, PrefUtil.getKeyForClazz(clazz));
    }

    /**
     * 从SharedPreferences中读取一个实例
     */
    public static <T> T get(Class<T> clazz, String customKey) {
        T obj;
        try {
            obj = clazz.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage());
        }
        SharedPreferences preferences = mContext.getSharedPreferences(customKey, Context.MODE_PRIVATE);
        for (Field field : PrefUtil.getFields(clazz)) {
            get(preferences, field, obj, customKey);
        }
        return obj;
    }

    /**
     * 将一个对象实例保存到SharedPreferences中
     */
    public static void put(Object object) {
        if (object == null) {
            return;
        }
        put(object, PrefUtil.getKeyForClazz(object.getClass()));
    }

    /**
     * 将一个对象实例保存到SharedPreferences中
     */
    public static void put(Object object, String customKey) {
        if (object == null || customKey == null) {
            return;
        }
        SharedPreferences.Editor editor = mContext.getSharedPreferences(customKey, Context.MODE_PRIVATE).edit();
        for (Field field : PrefUtil.getFields(object.getClass())) {
            put(editor, field, object, customKey);
        }
        editor.apply();
    }

    /**
     * 将一个变量保存到SharedPreferences中
     */
    private static void put(SharedPreferences.Editor editor, Field field, Object object, String prefKey) {
        String key = PrefUtil.getKeyForField(field);
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
                    if (PrefUtil.isSubPref(field)) {
                        put(field.get(object), prefKey + "$$$" + key);
                    } else if (PrefUtil.isArrayPref(field)) {
                        // TODO: 16/6/8
                    } else if (PrefUtil.isArrayListPref(field)) {
                        // TODO: 16/6/8
                    } else if (PrefUtil.isFieldStringSet(field)) {
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
    private static void get(SharedPreferences preferences, Field field, Object object, String prefKey) {
        String cacheKey = PrefUtil.getCacheKeyForField(field);
        String key = PrefUtil.getKeyForField(field);
        String type = field.getType().getCanonicalName();
        try {
            switch (type) {
                case "int":
                    field.setInt(object, preferences.getInt(key, (int) PrefUtil.getDefaultNumber(cacheKey)));
                    break;
                case "float":
                    field.setFloat(object, preferences.getFloat(key, (float) PrefUtil.getDefaultNumber(cacheKey)));
                    break;
                case "long":
                    field.setLong(object, preferences.getLong(key, (long) PrefUtil.getDefaultNumber(cacheKey)));
                    break;
                case "boolean":
                    field.setBoolean(object, preferences.getBoolean(key, PrefUtil.getDefaultBoolean(cacheKey)));
                    break;
                case "java.lang.String":
                    field.set(object, preferences.getString(key, PrefUtil.getDefaultString(cacheKey)));
                    break;
                default:
                    if (PrefUtil.isSubPref(field)) {
                        field.set(object, get(field.getType(), prefKey + "$$$" + key));
                    } else if (PrefUtil.isArrayPref(field)) {
                        // TODO: 16/6/8
                    } else if (PrefUtil.isArrayListPref(field)) {
                        // TODO: 16/6/8
                    } else if (PrefUtil.isFieldStringSet(field)) {
                        field.set(object, preferences.getStringSet(key, PrefUtil.getDefaultStringSet(cacheKey)));
                    }
                    break;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList getArrayList(Field field, String prefKey) {
        ArrayList arrayList = new ArrayList();
        return null;
    }

    public static ArrayList getArray() {
        return null;
    }

}