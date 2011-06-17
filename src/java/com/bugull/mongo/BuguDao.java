package com.bugull.mongo;

import com.bugull.mongo.annotations.Entity;
import com.bugull.mongo.lucene.annotations.Indexed;
import com.bugull.mongo.lucene.backend.EntityChangedListener;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
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
        coll.remove(toDBObject(obj));
        if(listener != null){
            listener.entityRemove(clazz, obj.getId());
        }
    }

    public void remove(String id){
        DBObject dbo = new BasicDBObject();
        dbo.put("_id", new ObjectId(id));
        coll.remove(dbo);
        if(listener != null){
            listener.entityRemove(clazz, id);
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
