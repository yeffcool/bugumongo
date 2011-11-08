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

import java.util.List;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class ListPropertyFieldHandler extends PropertyFieldHandler{
    
    protected ListPropertyFieldHandler(Object obj, java.lang.reflect.Field field, String prefix){
        super(obj, field, prefix);
    }
    
    protected void processList(List objList, Document doc, boolean analyze, boolean store, float boost) throws Exception{
        StringBuilder sb = new StringBuilder();
        Class<?> type = field.getType();
        if(type.isArray()){
            String typeName = type.getComponentType().getName();
            for(Object o : objList){
                Object value = field.get(o);
                if(value == null){
                    continue;
                }
                sb.append(getArrayString(value, typeName)).append(JOIN);
            }
        }else{
            for(Object o : objList){
                String value = field.get(o).toString();
                sb.append(value).append(JOIN);
            }
        }
        Field f = new Field(prefix + field.getName(), sb.toString(), 
                store ? Field.Store.YES : Field.Store.NO,
                analyze ? Field.Index.ANALYZED : Field.Index.NOT_ANALYZED);
        f.setBoost(boost);
        doc.add(f);
    }
    
}
