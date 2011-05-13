package com.bugull.mongo.ref;

import com.bugull.mongo.BuguConnection;
import com.bugull.mongo.BuguEntity;
import com.bugull.mongo.annotations.Entity;
import com.mongodb.DB;
import com.mongodb.DBRef;
import org.bson.types.ObjectId;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class RefUtil {
    
    public static DBRef toDBRef(BuguEntity obj){
        DB db = BuguConnection.getInstance().getDB();
        Class<?> clazz = obj.getClass();
        Entity entity = clazz.getAnnotation(Entity.class);
        String name = entity.name();
        ObjectId id = new ObjectId(obj.getId());
        return new DBRef(db, name, id);
    }
    
}
