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
import com.bugull.mongo.lucene.annotations.IndexEmbedList;
import com.bugull.mongo.lucene.annotations.IndexProperty;
import com.bugull.mongo.lucene.annotations.IndexRef;
import com.bugull.mongo.lucene.annotations.IndexRefBy;
import com.bugull.mongo.lucene.annotations.IndexRefList;
import com.bugull.mongo.mapper.DataType;
import com.bugull.mongo.mapper.Operator;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.NumericField;
import org.bson.types.ObjectId;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class ObjectIndexCreator extends IndexCreator{
    
    private final static Logger logger = Logger.getLogger(ObjectIndexCreator.class);
    
    protected Object obj;
    protected String prefix;
    
    public ObjectIndexCreator(Object obj, String prefix){
        this.obj = obj;
        if(prefix == null){
            this.prefix = "";
        }else{
            this.prefix = prefix + ".";
        }
    }
    
    @Override
    public void process(Document doc){
        java.lang.reflect.Field[] fields = FieldsCache.getInstance().get(obj.getClass());
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
            try{
                processField(doc, f);
            }catch(Exception e){
                logger.error(e.getMessage());
            }
        }
    }
    
    private void processField(Document doc, java.lang.reflect.Field f) throws Exception{
        if(f.get(obj) == null){
            return;
        }
        if(f.getAnnotation(Id.class) != null){
            processId(doc, f);
        }
        else if(f.getAnnotation(IndexProperty.class) != null){
            IndexProperty ip = f.getAnnotation(IndexProperty.class);
            processProperty(doc, f, ip.analyze(), ip.store(), ip.boost());
        }
        else if(f.getAnnotation(IndexEmbed.class) != null){
            processIndexEmbed(doc, f);
        }
        else if(f.getAnnotation(IndexEmbedList.class) != null){
            processIndexEmbedList(doc, f);
        }
        else if(f.getAnnotation(IndexRef.class) != null){
            processIndexRef(doc, f);
        }
        else if(f.getAnnotation(IndexRefList.class) != null){
            processIndexRefList(doc, f);
        }
    }
    
    private void processId(Document doc, java.lang.reflect.Field f) throws Exception{
        BuguEntity entity = (BuguEntity)obj;
        String fieldName = prefix + f.getName();
        doc.add(new Field(fieldName, entity.getId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
    }
    
    protected void processProperty(Document doc, java.lang.reflect.Field f, boolean analyze, boolean store, float boost) throws Exception{
        Class<?> type = f.getType();
        if(type.isArray()){
            processArrayField(doc, f, analyze, store, boost);
        }else{
            processPrimitiveField(doc, f, analyze, store, boost);
        }
    }
    
    private void processArrayField(Document doc, java.lang.reflect.Field f, boolean analyze, boolean store, float boost) throws Exception{
        Class<?> type = f.getType();
        String typeName = type.getComponentType().getName();
        String fieldName = prefix + f.getName();
        Field field = new Field(fieldName, getArrayString(f.get(obj), typeName),
                    store ? Field.Store.YES : Field.Store.NO,
                    analyze ? Field.Index.ANALYZED : Field.Index.NOT_ANALYZED);
        field.setBoost(boost);
        doc.add(field);
    }
    
    private void processPrimitiveField(Document doc, java.lang.reflect.Field f, boolean analyze, boolean store, float boost) throws Exception{
        Class<?> type = f.getType();
        String fieldName = prefix + f.getName();
        String typeName = type.getName();
        Fieldable field = null;
        if(DataType.isString(typeName)){
            String fieldValue = f.get(obj).toString();
            field = new Field(fieldName, fieldValue,
                    store ? Field.Store.YES : Field.Store.NO,
                    analyze ? Field.Index.ANALYZED : Field.Index.NOT_ANALYZED);
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
            for(Object o : coll){
                sb.append(o).append(JOIN);
            }
            field = new Field(fieldName, sb.toString(),
                    store ? Field.Store.YES : Field.Store.NO,
                    analyze ? Field.Index.ANALYZED : Field.Index.NOT_ANALYZED);
        }
        field.setBoost(boost);
        doc.add(field);
    }
    
    private void processIndexEmbed(Document doc, java.lang.reflect.Field f) throws Exception{
        Object embedObj = f.get(obj);
        if(embedObj != null){
            IndexCreator creater = new ObjectIndexCreator(embedObj, prefix + f.getName());
            creater.process(doc);
        }
    }
    
    private void processIndexEmbedList(Document doc, java.lang.reflect.Field f) throws Exception {
        ParameterizedType type = (ParameterizedType)f.getGenericType();
        Type[] types = type.getActualTypeArguments();
        if(types.length == 1){
            List list = (List)f.get(obj);
            Class cls = (Class)types[0];
            java.lang.reflect.Field[] fields = FieldsCache.getInstance().get(cls);
            for(java.lang.reflect.Field field : fields){
                if(field.getAnnotation(IndexProperty.class) != null){
                    IndexCreator creator = new ObjectsIndexCreator(list, field, prefix + f.getName());
                    creator.process(doc);
                }
            }
        }
    }
    
    private void processIndexRef(Document doc, java.lang.reflect.Field f) throws Exception{
        BuguEntity entity = (BuguEntity)f.get(obj);
        String refId = entity.getId();
        BuguDao dao = DaoCache.getInstance().get(f.getType());
        Object refObj = dao.findOne(refId);
        if(refObj != null){
            IndexCreator creator = new RefIndexCreator(obj.getClass(), refObj, prefix + f.getName());
            creator.process(doc);
        }
    }
    
    private void processIndexRefList(Document doc, java.lang.reflect.Field f) throws Exception {
        ParameterizedType type = (ParameterizedType)f.getGenericType();
        Type[] types = type.getActualTypeArguments();
        if(types.length == 1){
            List<BuguEntity> li = (List<BuguEntity>)f.get(obj);
            int size = li.size();
            ObjectId[] ids = new ObjectId[size];
            for(int i=0; i<size; i++){
                ids[i] = new ObjectId(li.get(i).getId());
            }
            Class cls = (Class)types[0];
            BuguDao dao = DaoCache.getInstance().get(cls);
            DBObject in = new BasicDBObject(Operator.IN, ids);
            DBObject query = new BasicDBObject(Operator.ID, in);
            List list = dao.find(query);
            java.lang.reflect.Field[] fields = FieldsCache.getInstance().get(cls);
            for(java.lang.reflect.Field field : fields){
                if(field.getAnnotation(IndexRefBy.class) != null){
                    IndexCreator creator = new RefListIndexCreator(obj.getClass(), list, field, prefix + f.getName());
                    creator.process(doc);
                }
            }
        }
    }
    
}
