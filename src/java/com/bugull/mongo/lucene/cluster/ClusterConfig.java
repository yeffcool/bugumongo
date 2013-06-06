/*
 * Copyright (c) www.bugull.com
 * 
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Configure the lucene clustering environment.
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class ClusterConfig {
    
    private final static int DEFAULT_PORT = 9200;
    private int serverPort = DEFAULT_PORT;
    
    private boolean selfNode;
    private List<String> localAddresses;
    private ConcurrentMap<String, ClusterNode> clusterNodes;
    
    private ExecutorService executor;  //thread pool to send/receive message to/from brother nodes 
    private int threadPoolSize = 10;  //default thread pool size is 10
    
    private ExecutorService serverExecutor;
    private ClusterServer server;
    
    private int bufferSize = 1024;  //1K
    private int maxEntitySize = 1*1024*1024;  //1M
    
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
        if(localAddresses.contains(host)){
            selfNode = true;
        }
        else{
            selfNode = false;
            ClusterNode node = new ClusterNode(host, port);
            clusterNodes.put(host, node);
        }
    }

    /**
     * Send a message to all the clustering nodes.
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
    public void invalidate() throws InterruptedException{
        //shutdown the thread pool
        if(executor != null){
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
        //shutdown the server
        server.close();
        if(serverExecutor != null){
            serverExecutor.shutdown();
            serverExecutor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }
    
    /**
     * Check if the current server is a lucene clustering node.
     * @return 
     */
    public boolean isSelfNode(){
        return selfNode;
    }

}
