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

package com.bugull.mongo.cache;

import com.bugull.mongo.annotations.Id;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;

/**
 * Cache(Map) contains entity classes' Fields
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class FieldsCache {
    
    private final static Logger logger = Logger.getLogger(FieldsCache.class);
    
    private static FieldsCache instance = new FieldsCache();
    
    private Map<String, Field[]> cache;
    
    private FieldsCache(){
        cache = new ConcurrentHashMap<String, Field[]>();
    }
    
    public static FieldsCache getInstance(){
        return instance;
    }
    
    /**
     * get all declared and inherited field
     * @param clazz
     * @return 
     */
    private Field[] getAllFields(Class<?> clazz){
        List<Field> allFields = new ArrayList<Field>();
        allFields.addAll(filterFields(clazz.getDeclaredFields()));
        Class parent = clazz.getSuperclass();
        while((parent != null) && (parent != Object.class)){
            allFields.addAll(filterFields(parent.getDeclaredFields()));
            parent = parent.getSuperclass();
        }
        return allFields.toArray(new Field[allFields.size()]);
    }
    
    /**
     * filter the static filed, and set all the filed to alccessibe=true
     * @param fields
     * @return 
     */
    private List<Field> filterFields(Field[] fields){
        List<Field> result = new ArrayList<Field>();
        for(Field field : fields){
            if (!Modifier.isStatic(field.getModifiers())){
                field.setAccessible(true);
                result.add(field);
            }
        }
        return result;
    }
    
    public Field[] get(Class<?> clazz){
        Field[] fields = null;
        String name = clazz.getName();
        if(cache.containsKey(name)){
            fields = cache.get(name);
        }else{
            fields = getAllFields(clazz);
            cache.put(name, fields);
        }
        return fields;
    }
    
    public String getIdFieldName(Class<?> clazz){
        String name = null;
        Field[] fields = get(clazz);
        for(Field f : fields){
            if(f.getAnnotation(Id.class) != null){
                name = f.getName();
                break;
            }
        }
        if(name == null){
            logger.error(clazz.getName() + " does not contain Id field.");
        }
        return name;
    }
    
    public Field getField(Class<?> clazz, String fieldName){
        Field field = null;
        Field[] fields = get(clazz);
        for(Field f : fields){
            if(f.getName().equals(fieldName)){
                field = f;
                break;
            }
        }
        if(field == null){
            logger.error(clazz.getName() + " does not contain field " + fieldName);
        }
        return field;
    }
    
}
