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

package com.bugull.mongo.mapper;

import com.bugull.mongo.cache.FieldsCache;
import com.bugull.mongo.face.BuguFace;
import java.lang.reflect.Field;
import org.apache.log4j.Logger;

/**
 * Utility class for operating object's fields.
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class FieldUtil {
    
    private final static Logger logger = Logger.getLogger(FieldUtil.class);
    
    public static Object get(Object obj, Field f){
        Object value = null;
        try {
            value = f.get(obj);
        } catch (IllegalArgumentException ex) {
            logger.error("Can not get the field's value", ex);
        } catch (IllegalAccessException ex) {
            logger.error("Can not get the field's value", ex);
        }
        return value;
    }
    
    public static void set(Object obj, Field f, Object value){
        try{
            f.set(obj, value);
        }catch(IllegalArgumentException ex){
            logger.error("Can not set the field's value", ex);
        }catch(IllegalAccessException ex){
            logger.error("Can not set the field's value", ex);
        }
    }
    
    /**
     * Copy an object's properties to another object. 
     * <p>Note: The source and target object can't be null.</p>
     * @param src
     * @param target 
     */
    public static void copy(Object src, Object target){
        if(src==null || target==null){
            return;
        }
        Field[] fields = FieldsCache.getInstance().get(src.getClass());
        for(Field f : fields){
            Object value = get(src, f);
            set(target, f, value);
        }
    }
    
    public static Class<?> getRealType(Field field){
        Class<?> clazz = field.getType();
        return getRealType(clazz);
    }
    
    public static Class<?> getRealType(Class<?> clazz){
        Class cls = clazz;
        if(clazz.isInterface()){
            cls = BuguFace.getIntance().getImplementation(clazz);
            if(cls == null){
                logger.error("The implementation of interface " + clazz.toString() + " is not specified.");
            }
        }
        return cls;
    }
    
}
