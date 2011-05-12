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
    
    protected AbstractDecoder(Field field, DBObject dbo){
        this.field = field;
        this.dbo = dbo;
    }
    
}
