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

import com.bugull.mongo.misc.InternalDao;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Cache(Map) contains dao instance.
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
@SuppressWarnings("unchecked")
public class DaoCache {
    
    private final ConcurrentMap<String, InternalDao<?>> cache = new ConcurrentHashMap<String, InternalDao<?>>();
    
    private static class Holder {
        final static DaoCache instance = new DaoCache();
    } 
    
    public static DaoCache getInstance(){
        return Holder.instance;
    }
    
    public <T> InternalDao<T> get(Class<T> clazz){
        String name = clazz.getName();
        InternalDao<?> dao = cache.get(name);
        if(dao != null){
            return (InternalDao<T>)dao;
        }
        
        dao = new InternalDao<T>(clazz);
        InternalDao<?> temp = cache.putIfAbsent(name, dao);
        if(temp != null){
            return (InternalDao<T>)temp;
        }else{
            return (InternalDao<T>)dao;
        }
    }
    
}
