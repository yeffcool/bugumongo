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

package com.bugull.mongo;

import com.bugull.mongo.annotations.EnsureIndex;
import com.bugull.mongo.annotations.Entity;
import com.bugull.mongo.lucene.backend.EntityChangedListener;
import com.bugull.mongo.lucene.backend.IndexChecker;
import com.bugull.mongo.mapper.DBIndex;
import com.bugull.mongo.mapper.EntityRemovedListener;
import com.bugull.mongo.mapper.MapperUtil;
import com.bugull.mongo.mapper.Operator;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bson.types.ObjectId;

/**
 * The basic Dao class.
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class BuguDao<T> {
    
    protected DBCollection coll;
    protected Class<T> clazz;
    protected DBObject keys;
    protected EntityChangedListener luceneListener;
    protected EntityRemovedListener cascadeListener;
    
    public BuguDao(Class<T> clazz){
        this.clazz = clazz;
        DB db = BuguConnection.getInstance().getDB();
        Entity entity = clazz.getAnnotation(Entity.class);
        String name = MapperUtil.getEntityName(clazz);
        //if capped
        if(entity.capped() && !db.collectionExists(name)){
            DBObject options = new BasicDBObject();
            options.put("capped", true);
            options.put("size", entity.capSize());
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
     * Insert an entity to MongoDB. The entity should have no id in it.
     * @param obj 
     */
    public void insert(BuguEntity obj){
        DBObject dbo = MapperUtil.toDBObject(obj);
        coll.insert(dbo);
        String id = dbo.get(Operator.ID).toString();
        obj.setId(id);
        if(luceneListener != null){
            luceneListener.entityInsert(obj);
        }
    }
    
    /**
     * Insert a list of entity to MongoDB. The entity should have no id in it.
     * @param list 
     */
    public void insert(List<BuguEntity> list){
        if(luceneListener != null){
            for(BuguEntity obj : list){
                insert(obj);
            }
        }else{
            List<DBObject> dboList = new ArrayList<DBObject>();
            for(BuguEntity obj : list){
                dboList.add(MapperUtil.toDBObject(obj));
            }
            coll.insert(dboList);
        }
    }
    
    /**
     * Save an entity. If no id in it, then insert the entity, else update the entity.
     * @param obj 
     */
    public void save(BuguEntity obj){
        if(obj.getId() == null){
            insert(obj);
        }else{
            coll.save(MapperUtil.toDBObject(obj));
            if(luceneListener != null){
                luceneListener.entityUpdate(obj);
            }
        }
    }
    
    /**
     * Remove all entity.
     */
    public void drop(){
        if(luceneListener == null && !cascadeListener.hasCascade()){
            coll.drop();
        }else{
            List list = findAll();
            for(Object o : list){
                BuguEntity entity = (BuguEntity)o;
                removeRich(entity);
            }
            coll.drop();
        }
    }
    
    private void removeRich(BuguEntity entity){
        DBObject query = new BasicDBObject();
        query.put(Operator.ID, new ObjectId(entity.getId()));
        coll.remove(query);
        if(luceneListener != null){
            luceneListener.entityRemove(entity.getId());
        }
        if(cascadeListener.hasCascade()){
            cascadeListener.entityRemove(entity);
        }
    }
    
    private void removeThin(String id){
        DBObject query = new BasicDBObject();
        query.put(Operator.ID, new ObjectId(id));
        coll.remove(query);
        if(luceneListener != null){
            luceneListener.entityRemove(id);
        }
    }
    
    public void remove(BuguEntity obj){
        if(cascadeListener.hasCascade()){
            BuguEntity entity = (BuguEntity)findOne(obj.getId());
            removeRich(entity);
        }else{
            removeThin(obj.getId());
        }
    }

    /**
     * Remove by id.
     * @param id 
     */
    public void remove(String id){
        if(cascadeListener.hasCascade()){
            BuguEntity entity = (BuguEntity)findOne(id);
            removeRich(entity);
        }else{
            removeThin(id);
        }
    }
    
    /**
     * Remove by array of id.
     * @param ids 
     */
    public void remove(String... ids){
        if(luceneListener != null || cascadeListener.hasCascade()){
            for(String id : ids){
                remove(id);
            }
        }else{
            int len = ids.length;
            ObjectId[] arr = new ObjectId[len];
            for(int i=0; i<len; i++){
                arr[i] = new ObjectId(ids[i]);
            }
            DBObject in = new BasicDBObject(Operator.IN, arr);
            DBObject query = new BasicDBObject(Operator.ID, in);
            coll.remove(query);
        }
    }
    
    /**
     * Remove entity by condition.
     * @param key the condition field
     * @param value the condition value
     */
    public void remove(String key, Object value){
        remove(new BasicDBObject(key, value));
    }
    
    /**
     * Remove by condition.
     * @param query 
     */
    public void remove(BuguQuery query){
        remove(query.getCondition());
    }
    
    /**
     * Remove by condition.
     * @param query 
     */
    public void remove(DBObject query){
        if(luceneListener != null || cascadeListener.hasCascade()){
            List ids = coll.distinct(Operator.ID, query);
            for(Object id : ids){
                remove(id.toString());
            }
        }else{
            coll.remove(query);
        }
    }
    
    private void update(String id, DBObject dbo){
        updateWithOutIndex(id, dbo);
        if(luceneListener != null){
            BuguEntity entity = (BuguEntity)findOne(id);
            luceneListener.entityUpdate(entity);
        }
    }
    
    private void updateWithOutIndex(String id, DBObject dbo){
        DBObject query = new BasicDBObject(Operator.ID, new ObjectId(id));
        coll.update(query, dbo);
    }
    
    /**
     * Update an entity, with new key/value pairs.
     * @param obj the entity needs to be updated
     * @param values the new key/value pairs
     */
    public void set(BuguEntity obj, Map values){
        DBObject dbo = new BasicDBObject(values);
        update(obj.getId(), new BasicDBObject(Operator.SET, dbo));
    }
    
    /**
     * Update an entity, with new key/value pairs.
     * @param obj the entity needs to be updated
     * @param dbo the new key/value pairs.
     */
    public void set(BuguEntity obj, DBObject dbo){
        update(obj.getId(), new BasicDBObject(Operator.SET, dbo));
    }
    
    /**
     * Update an entity, with new key/value pairs.
     * @param id the entity's id
     * @param values the new key/value pairs
     */
    public void set(String id, Map values){
        DBObject dbo = new BasicDBObject(values);
        update(id, new BasicDBObject(Operator.SET, dbo));
    }
    
    /**
     * Update an entity, with new key/value pairs.
     * @param id the entity's id
     * @param dbo the new key/value pairs
     */
    public void set(String id, DBObject dbo){
        update(id, new BasicDBObject(Operator.SET, dbo));
    }
    
    /**
     * Update some entities, with new key/value pairs.
     * @param query the query condition
     * @param values the new key/value pairs
     */
    public void set(BuguQuery query, Map values){
        set(query.getCondition(), new BasicDBObject(values));
    }
    
    /**
     * Update some entities, with new key/value pairs.
     * @param query the query condition
     * @param dbo the new key/value pairs
     */
    public void set(DBObject query, DBObject dbo){
        List ids = null;
        if(luceneListener != null){
            ids = coll.distinct(Operator.ID, query);
        }
        coll.updateMulti(query, new BasicDBObject(Operator.SET,dbo));
        if(luceneListener != null){
            for(Object id : ids){
                BuguEntity entity = (BuguEntity)findOne(id.toString());
                luceneListener.entityUpdate(entity);
            }
        }
    }
    
    /**
     * Update a field's value of an entity.
     * @param obj the entity needs to update
     * @param key the field's name
     * @param value the field's new value
     */
    public void set(BuguEntity obj, String key, Object value){
        set(obj.getId(), key, value);
    }
    
    /**
     * Update a field's value of an entity.
     * @param id the entity's id
     * @param key the field's name
     * @param value the field's new value
     */
    public void set(String id, String key, Object value){
        DBObject query = new BasicDBObject(key, value);
        DBObject set = new BasicDBObject(Operator.SET, query);
        if(luceneListener != null && IndexChecker.hasIndexAnnotation(clazz, key)){
            update(id, set);
        }else{
            updateWithOutIndex(id, set);
        }
    }
    
    public void unset(BuguEntity entity, String key){
        unset(entity.getId(), key);
    }
    
    public void unset(String id, String key){
        DBObject query = new BasicDBObject(key, 1);
        DBObject unset = new BasicDBObject(Operator.UNSET, query);
        if(luceneListener != null && IndexChecker.hasIndexAnnotation(clazz, key)){
            update(id, unset);
        }else{
            updateWithOutIndex(id, unset);
        }
    }
    
    public void unset(BuguQuery query, String key){
        unset(query.getCondition(), key);
    }
    
    public void unset(DBObject query, String key){
        boolean indexField = (luceneListener != null) && IndexChecker.hasIndexAnnotation(clazz, key); 
        List ids = null;
        if(indexField){
            ids = coll.distinct(Operator.ID, query);
        }
        DBObject dbo = new BasicDBObject(key, 1);
        coll.updateMulti(query, new BasicDBObject(Operator.UNSET, dbo));
        if(indexField){
            for(Object id : ids){
                BuguEntity entity = (BuguEntity)findOne(id.toString());
                luceneListener.entityUpdate(entity);
            }
        }
    }
    
    /**
     * Increase a numeric field of an entity.
     * @param obj the entity needs to update
     * @param key the field's name
     * @param value the numeric value to be added. It can be positive or negative integer, long, float, double
     */
    public void inc(BuguEntity obj, String key, Object value){
        inc(obj.getId(), key, value);
    }
    
    /**
     * Increase a numeric field of an entity.
     * @param id the entity's id
     * @param key the field's name
     * @param value the numeric value to be added. It can be positive or negative integer, long, float, double
     */
    public void inc(String id, String key, Object value){
        DBObject query = new BasicDBObject(key, value);
        DBObject inc = new BasicDBObject(Operator.INC, query);
        if(luceneListener != null && IndexChecker.hasIndexAnnotation(clazz, key)){
            update(id, inc);
        }else{
            updateWithOutIndex(id, inc);
        }
    }
    
    /**
     * Increase a numberic field of some entities.
     * @param query the query condition
     * @param key the field's name
     * @param value the numeric value to be added. It can be positive or negative integer, long, float, double
     */
    public void inc(BuguQuery query, String key, Object value){
        inc(query.getCondition(), key, value);
    }
    
    /**
     * Increase a numberic field of some entities.
     * @param query the query condition
     * @param key the field's name
     * @param value the numeric value to be added. It can be positive or negative integer, long, float, double
     */
    public void inc(DBObject query, String key, Object value){
        List ids = null;
        if(luceneListener != null){
            ids = coll.distinct(Operator.ID, query);
        }
        DBObject dbo = new BasicDBObject(key, value);
        coll.updateMulti(query, new BasicDBObject(Operator.INC, dbo));
        if(luceneListener != null){
            for(Object id : ids){
                BuguEntity entity = (BuguEntity)findOne(id.toString());
                luceneListener.entityUpdate(entity);
            }
        }
    }
    
    /**
     * Add an element to an entity's array/list/set field.
     * @param obj the entity needs to update
     * @param key the field's name
     * @param value the element to be added
     */
    public void push(BuguEntity obj, String key, Object value){
        push(obj.getId(), key, value);
    }
    
    /**
     * Add an element to an entity's array/list/set field.
     * @param id the entity's id
     * @param key the field's name
     * @param value the element to be addes
     */
    public void push(String id, String key, Object value){
        DBObject query = new BasicDBObject(key, value);
        DBObject push = new BasicDBObject(Operator.PUSH, query);
        if(luceneListener != null && IndexChecker.hasIndexAnnotation(clazz, key)){
            update(id, push);
        }else{
            updateWithOutIndex(id, push);
        }
    }
    
    /**
     * Remove an element of an entity's array/list/set field.
     * @param obj the entity needs to update
     * @param key the field's name
     * @param value the element to be removed
     */
    public void pull(BuguEntity obj, String key, Object value){
        pull(obj.getId(), key, value);
    }
    
    /**
     * Remove an element of an entity's array/list/set field.
     * @param id the entity's id
     * @param key the field's name
     * @param value the element to be removed
     */
    public void pull(String id, String key, Object value){
        DBObject query = new BasicDBObject(key, value);
        DBObject pull = new BasicDBObject(Operator.PULL, query);
        if(luceneListener != null && IndexChecker.hasIndexAnnotation(clazz, key)){
            update(id, pull);
        }else{
            updateWithOutIndex(id, pull);
        }
    }
    
    /**
     * Check if any entity match the condition.
     * @param key the condition field
     * @param value the condition value
     * @return 
     */
    public boolean exists(String key, Object value){
        return exists(new BasicDBObject(key, value));
    }
    
    /**
     * Check if any entity match the condition
     * @param query the condition
     * @return 
     */
    public boolean exists(DBObject query){
        DBObject dbo = coll.findOne(query);
        return dbo != null;
    }
    
    public T findOne(String id){
        DBObject dbo = new BasicDBObject();
        dbo.put(Operator.ID, new ObjectId(id));
        DBObject result = coll.findOne(dbo);
        return MapperUtil.fromDBObject(clazz, result);
    }
    
    public T findOne(String key, Object value){
        return findOne(new BasicDBObject(key, value));
    }
    
    public T findOne(DBObject query){
        DBObject dbo = coll.findOne(query);
        return MapperUtil.fromDBObject(clazz, dbo);
    }

    public List<T> findAll(){
        DBCursor cursor = coll.find(new BasicDBObject(), keys);
        return MapperUtil.toList(clazz, cursor);
    }
    
    public List<T> findAll(String orderBy){
        return findAll(MapperUtil.getSort(orderBy));
    }

    public List<T> findAll(DBObject orderBy){
        DBCursor cursor = coll.find(new BasicDBObject(), keys).sort(orderBy);
        return MapperUtil.toList(clazz, cursor);
    }

    public List<T> findAll(int pageNum, int pageSize){
        DBCursor cursor = coll.find(new BasicDBObject(), keys).skip((pageNum-1)*pageSize).limit(pageSize);
        return MapperUtil.toList(clazz, cursor);
    }
    
    public List<T> findAll(String orderBy, int pageNum, int pageSize){
        return findAll(MapperUtil.getSort(orderBy), pageNum, pageSize);
    }

    public List<T> findAll(DBObject orderBy, int pageNum, int pageSize){
        DBCursor cursor = coll.find(new BasicDBObject(), keys).sort(orderBy).skip((pageNum-1)*pageSize).limit(pageSize);
        return MapperUtil.toList(clazz, cursor);
    }
    
    public List<T> find(String key, Object value){
        return find(new BasicDBObject(key, value));
    }

    public List<T> find(DBObject query){
        DBCursor cursor = coll.find(query, keys);
        return MapperUtil.toList(clazz, cursor);
    }
    
    public List<T> find(String key, Object value, String orderBy){
        return find(new BasicDBObject(key, value), MapperUtil.getSort(orderBy));
    }
    
    public List<T> find(DBObject query, String orderBy){
        return find(query, MapperUtil.getSort(orderBy));
    }

    public List<T> find(DBObject query, DBObject orderBy){
        DBCursor cursor = coll.find(query, keys).sort(orderBy);
        return MapperUtil.toList(clazz, cursor);
    }
    
    public List<T> find(String key, Object value, int pageNum, int pageSize){
        return find(new BasicDBObject(key, value), pageNum, pageSize);
    }

    public List<T> find(DBObject query, int pageNum, int pageSize){
        DBCursor cursor = coll.find(query, keys).skip((pageNum-1)*pageSize).limit(pageSize);
        return MapperUtil.toList(clazz, cursor);
    }
    
    public List<T> find(String key, Object value, String orderBy, int pageNum, int pageSize){
        return find(new BasicDBObject(key, value), MapperUtil.getSort(orderBy), pageNum, pageSize);
    }
    
    public List<T> find(DBObject query, String orderBy, int pageNum, int pageSize){
        return find(query, MapperUtil.getSort(orderBy), pageNum, pageSize);
    }

    public List<T> find(DBObject query, DBObject orderBy, int pageNum, int pageSize){
        DBCursor cursor = coll.find(query, keys).sort(orderBy).skip((pageNum-1)*pageSize).limit(pageSize);
        return MapperUtil.toList(clazz, cursor);
    }
    
    /**
     * This is used for the automatic lucene index maintaining, do not use this method in your application.
     * @param query
     * @return 
     */
    public List<T> findForLucene(DBObject query){
        DBCursor cursor = coll.find(query);
        return MapperUtil.toList(clazz, cursor);
    }
    
    /**
     * This is used for the automatic lucene index maintaining, do not use this method in your application.
     * @param pageNum
     * @param pageSize
     * @return 
     */
    public List<T> findForLucene(int pageNum, int pageSize){
        DBCursor cursor = coll.find().skip((pageNum-1)*pageSize).limit(pageSize);
        return MapperUtil.toList(clazz, cursor);
    }
    
    public List distinct(String key){
        return coll.distinct(key);
    }

    public List distinct(String key, DBObject query){
        return coll.distinct(key, query);
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
        return count(new BasicDBObject(key, value));
    }

    /**
     * Count by condition
     * @param query the condition
     * @return 
     */
    public long count(DBObject query){
        return coll.count(query);
    }
    
    public DBCollection getCollection(){
        return coll;
    }
    
    /**
     * Create a query.
     * @return a new Query object
     */
    public BuguQuery<T> query(){
        return new BuguQuery<T>(coll, clazz, keys);
    }
    
}
