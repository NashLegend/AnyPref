package net.nashlegend.demo;

import net.nashlegend.anypref.annotations.PrefField;
import net.nashlegend.anypref.annotations.PrefIgnore;
import net.nashlegend.anypref.annotations.PrefModel;
import net.nashlegend.anypref.annotations.PrefSub;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by NashLegend on 16/5/22.
 */
@PrefModel("SampleKeys")
public class Sample extends SampleSub {
    @PrefField("intKey")
    public int intField = 65535;
    @PrefIgnore
    public float floatField = 1.235f;
    public long longField = 95789465213L;
    public String stringField = "string";
    public boolean boolField = false;
    public Set<String> setValue = new LinkedHashSet<>();
    public int intField2 = 65535;
    public float floatField2 = 1.235f;
    public long longField2 = 95789465213L;
    public String stringField2 = "string";
    public boolean boolField2 = false;
    public Set<String> setValue2 = new LinkedHashSet<>();
    public int intField3 = 65535;
    public float floatField3 = 1.235f;
    public long longField3 = 95789465213L;
    public String stringField3 = "string";
    public boolean boolField3 = false;
    public Set<String> setValue3 = new LinkedHashSet<>();

    @PrefField(numDef = 110)
    public long longField4 = 95789465213L;
    @PrefField(numDef = 110)
    public int intField4 = 2;
    @PrefField(numDef = 110)
    public float floatField4 = 1.2f;
    @PrefField(boolDef = true)
    public boolean boolField4 = false;
    @PrefField(strDef = "Default")
    public String stringField4 = "NotDefault";

    private String pvString = "private";
    private int pvInt = 10010;

    @PrefSub
    public SampleSub son1;

    public SampleSub son2;
}
