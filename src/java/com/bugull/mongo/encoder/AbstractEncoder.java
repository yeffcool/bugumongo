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
        try{
            Object objValue = field.get(obj);
            if(objValue == null){
                value = null;
            }else{
                Class<?> type = field.getType();
                if(type.isArray()){
                    setArrayValue(type.getComponentType().getName(), objValue);
                }else{
                    setValue(type.getName(), objValue);
                }
            }
        }catch(Exception e){
            logger.error(e.getMessage());
        }
    }
    
    @Override
    public boolean isNullField(){
        return value == null;
    }
    
    private void setArrayValue(String typeName, Object objValue) throws Exception {
        int len = Array.getLength(objValue);
        if(DataType.isString(typeName)){
            String[] arr = new String[len];
            for(int i=0; i<len; i++){
                arr[i] = Array.get(objValue, i).toString();
            }
            value = arr;
        }
        else if(DataType.isInteger(typeName)){
            int[] arr = new int[len];
            for(int i=0; i<len; i++){
                arr[i] = Array.getInt(objValue, i);
            }
            value = arr;
        }
        else if(DataType.isLong(typeName)){
            long[] arr = new long[len];
            for(int i=0; i<len; i++){
                arr[i] = Array.getLong(objValue, i);
            }
            value = arr;
        }
        else if(DataType.isShort(typeName)){
            short[] arr = new short[len];
            for(int i=0; i<len; i++){
                arr[i] = Array.getShort(objValue, i);
            }
            value = arr;
        }
        else if(DataType.isFloat(typeName)){
            float[] arr = new float[len];
            for(int i=0; i<len; i++){
                arr[i] = Array.getFloat(objValue, i);
            }
            value = arr;
        }
        else if(DataType.isDouble(typeName)){
            double[] arr = new double[len];
            for(int i=0; i<len; i++){
                arr[i] = Array.getDouble(objValue, i);
            }
            value = arr;
        }
        else if(DataType.isBoolean(typeName)){
            boolean[] arr = new boolean[len];
            for(int i=0; i<len; i++){
                arr[i] = Array.getBoolean(objValue, i);
            }
            value = arr;
        }
        else if(DataType.isChar(typeName)){
            String[] arr = new String[len];
            for(int i=0; i<len; i++){
                arr[i] = String.valueOf(Array.getChar(objValue, i));
            }
            value = arr;
        }
        else if(DataType.isDate(typeName)){
            Date[] arr = new Date[len];
            for(int i=0; i<len; i++){
                arr[i] = (Date)(Array.get(objValue, i));
            }
            value = arr;
        }
        else if(DataType.isTimestamp(typeName)){
            Timestamp[] arr = new Timestamp[len];
            for(int i=0; i<len; i++){
                arr[i] = (Timestamp)(Array.get(objValue, i));
            }
            value = arr;
        }
        else{
            value = objValue;
        }
    }
    
    private void setValue(String typeName, Object objValue) throws Exception {
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
            value = objValue;
        }
    }
    
}
