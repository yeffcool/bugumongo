package com.bugull.mongo.decoder;

import com.mongodb.DBObject;
import java.lang.reflect.Field;
import org.apache.log4j.Logger;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class IdDecoder extends AbstractDecoder{
    
    private final static Logger logger = Logger.getLogger(IdDecoder.class);
    
    public IdDecoder(Field field, DBObject dbo){
        super(field, dbo);
    }
    
    @Override
    public void decode(Object obj){
        try{
            field.set(obj, value.toString());
        }catch(Exception e){
            logger.error(e.getMessage());
        }
    }
    
    @Override
    public String getFieldName(){
        return "_id";
    }
    
}
