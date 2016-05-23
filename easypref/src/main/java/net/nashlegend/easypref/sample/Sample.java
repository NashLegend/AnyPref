package net.nashlegend.easypref.sample;

import net.nashlegend.annotations.PrefModel;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by NashLegend on 16/5/22.
 */
@PrefModel
public class Sample {
    public int intField = 65535;
    public float floatField = 1.235f;
    public long longField = 95789465213L;
    public String stringField = "string";
    public boolean boolField = false;
    public Set<String> setValue = new LinkedHashSet<>();
}
