package net.nashlegend.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import net.nashlegend.anypref.AnyPref;

import java.util.HashSet;

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
                MainActivity.this.click(v);
            }
        });
    }

    private void click(View v) {
        try {
            long start = System.currentTimeMillis();
            Sample sample = new Sample();
            sample.boolField = true;
            sample.intField = 2;
            sample.floatField = 3.54f;
            sample.stringField = "stringField";
            HashSet<String> sets = new HashSet<>();
            sets.add("1");
            sets.add("2");
            sets.add("3");
            sets.add("4");
            sets.add("5");
            sample.setValue = sets;
            AnyPref.apply(sample, this);
            System.out.println(System.currentTimeMillis()-start);
            start = System.currentTimeMillis();
            AnyPref.get(sample.getClass(), this);
            System.out.println(System.currentTimeMillis()-start);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
