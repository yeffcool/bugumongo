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

package com.bugull.mongo;

import com.bugull.mongo.annotations.Default;
import com.bugull.mongo.annotations.EnsureIndex;
import com.bugull.mongo.annotations.Entity;
import com.bugull.mongo.annotations.Id;
import com.bugull.mongo.annotations.IdType;
import com.bugull.mongo.annotations.SplitType;
import com.bugull.mongo.cache.FieldsCache;
import com.bugull.mongo.exception.DBConnectionException;
import com.bugull.mongo.exception.IdException;
import com.bugull.mongo.lucene.backend.EntityChangedListener;
import com.bugull.mongo.lucene.backend.IndexChecker;
import com.bugull.mongo.misc.CascadeChecker;
import com.bugull.mongo.misc.DBIndex;
import com.bugull.mongo.misc.EntityRemovedListener;
import com.bugull.mongo.utils.IdUtil;
import com.bugull.mongo.utils.MapperUtil;
import com.bugull.mongo.utils.Operator;
import com.bugull.mongo.utils.ReferenceUtil;
import com.bugull.mongo.utils.StringUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * The basic Dao class.
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class BuguDao<T> {
    
    private final static Logger logger = Logger.getLogger(BuguDao.class);
    
    protected DBCollection coll;
    protected Class<T> clazz;
    protected DBObject keys;  //non-lazy fields
    protected WriteConcern concern;
    protected EntityChangedListener luceneListener;
    protected EntityRemovedListener cascadeListener;
    
    public BuguDao(Class<T> clazz){
        this.clazz = clazz;
        DB db = null;
        try {
            db = BuguConnection.getInstance().getDB();
        } catch (DBConnectionException ex) {
            logger.error(ex.getMessage(), ex);
        }
        //The default write concern is ACKNOWLEDGED, set in MongoClientOptions.
        concern = db.getWriteConcern();
        //init none-split collection
        Entity entity = clazz.getAnnotation(Entity.class);
        SplitType split = entity.split();
        if(split == SplitType.NONE){
            String name = MapperUtil.getEntityName(clazz);
            initCollection(name);
        }
        //for keys
        keys = MapperUtil.getKeyFields(clazz);
        //for lucene
        if(IndexChecker.needListener(clazz)){
            luceneListener = new EntityChangedListener(clazz);
        }
        //for cascade delete
        if(CascadeChecker.needListener(clazz)){
            cascadeListener = new EntityRemovedListener(clazz);
        }
    }
    
    private void initCollection(String name){
        DB db = null;
        try {
            db = BuguConnection.getInstance().getDB();
        } catch (DBConnectionException ex) {
            logger.error(ex.getMessage(), ex);
        }
        Entity entity = clazz.getAnnotation(Entity.class);
        //if capped
        if(entity.capped() && !db.collectionExists(name)){
            DBObject options = new BasicDBObject("capped", true);
            long capSize = entity.capSize();
            if(capSize != Default.CAP_SIZE){
                options.put("size", capSize);
            }
            long capMax = entity.capMax();
            if(capMax != Default.CAP_MAX){
                options.put("max", capMax);
            }
            coll = db.createCollection(name, options);
        }else{
            coll = db.getCollection(name);
        }
        //for @EnsureIndex
        EnsureIndex ei = clazz.getAnnotation(EnsureIndex.class);
        if(ei != null){
            List<DBIndex> list = MapperUtil.getDBIndex(ei.value());
            for(DBIndex dbi : list){
                coll.createIndex(dbi.getKeys(), dbi.getOptions());
            }
        }
    }
    
    /**
     * If collection is splitted by date, you have to set the date to check which collection is in use.
     * @param date 
     */
    public void setSplitSuffix(Date date){
        Entity entity = clazz.getAnnotation(Entity.class);
        SplitType split = entity.split();
        SimpleDateFormat sdf = null;
        switch(split){
            case DAILY:
                sdf = new SimpleDateFormat("yyyy-MM-dd");
                break;
            case MONTHLY:
                sdf = new SimpleDateFormat("yyyy-MM");
                break;
            case YEARLY:
                sdf = new SimpleDateFormat("yyyy");
                break;
            default:
                break;
        }
        if(sdf != null){
            String ext = sdf.format(date);
            String name = MapperUtil.getEntityName(clazz);
            initCollection(name + "-" + ext);
        }
    }
    
    /**
     * If collection is splitted by string, you have to set the string value to check which collection is in use.
     * @param s 
     */
    public void setSplitSuffix(String s){
        Entity entity = clazz.getAnnotation(Entity.class);
        SplitType split = entity.split();
        if(split == SplitType.STRING){
            String name = MapperUtil.getEntityName(clazz);
            initCollection(name + "-" + s);
        }
    }
    
    /**
     * The default write concern is ACKNOWLEDGED, you can change it.
     * @param concern 
     */
    public void setWriteConcern(WriteConcern concern){
        this.concern = concern;
    }
    
    /**
     * Insert an entity to mongoDB.
     * @param t
     * @return 
     */
    public WriteResult insert(T t){
        DBObject dbo = MapperUtil.toDBObject(t);
        WriteResult wr = coll.insert(dbo, concern);
        String id = dbo.get(Operator.ID).toString();
        BuguEntity ent = (BuguEntity)t;
        ent.setId(id);
        if(luceneListener != null){
            luceneListener.entityInsert(ent);
        }
        return wr;
    }
    
    /**
     * Batch insert.
     * @param list 
     * @return 
     */
    public WriteResult insert(List<T> list){
        List<DBObject> dboList = new ArrayList<DBObject>();
        for(T t : list){
            dboList.add(MapperUtil.toDBObject(t));
        }
        WriteResult wr = coll.insert(dboList, concern);
        int len = dboList.size();
        for(int i=0; i<len; i++){
            String id = dboList.get(i).get(Operator.ID).toString();
            BuguEntity ent = (BuguEntity)(list.get(i));
            ent.setId(id);
        }
        if(luceneListener != null){
            for(T t : list){
                luceneListener.entityInsert((BuguEntity)t);
            }
        }
        return wr;
    }
    
    /**
     * Save an entity to mongoDB. 
     * If no id in it, then insert the entity.
     * Else, check the id type, to confirm do save or insert.
     * @param t 
     * @return 
     */
    public WriteResult save(T t){
        WriteResult wr = null;
        BuguEntity ent = (BuguEntity)t;
        if(StringUtil.isEmpty(ent.getId())){
            wr = insert(t);
        }
        else{
            Field idField = null;
            try{
                idField = FieldsCache.getInstance().getIdField(clazz);
            }catch(IdException ex){
                logger.error(ex.getMessage(), ex);
            }
            Id idAnnotation = idField.getAnnotation(Id.class);
            if(idAnnotation.type()==IdType.USER_DEFINE){
                if(this.exists(Operator.ID, ent.getId())){
                    wr = doSave(ent);
                }else{
                    wr = insert(t);
                }
            }
            else{
                wr = doSave(ent);
            }
        }
        return wr;
    }
    
    private WriteResult doSave(BuguEntity ent){
        WriteResult wr = coll.save(MapperUtil.toDBObject(ent), concern);
        if(luceneListener != null){
            luceneListener.entityUpdate(ent);
        }
        return wr;
    }
    
    /**
     * Drop the collection. 
     * It will automatically drop all indexes from this collection.
     */
    public void drop(){
        if(luceneListener != null || cascadeListener != null){
            List<T> list = findAll();
            for(T t : list){
                remove(t);
            }
        }
        coll.drop();
        coll.dropIndexes();
    }
    
    /**
     * Remove an entity.
     * @param t 
     * @return 
     */
    public WriteResult remove(T t){
        BuguEntity ent = (BuguEntity)t;
        return remove(ent.getId());
    }

    /**
     * Remove an entity by id.
     * @param id 
     * @return 
     */
    public WriteResult remove(String id){
        if(cascadeListener != null){
            BuguEntity entity = (BuguEntity)findOne(id);
            cascadeListener.entityRemove(entity);
        }
        if(luceneListener != null){
            luceneListener.entityRemove(id);
        }
        DBObject dbo = new BasicDBObject(Operator.ID, IdUtil.toDbId(clazz, id));
        return coll.remove(dbo, concern);
    }
    
    /**
     * Batch remove by id.
     * @param idList
     * @return 
     */
    public WriteResult remove(List<String> idList){
        int len = idList.size();
        Object[] arr = new Object[len];
        for(int i=0; i<len; i++){
            arr[i] = IdUtil.toDbId(clazz, idList.get(i));
        }
        DBObject in = new BasicDBObject(Operator.IN, arr);
        return removeMulti(new BasicDBObject(Operator.ID, in));
    }
    
    /**
     * Remove by condition.
     * @param key the condition field
     * @param value the condition value
     * @return 
     */
    public WriteResult remove(String key, Object value){
        value = checkSpecialValue(key, value);
        return removeMulti(new BasicDBObject(key, value));
    }
    
    /**
     * Remove by query condition.
     * @param query 
     * @return 
     */
    public WriteResult remove(BuguQuery query){
        return removeMulti(query.getCondition());
    }
    
    private WriteResult removeMulti(DBObject condition){
        DBCursor cursor = coll.find(condition, keys);
        List<T> list = MapperUtil.toList(clazz, cursor);
        if(cascadeListener != null){
            for(T t : list){
                cascadeListener.entityRemove((BuguEntity)t);
            }
        }
        if(luceneListener != null){
            for(T t : list){
                BuguEntity ent = (BuguEntity)t;
                luceneListener.entityRemove(ent.getId());
            }
        }
        return coll.remove(condition, concern);
    }
    
    private Object checkSpecialValue(String key, Object value){
        Object result = value;
        if(value instanceof BuguEntity){
            BuguEntity be = (BuguEntity)value;
            result = ReferenceUtil.toDbReference(clazz, key, be.getClass(), be.getId());
        }else if(!(value instanceof DBObject) &&
                (FieldsCache.getInstance().isEmbedField(clazz, key) || FieldsCache.getInstance().isEmbedListField(clazz, key))){
            result = MapperUtil.toDBObject(value);
        }
        return result;
    }
    
    /**
     * Check if any entity with id already exists
     * @param id the id value to check
     * @return 
     */
    public boolean exists(String id){
        DBObject query = new BasicDBObject();
        query.put(Operator.ID, IdUtil.toDbId(clazz, id));
        return coll.findOne(query) != null;
    }
    
    /**
     * Check if any entity match the condition.
     * @param key the condition field
     * @param value the condition value
     * @return 
     */
    public boolean exists(String key, Object value){
        value = checkSpecialValue(key, value);
        DBObject query = new BasicDBObject(key, value);
        return coll.findOne(query) != null;
    }
    
    /**
     * Find a single document by natural order
     * @return 
     */
    public T findOne(){
        DBObject result = coll.findOne();
        return MapperUtil.fromDBObject(clazz, result);
    }
    
    /**
     * Find a single document by id
     * @param id
     * @return 
     */
    public T findOne(String id){
        DBObject dbo = new BasicDBObject();
        dbo.put(Operator.ID, IdUtil.toDbId(clazz, id));
        DBObject result = coll.findOne(dbo);
        return MapperUtil.fromDBObject(clazz, result);
    }
    
    /**
     * Find a single document by key-value
     * @param key
     * @param value
     * @return 
     */
    public T findOne(String key, Object value){
        value = checkSpecialValue(key, value);
        DBObject query = new BasicDBObject(key, value);
        DBObject dbo = coll.findOne(query);
        return MapperUtil.fromDBObject(clazz, dbo);
    }

    /**
     * Find all document by natural order
     * @return 
     */
    public List<T> findAll(){
        DBCursor cursor = coll.find(new BasicDBObject(), keys);
        return MapperUtil.toList(clazz, cursor);
    }
    
    /**
     * Find all document by order
     * @param orderBy
     * @return 
     */
    public List<T> findAll(String orderBy){
        DBObject dbo = MapperUtil.getSort(orderBy);
        DBCursor cursor = coll.find(new BasicDBObject(), keys).sort(dbo);
        return MapperUtil.toList(clazz, cursor);
    }

    /**
     * Find all document, and return one page
     * @param pageNum
     * @param pageSize
     * @return 
     */
    public List<T> findAll(int pageNum, int pageSize){
        DBCursor cursor = coll.find(new BasicDBObject(), keys).skip((pageNum-1)*pageSize).limit(pageSize);
        return MapperUtil.toList(clazz, cursor);
    }
    
    /**
     * Find all document, and return one page
     * @param orderBy
     * @param pageNum
     * @param pageSize
     * @return 
     */
    public List<T> findAll(String orderBy, int pageNum, int pageSize){
        DBObject dbo = MapperUtil.getSort(orderBy);
        DBCursor cursor = coll.find(new BasicDBObject(), keys).sort(dbo).skip((pageNum-1)*pageSize).limit(pageSize);
        return MapperUtil.toList(clazz, cursor);
    }
    
    public List distinct(String key){
        return coll.distinct(key);
    }

    /**
     * Count all entity.
     * @return 
     */
    public long count(){
        return coll.count();
    }
    
    /**
     * Count by condition.
     * @param key the condition field
     * @param value the condition value
     * @return 
     */
    public long count(String key, Object value){
        value = checkSpecialValue(key, value);
        return coll.count(new BasicDBObject(key, value));
    }
    
    /**
     * Get the DBCollection object, supplied by the mongodb java driver.
     * @return 
     */
    public DBCollection getCollection(){
        return coll;
    }
    
    /**
     * Create a query.
     * @return a new BuguQuery object
     */
    public BuguQuery<T> query(){
        return new BuguQuery<T>(coll, clazz, keys);
    }
    
    /**
     * Create a updater.
     * @return a new BuguUpdater object
     */
    public BuguUpdater<T> update(){
        return new BuguUpdater(coll, clazz, concern, luceneListener);
    }
    
}
