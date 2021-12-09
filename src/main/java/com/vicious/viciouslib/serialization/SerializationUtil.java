package com.vicious.viciouslib.serialization;


import com.vicious.viciouslib.database.tracking.interfaces.TrackableValueStringParser;
import com.vicious.viciouslib.util.VCUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class SerializationUtil {
    public static final Map<Class<?>, TrackableValueStringParser<?>> stringparsers = new HashMap<>();
    public static final Map<Class<?>, BiFunction<String,Object[],?>> specialstringparsers = new HashMap<>();
    public static final Map<Class<?>, Function<Object,String>> serializers = new HashMap<>();
    public static final Map<Class<?>, BiFunction<Object,Object[],String>> specialserializers = new HashMap<>();
    static {
        stringparsers.put(Boolean.class, Boolean::parseBoolean);
        stringparsers.put(Integer.class, Integer::parseInt);
        stringparsers.put(Double.class, Double::parseDouble);
        stringparsers.put(Float.class, Float::parseFloat);
        stringparsers.put(Byte.class, Byte::parseByte);
        stringparsers.put(Short.class, Short::parseShort);
        stringparsers.put(Long.class, Long::parseLong);
        stringparsers.put(String.class,(j)-> j);
        stringparsers.put(UUID.class, UUID::fromString);
        stringparsers.put(Date.class, VCUtil.DATEFORMAT::parse);
    }
    static {
        specialserializers.put(SerializableArray.class,(o,exdat)-> ((SerializableArray<?>)o).serialize(exdat));
    }

    public static Object serialize(Object value, Object... extraData) {
        if(value == null) return value;
        String out = executeOnTargetClass((cls) -> serializers.get(cls).apply(value), serializers::containsKey, value.getClass());
        if (out != null) return out;
        out = executeOnTargetClass((cls) -> specialserializers.get(cls).apply(value, extraData), specialserializers::containsKey, value.getClass());
        return out != null ? out : value;
    }
    public static Object parse(Class<?> type, String s, Object... exdat) throws Exception{
        if(stringparsers.containsKey(type)){
            return stringparsers.get(type).parse(s);
        }
        else if(specialstringparsers.containsKey(type)){
            return specialstringparsers.get(type).apply(s,exdat);
        }
        return null;
    }
    public static <T> T executeOnTargetClass(Function<Class<?>,T> funct, Class<?> start) {
        Class<?>[] interfaces = start.getInterfaces();
        T ret = null;
        while(ret == null &&start != null){
            ret = funct.apply(start);
            if(ret != null) break;
            for (Class<?> anInterface : interfaces) {
                ret = funct.apply(anInterface);
            }
            start=start.getSuperclass();
        }
        return ret;
    }

    public static <T> T executeOnTargetClass(Function<Class<?>,T> funct, Predicate<Class<?>> doExec, Class<?> start) {
        Class<?>[] interfaces;
        while(start != null){
            interfaces=start.getInterfaces();
            if(doExec.test(start)) {
                return funct.apply(start);
            }
            for (Class<?> anInterface : interfaces) {
                if(doExec.test(anInterface)){
                    return funct.apply(anInterface);
                }
            }
            start=start.getSuperclass();
        }
        return null;
    }
}