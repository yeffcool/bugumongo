package com.bugull.mongo.decoder;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public interface Decoder {
    
    public void decode(Object obj);
    
    public boolean isNullField();
    
    public String getFieldName();
    
}
