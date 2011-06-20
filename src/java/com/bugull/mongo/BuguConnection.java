package com.bugull.mongo;

import com.mongodb.DB;
import com.mongodb.Mongo;
import java.net.UnknownHostException;
import org.apache.log4j.Logger;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class BuguConnection {
    
    private final static Logger logger = Logger.getLogger(BuguConnection.class);
    
    private static BuguConnection instance = new BuguConnection();
    
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private DB db;
    
    private BuguConnection(){
        
    }
    
    public static BuguConnection getInstance(){
        return instance;
    }
    
    public void connect(String host, int port, String database, String username, String password){
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        connect();
    }

    public void connect(){
        Mongo mongo = null;
        try{
            mongo = new Mongo(host, port);
        }catch(UnknownHostException e){
            logger.error(e.getMessage());
        }
        db = mongo.getDB(database);
        boolean auth = db.authenticate(username, password.toCharArray());
        if(auth){
            logger.warn("已成功连接MongoDB");
        }else{
            db = null;
            logger.error("错误的用户名和密码");
        }
    }
    
    public void setHost(String host){
        this.host = host;
    }
    
    public void setPort(int port){
        this.port = port;
    }
    
    public void setDatabase(String database){
        this.database = database;
    }
    
    public void setUsername(String username){
        this.username = username;
    }
    
    public void setPassword(String password){
        this.password = password;
    }

    public DB getDB(){
        return db;
    }
    
}
