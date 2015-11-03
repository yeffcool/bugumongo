# lazy属性 #
查询数据的时候，有些字段并不需要返回。这样能够节省内存，而且取数据的速度会更快一些。

虽然BuguQuery查询中的方法returnFields()和notReturnFields()，可以指定查询的时候返回、不返回哪些字段，但如果每次查询的时候都要特别指定，那会非常麻烦。有没有简便一点的方法呢？用lazy属性即可实现。

@Property、@Embed、@EmbedList都有lazy属性，其缺省值都是false。

如果把lazy设为true，那么表示：当取出一个List的时候，不会把lazy=true的属性取出来。

如果是取单条记录，会把这条记录的所有field都取出来，不管该field是否为lazy。也就是说，lazy属性只对取列表的时候起作用。

以一个简单的新闻系统为例：
```
@Entity
public class News implements BuguEntity{
    @Id
    private String id;
    private String title;  //新闻标题
    @Property(lazy=true)
    private String content;  //新闻内容

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    ...getter and setter...
    
}

public class NewsDao extends BuguDao{
    public NewsDao(){
        super(News.class);
    }
}
```
新闻的内容，是很长的字符串。而显示新闻列表的时候，并不需要把新闻内容取出来。
```
NewsDao dao = new NewsDao();
List<News> list = dao.query().pageNumber(1).pageSize(100).results();  //取出100条新闻，用于列表显示
```
因为content上的@Property注解，把lazy设为了true，因此，并没有把content字段从数据库中取出来，list里面每个News的content，值为null。

### fetchLazy() ###
在某些特殊的应用场合，对于设置了lazy=true的属性，仍然希望它能够在列表中显示。这时候，可以使用辅助工具类BuguMapper中的fetchLazy()方法：
```
NewsDao dao = new NewsDao();
List<News> list = dao.query().pageNumber(1).pageSize(100).results();  //News中不包含lazy=true的字段
BuguMapper.fetchLazy(list);  //现在，lazy=true的字段也被取出来了
```
fetchLazy()方法虽然用起来很方便，但它是以牺牲性能为代价的，因此，除非特殊情况，否则不要使用它。

# cascade属性 #
@Ref、@RefList都有cascade属性，其缺省值都是空字符串""，表示不做任何关联操作。

通过设置cascade属性，可以关联CRUD操作，给编写程序带来很大的方便。

例：
```
@Entity
public class Foo implements BuguEntity{
    @Id
    private String id;
    private String name;
    @Ref
    private Father father;

    ...getter and setter...
}

@Entity
public class Father implements BuguEntity{
    @Id
    private String id;
    private String name;

    ...getter and setter...
}
```
这里，没有设置任何关联操作。因此，向MongoDB中插入foo的时候，无法关联插入father。只能分别创建，如下：
```
FooDao fooDao = ...
FatherDao fatherDao = ...

Father father = new Father();
father.setName("John");
fatherDao.insert(father);  //先保存father

Foo foo = new Foo();
foo.setName("Frank");
foo.setFather(father);
fooDao.insert(foo);  //再保存foo
```
如果设置成关联创建，如下：
```
@Ref(cascade="C")
private Father father;
```
那么，创建foo的同时，会关联创建father。
```
FooDao fooDao = ...

Father father = new Father();
father.setName("John");

Foo foo = new Foo();
foo.setName("Frank");
foo.setFather(father);
fooDao.insert(foo);  //会关联保存father
```

### cascade的含义 ###

结合上面的例子，cascade的CRUD含义如下：

关联创建——cascade的值中包含字符“C”。当向MongoDB中插入foo的时候，如果father没有还没有被保存到数据库中，则会关联创建。

关联读取——cascade的值中包含支付“R”。当从数据库中取出foo的时候，会关联取出father对象。

关联修改——cascade的值中包含字符“U”。当保存foo的时候，如果father发生了变化，则会关联保存该变化。

关联删除——cascade的值中包含字符“D”。当删除foo的时候，同时会删除father对象。

对于用@RefList标注的属性，也是和@Ref一样的含义。

当关联取出@RefList注解标注的属性时，会根据注解上的sort属性进行排序。

### fetchCascade() ###
如果没有设置关联读取，即，cascade值中不包含“R”字符，那么，对@Ref、@RefList注解的实体，只有id值，其它值均为null。

以上面的代码为例，如果father属性没有设置关联读取，
```
@Ref
private Father father;
```
那么，
```
Foo foo = (Foo)fooDao.query().is("id", "4eccbea26c02f26351e56bcc").result();
Fahter father = foo.getFather();
String id = father.getId();  //有值
String name = father.getName();  //值为null
```
如果改为：
```
@Ref(cascade="R")
private Father father;
```
则father.getName()不为null。

对于没有设置关联读取的@Ref和@RefList属性，如果在某些情况下却又需要用到该属性，那么，可以使用BuguMapper类的fetchCascade()方法，把该属性关联取出。如：
```
Foo foo = (Foo)fooDao.query().is("id", "4eccbea26c02f26351e56bcc").result();
BuguMapper.fetchCascade(foo, "father");  //关联取出father属性
Fahter father = foo.getFather();
String id = father.getId();  //有值
String name = father.getName();  //有值

List list = fooDao.query().pageNumber(1).pageSize(50).results();
BuguMapper.fetchCascade(list, "father");  //关联取出list中每个foo的father属性。
```

当用fetchCascade()方法取出@RefList注解标注的属性时，会根据注解上的sort属性进行排序。

fetchCascade()方法可以一次关联取出多个@Ref、@RefList属性，如下：
```
BuguMapper.fetchCascade(list, "father", "children");
```

fetchCascade()方法还支持多级关联，可以一级一级往下取，例如：
```
BuguMapper.fetchCascade(list, "father.father");
```