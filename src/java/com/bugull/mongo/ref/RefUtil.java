package com.bugull.mongo.ref;

import com.bugull.mongo.BuguConnection;
import com.bugull.mongo.BuguEntity;
import com.bugull.mongo.annotations.Document;
import com.mongodb.DB;
import com.mongodb.DBRef;
import org.bson.types.ObjectId;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class RefUtil {
    
    public static DBRef toDBRef(BuguEntity entity){
        DB db = BuguConnection.getInstance().getDB();
        Class<?> clazz = entity.getClass();
        Document document = clazz.getAnnotation(Document.class);
        String name = document.name();
        ObjectId id = new ObjectId(entity.getId());
        return new DBRef(db, name, id);
    }
    
}
