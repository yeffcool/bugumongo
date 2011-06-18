package com.bugull.mongo.decoder;

import com.mongodb.DBObject;
import java.lang.reflect.Field;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public abstract class AbstractDecoder implements Decoder{
    
    protected Field field;
    protected DBObject dbo;
    protected Object value;
    
    protected AbstractDecoder(Field field, DBObject dbo){
        this.field = field;
        this.dbo = dbo;
        value = dbo.get(getFieldName());
    }
    
    @Override
    public boolean isNullField(){
        return value == null;
    }
    
}
