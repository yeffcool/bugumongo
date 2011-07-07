package com.bugull.mongo.cache;

import com.bugull.mongo.BuguConnection;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class ClearExpiredCacheTask extends TimerTask{

    @Override
    public void run() {
        long timeout = BuguConnection.getInstance().getCacheTimeout();
        QueryCache cache = QueryCache.getInstance();
        Map<String, Long> times = cache.getTimes();
        Set<String> keys = times.keySet();
        for(String key : keys){
            long createTime = times.get(key);
            long now = System.currentTimeMillis();
            if(createTime < now - timeout){
                cache.remove(key);
            }
        }
    }
    
}
