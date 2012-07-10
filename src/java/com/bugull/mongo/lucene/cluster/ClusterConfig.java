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

package com.bugull.mongo.lucene.cluster;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Configure the clustering environment.
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class ClusterConfig {
    
    private final static int DEFAULT_PORT = 9200;
    private int serverPort = DEFAULT_PORT;
    
    private List<String> localAddresses;
    private Map<String, ClusterNode> clusterNodes;
    
    private ExecutorService executor;  //thread pool to send/receive message to/from brother nodes 
    private int threadPoolSize = 10;  //default thread pool size is 10
    
    private ExecutorService serverExecutor;
    private ClusterServer server;
    
    private int connectTimeout = 3000;
    
    private int bufferSize = 1024;  //1K
    private int maxEntitySize = 4*1024*1024;  //4M
    
    public ClusterConfig(){
        clusterNodes = new ConcurrentHashMap<String, ClusterNode>();
        localAddresses = HostAddressUtil.getLocalAddresses();
    }
    
    public void validate(){
        //create the thread pool
        executor = Executors.newFixedThreadPool(threadPoolSize);
        //start the server
        serverExecutor = Executors.newSingleThreadExecutor();
        server = new ClusterServer();
        serverExecutor.execute(server);
    }
    
    public void addNode(String host){
        addNode(host, DEFAULT_PORT);
    }
    
    public void addNode(String host, int port){
        if(! localAddresses.contains(host)){
            ClusterNode node = new ClusterNode(host, port);
            clusterNodes.put(host, node);
        }
    }

    /**
     * send a message to all the brother nodes.
     * @param message 
     */
    public synchronized void sendMessage(ClusterMessage message) {
        for(ClusterNode node : clusterNodes.values()){
            SendMessageTask task = new SendMessageTask(node, message);
            executor.execute(task);
        }
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int getMaxEntitySize() {
        return maxEntitySize;
    }

    public void setMaxEntitySize(int maxEntitySize) {
        this.maxEntitySize = maxEntitySize;
    }

    public ExecutorService getExecutor() {
        return executor;
    }
    
    /**
     * Close the current cluster node.
     */
    public void invalidate(){
        //shutdown the thread pool
        if(executor != null){
            executor.shutdown();
        }
        //shutdown the server
        server.close();
        if(serverExecutor != null){
            serverExecutor.shutdown();
        }
    }

}
