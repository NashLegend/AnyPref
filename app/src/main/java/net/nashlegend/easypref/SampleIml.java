package net.nashlegend.easypref;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by NashLegend on 16/5/22.
 * 生成的类应该是这个样子
 */
public class SampleIml {
    Context context;
    //写入与读取应该都是写缓存的数据，如果外部读取了缓存数据，进行修改，那么就会有问题
    public String defaultPrefName = Sample.class.getCanonicalName();
    //应该满足动态定义PrefName的需求，比如为不同的用户添加不同的名字
    public Sample defaultInstance = new Sample();
    public final HashMap<String, Sample> subSamples = new HashMap<>();

    @NonNull
    synchronized public Sample readInstance(String user) {
        return copyInstance(getInstance(user));
    }

    @NonNull
    synchronized public Sample readInstance() {
        return copyInstance(getInstance());
    }

    synchronized public void save(Sample sample, String userName) {
        Sample instance = getInstance(userName);
        String prefName = getUserPrefName(userName);
        instance.intField = sample.intField;
        instance.floatField = sample.floatField;
        instance.longField = sample.longField;
        instance.stringField = sample.stringField;
        instance.boolField = sample.boolField;
        instance.setValue = sample.setValue;
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

    synchronized public void save(Sample sample) {
        save(sample, null);
    }

    synchronized public void clear() {
        defaultInstance = null;
        context.getSharedPreferences(defaultPrefName, Context.MODE_PRIVATE).edit().clear().apply();
    }

    private Sample readInstanceFromPref(String user) {
        SharedPreferences prefs = context.getSharedPreferences(getUserPrefName(user), Context.MODE_PRIVATE);
        Sample sample = new Sample();
        sample.intField = prefs.getInt("", 0);
        sample.floatField = prefs.getFloat("", 0);
        sample.longField = prefs.getLong("", 0);
        sample.stringField = prefs.getString("", null);
        sample.boolField = prefs.getBoolean("", false);
        sample.setValue = prefs.getStringSet("", null);
        return sample;
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

    private Sample copyInstance(Sample sourceSample) {
        Sample sample = new Sample();
        sample.intField = sourceSample.intField;
        sample.floatField = sourceSample.floatField;
        sample.longField = sourceSample.longField;
        sample.stringField = sourceSample.stringField;
        sample.boolField = sourceSample.boolField;
        sample.setValue = copySet(sourceSample.setValue);//此处是浅复制啊,TODO
        return sample;
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
        Sample sample = subSamples.get(user);
        if (sample == null) {
            sample = readInstanceFromPref(user);
            subSamples.put(user, sample);
        }
        return sample;
    }

    @NonNull
    synchronized private Sample getInstance() {
        if (defaultInstance == null) {
            defaultInstance = readInstanceFromPref(null);
        }
        return defaultInstance;
    }
}
