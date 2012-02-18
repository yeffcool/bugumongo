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

import com.bugull.mongo.BuguDao;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache(Map) contains dao instance.
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class DaoCache {
    
    private static DaoCache instance = new DaoCache();
    
    private Map<String, BuguDao> cache;
    
    private DaoCache(){
        cache = new ConcurrentHashMap<String, BuguDao>();
    }
    
    public static DaoCache getInstance(){
        return instance;
    }
    
    public BuguDao get(Class<?> clazz){
        BuguDao dao = null;
        String name = clazz.getName();
        if(cache.containsKey(name)){
            dao = cache.get(name);
        }else{
            dao = new BuguDao(clazz);
            cache.put(name, dao);
        }
        return dao;
    }
    
}
