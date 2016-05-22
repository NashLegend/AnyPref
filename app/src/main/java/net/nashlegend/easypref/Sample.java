package net.nashlegend.easypref;

import net.nashlegend.annotations.Pref;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by NashLegend on 16/5/22.
 */
public class Sample {
    @Pref
    public int intField = 65535;
    @Pref
    public float floatField = 1.235f;
    @Pref
    public long longField = 95789465213L;
    public String stringField = "string";
    public boolean boolField = false;
    public Set<String> setValue = new LinkedHashSet<>();
}
