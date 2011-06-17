
package com.bugull.mongo;

import com.bugull.mongo.annotations.Entity;
import com.bugull.mongo.cache.ConstructorCache;
import com.bugull.mongo.cache.FieldsCache;
import com.bugull.mongo.decoder.Decoder;
import com.bugull.mongo.decoder.DecoderFactory;
import com.bugull.mongo.encoder.Encoder;
import com.bugull.mongo.encoder.EncoderFactory;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import java.lang.reflect.Field;
import org.bson.types.ObjectId;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class BuguMapper {
    
    public Object fromDBObject(Class clazz, DBObject dbo){
        Object obj = ConstructorCache.getInstance().create(clazz);
        Field[] fields = FieldsCache.getInstance().get(clazz);
        for(Field field : fields){
            Decoder decoder = DecoderFactory.create(field, dbo);
            decoder.decode(obj);
        }
        return obj;
    }
    
    public DBObject toDBObject(Object obj){
        DBObject dbo = new BasicDBObject();
        Class<?> clazz = obj.getClass();
        Field[] fields = FieldsCache.getInstance().get(clazz);
        for(Field field : fields){
            Encoder encoder = EncoderFactory.create(obj, field);
            if(!encoder.isNullField()){
                dbo.put(encoder.getFieldName(), encoder.encode());
            }
        }
        return dbo;
    }
    
    public DBRef toDBRef(BuguEntity obj){
        DB db = BuguConnection.getInstance().getDB();
        Class<?> clazz = obj.getClass();
        Entity entity = clazz.getAnnotation(Entity.class);
        String name = entity.name();
        ObjectId id = new ObjectId(obj.getId());
        return new DBRef(db, name, id);
    }
    
}
