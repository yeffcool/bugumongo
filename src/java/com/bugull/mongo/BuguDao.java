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

import com.bugull.mongo.annotations.Entity;
import com.bugull.mongo.cache.FieldsCache;
import com.bugull.mongo.lucene.annotations.IndexEmbed;
import com.bugull.mongo.lucene.annotations.IndexFilter;
import com.bugull.mongo.lucene.annotations.IndexProperty;
import com.bugull.mongo.lucene.annotations.IndexRef;
import com.bugull.mongo.lucene.annotations.Indexed;
import com.bugull.mongo.lucene.backend.EntityChangedListener;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import org.bson.types.ObjectId;

/**
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class BuguDao {
    
    private DBCollection coll;
    private Class<?> clazz;
    private boolean indexed;
    private EntityChangedListener listener;
    
    public BuguDao(Class<?> clazz){
        this.clazz = clazz;
        Entity entity = clazz.getAnnotation(Entity.class);
        //the collection name
        String name = entity.name();
        if(name.equals("")){
            name = clazz.getSimpleName().toLowerCase();
        }
        //if capped
        DB db = BuguConnection.getInstance().getDB();
        if(entity.capped() && !db.collectionExists(name)){
            DBObject options = new BasicDBObject();
            options.put("capped", true);
            options.put("size", entity.capSize());
            coll = db.createCollection(name, options);
        }else{
            coll = db.getCollection(name);
        }
        //for lucene
        if(clazz.getAnnotation(Indexed.class) != null){
            indexed = true;
            listener = new EntityChangedListener();
        }
    }
    
    private Object fromDBObject(DBObject dbo){
        if(dbo == null){
            return null;
        }else{
            return BuguMapper.fromDBObject(clazz, dbo);
        }
    }

    private DBObject toDBObject(Object obj){
        if(obj == null){
            return null;
        }else{
            return BuguMapper.toDBObject(obj);
        }
    }
    
    private List toList(DBCursor cursor){
        List list = new LinkedList();
        while(cursor.hasNext()){
            DBObject dbo = cursor.next();
            list.add(fromDBObject(dbo));
        }
        cursor.close();
        return list;
    }
    
    public void insert(BuguEntity obj){
        DBObject dbo = toDBObject(obj);
        coll.insert(dbo);
        String id = dbo.get("_id").toString();
        obj.setId(id);
        if(indexed){
            listener.entityInsert(obj);
        }
    }
    
    public void save(BuguEntity obj){
        if(obj.getId() == null){
            insert(obj);
        }else{
            coll.save(toDBObject(obj));
            if(indexed){
                listener.entityUpdate(obj);
            }
        }
    }
    
    public void removeAll(){
        if(!indexed){
            coll.drop();
        }else{
            List list = findAll();
            for(Object o : list){
                BuguEntity entity = (BuguEntity)o;
                remove(entity);
            }
        }
    }
    
    public void remove(BuguEntity obj){
        remove(obj.getId());
    }

    public void remove(String id){
        DBObject query = new BasicDBObject();
        query.put("_id", new ObjectId(id));
        coll.remove(query);
        if(indexed){
            listener.entityRemove(clazz, id);
        }
    }
    
    public void remove(String key, Object value){
        remove(new BasicDBObject(key, value));
    }
    
    public void remove(DBObject query){
        List ids = null;
        if(indexed){
            ids = coll.distinct("_id", query);
        }
        coll.remove(query);
        if(indexed){
            for(Object id : ids){
                listener.entityRemove(clazz, id.toString());
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
        DBObject query = new BasicDBObject("_id", new ObjectId(id));
        coll.update(query, dbo);
    }
    
    public void update(DBObject query, DBObject dbo){
        List ids = null;
        if(indexed){
            ids = coll.distinct("_id", query);
        }
        coll.updateMulti(query, dbo);
        if(indexed){
            for(Object id : ids){
                BuguEntity entity = (BuguEntity)findOne(id.toString());
                listener.entityUpdate(entity);
            }
        }
    }
    
    private boolean hasIndexAnnotation(String key){
        if(!indexed){
            return false;
        }
        boolean result = false;
        Field field = FieldsCache.getInstance().getField(clazz, key);
        if(field.getAnnotation(IndexProperty.class)!=null
                || field.getAnnotation(IndexEmbed.class)!=null
                || field.getAnnotation(IndexRef.class)!= null
                || field.getAnnotation(IndexFilter.class)!=null){
            result = true;
        }
        return result;
    }
    
    public void set(BuguEntity obj, String key, Object value){
        set(obj.getId(), key, value);
    }
    
    public void set(String id, String key, Object value){
        DBObject query = new BasicDBObject(key, value);
        DBObject set = new BasicDBObject("$set", query);
        if(hasIndexAnnotation(key)){
            update(id, set);
        }else{
            updateWithOutIndex(id, set);
        }
    }
    
    public void inc(BuguEntity obj, String key, Object value){
        inc(obj.getId(), key, value);
    }
    
    public void inc(String id, String key, Object value){
        DBObject query = new BasicDBObject(key, value);
        DBObject inc = new BasicDBObject("$inc", query);
        if(hasIndexAnnotation(key)){
            update(id, inc);
        }else{
            updateWithOutIndex(id, inc);
        }
    }
    
    public void push(BuguEntity obj, String key, Object value){
        push(obj.getId(), key, value);
    }
    
    public void push(String id, String key, Object value){
        DBObject query = new BasicDBObject(key, value);
        DBObject push = new BasicDBObject("$push", query);
        updateWithOutIndex(id, push);
    }
    
    public void pull(BuguEntity obj, String key, Object value){
        pull(obj.getId(), key, value);
    }
    
    public void pull(String id, String key, Object value){
        DBObject query = new BasicDBObject(key, value);
        DBObject pull = new BasicDBObject("$pull", query);
        updateWithOutIndex(id, pull);
    }
    
    public boolean exists(String key, Object value){
        return exists(new BasicDBObject(key, value));
    }
    
    public boolean exists(DBObject query){
        DBObject dbo = coll.findOne(query);
        if(dbo != null){
            return true;
        }else{
            return false;
        }
    }
    
    public boolean exists(BuguEntity obj, String key, Object value){
        DBObject query = new BasicDBObject("_id", new ObjectId(obj.getId()));
        query.put(key, value);
        return exists(query);
    }
    
    public Object findOne(String id){
        DBObject dbo = new BasicDBObject();
        dbo.put("_id", new ObjectId(id));
        DBObject result = coll.findOne(dbo);
        return fromDBObject(result);
    }
    
    public Object findOne(String key, Object value){
        return findOne(new BasicDBObject(key, value));
    }
    
    public Object findOne(DBObject query){
        DBObject dbo = coll.findOne(query);
        return fromDBObject(dbo);
    }
    
    private DBObject getSort(String orderBy){
        String[] arr = orderBy.split(":");
        String key = arr[0].trim();
        String value = arr[1].trim();
        return new BasicDBObject(key, Integer.parseInt(value));
    }

    public List findAll(){
        DBCursor cursor = coll.find();
        return toList(cursor);
    }
    
    public List findAll(String orderBy){
        return findAll(getSort(orderBy));
    }

    public List findAll(DBObject orderBy){
        DBCursor cursor = coll.find().sort(orderBy);
        return toList(cursor);
    }

    public List findAll(int pageNum, int pageSize){
        DBCursor cursor = coll.find().skip((pageNum-1)*pageSize).limit(pageSize);
        return toList(cursor);
    }
    
    public List findAll(String orderBy, int pageNum, int pageSize){
        return findAll(getSort(orderBy), pageNum, pageSize);
    }

    public List findAll(DBObject orderBy, int pageNum, int pageSize){
        DBCursor cursor = coll.find().sort(orderBy).skip((pageNum-1)*pageSize).limit(pageSize);
        return toList(cursor);
    }
    
    public List find(String key, Object value){
        return find(new BasicDBObject(key, value));
    }

    public List find(DBObject query){
        DBCursor cursor = coll.find(query);
        return toList(cursor);
    }
    
    public List find(String key, Object value, String orderBy){
        return find(new BasicDBObject(key, value), getSort(orderBy));
    }
    
    public List find(DBObject query, String orderBy){
        return find(query, getSort(orderBy));
    }

    public List find(DBObject query, DBObject orderBy){
        DBCursor cursor = coll.find(query).sort(orderBy);
        return toList(cursor);
    }
    
    public List find(String key, Object value, int pageNum, int pageSize){
        return find(new BasicDBObject(key, value), pageNum, pageSize);
    }

    public List find(DBObject query, int pageNum, int pageSize){
        DBCursor cursor = coll.find(query).skip((pageNum-1)*pageSize).limit(pageSize);
        return toList(cursor);
    }
    
    public List find(String key, Object value, String orderBy, int pageNum, int pageSize){
        return find(new BasicDBObject(key, value), getSort(orderBy), pageNum, pageSize);
    }
    
    public List find(DBObject query, String orderBy, int pageNum, int pageSize){
        return find(query, getSort(orderBy), pageNum, pageSize);
    }

    public List find(DBObject query, DBObject orderBy, int pageNum, int pageSize){
        DBCursor cursor = coll.find(query).sort(orderBy).skip((pageNum-1)*pageSize).limit(pageSize);
        return toList(cursor);
    }

    public long count(){
        return coll.count();
    }
    
    public long count(String key, Object value){
        return count(new BasicDBObject(key, value));
    }

    public long count(DBObject query){
        return coll.count(query);
    }

    public List distinct(String key){
        if(key.equals("id")){
            key = "_id";
        }
        return coll.distinct(key);
    }

    public List distinct(String key, DBObject query){
        if(key.equals("id")){
            key = "_id";
        }
        return coll.distinct(key, query);
    }
    
    public Iterable<DBObject> mapReduce(DBObject command){
        return coll.mapReduce(command).results();
    }
    
    public Iterable<DBObject> mapReduce(MapReduceCommand command) {
        return coll.mapReduce(command).results();
    }
    
    public Iterable<DBObject> mapReduce(String map, String reduce, String outputTarget, DBObject query) {
        return coll.mapReduce(map, reduce, outputTarget, query).results();
    }
    
    public Iterable<DBObject> mapReduce(String map, String reduce, String outputTarget, MapReduceCommand.OutputType outputType, DBObject query) {
        return coll.mapReduce(map, reduce, outputTarget, outputType, query).results();
    }
    
}
