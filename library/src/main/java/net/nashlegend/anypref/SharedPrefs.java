package net.nashlegend.anypref;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import java.util.Map;
import java.util.Set;

/**
 * Created by NashLegend on 16/5/23.
 */
public class SharedPrefs {
    Context context;
    String prefName;

    private SharedPreferences.Editor mEditor;

    private boolean isInTransaction = false;

    public SharedPrefs(String prefName, Context context) {
        this.prefName = prefName;
        this.context = context;
    }

    public int getInt(String key, int def) {
        return getSharedPreferences().getInt(key, def);
    }

    public long getLong(String key, long def) {
        return getSharedPreferences().getLong(key, def);
    }

    public float getFloat(String key, float def) {
        return getSharedPreferences().getFloat(key, def);
    }

    public boolean getBoolean(String key, boolean def) {
        return getSharedPreferences().getBoolean(key, def);
    }

    public String getString(String key, String def) {
        return getSharedPreferences().getString(key, def);
    }

    public Set<String> getStringSet(String key, Set<String> def) {
        return getSharedPreferences().getStringSet(key, def);
    }

    public Map<String, ?> getAll() {
        return getSharedPreferences().getAll();
    }

    public SharedPrefs putInt(String key, int value) {
        wrap(getEditor().putInt(key, value));
        return this;
    }

    public SharedPrefs putLong(String key, long value) {
        wrap(getEditor().putLong(key, value));
        return this;
    }

    public SharedPrefs putFloat(String key, float value) {
        wrap(getEditor().putFloat(key, value));
        return this;
    }

    public SharedPrefs putBoolean(String key, boolean value) {
        wrap(getEditor().putBoolean(key, value));
        return this;
    }

    public SharedPrefs putString(String key, String value) {
        wrap(getEditor().putString(key, value));
        return this;
    }

    public SharedPrefs putStringSet(String key, Set<String> value) {
        wrap(getEditor().putStringSet(key, value));
        return this;
    }

    public SharedPrefs remove(String key) {
        wrap(getEditor().remove(key));
        return this;
    }

    public boolean contains(String key) {
        return getSharedPreferences().contains(key);
    }

    public void clear() {
        getEditor().clear().commit();
        this.isInTransaction = false;
    }

    public void commit() {
        getEditor().commit();
        this.isInTransaction = false;
    }

    public void apply() {
        getEditor().apply();
        this.isInTransaction = false;
    }

    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
    }

    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
    }

    public SharedPrefs beginTransaction() {
        this.isInTransaction = true;
        return this;
    }

    private void wrap(SharedPreferences.Editor editor) {
        if (!isInTransaction) {
            editor.apply();
        }
    }

    public SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
    }

    @SuppressLint("CommitPrefEdits")
    public SharedPreferences.Editor getEditor() {
        if (mEditor == null) {
            mEditor = getSharedPreferences().edit();
        }
        return mEditor;
    }
}
