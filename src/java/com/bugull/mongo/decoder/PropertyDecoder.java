/*
 * Copyright (c) www.bugull.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bugull.mongo.decoder;

import com.bugull.mongo.annotations.Default;
import com.bugull.mongo.annotations.Property;
import com.bugull.mongo.mapper.DataType;
import com.mongodb.DBObject;
import java.lang.reflect.Field;
import java.sql.Timestamp;
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
@SuppressWarnings("unchecked")
public class PropertyDecoder extends AbstractDecoder{
    
    private final static Logger logger = Logger.getLogger(PropertyDecoder.class);
    
    public PropertyDecoder(Field field, DBObject dbo){
        super(field);
        String fieldName = field.getName();
        Property property = field.getAnnotation(Property.class);
        if(property != null){
            String name = property.name();
            if(!name.equals(Default.NAME)){
                fieldName = name;
            }
        }
        value = dbo.get(fieldName);
    }
    
    @Override
    public void decode(Object obj){
        Class<?> type = field.getType();
        try{
            if(type.isArray()){
                decodeArray(obj, (ArrayList)value, type.getComponentType());
            }else{
                decodePrimitive(obj, type);
            }
        }catch(IllegalArgumentException ex){
            logger.error("Something is wrong when parse the field's value", ex);
        }catch(IllegalAccessException ex){
            logger.error("Something is wrong when parse the field's value", ex);
        }
    }
    
    private void decodeArray(Object obj, ArrayList list, Class type) throws IllegalArgumentException, IllegalAccessException{
        int size = list.size();
        if(DataType.isString(type)){
            String[] arr = new String[size];
            for(int i=0; i<size; i++){
                arr[i] = list.get(i).toString();
            }
            field.set(obj, arr);
        }
        else if(DataType.isInteger(type)){
            int[] arr = new int[size];
            for(int i=0; i<size; i++){
                arr[i] = Integer.parseInt(list.get(i).toString());
            }
            field.set(obj, arr);
        }
        else if(DataType.isIntegerObject(type)){
            Integer[] arr = new Integer[size];
            for(int i=0; i<size; i++){
                arr[i] = Integer.parseInt(list.get(i).toString());
            }
            field.set(obj, arr);
        }
        else if(DataType.isLong(type)){
            long[] arr = new long[size];
            for(int i=0; i<size; i++){
                arr[i] = Long.parseLong(list.get(i).toString());
            }
            field.set(obj, arr);
        }
        else if(DataType.isLongObject(type)){
            Long[] arr = new Long[size];
            for(int i=0; i<size; i++){
                arr[i] = Long.parseLong(list.get(i).toString());
            }
            field.set(obj, arr);
        }
        else if(DataType.isShort(type)){
            short[] arr = new short[size];
            for(int i=0; i<size; i++){
                arr[i] = Short.parseShort(list.get(i).toString());
            }
            field.set(obj, arr);
        }
        else if(DataType.isShortObject(type)){
            Short[] arr = new Short[size];
            for(int i=0; i<size; i++){
                arr[i] = Short.parseShort(list.get(i).toString());
            }
            field.set(obj, arr);
        }
        else if(DataType.isFloat(type)){
            float[] arr = new float[size];
            for(int i=0; i<size; i++){
                arr[i] = Float.parseFloat(list.get(i).toString());
            }
            field.set(obj, arr);
        }
        else if(DataType.isFloatObject(type)){
            Float[] arr = new Float[size];
            for(int i=0; i<size; i++){
                arr[i] = Float.parseFloat(list.get(i).toString());
            }
            field.set(obj, arr);
        }
        else if(DataType.isDouble(type)){
            double[] arr = new double[size];
            for(int i=0; i<size; i++){
                arr[i] = Double.parseDouble(list.get(i).toString());
            }
            field.set(obj, arr);
        }
        else if(DataType.isDoubleObject(type)){
            Double[] arr = new Double[size];
            for(int i=0; i<size; i++){
                arr[i] = Double.parseDouble(list.get(i).toString());
            }
            field.set(obj, arr);
        }
        else if(DataType.isBoolean(type)){
            boolean[] arr = new boolean[size];
            for(int i=0; i<size; i++){
                arr[i] = Boolean.parseBoolean(list.get(i).toString());
            }
            field.set(obj, arr);
        }
        else if(DataType.isBooleanObject(type)){
            Boolean[] arr = new Boolean[size];
            for(int i=0; i<size; i++){
                arr[i] = Boolean.parseBoolean(list.get(i).toString());
            }
            field.set(obj, arr);
        }
        else if(DataType.isChar(type)){
            char[] arr = new char[size];
            for(int i=0; i<size; i++){
                arr[i] = list.get(i).toString().charAt(0);
            }
            field.set(obj, arr);
        }
        else if(DataType.isCharObject(type)){
            Character[] arr = new Character[size];
            for(int i=0; i<size; i++){
                arr[i] = list.get(i).toString().charAt(0);
            }
            field.set(obj, arr);
        }
        else if(DataType.isDate(type)){
            Date[] arr = new Date[size];
            for(int i=0; i<size; i++){
                arr[i] = (Date)list.get(i);
            }
            field.set(obj, arr);
        }
        else if(DataType.isTimestamp(type)){
            Timestamp[] arr = new Timestamp[size];
            for(int i=0; i<size; i++){
                arr[i] = (Timestamp)list.get(i);
            }
            field.set(obj, arr);
        }
    }
    
    private void decodePrimitive(Object obj, Class type) throws IllegalArgumentException, IllegalAccessException{
        if(DataType.isInteger(type)){
            field.setInt(obj, Integer.parseInt(value.toString()));
        }
        else if(DataType.isLong(type)){
            field.setLong(obj, Long.parseLong(value.toString()));
        }
        else if(DataType.isShort(type)){
            field.setShort(obj, Short.parseShort(value.toString()));
        }
        else if(DataType.isFloat(type)){
            field.setFloat(obj, Float.parseFloat(value.toString()));
        }
        else if(DataType.isDouble(type)){
            field.setDouble(obj, Double.parseDouble(value.toString()));
        }
        else if(DataType.isBoolean(type)){
            field.setBoolean(obj, Boolean.parseBoolean(value.toString()));
        }        
        else if(DataType.isChar(type)){
            field.setChar(obj, value.toString().charAt(0));
        }
        else if(DataType.isSet(type)){
            List list = (ArrayList)value;
            Set set = new HashSet(list);
            field.set(obj, set);
        }
        else if(DataType.isTimestamp(type)){
            Date date = (Date)value;
            Timestamp ts = new Timestamp(date.getTime());
            field.set(obj, ts);
        }
        //When value is number, it's default to Integer and Double, must cast to Short and Float
        else if(DataType.isShortObject(type)){
            field.set(obj, Short.valueOf(value.toString()));
        }
        else if(DataType.isFloatObject(type)){
            field.set(obj, Float.valueOf(value.toString()));
        }
        else{
            field.set(obj, value);  //List, Map, Date, Integer, Long, Double and so on
        }
    }
    
}
