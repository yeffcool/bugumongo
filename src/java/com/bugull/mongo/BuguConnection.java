/**
 * Copyright (c) www.bugull.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bugull.mongo;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoOptions;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import java.net.UnknownHostException;
import java.util.List;
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
    private List<ServerAddress> replicaSet;
    private ReadPreference readPreference;
    private MongoOptions options;
    private String database;
    private String username;
    private String password;
    private DB db;
    
    private BuguConnection(){
        
    }
    
    public static BuguConnection getInstance(){
        return instance;
    }
    
    public void connect(String host, int port, String database){
        this.host = host;
        this.port = port;
        this.database = database;
        connect();
    }
    
    public void connect(String host, int port, String database, String username, String password){
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        connect();
    }
    
    public void connect(List<ServerAddress> replicaSet, String database, String username, String password){
        this.replicaSet = replicaSet;
        this.database = database;
        this.username = username;
        this.password = password;
        connect();
    }
    
    public void connect(){
        doConnect();
        if(username != null && password != null){
            auth();
        }
    }

    private void doConnect(){
        Mongo mongo = null;
        try{
            if(host != null && port != 0){
                ServerAddress sa = new ServerAddress(host, port);
                if(options != null){
                    mongo = new Mongo(sa, options);
                }else{
                    mongo = new Mongo(sa);
                }
            }
            else if(replicaSet != null){
                if(options != null){
                    mongo = new Mongo(replicaSet, options);
                }else{
                    mongo = new Mongo(replicaSet);
                }
                if(readPreference != null){
                    mongo.setReadPreference(readPreference);
                }
            }
        }catch(UnknownHostException e){
            logger.error(e.getMessage());
        }
        db = mongo.getDB(database);
    }
    
    private void auth(){
        boolean auth = db.authenticate(username, password.toCharArray());
        if(auth){
            logger.info("Connected to mongodb successfully!");
        }else{
            db = null;
            logger.error("Connect to mongodb failed! Failed to authenticate!");
        }
    }
    
    public BuguConnection setHost(String host){
        this.host = host;
        return this;
    }
    
    public BuguConnection setPort(int port){
        this.port = port;
        return this;
    }
    
    public BuguConnection setDatabase(String database){
        this.database = database;
        return this;
    }
    
    public BuguConnection setUsername(String username){
        this.username = username;
        return this;
    }
    
    public BuguConnection setPassword(String password){
        this.password = password;
        return this;
    }

    public BuguConnection setOptions(MongoOptions options) {
        this.options = options;
        return this;
    }

    public BuguConnection setReplicaSet(List<ServerAddress> replicaSet) {
        this.replicaSet = replicaSet;
        return this;
    }

    public BuguConnection setReadPreference(ReadPreference readPreference) {
        this.readPreference = readPreference;
        return this;
    }

    public DB getDB(){
        return db;
    }
    
}
