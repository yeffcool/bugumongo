package com.bugull.mongo.mapper;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class DataType {
    
    public static boolean isString(String typeName){
        return typeName.equals("java.lang.String");
    }
    
    public static boolean isInteger(String typeName){
        return typeName.equals("int");
    }
    
    public static boolean isIntegerObject(String typeName){
        return typeName.equals("java.lang.Integer");
    }
    
    public static boolean isLong(String typeName){
        return typeName.equals("long");
    }
    
    public static boolean isLongObject(String typeName){
        return typeName.equals("java.lang.Long");
    }
    
    public static boolean isShort(String typeName){
        return typeName.equals("short");
    }
    
    public static boolean isShortObject(String typeName){
        return typeName.equals("java.lang.Short");
    }
    
    public static boolean isFloat(String typeName){
        return typeName.equals("float");
    }
    
    public static boolean isFloatObject(String typeName){
        return typeName.equals("java.lang.Float");
    }
    
    public static boolean isDouble(String typeName){
        return typeName.equals("double");
    }
    
    public static boolean isDoubleObject(String typeName){
        return typeName.equals("java.lang.Double");
    }
    
    public static boolean isBoolean(String typeName){
        return typeName.equals("boolean");
    }
    
    public static boolean isBooleanObject(String typeName){
        return typeName.equals("java.lang.Boolean");
    }
    
    public static boolean isChar(String typeName){
        return typeName.equals("char");
    }
    
    public static boolean isCharObject(String typeName){
        return typeName.equals("java.lang.Character");
    }
    
    public static boolean isDate(String typeName){
        return typeName.equals("java.util.Date");
    }
    
    public static boolean isTimestamp(String typeName){
        return typeName.equals("java.sql.Timestamp");
    }
    
    public static boolean isList(String typeName){
        return typeName.equals("java.util.List") || typeName.equals("java.util.ArrayList") || typeName.equals("java.util.LinkedList");
    }
    
    public static boolean isSet(String typeName){
        return typeName.equals("java.util.Set") || typeName.equals("java.util.HashSet") || typeName.equals("java.util.TreeSet");
    }
    
    public static boolean isMap(String typeName){
        return typeName.equals("java.util.Map") || typeName.equals("java.util.HashMap") || typeName.equals("java.util.TreeMap");
    }
    
}
