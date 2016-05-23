package net.nashlegend.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;

import javax.lang.model.element.Modifier;

/**
 * Created by NashLegend on 16/5/23.
 */
public class ImlGenerator {
    Class clazz;
    String simpleName;

    public ImlGenerator(Class clazz) {
        this.clazz = clazz;
        simpleName = clazz.getSimpleName();
    }

    public TypeSpec genCode() {
        TypeSpec iml = TypeSpec.classBuilder(simpleName + "Iml")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(readInstanceUser())
                .addMethod(readInstance())
                .addMethod(saveUser())
                .addMethod(save())
                .addMethod(clearUser())
                .addMethod(clear())
                .addMethod(readInstanceFromPref())
                .addMethod(isStringEmpty())
                .addMethod(getUserPrefName())
                .addMethod(copyInstance())
                .addMethod(copySet())
                .addMethod(getInstanceUser())
                .addMethod(getInstance())
                .build();
        return iml;
    }

    /**
     * 返回一个新实例
     *
     * @return
     */
    public MethodSpec readInstanceUser() {
        return MethodSpec.methodBuilder("readInstance")
                .returns(TypeName.get(clazz))
                .addParameter(String.class, "user")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addCode("return copyInstance(getInstance(user));\n")
                .build();
    }

    /**
     * 返回一个新实例
     *
     * @return
     */
    public MethodSpec readInstance() {
        return MethodSpec.methodBuilder("readInstance")
                .returns(TypeName.get(clazz))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addCode("return copyInstance(getInstance());\n")
                .build();
    }

    /**
     * 保存一个新实例
     */
    public MethodSpec saveUser() {
        StringBuilder prefCodes = new StringBuilder();
        prefCodes.append("context.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit()");
        Field[] fields = clazz.getFields();
        for (Field field : fields) {
            String key = field.getName();
            String type = field.getType().getCanonicalName();
            String code = "";
            switch (type) {
                case "int":
                    code = ".putInt(\"" + key + "\", instance." + key + ")";
                    break;
                case "float":
                    code = ".putFloat(\"" + key + "\", instance." + key + ")";
                    break;
                case "long":
                    code = ".putLong(\"" + key + "\", instance." + key + ")";
                    break;
                case "boolean":
                    code = ".putBoolean(\"" + key + "\", instance." + key + ")";
                    break;
                case "java.lang.String":
                    code = ".putString(\"" + key + "\", instance." + key + ")";
                    break;
                default:
                    if (field.getType().isAssignableFrom(Set.class)) {
                        Type tp = field.getGenericType();
                        if (tp == null)
                            break;
                        if (tp instanceof ParameterizedType) {
                            ParameterizedType pt = (ParameterizedType) tp;
                            Type[] types = pt.getActualTypeArguments();
                            if (types.length == 1) {
                                Class genericClazz = (Class) pt.getActualTypeArguments()[0];
                                if (genericClazz.getCanonicalName().equals(String.class.getCanonicalName())) {
                                    code = ".putStringSet(\"" + key + "\", instance." + key + ")";
                                }
                            }
                        }
                    }
                    break;
            }
            prefCodes.append(code);
        }
        prefCodes.append(".apply();\n");
        return MethodSpec.methodBuilder("save")
                .returns(Void.class)
                .addParameter(clazz, "instanceToSave")
                .addParameter(String.class, "user")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addCode("String prefName = getUserPrefName(user);\n")
                .addCode(simpleName + " instance = copyInstance(instanceToSave);\n")
                .addCode(prefCodes.toString())
                .build();
    }

    /**
     * 保存一个新实例
     */
    public MethodSpec save() {
        return MethodSpec.methodBuilder("save")
                .returns(Void.class)
                .addParameter(clazz, "instanceToSave")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addCode("save(instanceToSave, null);\n")
                .build();
    }

    public MethodSpec clear() {
        return MethodSpec.methodBuilder("clear")
                .returns(Void.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addCode("defaultInstance = null;\n")
                .addCode("context.getSharedPreferences(defaultPrefName, Context.MODE_PRIVATE).edit().clear().apply();\n")
                .build();
    }


    public MethodSpec clearUser() {
        return MethodSpec.methodBuilder("clear")
                .returns(Void.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addParameter(String.class, "user")
                .addCode("subSamples.remove(user);\n")
                .addCode("context.getSharedPreferences(getUserPrefName(user), Context.MODE_PRIVATE).edit().clear().apply();\n")
                .build();
    }

    private MethodSpec readInstanceFromPref() {
        StringBuilder prefCodes = new StringBuilder();
        prefCodes.append("SharedPreferences prefs = context.getSharedPreferences(getUserPrefName(user), Context.MODE_PRIVATE);\n");
        prefCodes.append(simpleName + " instance = new " + simpleName + "();\n");
        Field[] fields = clazz.getFields();
        for (Field field : fields) {
            String key = field.getName();
            String type = field.getType().getCanonicalName();
            String code = "";
            switch (type) {
                case "int":
                    code = "instance." + key + " = prefs.getInt(\"" + key + "\", 0);\n";
                    break;
                case "float":
                    code = "instance." + key + " = prefs.getFloat(\"" + key + "\", 0);\n";
                    break;
                case "long":
                    code = "instance." + key + " = prefs.getLong(\"" + key + "\", 0);\n";
                    break;
                case "boolean":
                    code = "instance." + key + " = prefs.getBoolean(\"" + key + "\", false);\n";
                    break;
                case "java.lang.String":
                    code = "instance." + key + " = prefs.getString(\"" + key + "\", null);\n";
                    break;
                default:
                    if (field.getType().isAssignableFrom(Set.class)) {
                        Type tp = field.getGenericType();
                        if (tp == null)
                            break;
                        if (tp instanceof ParameterizedType) {
                            ParameterizedType pt = (ParameterizedType) tp;
                            Type[] types = pt.getActualTypeArguments();
                            if (types.length == 1) {
                                Class genericClazz = (Class) pt.getActualTypeArguments()[0];
                                if (genericClazz.getCanonicalName().equals(String.class.getCanonicalName())) {
                                    code = "instance." + key + " = prefs.getStringSet(\"" + key + "\", null);\n";
                                }
                            }
                        }
                    }
                    break;
            }
            prefCodes.append(code);
        }
        prefCodes.append("return instance;\n");

        return MethodSpec.methodBuilder("readInstanceFromPref")
                .returns(TypeName.get(clazz))
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .addParameter(String.class, "user")
                .addCode(prefCodes.toString())
                .build();
    }

    private MethodSpec isStringEmpty() {
        return MethodSpec.methodBuilder("isStringEmpty")
                .returns(TypeName.BOOLEAN)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .addParameter(CharSequence.class, "str")
                .addCode("return (str == null || str.length() == 0);\n")
                .build();
    }

    private MethodSpec getUserPrefName() {
        return MethodSpec.methodBuilder("isStringEmpty")
                .returns(String.class)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .addParameter(String.class, "user")
                .addCode("if (isStringEmpty(user)) {\n" +
                        "            return defaultPrefName;\n" +
                        "        }\n" +
                        "        return defaultPrefName + \".0\" + user;\n")
                .build();
    }

    private MethodSpec copyInstance() {
        StringBuilder codes = new StringBuilder();
        codes.append(simpleName + " sample = new " + simpleName + "();");
        Field[] fields = clazz.getFields();
        for (Field field : fields) {
            String key = field.getName();
            String type = field.getType().getCanonicalName();
            String code = "";
            switch (type) {
                case "int":
                case "float":
                case "long":
                case "boolean":
                case "java.lang.String":
                    code = "instance." + key + " = sourceInstance." + key + ";";
                default:
                    if (field.getType().isAssignableFrom(Set.class)) {
                        Type tp = field.getGenericType();
                        if (tp == null)
                            break;
                        if (tp instanceof ParameterizedType) {
                            ParameterizedType pt = (ParameterizedType) tp;
                            Type[] types = pt.getActualTypeArguments();
                            if (types.length == 1) {
                                Class genericClazz = (Class) pt.getActualTypeArguments()[0];
                                if (genericClazz.getCanonicalName().equals(String.class.getCanonicalName())) {
                                    code = ".putStringSet(\"" + key + "\", instance." + key + ")";
                                    code = "instance." + key + " = copySet(sourceInstance." + key + ");";
                                }
                            }
                        }
                    }
                    break;
            }
            codes.append(code);
        }
        codes.append("return instance;\n");

        return MethodSpec.methodBuilder("copyInstance")
                .returns(clazz)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .addParameter(clazz, "sourceSample")
                .addCode(codes.toString())
                .build();
    }

    private MethodSpec copySet() {
        ClassName str = ClassName.get("java.lang", "String");
        ClassName sets = ClassName.get("java.util", "Set");
        TypeName setName = ParameterizedTypeName.get(sets, str);
        return MethodSpec.methodBuilder("copySet")
                .returns(setName)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .addParameter(setName, "stringSet")
                .addCode("HashSet<String> stringHashSet = new HashSet<>();\n" +
                        "        if (stringSet != null) {\n" +
                        "            stringHashSet.addAll(stringSet);\n" +
                        "        }\n")
                .build();
    }

    private MethodSpec getInstanceUser() {
        return MethodSpec.methodBuilder("getInstance")
                .returns(clazz)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .addParameter(String.class, "user")
                .addCode("        if (isStringEmpty(user)) {\n" +
                        "            return getInstance();\n" +
                        "        }\n")
                .addCode(simpleName + " instance = subSamples.get(user);\n")
                .addCode("        if (instance == null) {\n" +
                        "            instance = readInstanceFromPref(user);\n" +
                        "            subSamples.put(user, instance);\n" +
                        "        }\n" +
                        "        return sample;")
                .build();
    }

    private MethodSpec getInstance() {
        return MethodSpec.methodBuilder("getInstance")
                .returns(clazz)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .addCode("if (defaultInstance == null) {\n" +
                        "            defaultInstance = readInstanceFromPref(null);\n" +
                        "        }\n" +
                        "        return defaultInstance;")
                .build();
    }
}
