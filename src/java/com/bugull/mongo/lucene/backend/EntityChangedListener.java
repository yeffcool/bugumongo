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
import com.bugull.mongo.annotations.Id;
import com.bugull.mongo.cache.FieldsCache;
import com.bugull.mongo.lucene.BuguIndex;
import com.bugull.mongo.lucene.annotations.IndexRefBy;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Change the relative lucene index when an entity is changed.
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class EntityChangedListener {
    
    private Class<?> clazz;
    private boolean onlyIdRefBy;
    private RefEntityChangedListener refListener;
    
    public EntityChangedListener(Class<?> clazz){
        this.clazz = clazz;
        Set<Class<?>> refBySet = new HashSet<Class<?>>();
        boolean byId = false;
        boolean byOther = false;
        Field[] fields = FieldsCache.getInstance().get(clazz);
        for(Field f : fields){
            IndexRefBy irb = f.getAnnotation(IndexRefBy.class);
            if(irb != null){
                Class<?>[] cls = irb.value();
                refBySet.addAll(Arrays.asList(cls));
                if(f.getAnnotation(Id.class) != null){
                    byId = true;
                }else{
                    byOther = true;
                }
            }
        }
        if(refBySet.size() > 0){
            refListener = new RefEntityChangedListener(refBySet);
            if(byId && !byOther){
                onlyIdRefBy = true;
            }
        }
    }
    
    public void entityInsert(BuguEntity obj){
        IndexFilterChecker checker = new IndexFilterChecker(obj);
        if(checker.needIndex()){
            IndexInsertTask task = new IndexInsertTask(obj);
            BuguIndex.getInstance().getExecutor().execute(task);
        }
    }
    
    public void entityUpdate(BuguEntity obj){
        IndexFilterChecker checker = new IndexFilterChecker(obj);
        if(checker.needIndex()){
            IndexUpdateTask task = new IndexUpdateTask(obj);
            BuguIndex.getInstance().getExecutor().execute(task);
        }else{
            IndexRemoveTask task = new IndexRemoveTask(clazz, obj.getId());
            BuguIndex.getInstance().getExecutor().execute(task);
        }
        //for refBy
        if(refListener != null && !onlyIdRefBy){
            refListener.entityChange(clazz, obj.getId());
        }
    }
    
    public void entityRemove(String id){
        IndexRemoveTask task = new IndexRemoveTask(clazz, id);
        BuguIndex.getInstance().getExecutor().execute(task);
        //for refBy
        if(refListener != null){
            refListener.entityChange(clazz, id);
        }
    }
    
}
