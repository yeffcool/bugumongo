package com.bugull.mongo.encoder;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public interface Encoder {
    
    public boolean isNullField();
    
    public String getFieldName();
    
    public Object encode();
    
}
