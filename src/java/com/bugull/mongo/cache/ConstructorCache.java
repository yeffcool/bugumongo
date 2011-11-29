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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class ConstructorCache {
    
    private final static Logger logger = Logger.getLogger(ConstructorCache.class);
    
    private static ConstructorCache instance = new ConstructorCache();
    
    private Map<String, Constructor> cache;
    
    private ConstructorCache(){
        cache = new ConcurrentHashMap<String, Constructor>();
    }
    
    public static ConstructorCache getInstance(){
        return instance;
    }
    
    private Constructor get(Class<?> clazz){
        Constructor cons = null;
        String name = clazz.getName();
        if(cache.containsKey(name)){
            cons = cache.get(name);
        }else{
            Class[] types = null;
            try {
                cons = clazz.getConstructor(types);
            } catch (NoSuchMethodException ex) {
                logger.error(ex.getMessage());
            } catch (SecurityException ex) {
                logger.error(ex.getMessage());
            }
            cache.put(name, cons);
        }
        return cons;
    }
    
    public Object create(Class<?> clazz){
        Object obj = null;
        Constructor cons = get(clazz);
        Object[] args = null;
        try {
            obj = cons.newInstance(args);
        } catch (InstantiationException ex) {
            logger.error(ex.getMessage());
        } catch (IllegalAccessException ex) {
            logger.error(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error(ex.getMessage());
        } catch (InvocationTargetException ex) {
            logger.error(ex.getMessage());
        }
        return obj;
    }
    
}
