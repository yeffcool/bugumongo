# 对象-文档映射 #
在对象(Object，也称实体Entity)、文档(Document)之间实现自动转换，是BuguMongo的最核心功能，这能让你直接用面向对象的概念来操作MongoDB数据库，而不用去关心底层的数据库细节。

在这方面，BuguMongo提供了：

8个注解：@Entity、@Id、@Property、@Embed、@EmbedList、@Ref、@RefList、@Ignore

1个接口：BuguEntity

1个抽象类：SimpleEntity

## BuguEntity接口 ##
要使得某个Java Entity能和MongoDB Document实现相互转换，该Entity需要实现BuguEntity接口，该接口中有2个方法，如下：
```
public void setId(String id);
    
public String getId();
```

## 注解 ##
先看一段示例代码：
```
import com.bugull.mongo.BuguEntity;
import com.bugull.mongo.annotations.Entity;
import com.bugull.mongo.annotations.Id;
import com.bugull.mongo.annotations.IdType;
import com.bugull.mongo.annotations.Property;
import com.bugull.mongo.annotations.Embed;
import com.bugull.mongo.annotations.EmbedList;
import com.bugull.mongo.annotations.Ref;
import com.bugull.mongo.annotations.RefList;
import java.util.List;

@Entity
public class Foo implements BuguEntity{
    @Id
    private String id;
    private String name;
    @Property
    private int level;
    @Embed
    private EmbedFoo embed;
    @EmbedList
    private List<EmbedFoo> embedList;
    @Ref
    private FatherFoo father;
    @RefList
    private List<ChildFoo> children;
    @Ignore
    private double sumScore;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    ...other getter and setter...

}

public class EmbedFoo {
    private float x;
    private int y;
    ...getter and setter
}

@Entity(name="father")
public class FatherFoo implements BuguEntity{
    @Id(type=IdType.AUTO_INCREASE, start=1000)
    private String id;
    private Date date;
    @Ref
    private FatherFoo father;
    ...getter and setter...
}

@Entity(name="child")
public class ChildFoo implements BuguEntity{
    @Id
    private String id;
    private List<String> list;
    ...getter and setter...
}
```

各个注解的含义如下：

### @Entity ###
表示需要映射到MongoDB中的一个实体。该注解有4个属性：

name——String型，表示其在MongoDB中的collection的名称。name属性可以省略，默认使用类名的全小写。

capped——boolean型，表示该Entity类对应的是Capped Collection，缺省值为false。

capSize——long型，设置Capped Collection的空间大小，以字节为单位，默认值为-1，表示未设置。

capMax——long型，设置Capped Collection的最多能存储多少个document，默认值为-1，表示未设置。

如果设置了capped=true，则需要设置capSize和capMax两者中的其中一个。

**提示：**使用@Entity注解的类，必须实现BuguEntity接口。

### @Id ###
映射到MongoDB中的`_`id。@Id属性在Java代码中必须为String类型，但其在MongoDB中的数据类型，则根据对@Id的type属性的设置而定。

@Id的type属性，有以下3个枚举值：

IdType.AUTO\_GENERATE——默认值。表示由MongoDB自动产生id值。在MongoDB中，id被保存成为一个ObjectId对象，形式如："`_`id" : ObjectId("4eb8beeaeaad0a390144f084")。

IdType.AUTO\_INCREASE——表示id值自动增长。如1、2、3、4...。在MongoDB中，id被保存成Long类型的数值。

IdType.USER\_DEFINE——表示id值由程序员自己定义。比如，String id = UUID.randomUUID().toString()；在MongoDB中，id被保存成一个字符串。

如果使用type=IdType.AUTO\_INCREASE，那么还可以设置start参数，使得id从指定的值开始增长。

一个例子如下：
```
@Id(type=IdType.AUTO_INCREASE, start=1000)
private String id;
```

### @Property ###
该注解可以省略。它用来映射基本数据类型，包括：String、int、long、short、byte、float、double、boolean、char、Date、Timestamp等，以及这些基本数据类型组成的数组、List、Set、Map。

@Property有2个属性：

name——String型，用于指定映射到MongoDB collection中某个field。属性name可以省略，表示采用与Entity的Field相同的名称。

lazy——Boolean型，含义是：取出该实体的列表时，是否取出该field值。如果为true，表示不取出。默认值为false。

对lazy属性的详细讲解，请查看[lazy和cascade属性](http://code.google.com/p/bugumongo/wiki/LazyAndCascade)

### @Ignore ###
表示该属性不需要被映射。当保存实体时，该属性不会保存至MongoDB；同样，该属性也不会从MongoDB中取出。

### @Embed ###
表示该属性是一个嵌入的对象。嵌入对象（如EmbedFoo）只是一个普通的POJO，不需要@Entity注解，不需要实现BuguEntity接口，也不需要有@Id。

跟@Property一样，@Embed也有2个属性：name、lazy，含义也是一样的。

嵌入对象的属性，可以用点号“.”来引用，例如查询：
```
FooDao dao = ...
List list = dao.query().is("embed.x", 3.14F).results();  //查询出全部embed.x=3.14的Foo对象
```

### @EmbedList ###
表示该属性是一组嵌入的对象。

@EmbedList注解支持数组、List、Set、Map、Queue，但都必须使用泛型。当使用Map的时候，嵌入对象只能作为Map的value，而不能作为key。

**注意：**和Embed一样，嵌入的对象都必须是自己定义的Java对象。如果是原始数据类型组成的List、Set、Map等，请使用@Property，而不是@EmbedList。

跟@Property一样，@EmbedList也有2个属性：name、lazy，含义也是一样的。

### @Ref ###
表示对另一个对象的引用，在MongoDB中保存的是形如"father" : {"$ref" : "father", "$id" : ObjectId("4dcb4d1d3febc6503e4e5933")}这样的一个DBRef。

@Ref有4个属性：

name——含义与@Property的参数name一样。

impl——如果@Ref标注在一个接口（Interface）上，那么可以用impl指定其实现类。例：
```
@Ref(impl=MyFather.class)
private IFather father;
```

cascade——String型，用于指定关联操作。其值可以是C、R、U、D四个字符中的任意一个或多个字符组成。如：cascade="CRUD", cascade="CD", cascade="CR"等等。

C、R、U、D四个字符分别代表的含义如下：

> C：Create，创建

> R：Read，读取

> U：Update，修改

> D：Delete，删除

对cascade属性的详细讲解，请查看[lazy和cascade属性](http://code.google.com/p/bugumongo/wiki/LazyAndCascade)

reduced——boolean型，用于指定是否使用精简存储。默认值为false。

reduced=false时，@Ref字段在MongoDB中保存的是形如"father" : {"$ref" : "father", "$id" : ObjectId("4dcb4d1d3febc6503e4e5933")}这样的一个DBRef。

reduced=true时，@Ref字段在MongoDB中保存的是引用对象的id值，没有$ref属性，如："father" : "ObjectId(4dcb4d1d3febc6503e4e5933)"。

设置reduced=true，可以节省数据库的存储空间，但数据内容不再那么直观。

### @RefList ###
表示对另一个对象的引用的集合。

@RefList注解支持数组、List、Set、Map、Queue，但都必须使用泛型。当使用Map的时候，引用对象只能作为Map的value，而不能作为key。

@RefList有5个属性：

name——含义与@Property的属性name一样。

impl——含义与@Ref的属性impl类似，指定集合中元素的实现类。

cascade——含义与@Ref的属性cascade一样。

reduced——含义与@Ref的属性reduced一样。

sort——String型，用于关联取出该List或Set属性时使用的排序规则，其形式是："{'level':1}"，或"{'level':1, 'timestamp':-1}"这样的排序字符串。如果不排序，则不用设置sort值。

### 排序字符串 ###
排序字符串可以采用标准的JSON格式，如：
```
@RefList(sort="{'level': -1}")
```
若有多个排序条件，中间用逗号（,）隔开：
```
@RefList(sort="{'level':1, 'timestamp':-1}")
```

也可以省略单引号，使用简化的JSON格式：
```
@RefList(sort="{level:1}")

@RefList(sort="{level:1, timestamp:-1}")
```
甚至连花括号也省略，简化成下面这样，也是支持的：
```
@RefList(sort="level:1")

@RefList(sort="level:1, timestamp:-1")
```

## 辅助类：SimpleEntity ##
BuguMongo框架提供了BuguEntity接口的一个简单实现：SimpleEntity。你可以不用自己直接实现BuguEntity接口，而是继承自SimpleEntity。

SimpleEntity是一个抽象类，有一个id属性，使用默认的IdType.AUTO\_GENERATE作为id的类型，并实现了getId()、setId()、equals()、hashCode()、toString()等方法。此外，它还提供了getTimestamp()方法，能告诉你该document是何时插入进MongoDB的（其原理是从ObjectId中获取创建时间）。

建议你看一看[SimpleEntity的源代码](http://code.google.com/p/bugumongo/source/browse/trunk/src/java/com/bugull/mongo/SimpleEntity.java)，它非常简单。

使用SimpleEntity，可以使得你的实体模型的代码非常简洁，如下：
```
@Entity
public class MyFoo extends SimpleEntity {
    
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}
```