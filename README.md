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
        @Pref(number = 110)//表示如果读取不到后使用的默认值
        public long longField = 95789465213L;
        public String stringField = "string";
        public boolean boolField = false;
        public Set<String> setValue = new LinkedHashSet<>();
    }
```

#### 保存数据:
```
    AnyPref.save(sample);
```

#### 读取数据
```
    Sample sample = AnyPref.get(Sample.class)
```

#### 清除数据
```
    AnyPref.clear(Sample.class)
```

PS，对于实例对象的读写：

1. 只支持SharedPreferences支持的数据类型;
2. 只会保存修饰符为```public```的变量，```static```与```final```的变量均不会保存;
3. 保存的类需要支持无参构造函数


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