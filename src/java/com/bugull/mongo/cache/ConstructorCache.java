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

package com.bugull.mongo.cache;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.log4j.Logger;

/**
 * Cache(Map) contains entity classes' constructor, for performance purporse.
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
@SuppressWarnings("unchecked")
public class ConstructorCache {
    
    private final static Logger logger = Logger.getLogger(ConstructorCache.class);
    
    private final ConcurrentMap<String, Constructor<?>> cache = new ConcurrentHashMap<String, Constructor<?>>();
    
    private static class Holder {
        final static ConstructorCache instance = new ConstructorCache();
    } 
    
    public static ConstructorCache getInstance(){
        return Holder.instance;
    }
    
    private <T> Constructor<T> get(Class<T> clazz){
        String name = clazz.getName();
        Constructor<?> cons = cache.get(name);
        if(cons != null){
            return (Constructor<T>)cons;
        }
        
        Class[] types = null;
        try {
            cons = clazz.getConstructor(types);
        } catch (NoSuchMethodException ex) {
            logger.error("Something is wrong when getting the constructor", ex);
        } catch (SecurityException ex) {
            logger.error("Something is wrong when getting the constructor", ex);
        }
        Constructor<?> temp = cache.putIfAbsent(name, cons);
        if(temp != null){
            return (Constructor<T>)temp;
        }else{
            return (Constructor<T>)cons;
        }
    }
    
    public <T> T create(Class<T> clazz){
        T obj = null;
        Constructor<T> cons = get(clazz);
        Object[] args = null;
        try {
            obj = cons.newInstance(args);
        } catch (InstantiationException ex) {
            logger.error("Something is wrong when create the new instance", ex);
        } catch (IllegalAccessException ex) {
            logger.error("Something is wrong when create the new instance", ex);
        } catch (IllegalArgumentException ex) {
            logger.error("Something is wrong when create the new instance", ex);
        } catch (InvocationTargetException ex) {
            logger.error("Something is wrong when create the new instance", ex);
        }
        return obj;
    }
    
}
