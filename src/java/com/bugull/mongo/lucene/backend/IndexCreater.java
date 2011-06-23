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

import com.bugull.mongo.BuguDao;
import com.bugull.mongo.BuguEntity;
import com.bugull.mongo.annotations.Id;
import com.bugull.mongo.cache.FieldsCache;
import com.bugull.mongo.lucene.annotations.IndexEmbed;
import com.bugull.mongo.lucene.annotations.IndexProperty;
import com.bugull.mongo.lucene.annotations.IndexRef;
import java.util.Collection;
import java.util.Date;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class IndexCreater {
    
    private final static Logger logger = Logger.getLogger(IndexCreater.class);
    
    private Object obj;
    private String prefix;
    private String id;
    
    public IndexCreater(Object obj, String id, String prefix){
        this.obj = obj;
        this.id = id;
        if(prefix == null){
            this.prefix = "";
        }else{
            this.prefix = prefix + ".";
        }
    }
    
    public void process(Document doc){
        Class<?> clazz = obj.getClass();
        java.lang.reflect.Field[] fields = FieldsCache.getInstance().get(clazz);
        for(java.lang.reflect.Field f : fields){
            Class<?> type = f.getType();
            try{
                if(type.isArray()){
                    processArrayField(doc, f);
                }else{
                    processPrimitiveField(doc, f);
                } 
            }catch(Exception e){
                logger.error(e.getMessage());
            }
        }
    }
    
    private void processArrayField(Document doc, java.lang.reflect.Field f) throws Exception{
        IndexProperty ip = f.getAnnotation(IndexProperty.class);
        String join = ip.join();
        Class<?> type = f.getType();
        String typeName = type.getComponentType().getName();
        StringBuilder sb = new StringBuilder();
        Object value = f.get(obj);
        if(typeName.equals("java.lang.String")){
            String[] arr = (String[])value;
            for(String e : arr){
                sb.append(e).append(join);
            }
        }
        else if(typeName.equals("boolean") || typeName.equals("java.lang.Boolean")){
            boolean[] arr = (boolean[])value;
            for(boolean e : arr){
                sb.append(e).append(join);
            }
        }
        else if(typeName.equals("char") || typeName.equals("java.lang.Character")){
            char[] arr = (char[])value;
            for(char e : arr){
                sb.append(e).append(join);
            }
        }
        else if(typeName.equals("int") || typeName.equals("java.lang.Integer")){
            int[] arr = (int[])value;
            for(int e : arr){
                sb.append(e).append(join);
            }
        }
        else if(typeName.equals("long") || typeName.equals("java.lang.Long")){
            long[] arr = (long[])value;
            for(long e : arr){
                sb.append(e).append(join);
            }
        }
        else if(typeName.equals("float") || typeName.equals("java.lang.Float")){
            float[] arr = (float[])value;
            for(float e : arr){
                sb.append(e).append(join);
            }
        }
        else if(typeName.equals("double") || typeName.equals("java.lang.Double")){
            double[] arr = (double[])value;
            for(double e : arr){
                sb.append(e).append(join);
            }
        }
        else if(typeName.equals("java.util.Date")){
            Date[] arr = (Date[])value;
            for(Date e : arr){
                sb.append(e.getTime()).append(join);
            }
        }
        String fieldName = prefix + f.getName();
        Field field = new Field(fieldName, sb.toString(),
                    ip.store() ? Field.Store.YES : Field.Store.NO,
                    ip.analyze() ? Field.Index.ANALYZED : Field.Index.NOT_ANALYZED);
        doc.add(field);
    }
    
    private void processPrimitiveField(Document doc, java.lang.reflect.Field f) throws Exception{
        if(id != null && f.getAnnotation(Id.class) != null){
            doc.add(new Field(f.getName(), id, Field.Store.YES, Field.Index.NOT_ANALYZED));
        }
        else if(f.getAnnotation(IndexProperty.class) != null){
            processIndexProperty(doc, f);
        }
        else if(f.getAnnotation(IndexEmbed.class) != null){
            processIndexEmbed(doc, f);
        }
        else if(f.getAnnotation(IndexRef.class) != null){
            processIndexRef(doc, f);
        }
    }
    
    private void processIndexProperty(Document doc, java.lang.reflect.Field f) throws Exception{
        Class<?> type = f.getType();
        String fieldName = prefix + f.getName();
        String typeName = type.getName();
        IndexProperty ip = f.getAnnotation(IndexProperty.class);
        if(typeName.equals("java.lang.String")){
            String fieldValue = f.get(obj).toString();
            Field field = new Field(fieldName, fieldValue,
                    ip.store() ? Field.Store.YES : Field.Store.NO,
                    ip.analyze() ? Field.Index.ANALYZED : Field.Index.NOT_ANALYZED);
            doc.add(field);
        }
        else if(typeName.equals("boolean") || typeName.equals("java.lang.Boolean")){
            String fieldValue = f.getBoolean(obj) ? "true" : "false";
            Field field = new Field(fieldName, fieldValue, Field.Store.NO, Field.Index.NOT_ANALYZED);
            doc.add(field);
        }
        else if(typeName.equals("char") || typeName.equals("java.lang.Character")){
            String fieldValue = String.valueOf(f.getChar(obj));
            Field field = new Field(fieldName, fieldValue, Field.Store.NO, Field.Index.NOT_ANALYZED);
            doc.add(field);
        }
        else if(typeName.equals("int") || typeName.equals("java.lang.Integer")){
            NumericField field = new NumericField(fieldName).setIntValue(f.getInt(obj));
            doc.add(field);
        }
        else if(typeName.equals("long") || typeName.equals("java.lang.Long")){
            NumericField field = new NumericField(fieldName).setLongValue(f.getLong(obj));
            doc.add(field);
        }
        else if(typeName.equals("float") || typeName.equals("java.lang.Float")){
            NumericField field = new NumericField(fieldName).setFloatValue(f.getFloat(obj));
            doc.add(field);
        }
        else if(typeName.equals("double") || typeName.equals("java.lang.Double")){
            NumericField field = new NumericField(fieldName).setDoubleValue(f.getDouble(obj));
            doc.add(field);
        }
        else if(typeName.equals("java.util.Date")){
            Date date = (Date)f.get(obj);
            NumericField field = new NumericField(fieldName).setLongValue(date.getTime());
            doc.add(field);
        }
        else if(typeName.equals("java.util.Set") || typeName.equals("java.util.List")){
            Collection coll = (Collection)f.get(obj);
            StringBuilder sb = new StringBuilder();
            String join = ip.join();
            for(Object o : coll){
                sb.append(o).append(join);
            }
            Field field = new Field(fieldName, sb.toString(),
                    ip.store() ? Field.Store.YES : Field.Store.NO,
                    ip.analyze() ? Field.Index.ANALYZED : Field.Index.NOT_ANALYZED);
            doc.add(field);
        }
    }
    
    private void processIndexEmbed(Document doc, java.lang.reflect.Field f) throws Exception{
        Object embedObj = f.get(obj);
        IndexCreater creater = new IndexCreater(embedObj, null, prefix + f.getName());
        creater.process(doc);
    }
    
    private void processIndexRef(Document doc, java.lang.reflect.Field f) throws Exception{
        BuguEntity entity = (BuguEntity)f.get(obj);
        String refId = entity.getId();
        BuguDao buguDao = new BuguDao(f.getType());
        Object refObj = buguDao.findOne(refId);
        IndexCreater creater = new IndexCreater(refObj, null, prefix + f.getName());
        creater.process(doc);
    }
    
}
