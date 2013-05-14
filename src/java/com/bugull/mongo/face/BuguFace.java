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

package com.bugull.mongo.face;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The specified implementations of entity interfaces.
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class BuguFace {
    
    private static BuguFace instance = new BuguFace();
    
    private Map<Class, Class> implementations;
    
    private BuguFace(){
        implementations = new ConcurrentHashMap<Class, Class>();
    }
    
    public static BuguFace getIntance(){
        return instance;
    }
    
    public void setImplementation(Class face, Class impl){
        implementations.put(face, impl);
    }
    
    public Class getImplementation(Class face){
        return implementations.get(face);
    }

}
