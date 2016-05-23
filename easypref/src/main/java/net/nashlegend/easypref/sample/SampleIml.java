package net.nashlegend.easypref.sample;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import net.nashlegend.annotations.PrefModel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by NashLegend on 16/5/22.
 * 生成的类应该是这个样子
 */
public class SampleIml {

    Context context;
    PrefModel prefModel;

    public SampleIml(PrefModel pref, Context ctx) {
        prefModel = pref;
        context = ctx;
    }

    //写入与读取应该都是写缓存的数据，如果外部读取了缓存数据，进行修改，那么就会有问题
    public String defaultPrefName = Sample.class.getCanonicalName();
    //应该满足动态定义PrefName的需求，比如为不同的用户添加不同的名字
    public Sample defaultInstance = new Sample();
    public final HashMap<String, Sample> subSamples = new HashMap<>();

    /**
     * 返回一个新实例
     *
     * @param user
     * @return
     */
    @NonNull
    synchronized public Sample readInstance(String user) {
        return copyInstance(getInstance(user));
    }

    /**
     * 返回一个新实例
     *
     * @return
     */
    @NonNull
    synchronized public Sample readInstance() {
        return copyInstance(getInstance());
    }

    /**
     * 保存一个新实例
     *
     * @param instanceToSave
     * @param user
     */
    synchronized public void save(Sample instanceToSave, String user) {
        String prefName = getUserPrefName(user);
        Sample instance = copyInstance(instanceToSave);
        context
                .getSharedPreferences(prefName, Context.MODE_PRIVATE)
                .edit()
                .putInt("intField", instance.intField)
                .putFloat("floatField", instance.floatField)
                .putLong("longField", instance.longField)
                .putString("stringField", instance.stringField)
                .putBoolean("boolField", instance.boolField)
                .putStringSet("setValue", instance.setValue)
                .apply();
    }

    /**
     * 保存一个新实例
     *
     * @param instanceToSave
     */
    synchronized public void save(Sample instanceToSave) {
        save(instanceToSave, null);
    }

    synchronized public void clear() {
        defaultInstance = null;
        context.getSharedPreferences(defaultPrefName, Context.MODE_PRIVATE).edit().clear().apply();
    }


    synchronized public void clear(String user) {
        subSamples.remove(user);
        context.getSharedPreferences(getUserPrefName(user), Context.MODE_PRIVATE).edit().clear().apply();
    }

    private Sample readInstanceFromPref(String user) {
        SharedPreferences prefs = context.getSharedPreferences(getUserPrefName(user), Context.MODE_PRIVATE);
        Sample instance = new Sample();
        instance.intField = prefs.getInt("intField", 0);
        instance.floatField = prefs.getFloat("floatField", 0);
        instance.longField = prefs.getLong("longField", 0);
        instance.boolField = prefs.getBoolean("boolField", false);
        instance.stringField = prefs.getString("stringField", null);
        instance.setValue = prefs.getStringSet("setValue", null);
        return instance;
    }

    private boolean isStringEmpty(CharSequence str) {
        return (str == null || str.length() == 0);
    }

    private String getUserPrefName(String user) {
        if (isStringEmpty(user)) {
            return defaultPrefName;
        }
        return defaultPrefName + ".0" + user;
    }

    private Sample copyInstance(Sample sourceInstance) {
        Sample instance = new Sample();
        instance.intField = sourceInstance.intField;
        instance.floatField = sourceInstance.floatField;
        instance.longField = sourceInstance.longField;
        instance.stringField = sourceInstance.stringField;
        instance.boolField = sourceInstance.boolField;
        instance.setValue = copySet(sourceInstance.setValue);
        return instance;
    }

    private Set<String> copySet(Set<String> stringSet) {
        HashSet<String> stringHashSet = new HashSet<>();
        if (stringSet != null) {
            stringHashSet.addAll(stringSet);
        }
        return stringHashSet;
    }

    @NonNull
    synchronized private Sample getInstance(String user) {
        if (isStringEmpty(user)) {
            return getInstance();
        }
        Sample instance = subSamples.get(user);
        if (instance == null) {
            instance = readInstanceFromPref(user);
            subSamples.put(user, instance);
        }
        return instance;
    }

    @NonNull
    synchronized private Sample getInstance() {
        if (defaultInstance == null) {
            defaultInstance = readInstanceFromPref(null);
        }
        return defaultInstance;
    }
}
