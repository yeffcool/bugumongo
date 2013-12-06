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

package com.bugull.mongo.lucene.backend;

import com.bugull.mongo.cache.FieldsCache;
import com.bugull.mongo.exception.FieldException;
import com.bugull.mongo.lucene.annotations.*;
import java.lang.reflect.Field;
import org.apache.log4j.Logger;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public final class IndexChecker {
    
    private final static Logger logger = Logger.getLogger(IndexChecker.class);
    
    /**
     * Check one or more filed whether it has index annotation on it.
     * @param clazz the class that field in
     * @param keys the field's name
     * @return 
     */
    public static boolean hasIndexAnnotation(Class<?> clazz, String... keys){
        boolean result = false;
        for(String key : keys){
            //check if the key has "." in it
            int index = key.indexOf(".");
            if(index != -1){
                key = key.substring(0, index);
            }
            Field field = null;
            try{
                field = FieldsCache.getInstance().getField(clazz, key);
            }catch(FieldException ex){
                logger.error(ex.getMessage(), ex);
            }
            if(field.getAnnotation(IndexProperty.class)!=null
                    || field.getAnnotation(IndexEmbed.class)!=null
                    || field.getAnnotation(IndexEmbedList.class)!=null
                    || field.getAnnotation(IndexRef.class)!= null
                    || field.getAnnotation(IndexRefList.class)!= null
                    || field.getAnnotation(IndexRefBy.class)!=null
                    || field.getAnnotation(BoostSwitch.class)!=null
                    || field.getAnnotation(IndexFilter.class)!=null){
                result = true;
            }
            if(result){
                break;
            }
        }
        return result;
    }
    
    /**
     * Check if the clazz need a lucene listener.
     * <p>If it has @Indexed annotation, or, some of it's fields has @IndexRefBy annotation,
     * then it need a lucene listener.</p>
     * 
     * @param clazz
     * @return 
     */
    public static boolean needListener(Class<?> clazz){
        boolean result = false;
        if(clazz.getAnnotation(Indexed.class) != null){
            result = true;
        }
        else{
            Field[] fields = FieldsCache.getInstance().get(clazz);
            for(Field f : fields){
                IndexRefBy irb = f.getAnnotation(IndexRefBy.class);
                if(irb != null){
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

}
