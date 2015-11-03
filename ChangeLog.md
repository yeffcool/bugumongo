# 1.14 #
2014-05-03发布。
  * 新增：增加了对byte和byte`[]`数据类型的支持。
  * 新增：增加了对Queue和LinkedList数据类型的支持。
  * 新增：新增BuguUpdater类，把BuguDao中更新数据的操作统一放到BuguUpdater中。
  * 新增：BuguUpdater中，新增mul()、min()、max()、popFirst()、popLast()、bitwise()等方法。
  * 新增：可以对BuguDao设置WriteConcern。
  * 改进：性能优化。更快的执行速度，更少的内存占用，更好的并发性能。
  * 改进：大量代码重构，并移除掉@Deprecated方法。
  * 解决bug：@IndexEmbed不起作用。 （感谢网友汝逸的反馈）
  * 解决bug：使用$in查询时，对空数组[.md](.md)的处理不正确。 （感谢网友David Cen的反馈）
  * 解决bug：当在@RefList中使用接口时，对实现类的解析不正确。
  * 解决bug：对List`<`Float`>`、List`<`Short`>`类型的解析不正确。
  * 变更：BuguFS不再使用new()方法来创建对象，而是使用对象工厂BuguFSFactory。
  * 变更：MongoDB Java Driver版本更新到2.12

由于Google Code不再提供下载支持，1.14版本，请[从这里下载](http://www.bugull.com/projects/bugu-mongo/bugu-mongo-1.14.zip)。

# 1.12 #
2013-11-23发布。
  * 新增：新增BuguMapper.toJsonString()方法，用于把Entity转换成JSON。（感谢网友hxj的建议）
  * 新增：BuguDao新增set(String id, Map values)方法。
  * 新增：BuguDao新增set(BuguQuery query, Map values)方法。
  * 改进：@Id字段能自动转换成"`_`id"进行查询。
  * 改进：重建Lucene索引的时候，可以多任务。（感谢网友ko的建议）
  * 改进：BuguDao中针对@Embed和@EmbedList字段操作的时候，支持直接使用Java对象。
  * 改进：通过UploadedFileServlet获取文件的时候，可以设置密码。
  * 改进：BuguDao中的unset()方法可以一次性移除多个字段。
  * 改进：把BuguDao中不建议使用的方法改成@Deprecated，以便在下一个版本移除。
  * 解决bug：BuguDao中的set()方法对@Embed等字段不起作用。（感谢网友hxj的反馈）
  * 解决bug：Short和Float对象类型的数据，无法从数据库取出。（感谢网友秀明新竹的反馈）
  * 解决bug：UploadedFileServlet对断点续传字节的解析，在某些情况下不正确。


# 1.10 #
2013-07-09发布。
  * 新增：@Ref和@RefList增加了对接口的支持。
  * 新增：BuguQuery查询，通过returnFields()、notReturnFields()方法，可以设定只取出特定的字段。
  * 改进：代码精简，移除了一些不必要的方法。
  * 解决bug：TTL collection功能不正确。（感谢网友云端漫步的反馈）
  * 解决bug：BuguDao中的push、pull操作，不支持EmbedList对象。（感谢网友羽翼齐飞的反馈）
  * 解决bug：@RefList中cascade="R"的处理不正确，无法关联取出数据。（感谢网友下一道彩虹的反馈）
  * 解决bug：@RefList中cascade="D"的处理不正确，无法关联删除数据。（感谢网友★甲方★的反馈）


# 1.8 #
2013-04-25发布。
  * 新增：BuguQuery新增对$where查询条件的支持。
  * 新增：BuguQuery新增对$slice操作的支持。
  * 新增：支持mongodb 2.4新增的text索引。
  * 新增：新增了AccessRestrictedServlet和AccessCount类，用以记录和限制同时访问GridFS的线程数。
  * 改进：insert、save、set、inc、push等操作，返回操作是否成功的标识。（感谢网友零距离的建议）
  * 改进：BuguDao的操作，支持BuguEntity作为参数。
  * 改进：通过UploadedFileServlet获取的文件，包含ContentLength信息。
  * 改进：通过UploadedFileServlet获取的文件，支持断点续传和多线程下载。
  * 改进：通过UploadedFileServlet获取文件的时候，可以设置允许或禁止某个bucket。
  * 改进：完善了线程池的关闭代码。
  * 解决bug：在某些情况下，lazy=true不起作用。
  * 解决bug：根据id排序的时候，可能不起作用。（感谢网友枷锁的反馈）
  * 变更：MongoDB Java Driver版本更新到2.11


# 1.6 #
2013-01-10发布。
  * 新增：提供了BuguEntity接口的简单实现类SimpleEntity，方便简化代码。
  * 新增：可以指定ID的类型，包括：自动产生、自动增长、用户自定义。
  * 新增：@Ref和@RefList注解新增reduced属性，用于优化数据库存储。
  * 新增：支持mongodb 2.2新增的new aggregation框架。
  * 新增：支持mongodb 2.2新增的TTL collection。
  * 新增：新增@IndexEmbedBy注解。
  * 改进：完善了BuguDao的泛型参数。
  * 改进：完善了AdvancedDao的功能和异常信息。
  * 改进：drop某个collection的时候，能自动drop其相应的数据库索引。
  * 改进：若干代码重构与性能优化。
  * 解决bug：重建索引时，分页查询不正确。（感谢网友Never88Gone的反馈）
  * 解决bug：DAO操作，统一使用数据库的字段名，而不是java的属性名。（感谢网友云端漫步的反馈）
  * 变更：MongoDB Java Driver版本更新到2.10


# 1.4 #
2012-06-29发布。
  * 变更：把Query重命名为BuguQuery，以便统一和区分。
  * 变更：BuguFS不再使用单例模式，而是用new()创建对象。
  * 改进：把BuguSearcher的返回结果List中的null值排除。
  * 改进：修改了上传到GridFS中的文件名的产生方式，以便在高并发的情况下也可确保文件名的唯一性。
  * 改进：提高了图片压缩的清晰度。
  * 改进：完善了几个地方的异常信息。
  * 新增功能：可以设置文件存储的bucketName和chunkSize。
  * 解决bug：IndexWriter和IndexSearcher初始化的时候，在多线程环境下出错。
  * 解决bug：调用FieldUtil.get()方法时可能出现空指针异常。
  * MongoDB Java Driver版本更新到2.8.0


# 1.2 #
2012-03-17发布。
  * BuguDao新增unset()系列方法，用于删除某一列。
  * BuguMapper新增一个toDBRef(Class<?> clazz, String idStr)方法。
  * BuguParser新增一个方法parseTerm(String field, String value)。
  * 新增功能：可以设置Lucene Index的RAM Buffer Size。
  * BuguConnection新增close()方法，用于释放资源。
  * 改进了log4j的日志格式，详细记录了异常信息。 (感谢网友wendal1985的建议)
  * 改进：Dao和Query增加了对泛型的支持。(感谢网友wendal1985的建议)
  * 改进：关闭程序的时候，如果出现异常，Lucene索引没有解锁，则能够自动解锁。
  * 改进：一个entity本身可以不创建lucene索引，但某些属性可以被其它索引引用。
  * 改进：如果@IndexRefBy标注的属性只是@Id属性，则entity被修改的时候，Lucene索引不需要同步更新。
  * 解决bug：toDBRef()方法中当entity的id不符合规范时出错。
  * 解决bug：max()和min()函数在collection为空的情况下出错。
  * 解决bug：Lucene搜索的高亮显示，在没有关键词相匹配的时候出错。
  * 解决bug：BuguMapper的fetchCascade()和fetchLazy()方法当entity为null时出错。


# 1.0 #
2011-12-14发布。