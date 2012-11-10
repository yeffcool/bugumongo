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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import org.apache.log4j.Logger;

/**
 * Presents a lucene node in clustering environment.
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class ClusterNode {
    
    private final static Logger logger = Logger.getLogger(ClusterNode.class);
    
    private String host;
    private int port;

    public ClusterNode(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    /**
     * Tansmit a message to this node.
     * <p>
     * This method should be synchronized, because there must 
     * be only one thread operating on the ClientNode at the time.
     * </p>
     * @param message 
     */
    public synchronized void transmitMessage(ClusterMessage message){
        //connect
        SocketChannel channel = null;
        try{
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            SocketAddress address = new InetSocketAddress(host, port);
            if(!channel.connect(address)){
                while(!channel.finishConnect()){
                    //poll until it's connected
                };
            }
            //write data
            channel.write(BufferUtil.toBuffer(message));
        }catch(IOException ex){
            logger.error("Error when transmit message to host: " + host + ", port: " + port, ex);
        }finally{
            //close
            if(channel != null){
                try{
                    channel.close();
                }catch(IOException ex){
                    logger.error("Error when close channel host: " + host + ", port: " + port, ex);
                }
            }
        }
    }

}
