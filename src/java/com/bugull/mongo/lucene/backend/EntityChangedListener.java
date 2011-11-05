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
import com.bugull.mongo.cache.FieldsCache;
import com.bugull.mongo.lucene.BuguIndex;
import com.bugull.mongo.lucene.annotations.IndexRefBy;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class EntityChangedListener {
    
    private ExecutorService executor = BuguIndex.getInstance().getExecutor();
    
    private Class<?> clazz;
    private boolean hasRefBy;
    private RefEntityChangedListener refListener;
    
    public EntityChangedListener(Class<?> clazz){
        this.clazz = clazz;
        Set<Class<?>> refBySet = new HashSet<Class<?>>();
        Field[] fields = FieldsCache.getInstance().get(clazz);
        for(Field f : fields){
            IndexRefBy irb = f.getAnnotation(IndexRefBy.class);
            if(irb != null){
                Class<?>[] cls = irb.value();
                refBySet.addAll(Arrays.asList(cls));
            }
        }
        if(refBySet.size() > 0){
            hasRefBy = true;
            refListener = new RefEntityChangedListener(refBySet);
        }
    }
    
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
            IndexRemoveTask task = new IndexRemoveTask(clazz, obj.getId());
            executor.execute(task);
        }
        //for refBy
        if(hasRefBy){
            refListener.entityChange(clazz, obj.getId());
        }
    }
    
    public void entityRemove(String id){
        IndexRemoveTask task = new IndexRemoveTask(clazz, id);
        executor.execute(task);
        //for refBy
        if(hasRefBy){
            refListener.entityChange(clazz, id);
        }
    }
    
}
