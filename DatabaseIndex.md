# 数据库索引 #

**注意：** 这里指的是数据库本身的索引，不要把它与Lucene的索引相混淆。

如果不需要打开shell执行create index，直接在程序源代码里就能指定数据库索引，是不是很酷？

是的，利用BuguMongo，你只需在程序里加上个@EnsureIndex注解，即可实现该功能。

以一个简单的新闻系统为例：
```
@Entity
@EnsureIndex("{type:1}")
public class News implements BuguEntity{
    @Id
    private String id;
    private String title;   //标题
    private String content;  //内容
    private String author;  //作者
    private String type;  //类别
    private int level;  //推荐级别
    private Date publishTime;  //发布时间

    ...getter and setter...
}
```
索引的书写规则，可以采用标准的JSON格式:
```
@EnsureIndex("{'type':1}")
```
或者简化的JSON格式：
```
@EnsureIndex("{type:1}")
```

如果有多个索引，那么这样写：
```
@EnsureIndex("{type:1},{level:-1}")
```

如果是组合索引，格式如下：
```
@EnsureIndex("{author:1, type:1}, {level:-1}")  //2个索引，第1个为组合索引
```

如果是地理空间索引，也是类似的：
```
@EnsureIndex("{gps:2d}")
```

还可以设置索引的属性，比如name、unique等：
```
@EnsureIndex("{author:1, level:-1, name:idx_author_level}")
```

支持TTL Collection：
```
@EnsureIndex("{publishTime:1, expireAfterSeconds:3600}")
```