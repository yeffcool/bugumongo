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

package com.bugull.mongo.encoder;

import com.bugull.mongo.mapper.DataType;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.Date;
import org.apache.log4j.Logger;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public abstract class AbstractEncoder implements Encoder{
    
    private final static Logger logger = Logger.getLogger(AbstractEncoder.class);
    
    protected Object obj;
    protected Field field;
    protected Object value;
    
    protected AbstractEncoder(Object obj, Field field){
        this.obj = obj;
        this.field = field;
        Class<?> type = field.getType();
        try{
            if(type.isArray()){
                setArrayValue(type.getComponentType().getName());
            }else{
                setValue(type.getName());
            }
        }catch(Exception e){
            logger.error(e.getMessage());
        }
    }
    
    @Override
    public boolean isNullField(){
        return value == null;
    }
    
    private void setArrayValue(String typeName) throws Exception {
        Object o = field.get(obj);
        int len = Array.getLength(o);
        if(DataType.isString(typeName)){
            String[] arr = new String[len];
            for(int i=0; i<len; i++){
                arr[i] = Array.get(o, i).toString();
            }
            value = arr;
        }
        else if(DataType.isInteger(typeName)){
            int[] arr = new int[len];
            for(int i=0; i<len; i++){
                arr[i] = Array.getInt(o, i);
            }
            value = arr;
        }
        else if(DataType.isLong(typeName)){
            long[] arr = new long[len];
            for(int i=0; i<len; i++){
                arr[i] = Array.getLong(o, i);
            }
            value = arr;
        }
        else if(DataType.isShort(typeName)){
            short[] arr = new short[len];
            for(int i=0; i<len; i++){
                arr[i] = Array.getShort(o, i);
            }
            value = arr;
        }
        else if(DataType.isFloat(typeName)){
            float[] arr = new float[len];
            for(int i=0; i<len; i++){
                arr[i] = Array.getFloat(o, i);
            }
            value = arr;
        }
        else if(DataType.isDouble(typeName)){
            double[] arr = new double[len];
            for(int i=0; i<len; i++){
                arr[i] = Array.getDouble(o, i);
            }
            value = arr;
        }
        else if(DataType.isBoolean(typeName)){
            boolean[] arr = new boolean[len];
            for(int i=0; i<len; i++){
                arr[i] = Array.getBoolean(o, i);
            }
            value = arr;
        }
        else if(DataType.isChar(typeName)){
            String[] arr = new String[len];
            for(int i=0; i<len; i++){
                arr[i] = String.valueOf(Array.getChar(o, i));
            }
            value = arr;
        }
        else if(DataType.isDate(typeName)){
            Date[] arr = new Date[len];
            for(int i=0; i<len; i++){
                arr[i] = (Date)(Array.get(o, i));
            }
            value = arr;
        }
        else if(DataType.isTimestamp(typeName)){
            Timestamp[] arr = new Timestamp[len];
            for(int i=0; i<len; i++){
                arr[i] = (Timestamp)(Array.get(o, i));
            }
            value = arr;
        }
    }
    
    private void setValue(String typeName) throws Exception {
        if(DataType.isInteger(typeName)){
            value = field.getInt(obj);
        }
        else if(DataType.isLong(typeName)){
            value = field.getLong(obj);
        }
        else if(DataType.isShort(typeName)){
            value = field.getShort(obj);
        }
        else if(DataType.isFloat(typeName)){
            value = field.getFloat(obj);
        }
        else if(DataType.isDouble(typeName)){
            value = field.getDouble(obj);
        }
        else if(DataType.isBoolean(typeName)){
            value = field.getBoolean(obj);
        }
        else if(DataType.isChar(typeName)){
            value = String.valueOf(field.getChar(obj));
        }
        else{  //List, Set, Map, Date, Timestamp, and other Object
            value = field.get(obj);
        }
    }
    
}
