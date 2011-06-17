package com.bugull.mongo.decoder;

import com.bugull.mongo.annotations.Embed;
import com.bugull.mongo.annotations.Id;
import com.bugull.mongo.annotations.Property;
import com.bugull.mongo.annotations.Ref;
import com.bugull.mongo.annotations.RefList;
import com.mongodb.DBObject;
import java.lang.reflect.Field;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class DecoderFactory {
    
    public static Decoder create(Field field, DBObject dbo){
        Decoder decoder = null;
        if(field.getAnnotations().length == 0 || field.getAnnotation(Property.class) != null){
            decoder = new PropertyDecoder(field, dbo);
        }
        else if(field.getAnnotation(Id.class) != null){
            decoder = new IdDecoder(field, dbo);
        }
        else if(field.getAnnotation(Embed.class) != null){
            decoder = new EmbedDecoder(field, dbo);
        }
        else if(field.getAnnotation(Ref.class) != null){
            decoder = new RefDecoder(field, dbo);
        }
        else if(field.getAnnotation(RefList.class) != null){
            decoder = new RefListDecoder(field, dbo);
        }
        return decoder;
    }
    
}
