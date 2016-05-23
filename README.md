
## 这是一个快速保存一个类的实例到SharedPreferences中或者从SharedPreferences中读取一个保存过的实例的工具

## 使用方法:

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

### 保存数据:
```
    AnyPref.apply(sample，context);
```

### 读取数据
```
    Sample sample = AnyPref.get(Sample.class)
```

PS：只会保修饰符为```public```的变量，```static```与```final```的变量均不会保存