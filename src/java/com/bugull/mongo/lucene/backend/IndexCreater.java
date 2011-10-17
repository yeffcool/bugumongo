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
import com.bugull.mongo.cache.DaoCache;
import com.bugull.mongo.cache.FieldsCache;
import com.bugull.mongo.lucene.annotations.BoostSwitch;
import com.bugull.mongo.lucene.annotations.IndexEmbed;
import com.bugull.mongo.lucene.annotations.IndexProperty;
import com.bugull.mongo.lucene.annotations.IndexRef;
import com.bugull.mongo.mapper.DataType;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
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
            BoostSwitch bs = f.getAnnotation(BoostSwitch.class);
            if(bs != null){
                CompareChecker checker = new CompareChecker(obj);
                boolean fit = checker.isFit(f, bs.compare(), bs.value());
                if(fit){
                    doc.setBoost(bs.fit());
                }else{
                    doc.setBoost(bs.unfit());
                }
            }
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
        if(DataType.isString(typeName)){
            String[] arr = (String[])value;
            for(String e : arr){
                sb.append(e).append(join);
            }
        }
        else if(DataType.isBoolean(typeName)){
            boolean[] arr = (boolean[])value;
            for(boolean e : arr){
                sb.append(e).append(join);
            }
        }
        else if(DataType.isChar(typeName)){
            char[] arr = (char[])value;
            for(char e : arr){
                sb.append(e).append(join);
            }
        }
        else if(DataType.isInteger(typeName)){
            int[] arr = (int[])value;
            for(int e : arr){
                sb.append(e).append(join);
            }
        }
        else if(DataType.isLong(typeName)){
            long[] arr = (long[])value;
            for(long e : arr){
                sb.append(e).append(join);
            }
        }
        else if(DataType.isShort(typeName)){
            short[] arr = (short[])value;
            for(short e : arr){
                sb.append(e).append(join);
            }
        }
        else if(DataType.isFloat(typeName)){
            float[] arr = (float[])value;
            for(float e : arr){
                sb.append(e).append(join);
            }
        }
        else if(DataType.isDouble(typeName)){
            double[] arr = (double[])value;
            for(double e : arr){
                sb.append(e).append(join);
            }
        }
        else if(DataType.isDate(typeName)){
            Date[] arr = (Date[])value;
            for(Date e : arr){
                sb.append(e.getTime()).append(join);
            }
        }
        else if(DataType.isTimestamp(typeName)){
            Timestamp[] arr = (Timestamp[])value;
            for(Timestamp e : arr){
                sb.append(e.getTime()).append(join);
            }
        }
        String fieldName = prefix + f.getName();
        Field field = new Field(fieldName, sb.toString(),
                    ip.store() ? Field.Store.YES : Field.Store.NO,
                    ip.analyze() ? Field.Index.ANALYZED : Field.Index.NOT_ANALYZED);
        field.setBoost(ip.boost());
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
        Fieldable field = null;
        if(DataType.isString(typeName)){
            String fieldValue = f.get(obj).toString();
            field = new Field(fieldName, fieldValue,
                    ip.store() ? Field.Store.YES : Field.Store.NO,
                    ip.analyze() ? Field.Index.ANALYZED : Field.Index.NOT_ANALYZED);
        }
        else if(DataType.isBoolean(typeName)){
            String fieldValue = f.getBoolean(obj) ? "true" : "false";
            field = new Field(fieldName, fieldValue, Field.Store.NO, Field.Index.NOT_ANALYZED);
        }
        else if(DataType.isChar(typeName)){
            String fieldValue = String.valueOf(f.getChar(obj));
            field = new Field(fieldName, fieldValue, Field.Store.NO, Field.Index.NOT_ANALYZED);
        }
        else if(DataType.isInteger(typeName)){
            field = new NumericField(fieldName).setIntValue(f.getInt(obj));
        }
        else if(DataType.isLong(typeName)){
            field = new NumericField(fieldName).setLongValue(f.getLong(obj));
        }
        else if(DataType.isShort(typeName)){
            field = new NumericField(fieldName).setIntValue(f.getShort(obj));
        }
        else if(DataType.isFloat(typeName)){
            field = new NumericField(fieldName).setFloatValue(f.getFloat(obj));
        }
        else if(DataType.isDouble(typeName)){
            field = new NumericField(fieldName).setDoubleValue(f.getDouble(obj));
        }
        else if(DataType.isDate(typeName)){
            Date date = (Date)f.get(obj);
            field = new NumericField(fieldName).setLongValue(date.getTime());
        }
        else if(DataType.isTimestamp(typeName)){
            Timestamp ts = (Timestamp)f.get(obj);
            field = new NumericField(fieldName).setLongValue(ts.getTime());
        }
        else if(DataType.isSet(typeName) || DataType.isList(typeName)){
            Collection coll = (Collection)f.get(obj);
            StringBuilder sb = new StringBuilder();
            String join = ip.join();
            for(Object o : coll){
                sb.append(o).append(join);
            }
            field = new Field(fieldName, sb.toString(),
                    ip.store() ? Field.Store.YES : Field.Store.NO,
                    ip.analyze() ? Field.Index.ANALYZED : Field.Index.NOT_ANALYZED);
        }
        field.setBoost(ip.boost());
        doc.add(field);
    }
    
    private void processIndexEmbed(Document doc, java.lang.reflect.Field f) throws Exception{
        Object embedObj = f.get(obj);
        IndexCreater creater = new IndexCreater(embedObj, null, prefix + f.getName());
        creater.process(doc);
    }
    
    private void processIndexRef(Document doc, java.lang.reflect.Field f) throws Exception{
        BuguEntity entity = (BuguEntity)f.get(obj);
        String refId = entity.getId();
        BuguDao buguDao = DaoCache.getInstance().get(f.getType());
        Object refObj = buguDao.findOne(refId);
        IndexCreater creater = new IndexCreater(refObj, null, prefix + f.getName());
        creater.process(doc);
    }
    
}
