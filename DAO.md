# DAO操作 #
像[对象-文档映射](ObjectDocumentMapping.md)中那样，给实体类加上了注解，并且实现了BuguEntity接口，接下来就可以使用BuguDao类操作该实体了。

## BuguDao构造函数 ##
你需要编写自己的Dao，如FooDao，来操作Foo相关的数据。FooDao需要继承自BuguDao，并且，在FooDao的构造函数中，需要传递Foo.class，如下：
```
public class FooDao extends BuguDao<Foo> {
    public FooDao(){
        super(Foo.class);
    }
}
```

## 插入 ##
BuguDao中有如下方法可用于插入数据：
```
public WriteResult insert(T t)

public WriteResult insert(List<T> list)  //批量插入

public WriteResult save(T t)  //如果t中没有ID值，则为插入，若有ID值，则为修改。用户自定义ID除外。
```
其中，当使用save(obj)方法时，如果obj中的id值为null，则实际执行插入操作；如果obj中的id值不为null，则实际执行修改操作。（@Id的type=IdType.USER\_DEFINE的情况除外）

例：
```
Foo foo = new Foo();
foo.setName("Frank");
foo.setLevel(10);

FooDao dao = new FooDao();
dao.save(foo);
String id = foo.getId();  //保存至数据库后，foo中有id值了
```

## 删除 ##
```
public void drop()  //删除整个Collection

public WriteResult remove(T t)

public WriteResult remove(String id)

public WriteResult remove(List<String> idList)  //批量删除

public WriteResult remove(String key, Object value)  //按条件删除

public WriteResult remove(BuguQuery query)  //按条件删除
```

## 修改 ##
对于修改过的对象，可以调用如下方法进行保存：
```
public WriteResult save(T t)
```
除此之外，mongoDB还提供了大量的对数据记录进行直接修改的方法。这些方法实在太多了，而且还在不断增加，因此BuguMongo把这些方法集中到了一个类BuguUpdater中：
```
BuguUpdater updater = dao.update();
```
我们看看BuguUpdater类中有哪些方法：
```
/* 修改属性值 */

public WriteResult set(BuguQuery query, String key, Object value)

public WriteResult set(BuguQuery query, Map values)

public WriteResult set(T t, String key, Object value)

public WriteResult set(String id, String key, Object value)

public WriteResult set(T t, Map values)

public WriteResult set(String id, Map values)

/* 删除属性列 */

public WriteResult unset(T t, String... keys)

public WriteResult unset(String id, String... keys)

public WriteResult unset(BuguQuery query, String... keys)

/* 增加、减少数值 */

public WriteResult inc(T t, String key, Object value)

public WriteResult inc(String id, String key, Object value)

public WriteResult inc(BuguQuery query, String key, Object value)

/* 乘以某个数值 */

public WriteResult mul(T t, String key, Object value)

public WriteResult mul(String id, String key, Object value)

public WriteResult mul(BuguQuery query, String key, Object value)

/* 数组操作 */

public WriteResult push(T t, String key, Object value)

public WriteResult push(String id, String key, Object value)

public WriteResult push(BuguQuery query, String key, Object value)

public WriteResult pushEach(T t, String key, List valueList)

public WriteResult pushEach(String id, String key, List valueList)

public WriteResult pushEach(BuguQuery query, String key, List valueList)

public WriteResult pull(T t, String key, Object value)

public WriteResult pull(String id, String key, Object value)

public WriteResult pull(BuguQuery query, String key, Object value)

public WriteResult popFirst(T t, String key)

public WriteResult popFirst(String id, String key)

public WriteResult popFirst(BuguQuery query, String key)

public WriteResult popLast(T t, String key)

public WriteResult popLast(String id, String key)

public WriteResult popLast(BuguQuery query, String key)

/* 设置成较小值 */

public WriteResult min(T t, String key, Object value)

public WriteResult min(String id, String key, Object value)

public WriteResult min(BuguQuery query, String key, Object value)

/* 设置成较大值 */

public WriteResult max(T t, String key, Object value)

public WriteResult max(String id, String key, Object value)

public WriteResult max(BuguQuery query, String key, Object value)

/* 按位操作 */

public WriteResult bitwise(T t, String key, int value, Bitwise bitwise)

public WriteResult bitwise(String id, String key, int value, Bitwise bitwise)

public WriteResult bitwise(BuguQuery query, String key, int value, Bitwise bitwise)

```
例：
```
FooDao dao = new FooDao();
Foo foo = dao.query().is("name", "Frank").result();

dao.update().set(foo, "level", 5);  //把foo的level值修改为5

dao.update().inc(foo, "level", 10);  //把foo的level值增加10
```

## 基本查询 ##

BuguDao类中提供了一些基本的查询方法，如下：
```
/* 查询一个 */

public T findOne(String id)

public T findOne(String key, Object value)

/* 查询全部 */

public List<T> findAll()

public List<T> findAll(String orderBy)

public List<T> findAll(int pageNum, int pageSize)

public List<T> findAll(String orderBy, int pageNum, int pageSize)

/* 查询是否存在 */

public boolean exists(String id)

public boolean exists(String key, Object value)

/* 查询数目 */

public long count()

public long count(String key, Object value)

/* 查询某一字段的不重复值 */

public List distinct(String key)

```

## 高级查询 ##

更高级的查询需要用到BuguQuery类。先看看BuguQuery类中有些什么方法：

**生成查询条件**：
```
public BuguQuery<T> is(String key, Object value)

public BuguQuery<T> notEquals(String key, Object value)

public BuguQuery<T> greaterThan(String key, Object value)

public BuguQuery<T> greaterThanEquals(String key, Object value)

public BuguQuery<T> lessThan(String key, Object value)

public BuguQuery<T> lessThanEquals(String key, Object value)

public BuguQuery<T> in(String key, Object... values)

public BuguQuery<T> in(String key, List list)

public BuguQuery<T> notIn(String key, Object... values)

public BuguQuery<T> notIn(String key, List list)

public BuguQuery<T> all(String key, Object... values)

public BuguQuery<T> size(String key, int value)

public BuguQuery<T> mod(String key, int divisor, int remainder)

public BuguQuery<T> existsField(String key)

public BuguQuery<T> notExistsField(String key){

public BuguQuery<T> regex(String key, String regex)

public BuguQuery<T> where(String whereStr)

public BuguQuery<T> or(BuguQuery... qs)

public BuguQuery<T> and(BuguQuery... qs)
```
**地理空间数据查询**：
```
public BuguQuery<T> near(String key, double x, double y)

public BuguQuery<T> near(String key, double x, double y, double maxDistance)

public BuguQuery<T> withinCenter(String key, double x, double y, double radius)

public BuguQuery<T> withinBox(String key, double x1, double y1, double x2, double y2)
```
**设置查询参数**:
```
public BuguQuery<T> slice(String key, long num)

public BuguQuery<T> returnFields(String... fieldNames)

public BuguQuery<T> notReturnFields(String... fieldNames)

public BuguQuery<T> sort(String orderBy)

public BuguQuery<T> pageNumber(int pageNumber)

public BuguQuery<T> pageSize(int pageSize)
```
**返回查询结果**：
```
public T result()  //返回一个实体

public List<T> results()  //返回多个实体

public long count()

public boolean exists()

public List distinct(String key)
```

## 创建BuguQuery ##
通过调用BuguDao中的query()方法，就可以创建一个BuguQuery对象：
```
public class FooDao extends BuguDao<Foo> {
    public FooDao(){
        super(Foo.class);
    }
    ...
}

FooDao dao = new FooDao();
BuguQuery<Foo> q = dao.query();
...
```

## 用BuguQuery实现查询 ##
### 支持连缀书写形式 ###
```
List<Foo> list = dao.query().greaterThan("level", 10).notEquals("name", "Frank").results();
```

### 支持分页 ###
```
List<Foo> list = dao.query().greaterThan("level", 10).notEquals("name", "Frank").pageNumber(1).pageSize(20).results();
```

### 支持指定返回、不返回某些字段 ###
```
//只返回id、name、level这三个字段
BuguQuery query1 = dao.query().greaterThan("level", 10).returnFields("name", "level") ;

//不返回detail、comments这两个字段
BuguQuery query2 = dao.query().greaterThan("level", 10).notReturnFields("detail", "comments");  
```

### 支持Entity对象作为查询条件 ###
```
FatherDao fDao = new FatherDao();
FatherFoo father = fDao.query().is("id", "4dcb4d1d3febc6503e4e5933").result();

Foo foo = dao.query().is("father", father).result();  //用FatherFoo对象作为查询条件
```

### 支持字符串形式的排序 ###
```
List<Foo> list = dao.query().in("name", "Frank", "John").sort("{level:1, timestamp: -1}").results();
```

## 注意事项 ##
1、对于数组、List、Set，在MongoDB中都被保存成数组，可以用push、pull等方法对其进行操作。

2、BuguMongo支持用字符串来表示排序规则。对于其书写方法，请[参考这里](http://code.google.com/p/bugumongo/wiki/ObjectDocumentMapping#排序字符串)。

3、对数据库的更新操作，都有一个返回值WriteResult，代表了操作的结果。该类由mongoDB Java Driver提供，其详细信息，可以[查看这里](http://api.mongodb.org/java/current/com/mongodb/WriteResult.html)。
4、使用DAO操作MongoDB时，应该使用数据库的字段名称，而不是Java的属性名称。

比如：
```
@Property(name="total_score")
private int totalScore;
```
那么，操作数据库的时候，应该是：
```
list = dao.query().is("total_score", 1000).results();    //注意：不是totalScore
```
一种较好的习惯是：尽量不设置@Property、@Embed、@EmbedList、@Ref、@RefList等注解的name属性，使得数据库的字段名称，与Java的属性名称，保持一致。