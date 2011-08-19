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

package com.bugull.mongo.lucene.backend;

import com.bugull.mongo.lucene.annotations.Compare;
import java.lang.reflect.Field;
import org.apache.log4j.Logger;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class CompareChecker {
    
    private final static Logger logger = Logger.getLogger(CompareChecker.class);
    
    private Object obj;
    
    public CompareChecker(Object obj){
        this.obj = obj;
    }
    
    public boolean isFit(Field f, Compare compare, String value){
        boolean fit = false;
        try{
            switch(compare){
                case IS_EQUALS:
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
        }catch(Exception e){
            logger.error(e.getMessage());
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
