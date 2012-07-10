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
import com.bugull.mongo.lucene.cluster.*;
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
    
    private ClusterConfig cluster = BuguIndex.getInstance().getClusterConfig();
    
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
            //insert to local index
            IndexInsertTask task = new IndexInsertTask(obj);
            BuguIndex.getInstance().getExecutor().execute(task);
            //insert to remote cluster index
            if(cluster != null){
                EntityMessage message = MessageFactory.createInsertMessage(obj);
                cluster.sendMessage(message);
            }
        }
    }
    
    public void entityUpdate(BuguEntity obj){
        IndexFilterChecker checker = new IndexFilterChecker(obj);
        if(checker.needIndex()){
            //update local index
            IndexUpdateTask task = new IndexUpdateTask(obj);
            BuguIndex.getInstance().getExecutor().execute(task);
            //update remote cluster index
            if(cluster != null){
                EntityMessage message = MessageFactory.createUpdateMessage(obj);
                cluster.sendMessage(message);
            }
        }
        else{
            //remove from local index
            IndexRemoveTask task = new IndexRemoveTask(clazz, obj.getId());
            BuguIndex.getInstance().getExecutor().execute(task);
            //remove from remote cluster index
            if(cluster != null){
                ClassIdMessage message = MessageFactory.createRemoveMessage(clazz, obj.getId());
                cluster.sendMessage(message);
            }
        }
        //for @IndexRefBy
        if(refListener != null && !onlyIdRefBy){
            //update local index
            refListener.entityChange(clazz, obj.getId());
            //update remote cluster index
            if(cluster != null){
                ClassIdMessage message = MessageFactory.createRefByMessage(clazz, obj.getId());
                cluster.sendMessage(message);
            }
        }
    }
    
    public void entityRemove(String id){
        //remove from local index
        IndexRemoveTask task = new IndexRemoveTask(clazz, id);
        BuguIndex.getInstance().getExecutor().execute(task);
        //remove from remote cluster index
        if(cluster != null){
            ClassIdMessage message = MessageFactory.createRemoveMessage(clazz, id);
            cluster.sendMessage(message);
        }
        //for @IndexRefBy
        if(refListener != null){
            //update local index
            refListener.entityChange(clazz, id);
            //update remote cluster index
            if(cluster != null){
                ClassIdMessage message = MessageFactory.createRefByMessage(clazz, id);
                cluster.sendMessage(message);
            }
        }
    }

    public RefEntityChangedListener getRefListener() {
        return refListener;
    }
    
}
