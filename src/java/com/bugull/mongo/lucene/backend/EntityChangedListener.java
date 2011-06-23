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

package com.bugull.mongo.lucene.backend;

import com.bugull.mongo.BuguEntity;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class EntityChangedListener {
    
    public void entityInsert(BuguEntity obj){
        IndexFilterChecker checker = new IndexFilterChecker(obj);
        if(checker.needIndex()){
            IndexInsertThread thread = new IndexInsertThread(obj);
            new Thread(thread).start();
        }
    }
    
    public void entityUpdate(BuguEntity obj){
        IndexFilterChecker checker = new IndexFilterChecker(obj);
        if(checker.needIndex()){
            IndexUpdateThread thread = new IndexUpdateThread(obj);
            new Thread(thread).start();
        }else{
            entityRemove(obj.getClass(), obj.getId());
        }
        
    }
    
    public void entityRemove(Class<?> clazz, String id){
        IndexRemoveThread thread = new IndexRemoveThread(clazz, id);
        new Thread(thread).start();
    }
    
}
