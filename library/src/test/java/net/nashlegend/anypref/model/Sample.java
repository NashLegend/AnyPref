package net.nashlegend.anypref.model;

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
public class Sample {
    public int intField = 65535;
    public float floatField = 1.235f;
    public long longField = 95789465213L;
    public String stringField = "string";
    public boolean boolField = false;
    public Set<String> setValue = new LinkedHashSet<>();

    @PrefIgnore
    public int intFieldIgnore = 10086;
    @PrefIgnore
    public float floatFieldIgnore = 10086.1f;
    @PrefIgnore
    public long longFieldIgnore = 100861008610086L;
    @PrefIgnore
    public String stringFieldIgnore = "10086";
    @PrefIgnore
    public boolean boolFieldIgnore = true;
    @PrefIgnore
    public Set<String> setValueIgnore = new LinkedHashSet<>();

    @PrefField("intFieldWithSpecifiedKey")
    public int intFieldNamed = 65535;
    @PrefField("floatFieldWithSpecifiedKey")
    public float floatFieldNamed = 1.235f;
    @PrefField("longFieldWithSpecifiedKey")
    public long longFieldNamed = 95789465213L;
    @PrefField("stringFieldWithSpecifiedKey")
    public String stringFieldNamed = "string";
    @PrefField("boolFieldWithSpecifiedKey")
    public boolean boolFieldNamed = false;
    @PrefField(value = "setValueWithSpecifiedKey", string = {"1", "2", "3", "4"})
    public Set<String> setValueNamed = new LinkedHashSet<>();

    @PrefField(number = 14789632501L)
    public long longFieldDefault = 12345678910L;
    @PrefField(number = 110)
    public int intFieldDefault = 2;
    @PrefField(number = 110)
    public float floatFieldDefault = 1.2f;
    @PrefField(bool = true)
    public boolean boolFieldDefault = false;
    @PrefField(string = "Default")
    public String stringFieldDefault = "NotDefault";
    @PrefField(string = {"1", "2", "3", "4"})
    public Set<String> setValueDefault = new LinkedHashSet<>();

    @PrefSub
    public SubSample son1;

    /**
     * object that not annotated with PrefSub
     */
    public SubSample son2;

    //private fields
    private String pvString = "private";
    private int pvInt = 10010;
}
