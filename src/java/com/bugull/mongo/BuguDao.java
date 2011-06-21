package com.bugull.mongo;

import com.bugull.mongo.annotations.Entity;
import com.bugull.mongo.cache.FieldsCache;
import com.bugull.mongo.lucene.annotations.IndexEmbed;
import com.bugull.mongo.lucene.annotations.IndexProperty;
import com.bugull.mongo.lucene.annotations.IndexRef;
import com.bugull.mongo.lucene.annotations.Indexed;
import com.bugull.mongo.lucene.backend.EntityChangedListener;
import com.mongodb.BasicDBObject;
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
    private EntityChangedListener listener;
    
    public BuguDao(Class<?> clazz){
        this.clazz = clazz;
        Entity entity = clazz.getAnnotation(Entity.class);
        String name = entity.name();
        coll = BuguConnection.getInstance().getDB().getCollection(name);
        //for lucene
        if(clazz.getAnnotation(Indexed.class) != null){
            listener = new EntityChangedListener();
        }
    }
    
    private Object fromDBObject(DBObject dbo){
        if(dbo == null){
            return null;
        }else{
            return new BuguMapper().fromDBObject(clazz, dbo);
        }
    }

    private DBObject toDBObject(Object obj){
        if(obj == null){
            return null;
        }else{
            return new BuguMapper().toDBObject(obj);
        }
    }
    
    private List toList(DBCursor cursor){
        List list = new LinkedList();
        while(cursor.hasNext()){
            DBObject dbo = cursor.next();
            list.add(fromDBObject(dbo));
        }
        return list;
    }
    
    public void insert(BuguEntity obj){
        DBObject dbo = toDBObject(obj);
        coll.insert(dbo);
        String id = dbo.get("_id").toString();
        obj.setId(id);
        if(listener != null){
            listener.entityInsert(obj);
        }
    }
    
    public void save(BuguEntity obj){
        if(obj.getId() == null){
            insert(obj);
        }else{
            coll.save(toDBObject(obj));
            if(listener != null){
                listener.entityUpdate(obj);
            }
        }
    }
    
    public void remove(BuguEntity obj){
        remove(obj.getId());
    }

    public void remove(String id){
        DBObject dbo = new BasicDBObject();
        dbo.put("_id", new ObjectId(id));
        coll.remove(dbo);
        if(listener != null){
            listener.entityRemove(clazz, id);
        }
    }
    
    public void update(BuguEntity obj, DBObject dbo){
        update(obj.getId(), dbo);
    }
    
    public void update(String id, DBObject dbo){
        updateWithOutIndex(id, dbo);
        BuguEntity entity = (BuguEntity)findOne(id);
        if(listener != null){
            listener.entityUpdate(entity);
        }
    }
    
    private void updateWithOutIndex(String id, DBObject dbo){
        DBObject query = new BasicDBObject("_id", new ObjectId(id));
        coll.update(query, dbo);
    }
    
    private boolean hasIndex(String key){
        boolean result = false;
        Field field = FieldsCache.getInstance().getField(clazz, key);
        if(field.getAnnotation(IndexProperty.class)!=null || field.getAnnotation(IndexEmbed.class)!=null || field.getAnnotation(IndexRef.class)!= null){
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
        if(hasIndex(key)){
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
        if(hasIndex(key)){
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
        DBCursor cursor = coll.find(query);
        if(cursor!=null && cursor.length()>0){
            return true;
        }else{
            return false;
        }
    }
    
    public Object findOne(String id){
        DBObject dbo = new BasicDBObject();
        dbo.put("_id", new ObjectId(id));
        DBObject result = coll.findOne(dbo);
        return fromDBObject(result);
    }
    
    public Object findOne(DBObject query){
        DBObject dbo = coll.findOne(query);
        return fromDBObject(dbo);
    }

    public List findAll(){
        DBCursor cursor = coll.find();
        return toList(cursor);
    }

    public List findAll(DBObject orderBy){
        DBCursor cursor = coll.find().sort(orderBy);
        return toList(cursor);
    }

    public List findAll(int pageNum, int pageSize){
        DBCursor cursor = coll.find().skip((pageNum-1)*pageSize).limit(pageSize);
        return toList(cursor);
    }

    public List findAll(DBObject orderBy, int pageNum, int pageSize){
        DBCursor cursor = coll.find().sort(orderBy).skip((pageNum-1)*pageSize).limit(pageSize);
        return toList(cursor);
    }

    public List find(DBObject query){
        DBCursor cursor = coll.find(query);
        return toList(cursor);
    }

    public List find(DBObject query, DBObject orderBy){
        DBCursor cursor = coll.find(query).sort(orderBy);
        return toList(cursor);
    }

    public List find(DBObject query, int pageNum, int pageSize){
        DBCursor cursor = coll.find(query).skip((pageNum-1)*pageSize).limit(pageSize);
        return toList(cursor);
    }

    public List find(DBObject query, DBObject orderBy, int pageNum, int pageSize){
        DBCursor cursor = coll.find(query).sort(orderBy).skip((pageNum-1)*pageSize).limit(pageSize);
        return toList(cursor);
    }

    public long count(){
        return coll.count();
    }

    public long count(DBObject query){
        return coll.count(query);
    }

    public List distinct(String key){
        return coll.distinct(key);
    }

    public List distinct(String key, DBObject query){
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
