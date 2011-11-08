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

import com.bugull.mongo.cache.FieldsCache;
import com.bugull.mongo.lucene.annotations.IndexProperty;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import org.apache.lucene.document.Document;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class EmbedListFieldHandler extends AbstractFieldHandler{
    
    public EmbedListFieldHandler(Object obj, Field field, String prefix){
        super(obj, field, prefix);
    }

    @Override
    public void handle(Document doc) throws Exception{
        ParameterizedType type = (ParameterizedType)field.getGenericType();
        Type[] types = type.getActualTypeArguments();
        if(types.length == 1){
            List list = (List)field.get(obj);
            Class cls = (Class)types[0];
            Field[] fields = FieldsCache.getInstance().get(cls);
            for(Field f : fields){
                IndexProperty ip = f.getAnnotation(IndexProperty.class);
                if(ip != null){
                    FieldHandler handler = new ListPropertyFieldHandler(list, f, prefix);
                    handler.handle(doc);
                }
            }
        }
    }
    
}
