/**
 * Copyright (c) www.bugull.com
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
public class PropertyDecoder extends AbstractDecoder{
    
    private final static Logger logger = Logger.getLogger(PropertyDecoder.class);
    
    public PropertyDecoder(Field field, DBObject dbo){
        super(field, dbo);
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
                decodeArray(obj, (ArrayList)value, type.getComponentType().getName());
            }else{
                decodePrimitive(obj, type.getName());
            }
        }catch(Exception e){
            logger.error(e.getMessage());
        }
    }
    
    private void decodeArray(Object obj, ArrayList list, String typeName) throws Exception{
        int size = list.size();
        if(DataType.isString(typeName)){
            String[] arr = new String[size];
            for(int i=0; i<size; i++){
                arr[i] = list.get(i).toString();
            }
            field.set(obj, arr);
        }
        else if(DataType.isInteger(typeName)){
            int[] arr = new int[size];
            for(int i=0; i<size; i++){
                arr[i] = Integer.parseInt(list.get(i).toString());
            }
            field.set(obj, arr);
        }
        else if(DataType.isLong(typeName)){
            long[] arr = new long[size];
            for(int i=0; i<size; i++){
                arr[i] = Long.parseLong(list.get(i).toString());
            }
            field.set(obj, arr);
        }
        else if(DataType.isShort(typeName)){
            short[] arr = new short[size];
            for(int i=0; i<size; i++){
                arr[i] = Short.parseShort(list.get(i).toString());
            }
            field.set(obj, arr);
        }
        else if(DataType.isFloat(typeName)){
            float[] arr = new float[size];
            for(int i=0; i<size; i++){
                arr[i] = Float.parseFloat(list.get(i).toString());
            }
            field.set(obj, arr);
        }
        else if(DataType.isDouble(typeName)){
            double[] arr = new double[size];
            for(int i=0; i<size; i++){
                arr[i] = Double.parseDouble(list.get(i).toString());
            }
            field.set(obj, arr);
        }
        else if(DataType.isBoolean(typeName)){
            boolean[] arr = new boolean[size];
            for(int i=0; i<size; i++){
                arr[i] = Boolean.parseBoolean(list.get(i).toString());
            }
            field.set(obj, arr);
        }
        else if(DataType.isChar(typeName)){
            char[] arr = new char[size];
            for(int i=0; i<size; i++){
                arr[i] = list.get(i).toString().charAt(0);
            }
            field.set(obj, arr);
        }
        else if(DataType.isDate(typeName)){
            Date[] arr = new Date[size];
            for(int i=0; i<size; i++){
                arr[i] = (Date)list.get(i);
            }
            field.set(obj, arr);
        }
        else if(DataType.isTimestamp(typeName)){
            Timestamp[] arr = new Timestamp[size];
            for(int i=0; i<size; i++){
                arr[i] = (Timestamp)list.get(i);
            }
            field.set(obj, arr);
        }
    }
    
    private void decodePrimitive(Object obj, String typeName) throws Exception {
        if(DataType.isInteger(typeName)){
            field.setInt(obj, Integer.parseInt(value.toString()));
        }
        else if(DataType.isLong(typeName)){
            field.setLong(obj, Long.parseLong(value.toString()));
        }
        else if(DataType.isShort(typeName)){
            field.setShort(obj, Short.parseShort(value.toString()));
        }
        else if(DataType.isFloat(typeName)){
            field.setFloat(obj, Float.parseFloat(value.toString()));
        }
        else if(DataType.isDouble(typeName)){
            field.setDouble(obj, Double.parseDouble(value.toString()));
        }
        else if(DataType.isBoolean(typeName)){
            field.setBoolean(obj, Boolean.parseBoolean(value.toString()));
        }        
        else if(DataType.isChar(typeName)){
            field.setChar(obj, value.toString().charAt(0));
        }
        else if(DataType.isSet(typeName)){
            List list = (ArrayList)value;
            Set set = new HashSet(list);
            field.set(obj, set);
        }
        else if(DataType.isTimestamp(typeName)){
            Date date = (Date)value;
            Timestamp ts = new Timestamp(date.getTime());
            field.set(obj, ts);
        }
        else{
            field.set(obj, value);
        }
    }
    
}
