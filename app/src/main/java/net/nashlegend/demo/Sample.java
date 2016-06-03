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
    public float floatField32 = 1.235f;
    public long longField1 = 95789465213L;
    public String stringField3 = "string";
    public boolean boolField3 = false;
    public Set<String> setValue3 = new LinkedHashSet<>();
    public int intField1 = 65535;
    public float floatField2 = 1.235f;
    public long longField3 = 95789465213L;
    public String stringField4 = "string";
    public boolean boolField5 = false;
    public Set<String> setValue6 = new LinkedHashSet<>();

    @PrefField(number = 110)
    public long longField123 = 95789465213L;
    @PrefField(number = 110)
    public int intField123 = 2;
    @PrefField(number = 110)
    public float floatField123 = 1.2f;
    @PrefField(bool = true)
    public boolean boolField123 = false;
    @PrefField(string = "Default")
    public String stringField123 = "NotDefault";

    private String dd = "ll";
    private String hidden = "hidden";

    @PrefSub
    public SampleSub son1;
    @PrefSub
    public SampleSub son2;
}
