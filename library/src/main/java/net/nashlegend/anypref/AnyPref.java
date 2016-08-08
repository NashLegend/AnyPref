package net.nashlegend.anypref;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Set;

/**
 * Created by NashLegend on 16/5/20.
 * 保存PrefSub的key是父Object的key+$$$+此PrefSub自己的fieldKey，如"Sample$$$son1";三个$
 * <p/>
 * 保存PrefArrayList的key是父Object的key+$$$$+fieldKey+_arraylist_+index，如 "Sample$$$$myArrayList_arraylist_3";
 * PrefArrayList的长度需要保存在一个Key里，key名为父Object的key+$$$$+fieldKey+_arraylist_length，如 "Sample$$$$myArrayList_arraylist_length";四个$
 */
@SuppressWarnings("unchecked")
public class AnyPref {

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
        return new SharedPrefs(PrefUtil.getPrefNameForClass(clazz), mContext);
    }

    /**
     * 从SharedPreferences中清除一个实例
     */
    public static void clear(Class clazz) {
        if (clazz != null) {
            clear(clazz, PrefUtil.getPrefNameForClass(clazz));
        }
    }

    /**
     * 从SharedPreferences中清除一个实例
     */
    public static void clear(Class clazz, String prefName) {
        if (clazz == null || TextUtils.isEmpty(prefName)) {
            return;
        }
        for (Field field : PrefUtil.getFields(clazz)) {
            if (PrefUtil.isSubPref(field)) {
                clear(field.getType(), prefName + "$$$" + PrefUtil.getKeyForField(field));
            } else if (PrefUtil.isArrayListPref(field)) {
                clearArrayList(field, prefName);
            }
        }
        getPrefs(prefName).clear();
    }

    /**
     * 从SharedPreferences中读取一个实例
     */
    public static <T> T get(Class<T> clazz) {
        if (clazz == null) {
            return null;
        }
        return get(clazz, PrefUtil.getPrefNameForClass(clazz));
    }

    /**
     * 从SharedPreferences中读取一个实例
     */
    public static <T> T get(Class<T> clazz, String prefName) {
        return get(clazz, prefName, false);
    }

    /**
     * 从SharedPreferences中读取一个实例
     *
     * @param clazz    要读取的对象的类
     * @param prefName 保存此对象的SharedPreferences name
     * @param nullable 是否可空
     * @return
     */
    public static <T> T get(Class<T> clazz, String prefName, boolean nullable) {
        if (clazz == null || TextUtils.isEmpty(prefName)) {
            return null;
        }
        SharedPreferences preferences = mContext.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        if (nullable && preferences.getAll().size() == 0) {
            return null;
        }
        T obj;
        try {
            obj = clazz.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage());
        }
        for (Field field : PrefUtil.getFields(clazz)) {
            get(preferences, field, obj, prefName);
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
        put(object, PrefUtil.getPrefNameForClass(object.getClass()));
    }

    /**
     * 将一个对象实例保存到SharedPreferences中
     */
    public static void put(Object object, String prefName) {
        if (object == null || TextUtils.isEmpty(prefName)) {
            return;
        }

        clear(object.getClass(), prefName);

        SharedPreferences.Editor editor = mContext.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit();
        for (Field field : PrefUtil.getFields(object.getClass())) {
            put(editor, field, object, prefName);
        }
        editor.apply();
    }

    /**
     * 将一个变量保存到SharedPreferences中
     *
     * @param field    变量的field
     * @param object   变量父对象
     * @param prefName 父对象保存SharedPreferences的name
     */
    private static void put(SharedPreferences.Editor editor, Field field, Object object, String prefName) {
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
                        put(field.get(object), prefName + "$$$" + key);
                    } else if (PrefUtil.isArrayListPref(field)) {
                        Object arr = field.get(object);
                        if (arr != null && arr instanceof ArrayList) {
                            putArrayList(editor, (ArrayList) arr, field, prefName);
                        }
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
     *
     * @param field    变量的field
     * @param object   变量父对象
     * @param prefName 父对象保存SharedPreferences的name
     */
    private static void get(SharedPreferences preferences, Field field, Object object, String prefName) {
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
                        field.set(object, get(field.getType(), prefName + "$$$" + key, PrefUtil.isPrefSubNullable(field)));
                    } else if (PrefUtil.isArrayListPref(field)) {
                        field.set(object, getArrayList(preferences, field, prefName));
                    } else if (PrefUtil.isFieldStringSet(field)) {
                        field.set(object, preferences.getStringSet(key, PrefUtil.getDefaultStringSet(cacheKey)));
                    }
                    break;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static ArrayList getArrayList(SharedPreferences preferences, Field field, String prefName) {
        int length = preferences.getInt(PrefUtil.getArrayListLengthKey(field, prefName), 0);
        int num = PrefUtil.isPrefArrayListNullable(field);
        boolean arrayNullable = (num >> 1) == 1;
        boolean itemNullable = (num & 1) == 1;
        if (length == 0 && arrayNullable) {
            return null;
        }
        ArrayList<String> keyList = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            keyList.add(PrefUtil.getArrayListItemKey(field, prefName, i));
        }
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < keyList.size(); i++) {
            Object obj = get(PrefUtil.getArrayListType(field), keyList.get(i), itemNullable);
            if (obj != null || itemNullable) {
                arrayList.add(obj);
            }
        }
        return arrayList;
    }

    private static void putArrayList(SharedPreferences.Editor editor, ArrayList arrayList, Field field, String prefName) {
        for (int i = 0; i < arrayList.size(); i++) {
            put(arrayList.get(i), PrefUtil.getArrayListItemKey(field, prefName, i));
        }
        editor.putInt(PrefUtil.getArrayListLengthKey(field, prefName), arrayList.size());
    }

    private static void clearArrayList(Field field, String prefName) {
        ArrayList<String> keys = PrefUtil.getArrayListKeys(field, prefName);
        for (int i = 0; i < keys.size(); i++) {
            clear(PrefUtil.getArrayListType(field), keys.get(i));
        }
    }
}