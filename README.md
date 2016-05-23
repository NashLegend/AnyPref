一个SharedPreferences工具类
-----------------------

## 1. 读写任意数据

    ```
        AnyPref.getPrefs("sample", this)//或者new SharedPrefs("sample", this)
                .putLong("long", 920394857382L)
                .putInt("int", 63)
                .putString("string", "sample string");
    
        AnyPref.getPrefs(Sample.class, this)
                .beginTransaction()
                .putLong("long", 920394857382L)
                .putInt("int", 63)
                .putString("string", "sample string")
                .commit();
    
        SharedPrefs sharedPrefs = AnyPref.getPrefs("sample", this);
        System.out.println(sharedPrefs.getInt("int", 0));
        System.out.println(sharedPrefs.getLong("long", 0));
        System.out.println(sharedPrefs.getString("string", ""));
    ```


## 2. 读写实例对象

假设有一个Sample类

```
    @PrefModel("prefName")//可不添加此注解，"prefName"表示保存SharedPreferences的name，可为任意String字符串，如果不写，则为类的全名
    public class Sample {
        @Pref("intFieldKey")//可不添加此注解，"Integer"表示保存此值时的key，可为任意String字符串，如果不写，则为此变量的变量名
        public int intField = 32;
        @PrefIgnore//添加此注解表示不保存这个变量
        public float floatField = 1.2345f;
        public long longField = 95789465213L;
        public String stringField = "string";
        public boolean boolField = false;
        public Set<String> setValue = new LinkedHashSet<>();
    }
```

#### 保存数据:
```
    AnyPref.save(sample，context);
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