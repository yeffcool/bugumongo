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
import com.bugull.mongo.lucene.BuguIndex;
import java.util.concurrent.ExecutorService;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class EntityChangedListener {
    
    private ExecutorService executor = BuguIndex.getInstance().getExecutor();
    
    public void entityInsert(BuguEntity obj){
        IndexFilterChecker checker = new IndexFilterChecker(obj);
        if(checker.needIndex()){
            IndexInsertTask task = new IndexInsertTask(obj);
            executor.execute(task);
        }
    }
    
    public void entityUpdate(BuguEntity obj){
        IndexFilterChecker checker = new IndexFilterChecker(obj);
        if(checker.needIndex()){
            IndexUpdateTask task = new IndexUpdateTask(obj);
            executor.execute(task);
        }else{
            entityRemove(obj.getClass(), obj.getId());
        }
    }
    
    public void entityRemove(Class<?> clazz, String id){
        IndexRemoveTask task = new IndexRemoveTask(clazz, id);
        executor.execute(task);
    }
    
}
