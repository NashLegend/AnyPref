package net.nashlegend.demo;

import android.app.Application;

import net.nashlegend.anypref.AnyPref;

/**
 * Created by NashLegend on 16/5/23.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AnyPref.init(this);
    }
}
