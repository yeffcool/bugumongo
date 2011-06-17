package com.bugull.mongo.decoder;

import com.bugull.mongo.BuguMapper;
import com.bugull.mongo.annotations.Embed;
import com.mongodb.DBObject;
import java.lang.reflect.Field;
import org.apache.log4j.Logger;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class EmbedDecoder extends AbstractDecoder{
    
    private final static Logger logger = Logger.getLogger(EmbedDecoder.class);
    
    public EmbedDecoder(Field field, DBObject dbo){
        super(field, dbo);
    }
    
    @Override
    public void decode(Object obj){
        Object embedObj = dbo.get(getFieldName());
        if(embedObj == null){
            try{
                field.set(obj, null);
            }catch(Exception e){
                logger.error(e.getMessage());
            }
            return;
        }
        Object value = new BuguMapper().fromDBObject(field.getType(), (DBObject)embedObj);
        try{
            field.set(obj, value);
        }catch(Exception e){
            logger.error(e.getMessage());
        }
    }
    
    private String getFieldName(){
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
    
}
