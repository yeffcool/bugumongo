package com.bugull.mongo.encoder;

import com.bugull.mongo.annotations.Embed;
import com.bugull.mongo.annotations.Id;
import com.bugull.mongo.annotations.Property;
import com.bugull.mongo.annotations.Ref;
import com.bugull.mongo.annotations.RefList;
import java.lang.reflect.Field;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class EncoderFactory {
    
    public static Encoder create(Object obj, Field field){
        Encoder encoder = null;
        if(field.getAnnotation(Id.class) != null){
            encoder = new IdEncoder(obj, field);
        }
        else if(field.getAnnotation(Embed.class) != null){
            encoder = new EmbedEncoder(obj, field);
        }
        else if(field.getAnnotation(Ref.class) != null){
            encoder = new RefEncoder(obj, field);
        }
        else if(field.getAnnotation(RefList.class) != null){
            encoder = new RefListEncoder(obj, field);
        }
        else{
            encoder = new PropertyEncoder(obj, field);
        }
        return encoder;
    }
    
}
