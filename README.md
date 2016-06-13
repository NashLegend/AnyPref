# 一个SharedPreferences工具类

[![](https://jitpack.io/v/NashLegend/AnyPref.svg)](https://jitpack.io/#NashLegend/AnyPref)

在工程根目录build.gradle添加jitpack:
```gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```

在使用AnyPref的模块中添加:

```gradle
dependencies {
    compile 'com.github.NashLegend:AnyPref:1.1.0'
}
```

在应用的Application的```onCreate()```中添加如下代码（主要是为了省却后面要传入Context参数的麻烦）

```
    AnyPref.init(this);
```

## 1. 读写实例对象

假设有一个Sample类

```
    @PrefModel("prefName")//可不添加此注解，"prefName"表示保存SharedPreferences的name，可为任意String字符串，如果不写，则为类的全名
    public class Sample {
        @PrefField("intFieldKey")//可不添加此注解，"intFieldKey"表示保存此值时的key，可为任意String字符串，如果不写，则为此变量的变量名
        public int intField = 32;
        @PrefIgnore//添加此注解表示不保存这个变量
        public float floatField = 1.2345f;
        @PrefField(numDef = 110)//表示如果读取不到后使用的默认值
        public long longField = 95789465213L;
        public String stringField = "string";
        @PrefField(boolDef = true)
        public boolean boolField = false;
        @PrefField(value = "setValueWithSpecifiedKey", strDef = {"1", "2", "3", "4"})//默认值是[1,2,3,4]
        public Set<String> setValue = new LinkedHashSet<>(); 
        @PrefSub
        public SubSample son1;//标注了@PrefSub的变量，虽然不是SharedPreferences支持的类型，但是仍会被保存
        @PrefArrayList
        public ArrayList<SubSample> sampleArrayList;//标注了@PrefArrayList的ArrayList会被保存，但是ArrayList不能是基本类型的
    }
```

#### 保存数据:
```
    AnyPref.put(sample);
    //或者
    AnyPref.put(sample, "your key");第二个参数是自己定义的保存此类的key，不是PrefModel定义的那个Key
```

#### 读取数据
```
    Sample sample = AnyPref.get(Sample.class);
    //或者
    Sample sample = AnyPref.get(Sample.class, "your key");
```

#### 清除数据
```
    AnyPref.clear(Sample.class);
    //或者
    AnyPref.clear(Sample.class, "your key");
```

PS，对于实例对象的读写：

0. 保存的对象必须支持无参构造函数，它是写代码时用到的Model对象或者一组Setting等，不是用来保存一些系统对象比如String,View的;
1. 保存的对象的变量们中只保存SharedPreferences支持的以及标注了@PrefSub和@PrefArrayList的变量;
2. 标注了@PrefSub和@PrefArrayList的类型要求同第一条
3. 只会保存修饰符为```public```的变量，```static```与```final```的变量均不会保存;
4. 标注了@PrefSub和@PrefArrayList的变量为空时，取出来的不为null，而是默认对象或者空ArrayList

##### 如果使用了ProGuard，在proguard配置文件中添加

```
-keep class net.nashlegend.demo.Sample{ *; }
```
或者
```
-keepclasseswithmembernames class net.nashlegend.demo.Sample {
   public <fields>;
}
```

将```net.nashlegend.demo.Sample```改成对应的类

## 2. 读写任意数据

```
    AnyPref.getPrefs("sample")//或者new SharedPrefs("sample")
            .putLong("long", 920394857382L)
            .putInt("int", 63)
            .putString("string", "sample string");

    AnyPref.getPrefs(Sample.class)
            .beginTransaction()
            .putLong("long", 920394857382L)
            .putInt("int", 63)
            .putString("string", "sample string")
            .commit();

    SharedPrefs sharedPrefs = AnyPref.getPrefs("sample");
    System.out.println(sharedPrefs.getInt("int", 0));
    System.out.println(sharedPrefs.getLong("long", 0));
    System.out.println(sharedPrefs.getString("string", ""));
```