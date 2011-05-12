
package com.bugull.mongo;

import com.bugull.mongo.cache.ConstructorCache;
import com.bugull.mongo.cache.FieldsCache;
import com.bugull.mongo.decoder.Decoder;
import com.bugull.mongo.decoder.DecoderFactory;
import com.bugull.mongo.encoder.Encoder;
import com.bugull.mongo.encoder.EncoderFactory;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import java.lang.reflect.Field;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class BuguMapper {
    
    public Object fromDBObject(Class clazz, DBObject dbo){
        Object obj = ConstructorCache.getInstance().createObject(clazz);
        Field[] fields = FieldsCache.getInstance().getFields(clazz);
        for(Field field : fields){
            Decoder decoder = DecoderFactory.createDecoder(field, dbo);
            decoder.decode(obj);
        }
        return obj;
    }
    
    public DBObject toDBObject(Object obj){
        DBObject dbo = new BasicDBObject();
        Class<?> clazz = obj.getClass();
        Field[] fields = FieldsCache.getInstance().getFields(clazz);
        for(Field field : fields){
            Encoder encoder = EncoderFactory.createEncoder(obj, field);
            if(!encoder.isNullField()){
                dbo.put(encoder.getFieldName(), encoder.encode());
            }
        }
        return dbo;
    }
    
}
