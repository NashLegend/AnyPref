package net.nashlegend.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import net.nashlegend.anypref.AnyPref;
import net.nashlegend.anypref.SharedPrefs;

import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = (Button) findViewById(R.id.btn);
        assert button != null;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anyPref();
                namedPref();
                print(Sample.class);
            }
        });
    }

    private void anyPref() {
        Sample sample = new Sample();
        sample.boolField = true;
        sample.intField = 63;
        sample.floatField = 42.0f;
        sample.stringField = "sample string";
        SampleSub son1 = new SampleSub();
        son1.name = "son1";
        SampleSub son2 = new SampleSub();
        son2.name = "son2";
        sample.son1 = son1;
        sample.son2 = son2;
        AnyPref.save(sample);
        Sample sampleOut = AnyPref.read(Sample.class);
        System.out.println("son1:" + sampleOut.son1.name);
        System.out.println("son2:" + sampleOut.son2.name);
    }

    private void namedPref() {
        AnyPref.getPrefs(getLocalClassName())
                .putLong("long", 920394857382L)
                .putInt("int", 63)
                .putString("string", "sample string");

        AnyPref.getPrefs(Sample.class)
                .beginTransaction()
                .putLong("long", 33333)
                .putInt("int", 22222)
                .putString("string", "sssss")
                .commit();

        SharedPrefs sharedPrefs = AnyPref.getPrefs(getLocalClassName());
        System.out.println(sharedPrefs.getInt("int", 0));
        System.out.println(sharedPrefs.getLong("long", 0));
        System.out.println(sharedPrefs.getString("string", ""));
    }

    private void print(Class clazz) {
        Field[] fieldArray = clazz.getFields();
        for (Field field : fieldArray) {
            System.out.println(field.getName());
        }
    }
}
