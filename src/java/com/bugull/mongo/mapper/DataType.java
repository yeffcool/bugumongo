package com.bugull.mongo.mapper;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class DataType {
    
    public static boolean isString(Class type){
        return type.equals(String.class);
    }
    
    public static boolean isInteger(Class type){
        return type.equals(int.class);
    }
    
    public static boolean isIntegerObject(Class type){
        return type.equals(Integer.class);
    }
    
    public static boolean isLong(Class type){
        return type.equals(long.class);
    }
    
    public static boolean isLongObject(Class type){
        return type.equals(Long.class);
    }
    
    public static boolean isShort(Class type){
        return type.equals(short.class);
    }
    
    public static boolean isShortObject(Class type){
        return type.equals(Short.class);
    }
    
    public static boolean isFloat(Class type){
        return type.equals(float.class);
    }
    
    public static boolean isFloatObject(Class type){
        return type.equals(Float.class);
    }
    
    public static boolean isDouble(Class type){
        return type.equals(double.class);
    }
    
    public static boolean isDoubleObject(Class type){
        return type.equals(Double.class);
    }
    
    public static boolean isBoolean(Class type){
        return type.equals(boolean.class);
    }
    
    public static boolean isBooleanObject(Class type){
        return type.equals(Boolean.class);
    }
    
    public static boolean isChar(Class type){
        return type.equals(char.class);
    }
    
    public static boolean isCharObject(Class type){
        return type.equals(Character.class);
    }
    
    public static boolean isDate(Class type){
        return type.equals(java.util.Date.class);
    }
    
    public static boolean isTimestamp(Class type){
        return type.equals(java.sql.Timestamp.class);
    }
    
    public static boolean isList(Class type){
        return type.equals(java.util.List.class) || type.equals(java.util.ArrayList.class) || type.equals(java.util.LinkedList.class);
    }
    
    public static boolean isSet(Class type){
        return type.equals(java.util.Set.class) || type.equals(java.util.HashSet.class) || type.equals(java.util.TreeSet.class);
    }
    
    public static boolean isMap(Class type){
        return type.equals(java.util.Map.class) || type.equals(java.util.HashMap.class) || type.equals(java.util.TreeMap.class);
    }
    
}
