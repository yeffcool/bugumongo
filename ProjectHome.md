## 简介 ##

BuguMongo是一个MongoDB Java开发框架，它的主要功能包括：

  * 基于注解的对象-文档映射（Object-Document Mapping，简称ODM）。
  * DAO支持。提供了大量常用的DAO方法。
  * Query支持。提供了生成查询的简便方法。
  * 基于注解的Lucene索引。
  * 简单方便的Lucene搜索。支持关键词高亮显示。
  * 简单方便的GridFS文件上传。支持上传的时候对图片加水印、图片压缩。
  * 简单方便的GridFS文件获取。能用HTTP获取文件，支持断点续传，图片文件能使用HTTP缓存。

BuguMongo已在多个正式商业项目中使用，并取得了理想的效果。

## 帮助文档 ##

BuguMongo的使用非常简单和直观，请查看帮助文档：
  * [连接数据库](ConnectToMongoDB.md)
  * [对象-文档映射](ObjectDocumentMapping.md)
  * [DAO操作](DAO.md)
  * [高级DAO操作](AdvancedDao.md)
  * [lazy和cascade属性](LazyAndCascade.md)
  * [数据库索引](DatabaseIndex.md)
  * [Lucene索引和搜索](LuceneIndexAndSearch.md)
  * [GridFS文件操作](GridFSOperation.md)

更多的Wiki文档，请关注作者的[个人主页](http://xbwen.github.io/)。

## 版本说明 ##

BuguMongo目前的版本是1.14，于2014-05-03发布，请查看[版本更新日志](http://code.google.com/p/bugumongo/wiki/ChangeLog)。

BuguMongo 1.14使用：
  * JDK1.6
  * MongoDB Java Driver 2.12.1
  * Lucene 3.5
  * Log4j记录日志

1.14版本，请[从这里下载](http://www.bugull.com/projects/bugu-mongo/bugu-mongo-1.14.zip)。

以前的版本，请[查看这里](https://code.google.com/p/bugumongo/downloads/list)。

## 交流反馈 ##

欢迎提出反馈意见和建议。
  * Email：xiaobinwen(at)gmail.com
  * QQ群：165738383