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

package com.bugull.mongo.misc;

import com.bugull.mongo.BuguConnection;
import com.bugull.mongo.BuguEntity;
import com.bugull.mongo.annotations.Default;
import com.bugull.mongo.annotations.Ref;
import com.bugull.mongo.annotations.RefList;
import com.bugull.mongo.cache.FieldsCache;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Listener for cascade delete
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class EntityRemovedListener {
    
    private List<Field> refFields = new ArrayList<Field>();
    private List<Field> refListFields = new ArrayList<Field>();

    public EntityRemovedListener(Class<?> clazz) {
        Field[] fields = FieldsCache.getInstance().get(clazz);
        for(Field f : fields){
            Ref ref = f.getAnnotation(Ref.class);
            if(ref!=null && ref.cascade().toUpperCase().indexOf(Default.CASCADE_DELETE)!=-1){
                refFields.add(f);
                continue;
            }
            RefList refList = f.getAnnotation(RefList.class);
            if(refList!=null && refList.cascade().toUpperCase().indexOf(Default.CASCADE_DELETE)!=-1){
                refListFields.add(f);
                continue;
            }
        }
    }
    
    public void entityRemove(BuguEntity entity){
        DeleteCascadeTask task = new DeleteCascadeTask(refFields, refListFields, entity);
        BuguConnection.getInstance().getExecutor().execute(task);
    }
    
}
