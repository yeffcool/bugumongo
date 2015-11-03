# AdvancedDao #

AdvancedDao是BuguDao的子类，它主要提供：

(1) MapReduce功能，以及基于MapReduce实现的一些统计功能。

(2) Aggregation功能。

## MapReduce ##
AdvancedDao提供了对MapReduce的支持。但鉴于目前MongoDB的MapReduce性能不是很理想，如果您的数据量非常庞大，请谨慎使用。

**统计：**
```
public double max(String key)

public double max(String key, BuguQuery query)

public double min(String key)

public double min(String key, BuguQuery query)

public double sum(String key)

public double sum(String key, BuguQuery query)
```
**MapReduce：**
```
public Iterable<DBObject> mapReduce(MapReduceCommand cmd)

public Iterable<DBObject> mapReduce(String map, String reduce)

public Iterable<DBObject> mapReduce(String map, String reduce, DBObject query) 

public Iterable<DBObject> mapReduce(String map, String reduce, String outputTarget, MapReduceCommand.OutputType outputType, DBObject sort, DBObject query)

public Iterable<DBObject> mapReduce(String map, String reduce, String outputTarget, MapReduceCommand.OutputType outputType, DBObject sort, int pageNum, int pageSize, DBObject query)
```

示例代码：
```
//扩展自AdvancedDao
public class FooDao extends AdvancedDao<Foo>{
    public FooDao(){
        super(Foo.class);
    }
}

FooDao dao = new FooDao();
double d = dao.max("embed.x");
```

## Aggregation ##
待补充。