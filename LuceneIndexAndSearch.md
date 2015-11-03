# Lucene索引和搜索 #
BuguMongo集成了Lucene的功能。当往MongoDB中新增一个Document时，能自动为该Document建立Lucene索引。相应的，当MongoDB中的Document被修改、删除时，对应的Lucene索引也会修改、删除。

另外，BuguMongo还提供了对Lucene搜索的支持。根据Lucene索引进行搜索的时候，搜索结果能自动转换成对应的Entity对象。

在Lucene集成方面，BuguMongo提供了：

10个注解：@Indexed、@IndexProperty、@IndexEmbed、@IndexEmbedList、@IndexRef、@IndexRefList、@IndexRefBy、@IndexEmbedBy、@IndexFilter、@BoostSwitch

5个类：BuguIndex、BuguParser、BuguSearcher、BuguHighlighter、IndexRebuilder

## 步骤一：在Entity上加注解 ##
代码例子如下：
```
package com.bugull.mongo.test;

import com.bugull.mongo.BuguEntity;
import com.bugull.mongo.annotations.Entity;
import com.bugull.mongo.annotations.Embed;
import com.bugull.mongo.annotations.Id;
import com.bugull.mongo.annotations.Ref;
import com.bugull.mongo.lucene.annotations.Indexed;
import com.bugull.mongo.lucene.annotations.IndexProperty;
import com.bugull.mongo.lucene.annotations.IndexEmbed;
import com.bugull.mongo.lucene.annotations.IndexRef;
import com.bugull.mongo.lucene.annotations.IndexFilter;
import com.bugull.mongo.lucene.annotations.Compare;
import java.util.List;

@Entity
@Indexed
public class Foo implements BuguEntity{
    @Id
    private String id;
    @IndexProperty(analyze=true,boost=2.0f)
    private String title;
    @IndexProperty(analyze=true)
    private String introduce;
    @Embed
    @IndexEmbed
    private EmbedFoo embed;
    @Ref
    @IndexRef
    private FatherFoo father;
    @IndexFilter(compare=Compare.IS_EQUALS,value="true")
    private boolean valid;
    @BoostSwitch(compare=Compare.IS_EQUALS,value="true",fit=1.5f,unfit=0.5f)
    private boolean vip;
    ...getter and setter...
}

public class EmbedFoo {
    @IndexEmbedBy(value=Foo.class)
    private float x;
    @IndexEmbedBy(value=Foo.class)
    private int y;
    ...getter and setter
}

@Entity(name="father")
public class FatherFoo implements BuguEntity{
    @Id
    private String id;
    @IndexRefBy(value=Foo.class, analyze=true)
    private String name;
    ...getter and setter...
}
```
各个注解的含义如下：

### @Indexed ###
表示需要对该Entity建索引。该Entity上必须要有@Entity和@Id注解，@Indexed才能起作用。

### @IndexProperty ###
表示需要对该属性建立索引。

@IndexProperty支持的数据类型包括：String、char、boolean、int、long、float、double、Date等基本数据类型。

@IndexProperty注解有3个参数：

analyze——boolean型，表示是否需要分词，缺省值为false

store——boolean型，表示是否需要存储，缺省值为false

boost——float型，表示该Field的权重，缺省值为1.0

@IndexProperty还支持上述基本数据类型组成的数组、List、Set等。这些集合中的元素，不管是什么数据类型，都会连结成一个字符串，然后加以索引。

### @IndexEmbed ###
表示需要嵌入对该Embed对象的索引。结合@IndexEmbedBy使用。索引域的名称形如“embed.x”。

### @IndexEmbedList ###
表示需要嵌入对该EmbedList对象的索引。结合@IndexEmbedBy使用。索引域的名称形如“embed.x”。

### @IndexRef ###
表示需要嵌入对该Ref对象的索引。结合@IndexRefBy使用。索引域的名称形如“father.name”。

### @IndexRefList ###
表示需要嵌入对该RefList对象的索引。结合@IndexRefBy使用。索引域的名称形如“father.name”。

### @IndexRefBy ###
表示需要嵌入到其它对象的@Ref或@RefList域的索引中。

@IndexRefBy有4个参数：

value——Class类型，表示被引用的类，该值必须设置

analyze——boolean型，表示是否需要分词，缺省值为false

store——boolean型，表示是否需要存储，缺省值为false

boost——float型，表示该Field的权重，缺省值为1.0

当某属性在多个类的索引中被引用时，上述4个参数都需要设置成数组形式。

### @IndexEmbedBy ###
表示需要嵌入到其它对象的@Embed或@EmbedList域的索引中。

@IndexEmbedBy拥有与@IndexRefBy相同的4个参数。

### @IndexFilter ###
表示只有满足该条件的实体才会被索引，否则不创建索引。

@IndexFilter有2个参数：compare和value。compare表示比较操作，是枚举类型Compare。value是比较的值，是字符串，会相应的解析成该属性类型的值。

compare有多个枚举值，它们的含义和所支持的数据类型如下：

Compare.IS\_EQUALS——等于（==）。支持String、boolean、int、long、float、double、char。

Compare.NOT\_EQUALS——不等于（!=）。支持String、boolean、int、long、float、double、char。

Compare.GREATER\_THAN——大于（>）。支持int、long、float、double。

Compare.GREATER\_THAN\_EQUALS——大于等于（>=）。支持int、long、float、double。

Compare.LESS\_THAN——小于（<）。支持int、long、float、double。

Compare.LESS\_THAN\_EQUALS——小于等于（<=）。支持int、long、float、double。

Compare.IS\_NULL——为空（==null）。支持Object类型，包括String。这时不需要value参数。

Compare.NOT\_NULL——不为空（!=null）。支持Object类型，包括String。这时不需要value参数。

在一个Entity类上可以有多个@IndexFilter注解，表示需要同时满足这些条件。

### @BoostSwitch ###
同一个Entity类的不同的Document，可能需要设置不同的权重，@BoostSwitch注解就是用来实现这个功能的，它有4个参数：

compare——与上面@IndexFilter的compare含义相同

value——与上面@IndexFilter的value含义相同

fit——float型，表示满足比较条件时该Document的boost值，缺省值为1.0

unfit——float型，表示不满足比较条件时该Document的boost值，缺省值为1.0

在一个Entity类上只能有一个@BoostSwitch注解。

## 步骤二：设置索引参数 ##
首先，系统初始化的时候，需要设置BuguIndex的各个参数，并执行open()方法，代码例子如下：
```
BuguIndex index = BuguIndex.getInstance();

index.setDirectoryPath("/root/lucene_index/");
Version version = index.getVersion();
index.setAnalyzer(new StandardAnalyzer(version));
index.setThreadPoolSize(5);
index.setIndexReopenPeriod(60L*1000L);

index.open();
```
退出系统的时候，记得关闭索引：
```
index.close();
```

与BuguConnection一样，你也可以把BuguIndex看作是配置文件，它也是一个单例模式，BuguMongo框架中Lucene相关的组件，会自动使用BuguIndex的实例。

上述代码中，各个方法的含义都很直观。其中：

BuguMongo使用FSDirectory存储索引，通过setDirectoryPath()可以设置索引文件存放的位置。该值必须设置。

BuguMongo使用当前Lucene的默认版本号。用getVersion()可以获得版本号。如果你不想使用默认的Lucene版本，可以用setVersion()来设置Lucene索引的版本。

你可以用setAnalyzer()来设置你所需要的Analyzer。如果不设置，则BuguIndex默认使用StandardAnalyzer。

当需要对一个Entity创建索引，或者修改、删除索引的时候，BuguMongo会从线程池中选择一个线程来执行该任务。setThreadPoolSize(int poolSize)，就是设置线程池的大小。该值可以不设置，默认值为10。

setIndexReopenPeriod(long milli)的参数以毫秒为单位，表示索引刷新的时间周期，该参数值根据应用的情况而定。该值可以不设置，默认值为30L\*1000L，即30秒。

## 步骤三：搜索 ##
通过上面的设置，BuguMongo能够实现Entity和Lucene索引之间的自动同步，你只要实现搜索就行了。最基本的搜索的例子如下：
```
Query query = BuguParser.parse("introduce", "谷歌");
BuguSearcher<Foo> searcher = new BuguSearcher(Foo.class);
searcher.setQuery(query).setSort().setFilter().setPageNumber(1).setPageSize(20).setMaxPage(50);
List<Foo> list = searcher.search();
int count = searcher.getResultCount();
searcher.close();  //务必记得关闭BuguSearcher
for(Foo foo : list){
    ...
}
```
setMaxPage(maxPage)，表示最多返回搜索结果的前maxPage页。设置该参数的原因是：如果返回所有的搜索结果，当数据量太大的时候，会导致性能问题。

pageNumber、pageSize、maxPage的值都可以不设置，默认值为分别为：1、20、50。

如果不排序，可以不用setSort()。

如果没有Filter，则可以不用设置setFilter()。

对于通过@IndexEmbed和@IndexRef嵌入的索引，索引的field名称需要加上该属性的名称作为前缀，如：father.name。参见下面的例子。

```
Query strQuery = BuguParser.parse(new String[]{"introduce","father.name"},"谷歌");
Query xQuery = BuguParser.parse("embed.x",3.14f);
Query yQuery = BuguParser.parse("embed.y", 100, 300);
BooleanQuery bQuery = new BooleanQuery();
bQuery.add(strQuery, Occur.SHOULD);
bQuery.add(xQuery, Occur.MUST_NOT);
bQuery.add(yQuery, Occur.MUST);
BuguSearcher<Foo> searcher = new BuguSearcher(Foo.class);
List<Foo> list = searcher.search(bQuery);
int count = searcher.getResultCount();
searcher.close();
for(Foo foo : list){
    ...
}
```

### 获得IndexSearcher ###

BuguSearcher能实现大部分常用的搜索功能，但在一些特殊的情况下，你可能需要使用底层的Lucene搜索功能，比如使用Collector，这时，你需要通过调用BuguSearcher的getSearcher()方法，来获得底层Lucene的IndexSearcher，然后用它来进行搜索。
```
BuguSearcher<Foo> searcher = new BuguSearcher(Foo.class);
IndexSearcher is = searcher.getSearcher();
Query query = ...
Collector collector = ...
is.search(query, collector);
searcher.close();
```

## 查询分析器：BuguParser ##

正如前面的例子中看到的那样，BuguParser类提供了对各种数据类型的查询支持，包括数值查询、范围查询等，还提供了MultiFieldQueryParser的功能。

BuguParser提供了一系列静态方法，用于创建Query。

**字符串查询：**
```
public static Query parseTerm(String field, String value)  //创建一个TermQuery

public static Query parse(String field, String value)  //默认使用Operator.OR

public static Query parse(String field, String value, Operator op)

public static Query parse(String[] fields, String value)  //默认使用Operator.OR

public static Query parse(String[] fields, String value, Operator op)

public static Query parse(String[] fields, Occur[] occurs, String value)
```
**数值查询：**
```
public static Query parse(String field, int value)

public static Query parse(String field, long value)

public static Query parse(String field, float value)

public static Query parse(String field, double value)
```
**数值范围查询：**
```
public static Query parse(String field, int minValue, int maxValue)

public static Query parse(String field, long minValue, long maxValue)

public static Query parse(String field, float minValue, float maxValue)

public static Query parse(String field, double minValue, double maxValue)

public static Query parse(String field, Date begin, Date end)

public static Query parse(String field, Timestamp begin, Timestamp end)
```
**其它：**
```
public static Query parse(String field, boolean value)

public static Query parse(String field, char value)
```

## 辅助工具：重建索引 ##
在某些情况下，由于特殊的原因，Lucene索引文件没有与MongoDB中的记录一一对应，比如，用shell命令往MongoDB中添加或修改了数据，这时，需要对MongoDB中的文档重新建立Lucene索引。BuguMongo提供了一个类IndexRebuilder来实现该功能。代码例子如下：
```
IndexRebuilder r1 = new IndexRebuilder(Foo.class);
r1.rebuild();
...
int batchSize = 50;
IndexRebuilder r2 = new IndexRebuilder(MyEntity.class, batchSize);
r2.rebuild();
```
参数batchSize表示IndexRebuilder每次从MongoDB中取多少条数据进行索引，默认值为100。

## 辅助工具：高亮显示 ##
对搜索结果中的关键词进行高亮显示，是一个经常用到的功能。BuguMongo中提供了一个类BuguHighlighter来实现该功能。代码例子如下：
```
Query query = BuguParser.parse("introduce", "谷歌");
BuguSearcher<Foo> searcher = new BuguSearcher(Foo.class);
BuguHighlighter highlighter = new BuguHighlighter("introduce", "谷歌");  //对introduce字段的“谷歌”关键词高亮显示
searcher.setHighlighter(highlighter);
List<Foo> list = searcher.search(query);
searcher.close();
```
在网页上，只需要显示foo.introduce字段的内容，其关键字部分，会自动高亮显示。

可以对多个字段中的关键词高亮显示。比如，同时对title、introduce字段中的“谷歌”关键词高亮显示，BuguHighlighter可以这样定义：
```
String[] fields = new String[]{"title", "introduce"};
BuguHighlighter highlighter = new BuguHighlighter(fields, "谷歌");
```
BuguHighlighter默认将关键词用红色显示，如果需要把它改成其它的显示样式，可以这样定义：
```
BuguHighlighter highlighter = new BuguHighlighter("introduce", "谷歌");
Formatter formatter = new SimpleHTMLFormatter("<font color=\"#0000FF\"><b>", "</b></font>");  //蓝色，加粗
highlighter.setFormatter(formatter);
```