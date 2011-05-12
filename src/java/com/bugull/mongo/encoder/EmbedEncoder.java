package com.bugull.mongo.encoder;

import com.bugull.mongo.BuguMapper;
import com.bugull.mongo.annotations.Embed;
import java.lang.reflect.Field;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class EmbedEncoder extends AbstractEncoder{
    
    public EmbedEncoder(Object obj, Field field){
        super(obj, field);
    }
    
    @Override
    public String getFieldName(){
        String fieldName = field.getName();
        Embed embed = field.getAnnotation(Embed.class);
        if(embed != null){
            String name = embed.name();
            if(!name.equals("")){
                fieldName = name;
            }
        }
        return fieldName;
    }
    
    @Override
    public Object encode(){
        return new BuguMapper().toDBObject(value);
    }
    
}
