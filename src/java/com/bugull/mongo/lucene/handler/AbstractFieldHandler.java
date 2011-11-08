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

package com.bugull.mongo.lucene.handler;

import com.bugull.mongo.mapper.DataType;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.Date;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public abstract class AbstractFieldHandler implements FieldHandler{
    
    public final static String JOIN = ";";
    
    protected Object obj;
    protected Field field;
    protected String prefix;
    
    protected AbstractFieldHandler(Object obj, Field field, String prefix){
        this.obj = obj;
        this.field = field;
        this.prefix = prefix;
    }
    
    protected String getArrayString(Object value, String typeName){
        StringBuilder sb = new StringBuilder();
        if(DataType.isString(typeName)){
            String[] arr = (String[])value;
            for(String e : arr){
                sb.append(e).append(JOIN);
            }
        }
        else if(DataType.isBoolean(typeName)){
            boolean[] arr = (boolean[])value;
            for(boolean e : arr){
                sb.append(e).append(JOIN);
            }
        }
        else if(DataType.isChar(typeName)){
            char[] arr = (char[])value;
            for(char e : arr){
                sb.append(e).append(JOIN);
            }
        }
        else if(DataType.isInteger(typeName)){
            int[] arr = (int[])value;
            for(int e : arr){
                sb.append(e).append(JOIN);
            }
        }
        else if(DataType.isLong(typeName)){
            long[] arr = (long[])value;
            for(long e : arr){
                sb.append(e).append(JOIN);
            }
        }
        else if(DataType.isShort(typeName)){
            short[] arr = (short[])value;
            for(short e : arr){
                sb.append(e).append(JOIN);
            }
        }
        else if(DataType.isFloat(typeName)){
            float[] arr = (float[])value;
            for(float e : arr){
                sb.append(e).append(JOIN);
            }
        }
        else if(DataType.isDouble(typeName)){
            double[] arr = (double[])value;
            for(double e : arr){
                sb.append(e).append(JOIN);
            }
        }
        else if(DataType.isDate(typeName)){
            Date[] arr = (Date[])value;
            for(Date e : arr){
                sb.append(e.getTime()).append(JOIN);
            }
        }
        else if(DataType.isTimestamp(typeName)){
            Timestamp[] arr = (Timestamp[])value;
            for(Timestamp e : arr){
                sb.append(e.getTime()).append(JOIN);
            }
        }
        return sb.toString();
    }
    
}
