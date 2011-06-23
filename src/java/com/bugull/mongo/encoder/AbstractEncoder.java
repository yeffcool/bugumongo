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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
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
                setArrayValue(type.getComponentType());
            }else{
                setPrimitiveValue(type.getName());
            }
        }catch(Exception e){
            logger.error(e.getMessage());
        }
    }
    
    @Override
    public boolean isNullField(){
        return value == null;
    }
    
    private void setArrayValue(Class<?> dataType) throws Exception {
        Object o = field.get(obj);
        int len = Array.getLength(o);
        String typeName = dataType.getName();
        if(typeName.equals("java.lang.String")){
            String[] arr = new String[len];
            for(int i=0; i<len; i++){
                arr[i] = Array.get(o, i).toString();
            }
            value = arr;
        }
        else if(typeName.equals("int") || typeName.equals("java.lang.Integer")){
            int[] arr = new int[len];
            for(int i=0; i<len; i++){
                arr[i] = Array.getInt(o, i);
            }
            value = arr;
        }
        else if(typeName.equals("long") || typeName.equals("java.lang.Long")){
            long[] arr = new long[len];
            for(int i=0; i<len; i++){
                arr[i] = Array.getLong(o, i);
            }
            value = arr;
        }
        else if(typeName.equals("float") || typeName.equals("java.lang.Float")){
            float[] arr = new float[len];
            for(int i=0; i<len; i++){
                arr[i] = Array.getFloat(o, i);
            }
            value = arr;
        }
        else if(typeName.equals("double") || typeName.equals("java.lang.Double")){
            double[] arr = new double[len];
            for(int i=0; i<len; i++){
                arr[i] = Array.getDouble(o, i);
            }
            value = arr;
        }
        else if(typeName.equals("boolean") || typeName.equals("java.lang.Boolean")){
            boolean[] arr = new boolean[len];
            for(int i=0; i<len; i++){
                arr[i] = Array.getBoolean(o, i);
            }
            value = arr;
        }
        else if(typeName.equals("char") || typeName.equals("java.lang.Character")){
            String[] arr = new String[len];
            for(int i=0; i<len; i++){
                arr[i] = String.valueOf(Array.getChar(o, i));
            }
            value = arr;
        }
        else if(typeName.equals("java.util.Date")){
            Date[] arr = new Date[len];
            for(int i=0; i<len; i++){
                arr[i] = (Date)(Array.get(o, i));
            }
            value = arr;
        }
    }
    
    private void setPrimitiveValue(String typeName) throws Exception {
        if(typeName.equals("int") || typeName.equals("java.lang.Integer")){
            value = field.getInt(obj);
        }
        else if(typeName.equals("long") || typeName.equals("java.lang.Long")){
            value = field.getLong(obj);
        }
        else if(typeName.equals("float") || typeName.equals("java.lang.Float")){
            value = field.getFloat(obj);
        }
        else if(typeName.equals("double") || typeName.equals("java.lang.Double")){
            value = field.getDouble(obj);
        }
        else if(typeName.equals("boolean") || typeName.equals("java.lang.Boolean")){
            value = field.getBoolean(obj);
        }
        else if(typeName.equals("char") || typeName.equals("java.lang.Character")){
            value = String.valueOf(field.getChar(obj));
        }
        else{
            value = field.get(obj);
        }
    }
    
}
