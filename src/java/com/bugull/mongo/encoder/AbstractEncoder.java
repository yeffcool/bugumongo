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
import com.bugull.mongo.mapper.FieldUtil;
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
        Object objValue = FieldUtil.get(obj, field);
        if(objValue == null){
            value = null;
        }
        else{
            Class<?> type = field.getType();
            try{
                if(type.isArray()){
                    setArrayValue(type.getComponentType(), objValue);
                }else{
                    setValue(type, objValue);
                }
            }catch(Exception ex){
                logger.error(ex.getMessage());
            }
        }
    }
    
    @Override
    public boolean isNullField(){
        return value == null;
    }
    
    private void setArrayValue(Class type, Object objValue) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        int len = Array.getLength(objValue);
        if(DataType.isString(type)){
            String[] arr = new String[len];
            for(int i=0; i<len; i++){
                arr[i] = Array.get(objValue, i).toString();
            }
            value = arr;
        }
        else if(DataType.isInteger(type) || DataType.isIntegerObject(type)){
            int[] arr = new int[len];
            for(int i=0; i<len; i++){
                String s = Array.get(objValue, i).toString();
                arr[i] = Integer.parseInt(s);
            }
            value = arr;
        }
        else if(DataType.isLong(type) || DataType.isLongObject(type)){
            long[] arr = new long[len];
            for(int i=0; i<len; i++){
                String s = Array.get(objValue, i).toString();
                arr[i] = Long.parseLong(s);
            }
            value = arr;
        }
        else if(DataType.isShort(type) || DataType.isShortObject(type)){
            short[] arr = new short[len];
            for(int i=0; i<len; i++){
                String s = Array.get(objValue, i).toString();
                arr[i] = Short.parseShort(s);
            }
            value = arr;
        }
        else if(DataType.isFloat(type) || DataType.isFloatObject(type)){
            float[] arr = new float[len];
            for(int i=0; i<len; i++){
                String s = Array.get(objValue, i).toString();
                arr[i] = Float.parseFloat(s);
            }
            value = arr;
        }
        else if(DataType.isDouble(type) || DataType.isDoubleObject(type)){
            double[] arr = new double[len];
            for(int i=0; i<len; i++){
                String s = Array.get(objValue, i).toString();
                arr[i] = Double.parseDouble(s);
            }
            value = arr;
        }
        else if(DataType.isBoolean(type) || DataType.isBooleanObject(type)){
            boolean[] arr = new boolean[len];
            for(int i=0; i<len; i++){
                String s = Array.get(objValue, i).toString();
                arr[i] = Boolean.parseBoolean(s);
            }
            value = arr;
        }
        else if(DataType.isChar(type) || DataType.isCharObject(type)){
            String[] arr = new String[len];
            for(int i=0; i<len; i++){
                arr[i] = Array.get(objValue, i).toString();
            }
            value = arr;
        }
        else if(DataType.isDate(type)){
            Date[] arr = new Date[len];
            for(int i=0; i<len; i++){
                arr[i] = (Date)(Array.get(objValue, i));
            }
            value = arr;
        }
        else if(DataType.isTimestamp(type)){
            Timestamp[] arr = new Timestamp[len];
            for(int i=0; i<len; i++){
                arr[i] = (Timestamp)(Array.get(objValue, i));
            }
            value = arr;
        }
        else{
            value = objValue;  //other object array, with @EmbedList or @RefList
        }
    }
    
    private void setValue(Class type, Object objValue) throws IllegalArgumentException, IllegalAccessException{
        if(DataType.isInteger(type)){
            value = field.getInt(obj);
        }
        else if(DataType.isLong(type)){
            value = field.getLong(obj);
        }
        else if(DataType.isShort(type)){
            value = field.getShort(obj);
        }
        else if(DataType.isFloat(type)){
            value = field.getFloat(obj);
        }
        else if(DataType.isDouble(type)){
            value = field.getDouble(obj);
        }
        else if(DataType.isBoolean(type)){
            value = field.getBoolean(obj);
        }
        else if(DataType.isChar(type)){
            value = String.valueOf(field.getChar(obj));
        }
        else{
            value = objValue;  //List, Set, Map, Date, Timestamp, Integer, Long, Float, Double, Boolean and other object
        }
    }
    
}
