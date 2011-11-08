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

package com.bugull.mongo.lucene.handler;

import com.bugull.mongo.BuguDao;
import com.bugull.mongo.BuguEntity;
import com.bugull.mongo.cache.DaoCache;
import com.bugull.mongo.cache.FieldsCache;
import com.bugull.mongo.lucene.annotations.IndexRefBy;
import java.lang.reflect.Field;
import org.apache.lucene.document.Document;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class RefFieldHandler extends AbstractFieldHandler{
    
    public RefFieldHandler(Object obj, Field field, String prefix){
        super(obj, field, prefix);
    }

    @Override
    public void handle(Document doc) throws Exception{
        BuguEntity entity = (BuguEntity)field.get(obj);
        String refId = entity.getId();
        Class<?> clazz = field.getType();
        BuguDao dao = DaoCache.getInstance().get(field.getType());
        Object refObj = dao.findOne(refId);
        if(refObj != null){
            Field[] fields = FieldsCache.getInstance().get(clazz);
            for(Field f : fields){
                IndexRefBy irb = f.getAnnotation(IndexRefBy.class);
                if(irb != null){
                    FieldHandler handler = new RefByFieldHandler(obj.getClass(), refObj, f, field.getName());
                    handler.handle(doc);
                }
            }
        }
    }
    
}
