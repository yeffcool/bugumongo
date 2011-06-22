package com.bugull.mongo.lucene.backend;

import com.bugull.mongo.BuguEntity;
import com.bugull.mongo.cache.FieldsCache;
import com.bugull.mongo.lucene.annotations.Compare;
import com.bugull.mongo.lucene.annotations.IndexFilter;
import java.lang.reflect.Field;
import org.apache.log4j.Logger;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class IndexFilterChecker {
    
    private final static Logger logger = Logger.getLogger(IndexFilterChecker.class);
    
    private BuguEntity obj;
    
    public IndexFilterChecker(BuguEntity obj){
        this.obj = obj;
    }
    
    public boolean needIndex(){
        Class<?> clazz = obj.getClass();
        Field[] fields = FieldsCache.getInstance().get(clazz);
        for(Field f : fields){
            IndexFilter filter = f.getAnnotation(IndexFilter.class);
            if(filter != null){
                Compare compare = filter.compare();
                String value = filter.value();
                boolean fit = false;
                try{
                    fit = isFit(f, compare, value);
                }catch(Exception e){
                    logger.error(e.getMessage());
                }
                if(!fit){
                    return false;
                }
            }
        }
        return true;
    }
    
    private boolean isFit(Field f, Compare compare, String value) throws Exception {
        boolean fit = false;
        switch(compare){
            case EQUALS:
                fit = isEquals(f, value);
                break;
            case NOT_EQUALS:
                fit = notEquals(f, value);
                break;
            case GREATER_THAN:
                fit = greaterThan(f, value);
                break;
            case GREATER_THAN_EQUALS:
                fit = greaterThanEquals(f, value);
                break;
            case LESS_THAN:
                fit = lessThan(f, value);
                break;
            case LESS_THAN_EQUALS:
                fit = lessThanEquals(f, value);
                break;
            case IS_NULL:
                fit = isNull(f.get(obj));
                break;
            case NOT_NULL:
                fit = notNull(f.get(obj));
                break;
            default:
                break;
        }
        return fit;
    }
    
    private boolean isEquals(Field f, String value) throws Exception {
        String typeName = f.getType().getName();
        if(typeName.equals("java.lang.String")){
            String fieldValue = f.get(obj).toString();
            return value.equals(fieldValue);
        }
        else if(typeName.equals("boolean") || typeName.equals("java.lang.Boolean")){
            boolean fieldValue = f.getBoolean(obj);
            return  fieldValue == Boolean.parseBoolean(value);
        }
        else if(typeName.equals("char") || typeName.equals("java.lang.Character")){
            char fieldValue = f.getChar(obj);
            return fieldValue == value.charAt(0);
        }
        else if(typeName.equals("int") || typeName.equals("java.lang.Integer")){
            int fieldValue = f.getInt(obj);
            return fieldValue == Integer.parseInt(value);
        }
        else if(typeName.equals("long") || typeName.equals("java.lang.Long")){
            long fieldValue = f.getLong(obj);
            return fieldValue == Long.parseLong(value);
        }
        else if(typeName.equals("float") || typeName.equals("java.lang.Float")){
            float fieldValue = f.getFloat(obj);
            return fieldValue == Float.parseFloat(value);
        }
        else if(typeName.equals("double") || typeName.equals("java.lang.Double")){
            double fieldValue = f.getDouble(obj);
            return fieldValue == Double.parseDouble(value);
        }
        else{
            return false;
        }
    }
    
    private boolean notEquals(Field f, String value) throws Exception{
        return !isEquals(f, value);
    }
    
    private boolean greaterThan(Field f, String value) throws Exception{
        String typeName = f.getType().getName();
        if(typeName.equals("int") || typeName.equals("java.lang.Integer")){
            int fieldValue = f.getInt(obj);
            return fieldValue > Integer.parseInt(value);
        }
        else if(typeName.equals("long") || typeName.equals("java.lang.Long")){
            long fieldValue = f.getLong(obj);
            return fieldValue > Long.parseLong(value);
        }
        else if(typeName.equals("float") || typeName.equals("java.lang.Float")){
            float fieldValue = f.getFloat(obj);
            return fieldValue > Float.parseFloat(value);
        }
        else if(typeName.equals("double") || typeName.equals("java.lang.Double")){
            double fieldValue = f.getDouble(obj);
            return fieldValue > Double.parseDouble(value);
        }
        else{
            return false;
        }
    }
    
    private boolean greaterThanEquals(Field f, String value) throws Exception{
        String typeName = f.getType().getName();
        if(typeName.equals("int") || typeName.equals("java.lang.Integer")){
            int fieldValue = f.getInt(obj);
            return fieldValue >= Integer.parseInt(value);
        }
        else if(typeName.equals("long") || typeName.equals("java.lang.Long")){
            long fieldValue = f.getLong(obj);
            return fieldValue >= Long.parseLong(value);
        }
        else if(typeName.equals("float") || typeName.equals("java.lang.Float")){
            float fieldValue = f.getFloat(obj);
            return fieldValue >= Float.parseFloat(value);
        }
        else if(typeName.equals("double") || typeName.equals("java.lang.Double")){
            double fieldValue = f.getDouble(obj);
            return fieldValue >= Double.parseDouble(value);
        }
        else{
            return false;
        }
    }
    
    private boolean lessThan(Field f, String value) throws Exception {
        return !greaterThanEquals(f, value);
    }
    
    private boolean lessThanEquals(Field f, String value) throws Exception {
        return !greaterThan(f, value);
    }
    
    private boolean isNull(Object o){
        return o==null ? true : false;
    }
    
    private boolean notNull(Object o){
        return !isNull(o);
    }
    
}
