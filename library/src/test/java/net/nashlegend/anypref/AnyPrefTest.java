package net.nashlegend.anypref;

import android.os.Build;

import net.nashlegend.anypref.model.Sample;
import net.nashlegend.anypref.model.SubSample;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by NashLegend on 16/6/7.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.M)
public class AnyPrefTest {

    @Before
    public void setUp() throws Exception {
        AnyPref.init(RuntimeEnvironment.application);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testClearByClass() throws Exception {
        Sample sample = new Sample();
        sample.boolField = true;
        sample.intField = 67;
        sample.longField = 12369874569L;
        AnyPref.put(sample);

        AnyPref.clear(Sample.class);

        Sample shadowSample = AnyPref.get(Sample.class);
        Assert.assertEquals(0, shadowSample.intField);
        Assert.assertEquals(0, shadowSample.floatField, 0.0001);
        Assert.assertEquals(0, shadowSample.longField, 0.0001);
        Assert.assertNull(shadowSample.stringField);
        Assert.assertEquals(110, shadowSample.intFieldDefault);
    }

    @Test
    public void testClearByKey() throws Exception {
        Sample sample = new Sample();
        sample.boolField = true;
        sample.intField = 67;
        sample.longField = 12369874569L;
        AnyPref.put(sample, "sample");

        AnyPref.clear(Sample.class, "sample");

        Sample shadowSample = AnyPref.get(Sample.class);
        Assert.assertEquals(0, shadowSample.intField);
        Assert.assertEquals(0, shadowSample.floatField, 0.0001);
        Assert.assertEquals(0, shadowSample.longField, 0.0001);
        Assert.assertNull(shadowSample.stringField);
        Assert.assertEquals(110, shadowSample.intFieldDefault);
    }

    @Test
    public void testPrefFieldKey() throws Exception {
        Sample sample = new Sample();
        sample.boolFieldNamed = true;
        sample.intFieldNamed = 68;
        sample.longFieldNamed = 12369874560L;
        sample.floatFieldNamed = 1.37F;
        sample.stringFieldNamed = "testStringNamed";
        AnyPref.put(sample);

        SharedPrefs prefs = AnyPref.getPrefs(Sample.class);
        Assert.assertEquals(true, prefs.getBoolean("boolFieldWithSpecifiedKey", false));
        Assert.assertEquals(68, prefs.getInt("intFieldWithSpecifiedKey", 0));
        Assert.assertEquals(12369874560L, prefs.getLong("longFieldWithSpecifiedKey", 0));
        Assert.assertEquals(1.37F, prefs.getFloat("floatFieldWithSpecifiedKey", 0), 0.00001);
        Assert.assertEquals("testStringNamed", prefs.getString("stringFieldWithSpecifiedKey", ""));
    }

    @Test
    public void testDefaultValue() throws Exception {
        Sample shadowSample = AnyPref.get(Sample.class);
        Assert.assertEquals(14789632501L, shadowSample.longFieldDefault);
        Assert.assertEquals(110, shadowSample.intFieldDefault);
        Assert.assertEquals(110, shadowSample.floatFieldDefault, 0.0001);
        Assert.assertEquals(true, shadowSample.boolFieldDefault);
        Assert.assertEquals("Default", shadowSample.stringFieldDefault);

        Set<String> copied = shadowSample.setValueDefault;
        Assert.assertNotNull(copied);
        int i = 1;
        for (String s : copied) {
            Assert.assertEquals(String.valueOf(i), s);
            i++;
        }
    }

    @Test
    public void testReadWriteByClazz() throws Exception {
        Sample sample = new Sample();
        sample.boolField = true;
        sample.intField = 67;
        sample.longField = 12369874569L;
        sample.floatField = 1.36F;
        sample.stringField = "testString";

        sample.boolFieldDefault = true;
        sample.intFieldDefault = 66;
        sample.longFieldDefault = 12369874568L;
        sample.floatFieldDefault = 1.35F;
        sample.stringFieldDefault = "testStringDefault";

        sample.boolFieldNamed = true;
        sample.intFieldNamed = 68;
        sample.longFieldNamed = 12369874560L;
        sample.floatFieldNamed = 1.37F;
        sample.stringFieldNamed = "testStringNamed";

        sample.boolFieldIgnore = false;
        sample.intFieldIgnore = 1;
        sample.longFieldIgnore = 1;
        sample.floatFieldIgnore = 1;
        sample.stringFieldIgnore = "1";

        SubSample subSample = new SubSample();
        subSample.name = "subSampleTest";

        sample.son1 = subSample;

        AnyPref.put(sample);

        Sample shadowSample = AnyPref.get(Sample.class);

        Assert.assertEquals(true, shadowSample.boolField);
        Assert.assertEquals(67, shadowSample.intField);
        Assert.assertEquals(12369874569L, shadowSample.longField);
        Assert.assertEquals(1.36F, shadowSample.floatField, 0.0001);
        Assert.assertEquals("testString", shadowSample.stringField);

        Assert.assertEquals(true, shadowSample.boolFieldNamed);
        Assert.assertEquals(68, shadowSample.intFieldNamed);
        Assert.assertEquals(12369874560L, shadowSample.longFieldNamed);
        Assert.assertEquals(1.37F, shadowSample.floatFieldNamed, 0.0001);
        Assert.assertEquals("testStringNamed", shadowSample.stringFieldNamed);

        Assert.assertEquals(true, shadowSample.boolFieldDefault);
        Assert.assertEquals(66, shadowSample.intFieldDefault);
        Assert.assertEquals(12369874568L, shadowSample.longFieldDefault);
        Assert.assertEquals(1.35F, shadowSample.floatFieldDefault, 0.0001);
        Assert.assertEquals("testStringDefault", shadowSample.stringFieldDefault);

        Assert.assertNotEquals(false, shadowSample.boolFieldIgnore);
        Assert.assertNotEquals(1, shadowSample.intFieldIgnore);
        Assert.assertNotEquals(1, shadowSample.longFieldIgnore);
        Assert.assertNotEquals(1, shadowSample.floatFieldIgnore, 0.0001);
        Assert.assertNotEquals("1", shadowSample.stringFieldIgnore);

        Assert.assertNotNull(shadowSample.son1);
        Assert.assertNull(shadowSample.son2);

        Assert.assertEquals("subSampleTest", shadowSample.son1.name);
    }

    @Test
    public void testReadWriteByKey() throws Exception {
        Sample sample = new Sample();
        sample.boolField = true;
        sample.intField = 67;
        sample.longField = 12369874569L;
        sample.floatField = 1.36F;
        sample.stringField = "testString";

        sample.boolFieldDefault = true;
        sample.intFieldDefault = 66;
        sample.longFieldDefault = 12369874568L;
        sample.floatFieldDefault = 1.35F;
        sample.stringFieldDefault = "testStringDefault";

        sample.boolFieldNamed = true;
        sample.intFieldNamed = 68;
        sample.longFieldNamed = 12369874560L;
        sample.floatFieldNamed = 1.37F;
        sample.stringFieldNamed = "testStringNamed";

        sample.boolFieldIgnore = false;
        sample.intFieldIgnore = 1;
        sample.longFieldIgnore = 1;
        sample.floatFieldIgnore = 1;
        sample.stringFieldIgnore = "1";

        SubSample subSample = new SubSample();
        subSample.name = "subSampleTest";

        sample.son1 = subSample;

        AnyPref.put(sample, "sample");

        Sample shadowSample = AnyPref.get(Sample.class, "sample");

        Assert.assertEquals(true, shadowSample.boolField);
        Assert.assertEquals(67, shadowSample.intField);
        Assert.assertEquals(12369874569L, shadowSample.longField);
        Assert.assertEquals(1.36F, shadowSample.floatField, 0.0001);
        Assert.assertEquals("testString", shadowSample.stringField);

        Assert.assertEquals(true, shadowSample.boolFieldNamed);
        Assert.assertEquals(68, shadowSample.intFieldNamed);
        Assert.assertEquals(12369874560L, shadowSample.longFieldNamed);
        Assert.assertEquals(1.37F, shadowSample.floatFieldNamed, 0.0001);
        Assert.assertEquals("testStringNamed", shadowSample.stringFieldNamed);

        Assert.assertEquals(true, shadowSample.boolFieldDefault);
        Assert.assertEquals(66, shadowSample.intFieldDefault);
        Assert.assertEquals(12369874568L, shadowSample.longFieldDefault);
        Assert.assertEquals(1.35F, shadowSample.floatFieldDefault, 0.0001);
        Assert.assertEquals("testStringDefault", shadowSample.stringFieldDefault);

        Assert.assertNotEquals(false, shadowSample.boolFieldIgnore);
        Assert.assertNotEquals(1, shadowSample.intFieldIgnore);
        Assert.assertNotEquals(1, shadowSample.longFieldIgnore);
        Assert.assertNotEquals(1, shadowSample.floatFieldIgnore, 0.0001);
        Assert.assertNotEquals("1", shadowSample.stringFieldIgnore);

        Assert.assertNotNull(shadowSample.son1);
        Assert.assertNull(shadowSample.son2);

        Assert.assertEquals("subSampleTest", shadowSample.son1.name);
    }

    @Test
    public void testStringSet() throws Exception {
        Sample sample = new Sample();
        HashSet<String> set = new LinkedHashSet<>();
        set.add("1");
        set.add("2");
        set.add("3");
        set.add("4");

        sample.setValue = set;
        AnyPref.put(sample, "sample");

        Sample shadowSample = AnyPref.get(Sample.class, "sample");
        Set<String> copied = shadowSample.setValue;
        Assert.assertNotNull(copied);
        int i = 1;
        for (String s : copied) {
            Assert.assertEquals(String.valueOf(i), s);
            i++;
        }
    }

    @Test
    public void testStringSetDefault() throws Exception {
        Sample sample = new Sample();
        HashSet<String> set = new LinkedHashSet<>();
        set.add("1");
        set.add("2");
        set.add("3");
        set.add("4");

        sample.setValue = set;
        AnyPref.put(sample, "sample");

        Sample shadowSample = AnyPref.get(Sample.class, "sample");
        Set<String> copied = shadowSample.setValue;
        Assert.assertNotNull(copied);
        int i = 1;
        for (String s : copied) {
            Assert.assertEquals(String.valueOf(i), s);
            i++;
        }
    }

    @Test
    public void testArrayList() throws Exception {
        Sample sample = new Sample();
        sample.sampleArrayList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            SubSample subSample = new SubSample();
            subSample.name = "index_" + i;
            sample.sampleArrayList.add(subSample);
        }

        AnyPref.put(sample);

        Sample shadowSample = AnyPref.get(Sample.class);

        Assert.assertEquals(shadowSample.sampleArrayList.size(), 5);
    }

    @Test
    public void testClearArrayList() throws Exception {
        Sample sample = new Sample();
        sample.sampleArrayList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            SubSample subSample = new SubSample();
            subSample.name = "index_" + i;
            sample.sampleArrayList.add(subSample);
        }

        AnyPref.put(sample);

        AnyPref.clear(Sample.class);

        String key = "SampleKeys$$$$sampleArrayList_arraylist_0";
        SubSample shadowSubSample = AnyPref.get(SubSample.class, key);
        Assert.assertNull(shadowSubSample.name);
    }
}