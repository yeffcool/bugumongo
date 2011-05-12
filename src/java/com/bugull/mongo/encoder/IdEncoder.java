package com.bugull.mongo.encoder;

import java.lang.reflect.Field;
import org.bson.types.ObjectId;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class IdEncoder extends AbstractEncoder{
    
    public IdEncoder(Object obj, Field field){
        super(obj, field);
    }
    
    @Override
    public boolean isNullField(){
        return false;
    }
    
    @Override
    public String getFieldName(){
        return "_id";
    }
    
    @Override
    public Object encode(){
        if(value == null){
            return new ObjectId();
        }else{
            return new ObjectId(value.toString());
        }
    }
    
}
