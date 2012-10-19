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

import com.bugull.mongo.lucene.BuguIndex;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import org.apache.log4j.Logger;

/**
 * A server accept socket request from brother nodes in clustering environment.
 * <p>
 * The server is written in NIO. Data is received via ByteBuffer with small size(default 1k). 
 * All the small buffers totally combine a large buffer(default 4M), 
 * then deserialized to a ClusterMessage object.
 * </p>
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class ClusterServer implements Runnable {
    
    private final static Logger logger = Logger.getLogger(ClusterServer.class);
    
    private Selector selector;
    private ServerSocketChannel serverChannel;
    private ByteBuffer buffer;
    private ByteBuffer totalBuffer;
    
    private ClusterConfig cluster = BuguIndex.getInstance().getClusterConfig();

    @Override
    public void run() {
        try{
            init();
        }catch(IOException ex){
            logger.error("Error when init cluster server", ex);
        }
        loop();
    }
    
    private void init() throws IOException {
        //prepare the socket
        selector = Selector.open();
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        SocketAddress address = new InetSocketAddress(cluster.getServerPort());
        serverChannel.socket().bind(address);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        //prepare the buffer
        buffer = ByteBuffer.allocate(cluster.getBufferSize());
        totalBuffer = ByteBuffer.allocate(cluster.getMaxEntitySize());
    }
    
    private void loop() {
        while(true){
            int i = 0;
            try{
                i = selector.select();
            }catch(IOException ex){
                logger.error("Error when selecting", ex);
            }
            if(i == 0){
                continue;
            }
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while(it.hasNext()){
                SelectionKey key = it.next();
                if(key.isAcceptable()){
                    handleAccept(key);
                }
                else if(key.isReadable()){
                    handleRead(key);
                }
                it.remove();  //must remove the current key from the iterator
            }
        }
    }
    
    /**
     * Connection accepted.
     * @param key
     * @throws IOException 
     */
    private void handleAccept(SelectionKey key) {
        ServerSocketChannel ssc = (ServerSocketChannel)key.channel();
        try{
            SocketChannel sc = ssc.accept();
            sc.configureBlocking(false);
            sc.register(selector, SelectionKey.OP_READ);
        }catch(IOException ex){
            logger.error("Error when handle accept", ex);
        }
    }
    
    /**
     * Data received.
     * @param key
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    private void handleRead(SelectionKey key) {
        SocketChannel sc = (SocketChannel)key.channel();
        int count = -1;
        try{
            while( (count = sc.read(buffer)) >= 0){
                if(count > 0){
                    buffer.flip();
                    totalBuffer.put(buffer);
                    buffer.clear();
                }
            }
            totalBuffer.flip();
            ClusterMessage message = (ClusterMessage)BufferUtil.fromBuffer(totalBuffer);
            totalBuffer.clear();
            if(message != null){
                HandleMessageTask task = new HandleMessageTask(message);
                cluster.getExecutor().execute(task);
            }
        }catch(IOException ex){
            logger.error("Error when handle read", ex);
        }catch(ClassNotFoundException ex){
            logger.error("Error when handle read", ex);
        }finally{
            try{
                sc.close();
            }catch(IOException ex){
                logger.error("Error when close SocketChannel", ex);
            }
        }
    }
    
    public void close(){
        try{
            serverChannel.close();
            selector.close();
        }catch(IOException ex){
            logger.error("Error when close cluster server", ex);
        }
    }

}
