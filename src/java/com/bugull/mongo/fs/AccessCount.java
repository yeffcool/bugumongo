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

package com.bugull.mongo.fs;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Count the number of threads that accessing file in GridFS.
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class AccessCount {
    
    private static AccessCount instance = new AccessCount();
    
    private final ConcurrentMap<String, Integer> map = new ConcurrentHashMap<String, Integer>();
    
    private AccessCount(){
        
    }
    
    public static AccessCount getInstance(){
        return instance;
    }
    
    public int getCount(String resourceName){
        int count = 0;
        Integer v = map.get(resourceName);
        if(v != null){
            count = v;
        }
        return count;
    }
    
    public synchronized void increaseCount(String resourceName){
        int count = this.getCount(resourceName) + 1;
        map.put(resourceName, count);
    }
    
    public synchronized void descreaseCount(String resourceName){
        int count = this.getCount(resourceName) - 1;
        if(count < 0){
            count = 0;
        }
        map.put(resourceName, count);
    }

}
