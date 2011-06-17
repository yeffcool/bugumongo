package com.bugull.mongo.decoder;

import com.bugull.mongo.annotations.Property;
import com.mongodb.DBObject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class PropertyDecoder extends AbstractDecoder{
    
    private final static Logger logger = Logger.getLogger(PropertyDecoder.class);
    
    public PropertyDecoder(Field field, DBObject dbo){
        super(field, dbo);
    }
    
    @Override
    public void decode(Object obj){
        Object value = dbo.get(getFieldName());
        Class<?> type = field.getType();
        if(value != null){
            try{
                if(type.isArray()){
                    decodeArray(obj, (ArrayList)value, type.getComponentType().getName());
                }else{
                    decodePrimitive(obj, value, type.getName());
                }
            }catch(Exception e){
                logger.error(e.getMessage());
            }
        }
    }
    
    private void decodeArray(Object obj, ArrayList list, String typeName) throws Exception{
        int size = list.size();
        if(typeName.equals("java.lang.String")){
            String[] arr = new String[size];
            for(int i=0; i<size; i++){
                arr[i] = list.get(i).toString();
            }
            field.set(obj, arr);
        }
        else if(typeName.equals("int") || typeName.equals("java.lang.Integer")){
            int[] arr = new int[size];
            for(int i=0; i<size; i++){
                arr[i] = Integer.parseInt(list.get(i).toString());
            }
            field.set(obj, arr);
        }
        else if(typeName.equals("long") || typeName.equals("java.lang.Long")){
            long[] arr = new long[size];
            for(int i=0; i<size; i++){
                arr[i] = Long.parseLong(list.get(i).toString());
            }
            field.set(obj, arr);
        }
        else if(typeName.equals("float") || typeName.equals("java.lang.Float")){
            float[] arr = new float[size];
            for(int i=0; i<size; i++){
                arr[i] = Float.parseFloat(list.get(i).toString());
            }
            field.set(obj, arr);
        }
        else if(typeName.equals("double") || typeName.equals("java.lang.Double")){
            double[] arr = new double[size];
            for(int i=0; i<size; i++){
                arr[i] = Double.parseDouble(list.get(i).toString());
            }
            field.set(obj, arr);
        }
        else if(typeName.equals("boolean") || typeName.equals("java.lang.Boolean")){
            boolean[] arr = new boolean[size];
            for(int i=0; i<size; i++){
                arr[i] = Boolean.parseBoolean(list.get(i).toString());
            }
            field.set(obj, arr);
        }
        else if(typeName.equals("char") || typeName.equals("java.lang.Character")){
            char[] arr = new char[size];
            for(int i=0; i<size; i++){
                arr[i] = list.get(i).toString().charAt(0);
            }
            field.set(obj, arr);
        }
        else if(typeName.equals("java.util.Date")){
            Date[] arr = new Date[size];
            for(int i=0; i<size; i++){
                arr[i] = (Date)list.get(i);
            }
            field.set(obj, arr);
        }
    }
    
    private void decodePrimitive(Object obj, Object value, String typeName) throws Exception {
        if(typeName.equals("int") || typeName.equals("java.lang.Integer")){
            field.setInt(obj, Integer.parseInt(value.toString()));
        }
        else if(typeName.equals("long") || typeName.equals("java.lang.Long")){
            field.setLong(obj, Long.parseLong(value.toString()));
        }
        else if(typeName.equals("float") || typeName.equals("java.lang.Float")){
            field.setFloat(obj, Float.parseFloat(value.toString()));
        }
        else if(typeName.equals("double") || typeName.equals("java.lang.Double")){
            field.setDouble(obj, Double.parseDouble(value.toString()));
        }
        else if(typeName.equals("boolean") || typeName.equals("java.lang.Boolean")){
            field.setBoolean(obj, Boolean.parseBoolean(value.toString()));
        }        
        else if(typeName.equals("char") || typeName.equals("java.lang.Character")){
            field.setChar(obj, value.toString().charAt(0));
        }
        else if(typeName.equals("java.util.Set")){
            List list = (ArrayList)value;
            Set set = new HashSet(list);
            field.set(obj, set);
        }
        else{
            field.set(obj, value);
        }
    }
    
    private String getFieldName(){
        String fieldName = field.getName();
        Property property = field.getAnnotation(Property.class);
        if(property != null){
            String name = property.name();
            if(!name.equals("")){
                fieldName = name;
            }
        }
        return fieldName;
    }
    
}
