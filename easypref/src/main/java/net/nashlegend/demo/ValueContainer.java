package net.nashlegend.demo;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by NashLegend on 16/5/23.
 */
public class ValueContainer {
    String name = "";
    private HashMap<String, Integer> intHashMap = new HashMap<>();
    private HashMap<String, Long> longHashMap = new HashMap<>();
    private HashMap<String, Float> floatHashMap = new HashMap<>();
    private HashMap<String, Double> doubleHashMap = new HashMap<>();
    private HashMap<String, Boolean> booleanHashMap = new HashMap<>();
    private HashMap<String, String> stringHashMap = new HashMap<>();
    private HashMap<String, Set<String>> stringSetHashMap = new HashMap<>();

    public ValueContainer(Class clazz) {
        this.name = clazz.getCanonicalName();
    }

    public void put(String key, int value) {
        intHashMap.put(key, value);
    }


    public void put(String key, long value) {
        longHashMap.put(key, value);
    }


    public void put(String key, float value) {
        floatHashMap.put(key, value);
    }


    public void put(String key, double value) {
        doubleHashMap.put(key, value);
    }


    public void put(String key, boolean value) {
        booleanHashMap.put(key, value);
    }

    public void put(String key, String value) {
        stringHashMap.put(key, value);
    }

    public void put(String key, Set<String> value) {
        stringSetHashMap.put(key, value);
    }

    public int getInt(String key) {
        return intHashMap.get(key);
    }

    public long getLong(String key) {
        return longHashMap.get(key);
    }

    public float getFloat(String key) {
        return floatHashMap.get(key);
    }

    public double getDouble(String key) {
        return doubleHashMap.get(key);
    }

    public boolean getBoolean(String key) {
        return booleanHashMap.get(key);
    }

    public String getString(String key) {
        return stringHashMap.get(key);
    }

    public Set<String> getStringSet(String key) {
        return stringSetHashMap.get(key);
    }
}
