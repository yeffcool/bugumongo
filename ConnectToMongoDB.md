# 连接MongoDB #
在能够对MongDB进行操作之前，需要使用BuguConnection连接到MongoDB数据库。代码如下：
```
BuguConnection conn = BuguConnection.getInstance();
conn.connect("192.168.0.100", 27017, "mydb", "username", "password");
```
也可以这样写：
```
BuguConnection conn = BuguConnection.getInstance();
conn.setHost("192.168.0.100").setPort(27017).setDatabase("mydb").setUsername("username").setPassword("password").connect();
```
如果不需要提供授权帐号，那么可以省略用户名和密码：
```
BuguConnection conn = BuguConnection.getInstance();
conn.connect("192.168.0.100", 27017, "mydb");
```

## 连接副本集（ReplicaSet） ##
如果需要连接到一组ReplicaSet，那么代码例子如下：
```
List<ServerAddress> addrs = new ArrayList<ServerAddress>();
addrs.add(new ServerAddress("192.168.0.100", 27017));
addrs.add(new ServerAddress("192.168.0.101", 27017));
BuguConnection conn = BuguConnection.getInstance();
conn.setReplicaSet(addrs).setDatabase("mydb").setUsername("username").setPassword("password").connect();
```

## 设置连接参数 ##
如果不想使用默认的连接参数，那么可以自己提供一个MongoClientOptions：
```
MongoClientOptions options = ...
BuguConnection conn = BuguConnection.getInstance();
conn.setHost("192.168.0.100").setPort(27017).setDatabase("mydb").setOptions(options).connect();
```
对于各个连接参数和它们的默认值，可以查看[MongoClientOptions的源代码](https://github.com/mongodb/mongo-java-driver/blob/master/src/main/com/mongodb/MongoClientOptions.java)。

其中一个大家最为关心的参数，就是数据库连接数（连接池）。最新的MongoDB Java Driver默认使用100个连接，这个数目足够大了。如果你要修改这个默认的连接数，可以这样写：
```
MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
builder.connectionsPerHost(200);    //使用200个连接
MongoClientOptions options = builder.build();
BuguConnection conn = BuguConnection.getInstance();
conn.setHost("192.168.0.100").setPort(27017).setDatabase("mydb").setUsername("username").setPassword("password").setOptions(options).connect();
```

## 关闭连接 ##
当应用程序退出的时候，可以调用close()方法关闭BuguConnection，以便立即释放所有资源。
```
BuguConnection.getInstance().close();
```

## 连接数据库的代码应该写在哪里？ ##
你可以把BuguConnection类看作是配置文件。它是一个单例模式。BuguMongo框架的其它组件，如DAO，会自动使用BuguConnection类的实例作为数据库连接。

连接数据库的代码，应该写在应用程序初始化的地方。如果是开发Java Application，那么可能是在main()方法里的开始部分；如果开发的是web应用，那么可以在web.xml里添加一个监听器，比如：
```
<listener>  
    <listener-class>com.xbwen.context.MySystemListener</listener-class>  
</listener> 
```
MySystemListener是自己定义的一个监听器，用来在系统启动的时候进行一些初始化设置，在系统关闭的时候回收资源。示例代码如下：
```
package com.xbwen.context;  
  
import com.bugull.mongo.BuguConnection;  
import javax.servlet.ServletContextEvent;  
import javax.servlet.ServletContextListener;  
  
public class MySystemListener implements ServletContextListener{  
    @Override  
    public void contextInitialized(ServletContextEvent event) {  
        //连接数据库  
        BuguConnection conn = BuguConnection.getInstance();  
        conn.connect("192.168.0.100", 27017, "test", "test", "test");  
    }  
   
    @Override  
    public void contextDestroyed(ServletContextEvent event) {
        //关闭数据库连接
        BuguConnection.getInstance().close();  
    } 
}  
```