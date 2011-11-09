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
import com.bugull.mongo.lucene.annotations.Indexed;
import com.bugull.mongo.lucene.backend.EntityChangedListener;
import com.bugull.mongo.mapper.DBIndex;
import com.bugull.mongo.mapper.MapperUtil;
import com.bugull.mongo.mapper.Operator;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;

/**
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class BuguDao {
    
    protected DBCollection coll;
    protected Class<?> clazz;
    protected DBObject keys;
    protected boolean indexed;
    protected EntityChangedListener listener;
    
    public BuguDao(Class<?> clazz){
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
        if(clazz.getAnnotation(Indexed.class) != null){
            indexed = true;
            listener = new EntityChangedListener(clazz);
        }
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
        if(indexed){
            listener.entityInsert(obj);
        }
    }
    
    /**
     * Insert a list of entity to MongoDB. The entity should have no id in it.
     * @param list 
     */
    public void insert(List<BuguEntity> list){
        if(indexed){
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
            if(indexed){
                listener.entityUpdate(obj);
            }
        }
    }
    
    /**
     * Remove all entity.
     */
    public void drop(){
        if(!indexed){
            coll.drop();
        }else{
            List list = findAll();
            for(Object o : list){
                BuguEntity entity = (BuguEntity)o;
                remove(entity);
            }
            coll.drop();
        }
    }
    
    public void remove(BuguEntity obj){
        remove(obj.getId());
    }

    /**
     * Remove by id.
     * @param id 
     */
    public void remove(String id){
        DBObject query = new BasicDBObject();
        query.put(Operator.ID, new ObjectId(id));
        coll.remove(query);
        if(indexed){
            listener.entityRemove(id);
        }
    }
    
    /**
     * Remove by array of id.
     * @param ids 
     */
    public void remove(String... ids){
        if(indexed){
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
    public void remove(DBObject query){
        List ids = null;
        if(indexed){
            ids = coll.distinct(Operator.ID, query);
        }
        coll.remove(query);
        if(indexed){
            for(Object id : ids){
                listener.entityRemove(id.toString());
            }
        }
    }
    
    public void update(BuguEntity obj, DBObject dbo){
        update(obj.getId(), dbo);
    }
    
    public void update(String id, DBObject dbo){
        updateWithOutIndex(id, dbo);
        if(indexed){
            BuguEntity entity = (BuguEntity)findOne(id);
            listener.entityUpdate(entity);
        }
    }
    
    private void updateWithOutIndex(String id, DBObject dbo){
        DBObject query = new BasicDBObject(Operator.ID, new ObjectId(id));
        coll.update(query, dbo);
    }
    
    public void update(DBObject query, DBObject dbo){
        List ids = null;
        if(indexed){
            ids = coll.distinct(Operator.ID, query);
        }
        coll.updateMulti(query, dbo);
        if(indexed){
            for(Object id : ids){
                BuguEntity entity = (BuguEntity)findOne(id.toString());
                listener.entityUpdate(entity);
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
        if(indexed && MapperUtil.hasIndexAnnotation(clazz, key)){
            update(id, set);
        }else{
            updateWithOutIndex(id, set);
        }
    }
    
    /**
     * Update a numeric field of an entity.
     * @param obj the entity needs to update
     * @param key the field's name
     * @param value the numeric value to be added. It can be positive or negative integer, long, float, double
     */
    public void inc(BuguEntity obj, String key, Object value){
        inc(obj.getId(), key, value);
    }
    
    /**
     * Update a numeric field of an entity.
     * @param id the entity's id
     * @param key the field's name
     * @param value the numeric value to be added. It can be positive or negative integer, long, float, double
     */
    public void inc(String id, String key, Object value){
        DBObject query = new BasicDBObject(key, value);
        DBObject inc = new BasicDBObject(Operator.INC, query);
        if(indexed && MapperUtil.hasIndexAnnotation(clazz, key)){
            update(id, inc);
        }else{
            updateWithOutIndex(id, inc);
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
        if(indexed && MapperUtil.hasIndexAnnotation(clazz, key)){
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
        if(indexed && MapperUtil.hasIndexAnnotation(clazz, key)){
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
        if(dbo != null){
            return true;
        }else{
            return false;
        }
    }
    
    public Object findOne(String id){
        DBObject dbo = new BasicDBObject();
        dbo.put(Operator.ID, new ObjectId(id));
        DBObject result = coll.findOne(dbo);
        return MapperUtil.fromDBObject(clazz, result);
    }
    
    public Object findOne(String key, Object value){
        return findOne(new BasicDBObject(key, value));
    }
    
    public Object findOne(DBObject query){
        DBObject dbo = coll.findOne(query);
        return MapperUtil.fromDBObject(clazz, dbo);
    }

    public List findAll(){
        DBCursor cursor = coll.find(new BasicDBObject(), keys);
        return MapperUtil.toList(clazz, cursor);
    }
    
    public List findAll(String orderBy){
        return findAll(MapperUtil.getSort(orderBy));
    }

    public List findAll(DBObject orderBy){
        DBCursor cursor = coll.find(new BasicDBObject(), keys).sort(orderBy);
        return MapperUtil.toList(clazz, cursor);
    }

    public List findAll(int pageNum, int pageSize){
        DBCursor cursor = coll.find(new BasicDBObject(), keys).skip((pageNum-1)*pageSize).limit(pageSize);
        return MapperUtil.toList(clazz, cursor);
    }
    
    public List findAll(String orderBy, int pageNum, int pageSize){
        return findAll(MapperUtil.getSort(orderBy), pageNum, pageSize);
    }

    public List findAll(DBObject orderBy, int pageNum, int pageSize){
        DBCursor cursor = coll.find(new BasicDBObject(), keys).sort(orderBy).skip((pageNum-1)*pageSize).limit(pageSize);
        return MapperUtil.toList(clazz, cursor);
    }
    
    public List find(String key, Object value){
        return find(new BasicDBObject(key, value));
    }

    public List find(DBObject query){
        DBCursor cursor = coll.find(query, keys);
        return MapperUtil.toList(clazz, cursor);
    }
    
    public List find(String key, Object value, String orderBy){
        return find(new BasicDBObject(key, value), MapperUtil.getSort(orderBy));
    }
    
    public List find(DBObject query, String orderBy){
        return find(query, MapperUtil.getSort(orderBy));
    }

    public List find(DBObject query, DBObject orderBy){
        DBCursor cursor = coll.find(query, keys).sort(orderBy);
        return MapperUtil.toList(clazz, cursor);
    }
    
    public List find(String key, Object value, int pageNum, int pageSize){
        return find(new BasicDBObject(key, value), pageNum, pageSize);
    }

    public List find(DBObject query, int pageNum, int pageSize){
        DBCursor cursor = coll.find(query, keys).skip((pageNum-1)*pageSize).limit(pageSize);
        return MapperUtil.toList(clazz, cursor);
    }
    
    public List find(String key, Object value, String orderBy, int pageNum, int pageSize){
        return find(new BasicDBObject(key, value), MapperUtil.getSort(orderBy), pageNum, pageSize);
    }
    
    public List find(DBObject query, String orderBy, int pageNum, int pageSize){
        return find(query, MapperUtil.getSort(orderBy), pageNum, pageSize);
    }

    public List find(DBObject query, DBObject orderBy, int pageNum, int pageSize){
        DBCursor cursor = coll.find(query, keys).sort(orderBy).skip((pageNum-1)*pageSize).limit(pageSize);
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
    public Query query(){
        return new Query(coll, clazz, keys);
    }
    
}
