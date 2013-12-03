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
import com.bugull.mongo.cache.FieldsCache;
import com.bugull.mongo.exception.DBConnectionException;
import com.bugull.mongo.exception.IdException;
import com.bugull.mongo.lucene.backend.EntityChangedListener;
import com.bugull.mongo.lucene.backend.IndexChecker;
import com.bugull.mongo.mapper.DBIndex;
import com.bugull.mongo.mapper.EntityRemovedListener;
import com.bugull.mongo.mapper.IdUtil;
import com.bugull.mongo.mapper.MapperUtil;
import com.bugull.mongo.mapper.Operator;
import com.bugull.mongo.mapper.ReferenceUtil;
import com.bugull.mongo.mapper.StringUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        Entity entity = clazz.getAnnotation(Entity.class);
        String name = MapperUtil.getEntityName(clazz);
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
        //for keys
        keys = MapperUtil.getKeyFields(clazz);
        //for @EnsureIndex
        EnsureIndex ei = clazz.getAnnotation(EnsureIndex.class);
        if(ei != null){
            List<DBIndex> list = MapperUtil.getDBIndex(ei.value());
            for(DBIndex dbi : list){
                coll.ensureIndex(dbi.getKeys(), dbi.getOptions());
            }
        }
        //for lucene
        if(IndexChecker.needListener(clazz)){
            luceneListener = new EntityChangedListener(clazz);
        }
        //for cascade delete
        cascadeListener = new EntityRemovedListener(clazz);
    }
    
    /**
     * Insert an entity to mongoDB.
     * @param t
     * @return 
     */
    public WriteResult insert(T t){
        DBObject dbo = MapperUtil.toDBObject(t);
        WriteResult wr = coll.insert(dbo);
        String id = dbo.get(Operator.ID).toString();
        BuguEntity ent = (BuguEntity)t;
        ent.setId(id);
        if(luceneListener != null){
            luceneListener.entityInsert(ent);
        }
        return wr;
    }
    
    /**
     * Insert a list of entity to mongoDB.
     * @param list 
     * @return 
     */
    public WriteResult insert(List<T> list){
        List<DBObject> dboList = new ArrayList<DBObject>();
        for(T t : list){
            dboList.add(MapperUtil.toDBObject(t));
        }
        WriteResult wr = coll.insert(dboList);
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
        WriteResult wr = coll.save(MapperUtil.toDBObject(ent));
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
        if(luceneListener != null || cascadeListener.hasCascade()){
            List<T> list = findAll();
            for(T t : list){
                remove(t);
            }
        }
        coll.drop();
        coll.dropIndexes();
    }
    
    /**
     * Remove an entity has id value in it.
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
        if(cascadeListener.hasCascade()){
            BuguEntity entity = (BuguEntity)findOne(id);
            cascadeListener.entityRemove(entity);
        }
        if(luceneListener != null){
            luceneListener.entityRemove(id);
        }
        DBObject query = new BasicDBObject();
        query.put(Operator.ID, IdUtil.toDbId(clazz, id));
        return coll.remove(query);
    }
    
    /**
     * Remove by array of id.
     * @param ids 
     * @return 
     */
    public WriteResult remove(String... ids){
        int len = ids.length;
        if(cascadeListener.hasCascade()){
            for(int i=0; i<len; i++){
                BuguEntity entity = (BuguEntity)findOne(ids[i]);
                cascadeListener.entityRemove(entity);
            }
        }
        if(luceneListener != null){
            for(int i=0; i<len; i++){
                luceneListener.entityRemove(ids[i]);
            }
        }
        Object[] arr = new Object[len];
        for(int i=0; i<len; i++){
            arr[i] = IdUtil.toDbId(clazz, ids[i]);
        }
        DBObject in = new BasicDBObject(Operator.IN, arr);
        DBObject query = new BasicDBObject(Operator.ID, in);
        return coll.remove(query);
    }
    
    /**
     * Remove entity by condition.
     * @param key the condition field
     * @param value the condition value
     * @return 
     */
    public WriteResult remove(String key, Object value){
        if(value instanceof BuguEntity){
            BuguEntity be = (BuguEntity)value;
            value = ReferenceUtil.toDbReference(clazz, key, be.getClass(), be.getId());
        }
        return remove(new BasicDBObject(key, value));
    }
    
    /**
     * Remove by condition.
     * @param query 
     * @return 
     */
    public WriteResult remove(BuguQuery query){
        return remove(query.getCondition());
    }
    
    private WriteResult remove(DBObject query){
        DBCursor cursor = coll.find(query, keys);
        List<T> list = MapperUtil.toList(clazz, cursor);
        if(cascadeListener.hasCascade()){
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
        return coll.remove(query);
    }
    
    private WriteResult update(String id, DBObject dbo){
        WriteResult wr = updateWithOutIndex(id, dbo);
        if(luceneListener != null){
            BuguEntity entity = (BuguEntity)findOne(id);
            luceneListener.entityUpdate(entity);
        }
        return wr;
    }
    
    private WriteResult updateWithOutIndex(String id, DBObject dbo){
        DBObject query = new BasicDBObject(Operator.ID, IdUtil.toDbId(clazz, id));
        return coll.update(query, dbo);
    }
    
    /**
     * Update some entities, with new key/value pairs.
     * Notice: EmbedList and RefList fields is not supported yet.
     * @param query the query condition
     * @param key the field's name
     * @param value the field's new value
     * @return 
     */
    public WriteResult set(BuguQuery query, String key, Object value){
        if(value instanceof BuguEntity){
            BuguEntity be = (BuguEntity)value;
            value = ReferenceUtil.toDbReference(clazz, key, be.getClass(), be.getId());
        }else if(!(value instanceof DBObject) && 
                FieldsCache.getInstance().isEmbedField(clazz, key)){
            value = MapperUtil.toDBObject(value);
        }
        DBObject dbo = new BasicDBObject(key, value);
        return set(query.getCondition(), dbo);
    }
    
    /**
     * Update some entities, with new key/value pairs.
     * Notice: the Map values must can be converted to DBObject.
     * @param query the query condition
     * @param values
     * @return the new key/value pairs
     */
    public WriteResult set(BuguQuery query, Map values){
        DBObject dbo = new BasicDBObject(values);
        return set(query.getCondition(), dbo);
    }
    
    private WriteResult set(DBObject query, DBObject dbo){
        List ids = null;
        if(luceneListener != null){
            ids = coll.distinct(Operator.ID, query);
        }
        WriteResult wr = coll.updateMulti(query, new BasicDBObject(Operator.SET,dbo));
        if(luceneListener != null){
            for(Object id : ids){
                BuguEntity entity = (BuguEntity)findOne(id.toString());
                luceneListener.entityUpdate(entity);
            }
        }
        return wr;
    }
    
    /**
     * Update a field's value of an entity.
     * Notice: EmbedList and RefList fields is not supported yet.
     * @param t the entity needs to update
     * @param key the field's name
     * @param value the field's new value
     * @return 
     */
    public WriteResult set(T t, String key, Object value){
        BuguEntity ent = (BuguEntity)t;
        return set(ent.getId(), key, value);
    }
    
    /**
     * Update a field's value of an entity.
     * Notice: EmbedList and RefList fields is not supported yet.
     * @param id the entity's id
     * @param key the field's name
     * @param value the field's new value
     * @return 
     */
    public WriteResult set(String id, String key, Object value){
        if(value instanceof BuguEntity){
            BuguEntity be = (BuguEntity)value;
            value = ReferenceUtil.toDbReference(clazz, key, be.getClass(), be.getId());
        }else if(!(value instanceof DBObject) && 
                FieldsCache.getInstance().isEmbedField(clazz, key)){
            value = MapperUtil.toDBObject(value);
        }
        DBObject query = new BasicDBObject(key, value);
        DBObject set = new BasicDBObject(Operator.SET, query);
        if(luceneListener != null && IndexChecker.hasIndexAnnotation(clazz, key)){
            return update(id, set);
        }else{
            return updateWithOutIndex(id, set);
        }
    }
    
    /**
     * Update an entity, with new key/value pairs.
     * Notice: the Map values must can be converted to DBObject.
     * @param t the entity needs to be updated
     * @param values the new key/value pairs
     * @return 
     */
    public WriteResult set(T t, Map values){
        BuguEntity ent = (BuguEntity)t;
        return set(ent.getId(), values);
    }
    
    /**
     * Update an entity, with new key/value pairs.
     * Notice: the Map values must can be converted to DBObject.
     * @param id the entity's id
     * @param values the new key/value pairs
     * @return 
     */
    public WriteResult set(String id, Map values){
        DBObject dbo = new BasicDBObject(values);
        return update(id, new BasicDBObject(Operator.SET, dbo));
    } 
    
    /**
     * Remove one or more filed(column) of an entity.
     * @param t the entity to operate
     * @param keys the field's name
     * @return 
     */
    public WriteResult unset(T t, String... keys){
        BuguEntity ent = (BuguEntity)t;
        return unset(ent.getId(), keys);
    }
    
    /**
     * Remove one or more filed(column) of an entity
     * @param id the entity's id
     * @param keys the field's name
     * @return 
     */
    public WriteResult unset(String id, String... keys){
        DBObject query = new BasicDBObject();
        for(String key : keys){
            query.put(key, 1);
        }
        DBObject unset = new BasicDBObject(Operator.UNSET, query);
        if(luceneListener != null && IndexChecker.hasIndexAnnotation(clazz, keys)){
            return update(id, unset);
        }else{
            return updateWithOutIndex(id, unset);
        }
    }
    
    /**
     * Remove one or more filed(column).
     * @param query mathcing conditon
     * @param key the field's name
     * @return 
     */
    public WriteResult unset(BuguQuery query, String... keys){
        return unset(query.getCondition(), keys);
    }
    
    private WriteResult unset(DBObject query, String... keys){
        boolean indexField = (luceneListener != null) && IndexChecker.hasIndexAnnotation(clazz, keys); 
        List ids = null;
        if(indexField){
            ids = coll.distinct(Operator.ID, query);
        }
        DBObject dbo = new BasicDBObject();
        for(String key : keys){
            dbo.put(key, 1);
        }
        WriteResult wr = coll.updateMulti(query, new BasicDBObject(Operator.UNSET, dbo));
        if(indexField){
            for(Object id : ids){
                BuguEntity entity = (BuguEntity)findOne(id.toString());
                luceneListener.entityUpdate(entity);
            }
        }
        return wr;
    }
    
    /**
     * Increase a numeric field of an entity.
     * @param t the entity needs to update
     * @param key the field's name
     * @param value the numeric value to be added. It can be positive or negative integer, long, float, double.
     * @return 
     */
    public WriteResult inc(T t, String key, Object value){
        BuguEntity ent = (BuguEntity)t;
        return inc(ent.getId(), key, value);
    }
    
    /**
     * Increase a numeric field of an entity.
     * @param id the entity's id
     * @param key the field's name
     * @param value the numeric value to be added. It can be positive or negative integer, long, float, double.
     * @return 
     */
    public WriteResult inc(String id, String key, Object value){
        DBObject query = new BasicDBObject(key, value);
        DBObject inc = new BasicDBObject(Operator.INC, query);
        if(luceneListener != null && IndexChecker.hasIndexAnnotation(clazz, key)){
            return update(id, inc);
        }else{
            return updateWithOutIndex(id, inc);
        }
    }
    
    /**
     * Increase a numberic field of some entities.
     * @param query the query condition
     * @param key the field's name
     * @param value the numeric value to be added. It can be positive or negative integer, long, float, double.
     * @return 
     */
    public WriteResult inc(BuguQuery query, String key, Object value){
        return inc(query.getCondition(), key, value);
    }
    
    private WriteResult inc(DBObject query, String key, Object value){
        List ids = null;
        if(luceneListener != null){
            ids = coll.distinct(Operator.ID, query);
        }
        DBObject dbo = new BasicDBObject(key, value);
        WriteResult wr = coll.updateMulti(query, new BasicDBObject(Operator.INC, dbo));
        if(luceneListener != null){
            for(Object id : ids){
                BuguEntity entity = (BuguEntity)findOne(id.toString());
                luceneListener.entityUpdate(entity);
            }
        }
        return wr;
    }
    
    /**
     * Add an element to an entity's array/list/set field.
     * @param t the entity needs to update
     * @param key the field's name
     * @param value the element to be added
     * @return 
     */
    public WriteResult push(T t, String key, Object value){
        BuguEntity ent = (BuguEntity)t;
        return push(ent.getId(), key, value);
    }
    
    /**
     * Add an element to an entity's array/list/set field.
     * @param id the entity's id
     * @param key the field's name
     * @param value the element to be addes
     * @return 
     */
    public WriteResult push(String id, String key, Object value){
        if(value instanceof BuguEntity){
            BuguEntity be = (BuguEntity)value;
            value = ReferenceUtil.toDbReference(clazz, key, be.getClass(), be.getId());
        }else if(!(value instanceof DBObject) && 
                FieldsCache.getInstance().isEmbedListField(clazz, key)){
            value = MapperUtil.toDBObject(value);
        }
        DBObject query = new BasicDBObject(key, value);
        DBObject push = new BasicDBObject(Operator.PUSH, query);
        if(luceneListener != null && IndexChecker.hasIndexAnnotation(clazz, key)){
            return update(id, push);
        }else{
            return updateWithOutIndex(id, push);
        }
    }
    
    /**
     * Remove an element of an entity's array/list/set field.
     * @param t the entity needs to update
     * @param key the field's name
     * @param value the element to be removed
     * @return 
     */
    public WriteResult pull(T t, String key, Object value){
        BuguEntity ent = (BuguEntity)t;
        return pull(ent.getId(), key, value);
    }
    
    /**
     * Remove an element of an entity's array/list/set field.
     * @param id the entity's id
     * @param key the field's name
     * @param value the element to be removed
     * @return 
     */
    public WriteResult pull(String id, String key, Object value){
        if(value instanceof BuguEntity){
            BuguEntity be = (BuguEntity)value;
            value = ReferenceUtil.toDbReference(clazz, key, be.getClass(), be.getId());
        }else if(!(value instanceof DBObject) && 
                FieldsCache.getInstance().isEmbedListField(clazz, key)){
            value = MapperUtil.toDBObject(value);
        }
        DBObject query = new BasicDBObject(key, value);
        DBObject pull = new BasicDBObject(Operator.PULL, query);
        if(luceneListener != null && IndexChecker.hasIndexAnnotation(clazz, key)){
            return update(id, pull);
        }else{
            return updateWithOutIndex(id, pull);
        }
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
        if(value instanceof BuguEntity){
            BuguEntity be = (BuguEntity)value;
            value = ReferenceUtil.toDbReference(clazz, key, be.getClass(), be.getId());
        }else if(!(value instanceof DBObject) &&
                (FieldsCache.getInstance().isEmbedField(clazz, key) || FieldsCache.getInstance().isEmbedListField(clazz, key))){
            value = MapperUtil.toDBObject(value);
        }
        DBObject query = new BasicDBObject(key, value);
        return coll.findOne(query) != null;
    }
    
    public T findOne(String id){
        DBObject dbo = new BasicDBObject();
        dbo.put(Operator.ID, IdUtil.toDbId(clazz, id));
        DBObject result = coll.findOne(dbo);
        return MapperUtil.fromDBObject(clazz, result);
    }
    
    public T findOne(String key, Object value){
        if(value instanceof BuguEntity){
            BuguEntity be = (BuguEntity)value;
            value = ReferenceUtil.toDbReference(clazz, key, be.getClass(), be.getId());
        }else if(!(value instanceof DBObject) &&
                (FieldsCache.getInstance().isEmbedField(clazz, key) || FieldsCache.getInstance().isEmbedListField(clazz, key))){
            value = MapperUtil.toDBObject(value);
        }
        DBObject query = new BasicDBObject(key, value);
        DBObject dbo = coll.findOne(query);
        return MapperUtil.fromDBObject(clazz, dbo);
    }


    public List<T> findAll(){
        DBCursor cursor = coll.find(new BasicDBObject(), keys);
        return MapperUtil.toList(clazz, cursor);
    }
    
    public List<T> findAll(String orderBy){
        DBObject dbo = MapperUtil.getSort(orderBy);
        DBCursor cursor = coll.find(new BasicDBObject(), keys).sort(dbo);
        return MapperUtil.toList(clazz, cursor);
    }

    public List<T> findAll(int pageNum, int pageSize){
        DBCursor cursor = coll.find(new BasicDBObject(), keys).skip((pageNum-1)*pageSize).limit(pageSize);
        return MapperUtil.toList(clazz, cursor);
    }
    
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
        if(value instanceof BuguEntity){
            BuguEntity be = (BuguEntity)value;
            value = ReferenceUtil.toDbReference(clazz, key, be.getClass(), be.getId());
        }else if(!(value instanceof DBObject) &&
                (FieldsCache.getInstance().isEmbedField(clazz, key) || FieldsCache.getInstance().isEmbedListField(clazz, key))){
            value = MapperUtil.toDBObject(value);
        }
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
    
}
