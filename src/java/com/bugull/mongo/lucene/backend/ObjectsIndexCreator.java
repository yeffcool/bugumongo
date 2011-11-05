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

import com.bugull.mongo.lucene.annotations.IndexProperty;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class ObjectsIndexCreator extends IndexCreator{
    
    private final static Logger logger = Logger.getLogger(ObjectsIndexCreator.class);
    
    protected List objList;
    protected java.lang.reflect.Field field;
    protected String fieldName;
    
    public ObjectsIndexCreator(List objList, java.lang.reflect.Field field, String prefix){
        this.objList = objList;
        this.field = field;
        this.fieldName = prefix + field.getName();
    }

    @Override
    public void process(Document doc) {
        IndexProperty ip = field.getAnnotation(IndexProperty.class);
        try{
            processProperty(doc, ip.analyze(), ip.store(), ip.boost());
        }catch(Exception e){
            logger.error(e.getMessage());
        }
    }
    
    protected void processProperty(Document doc, boolean analyze, boolean store, float boost) throws Exception{
        Class<?> type = field.getType();
        if(type.isArray()){
            processArrayField(doc, analyze, store, boost);
        }else{
            processPrimitiveField(doc, analyze, store, boost);
        }
    }
    
    private void processArrayField(Document doc, boolean analyze, boolean store, float boost) throws Exception{
        StringBuilder sb = new StringBuilder();
        Class<?> type = field.getType();
        String typeName = type.getComponentType().getName();
        for(Object obj : objList){
            Object value = field.get(obj);
            if(value == null){
                continue;
            }
            sb.append(getArrayString(value, typeName)).append(JOIN);
        }
        Field f = new Field(fieldName, sb.toString(), 
                store ? Field.Store.YES : Field.Store.NO,
                analyze ? Field.Index.ANALYZED : Field.Index.NOT_ANALYZED);
        f.setBoost(boost);
        doc.add(f);
    }
    
    private void processPrimitiveField(Document doc, boolean analyze, boolean store, float boost) throws Exception{
        StringBuilder sb = new StringBuilder();
        for(Object obj : objList){
            String value = field.get(obj).toString();
            sb.append(value).append(JOIN);
        }
        Field f = new Field(fieldName, sb.toString(), 
                store ? Field.Store.YES : Field.Store.NO,
                analyze ? Field.Index.ANALYZED : Field.Index.NOT_ANALYZED);
        f.setBoost(boost);
        doc.add(f);
    }
    
}
