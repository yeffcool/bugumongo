# GridFS文件操作 #

## 1、BuguFS类 ##
BuguMongo通过BuguFS类来操作GridFS文件系统，能够实现对文件的保存、获取、删除、重命名、移动等操作。

**创建BuguFS对象**

BuguMongo框架提供了工厂类BuguFSFactory，用于创建BuguFS对象：
```
public BuguFS create()

public BuguFS create(String bucketName)

public BuguFS create(long chunkSize)

public BuguFS create(String bucketName, long chunkSize)
```

例如：
```
BuguFS fs = BuguFSFactory.getInstance().create();
```

其中，bucketName是mongoDB Java Driver中的叫法，在GridFS中，又被称为namespace（命名空间）。缺省情况下，GridFS把文件保存在fs命名空间里。缺省的文件块大小是256K。关于GridFS更详细的介绍，请查看[mongoDB的官方文档](http://www.mongodb.org/display/DOCS/GridFS+Specification)。

BuguFS提供了如下操作：

**保存**
```
public void save(File file)

public void save(File file, String filename)

public void save(File file, String filename, Map<String, Object> params)

public void save(InputStream is, String filename)

public void save(InputStream is, String filename, Map<String, Object> params)

public void save(byte[] data, String filename)

public void save(byte[] data, String filename, Map<String, Object> params)
```
**获取一个文件**
```
public GridFSDBFile findOne(String filename)

public GridFSDBFile findOne(DBObject query)
```
**获取文件列表**
```
public List<GridFSDBFile> find(DBObject query)

public List<GridFSDBFile> find(DBObject query, int pageNum, int pageSize)

public List<GridFSDBFile> find(DBObject query, String orderBy)

public List<GridFSDBFile> find(DBObject query, String orderBy, int pageNum, int pageSize)
```
**文件重命名**
```
public void rename(String oldName, String newName)

public void rename(GridFSDBFile file, String newName)
```
**删除文件**
```
public void remove(String filename)

public void remove(DBObject query)
```
与BuguDao中的方法类似，上面的find方法支持使用形如"level:1"、"level:1,price:-1"这样的字符串进行排序。

此外，BuguFS中还有几个常用的\*静态常量**：
```
public final static String BUCKET = "bucket";  //文件存放空间
public final static String FILENAME = "filename";  //文件名
public final static String LENGTH = "length";  //文件大小，以byte为单位
public final static String UPLOADDATE = "uploadDate";  //上传时间
```
对于GridFS中每一个文件（GridFSDBFile），都会有filename、length、uploadDate这三个属性。**

一个代码例子如下：
```
import com.bugull.mongo.fs.BuguFS;

BuguFS fs = BuguFSFactory.getInstance().create();

//保存
File file = ...
fs.save(file);   //filename使用file.getName()

String filename = ...  //提供一个文件名
fs.save(file, filename);

Byte[] data = ...
String filename = ...
fs.save(data, filename);

Map params = new HashMap();
params.put("author", "张三");
params.put("group", "技术");
fs.save(data, filename, params);

//获取
GridFSDBFile dbFile = fs.findOne(filename);
InputStream is = dbFile.getInputStream();

DBObject query = ...
List<GridFSDBFile> list = fs.find(query);

//删除
fs.remove(filename);
fs.remove(query);
```

除了BuguFS外，BuguMongo还提供了另外几个辅助类，来简化GridFS文件的上传和获取，而且功能更加强大。这几个类分别是：

上传：Uploader、ImageUploader、Watermark。

读取：UploadedFileServlet、AccessRestrictedServlet

需要注意的是，这些辅助类，都是以BuguFS为基础的，只是做了进一步的封装和简化，方便使用。

## 2、文件上传类Uploader ##
Uploader类有以下几个构造函数：
```
public Uploader(File file, String originalName)  //默认rename = false

public Uploader(File file, String originalName, boolean rename)

public Uploader(InputStream input, String originalName)

public Uploader(InputStream input, String originalName, boolean rename)

public Uploader(byte[] data, String originalName)

public Uploader(byte[] data, String originalName, boolean rename)
```
上面这些构造函数，如果rename = false，则以提供的originalName作为文件名来保存文件。如果rename = true，则会对文件进行重命名，由系统生成一个形如“2011070799999999999999.doc”的文件名，其中，20110707为日期，9999999999999为当前时间的纳秒数（long值）。

这里，以一个简单的新闻系统为例。

发布一篇新闻的时候，同时上传新闻的附件（比如.doc文件），那么，在Struts2的Action中，代码这样写：
```
import com.bugull.mongo.fs.Uploader;
...
public class CreateNewsAction extends ActionSupport{

    private File file;
    private String fileFileName;
    private News news;
    private NewsDao newsDao;

    public String execute(){
        Uploader uploader = new Uploader(file, fileFileName, true);
        uploader.save();
        news.setAttachment(uploader.getFilename());
        newsDao.save(news);
        return SUCCESS;
    }
    ...
}
```
上面的代码中，因为rename = true，因此，uploader.getFilename()，返回的是，文件经过保存以后，系统自动生成的文件名（含扩展名），格式如：2011070799999999999999.doc，其中，20110707为日期，9999999999999为当前时间的纳秒数。news实体中attachment值，就是这个字符串。

如果是使用SpringMVC，那么Controller的代码示例如下：
```
@Controller    
public class FileUploadController {

    @RequestMapping(value="/upload", method=RequestMethod.POST)  
    public String handleUpload(@RequestParam("file") MultipartFile file) throws IOException{
        InputStream is = file.getInputStream();
        String originalName = file.getOriginalFilename();
        Uploader uploader = new Uploader(is, originalName, true);
        uploader.save();
        ...
    }

}
```

如果需要把文件上传到指定的namespace（bucket）中，或者要设置文件块的大小，代码可以这样写：
```
Uploader uploader = new Uploader(file, originalName);  //不重命名，使用原文件名
uploader.setBucketName("attach");  //相应的Collection为：attach.files 和 attach.chunks
uploader.setChunkSize(64L * 1024L);  //64K
uploader.save();
```

还可以为上传的文件设置其它属性。例如，设置文件的作者：
```
Uploader uploader = new Uploader(file, fileFileName, true);
uploader.setAttribute("author", "Frank");
uploader.save();
```

## 3、图片文件的上传ImageUploader ##
图片上传使用类ImageUploader，它继承自Uploader，除了具备上面讲的Uploader的功能以外，ImageUploader还能实现图片加水印、图片压缩。

### 3.1、图片加水印 ###
图片上传的时候，可以自动给图片加上水印。可以是图片水印，也可以是文字水印。

上传图片的时候，给其加上图片水印的代码如下：
```
import com.bugull.mongo.fs.Watermark;
import com.bugull.mongo.fs.ImageUploader;
...
public class CreateProductAction extends ActionSupport{

    private File img;
    private String imgFileName;
    private Product product;
    private ProductDao productDao;

    public String execute(){
        Watermark watermark = new Watermark();
        watermark.setImagePath("/root/website/images/watermark.png");
        ImageUploader uploader = new ImageUploader(img, imgFileName, true);
        uploader.setBucketName("images");
        uploader.save(watermark);
        product.setPicture(uploader.getFilename());
        productDao.save(product);
        return SUCCESS;
    }
    ...
}
```
watermark.setImagePath(imagePath)设置水印文件的路径，该路径是操作系统的绝对路径。

还可以给图片加上文字水印，代码示例如下：
```
ImageUploader uploader = new ImageUploader(file, fileFileName, true);
Watermark watermark = new Watermark();
watermark.setText("www.mongodb.com");
uploader.save(watermark);
```

Watermark类还有许多其它参数，它们的定义和初始值如下，这些属性值都可以通过watermark.setXXX()来进行设置：
```
public final static int CENTER = 1;
public final static int BOTTOM_RIGHT = 2;
    
private String imagePath;
private String text;
    
private String fontName = "宋体";
private int fontStyle = Font.PLAIN;
private Color color = Color.GRAY;
private int fontSize = 30;
private float alpha = 0.5f;
private int align = CENTER;
private int right = 20;
private int bottom = 20;
```

### 3.2、图片压缩 ###
图片上传的时候，经常还需要将该图片压缩、保存成若干份，代码例子如下：
```
import com.bugull.mongo.fs.ImageUploader;
...
public class CreateProductAction extends ActionSupport{

    private File img;
    private String imgFileName;
    private Product product;
    private ProductDao productDao;

    public String execute(){
        ImageUploader uploader = new ImageUploader(img, imgFileName, true);
        uploader.save();
        uploader.compress("medium", 300, 300);
        uploader.compress("small", 100, 100);
        product.setPicture(uploader.getFilename());
        productDao.save(product);
        return SUCCESS;
    }
    ...
}
```
public void compress(String dimension, int maxWidth, int maxHeight) 方法中，dimension是指图片尺寸的类型，用来区分同一图片的不同尺寸的拷贝。经过compress方法压缩保存后的图片文件，和原图有相同的filename，例如，同样是2011070799999999999999.jpg。

### 3.3、获取图片尺寸 ###
通过ImageUploader上传的图片，经过保存后，还可以取得图片的宽度和高度，代码示例如下：
```
ImageUploader uploader = new ImageUploader(file, fileFileName, true);
uploader.save();
int[] size = uploader.getSize();
int width = size[0];   //宽度
int height = size[1];  //高度
```

## 4、通过HTTP获取文件 ##

### 4.1 UploadedFileServlet ###
如果你是在Web项目中使用BuguMongo，那么，保存到GridFS中的文件，可以通过UploadedFileServlet来获取，在web.xml中配置如下：
```
<servlet>
    <servlet-name>UploadedFile</servlet-name>
    <servlet-class>com.bugull.mongo.fs.UploadedFileServlet</servlet-class>
</servlet>
<servlet-mapping>
    <servlet-name>UploadedFile</servlet-name>
    <url-pattern>/UploadedFile/*</url-pattern>
</servlet-mapping>
```
然后，可以通过如下的URL链接来获取文件：
```
http://www.domain.com/UploadedFile/xxxx.jpg
或者：
http://www.domain.com/UploadedFile/key/value/xxxx.jpg
http://www.domain.com/UploadedFile/key1/value1/key2/value2/xxxx.jpg
```
其中：

xxxx.jpg——表示文件名，必须提供。

key、value——表示属性名称、属性值。

例如：
```
http://www.domain.com/UploadedFile/2011070799999999999999.jpg
http://www.domain.com/UploadedFile/bucket/images/2011070799999999999999.jpg
http://www.domain.com/UploadedFile/dimension/small/2011070799999999999999.jpg
http://www.domain.com/UploadedFile/author/Frank/dimension/small/2011070799999999999999.jpg
```
有2点需要特别注意的是：

（1）如果保存文件的时候使用了bucketName，则获取文件的时候，需要指定bucket，如：
```
http://www.domain.com/UploadedFile/bucket/images/2011070799999999999999.jpg
```

（2）经过ImageUploader.compress()压缩后的图片，默认有一个尺寸属性，属性名称是"dimension"，属性值就是compress()函数中的参数值。如：
```
http://www.domain.com/UploadedFile/dimension/small/2011070799999999999999.jpg
```

前面配置的Servlet，可以通过它访问任何bucket中的文件。你可以把它配置成只允许访问某个bucket，或者禁止访问某个bucket。如，只允许通过该Servlet访问images这个bucket中的文件：
```
<servlet>
    <servlet-name>UploadedFile</servlet-name>
    <servlet-class>com.bugull.mongo.fs.UploadedFileServlet</servlet-class>
    <init-param>
        <param-name>allowBucket</param-name>
        <param-value>images</param-value>
    </init-param>
</servlet>
```
如，禁止通过该Servlet访问personal这个bucket中的文件：
```
<servlet>
    <servlet-name>UploadedFile</servlet-name>
    <servlet-class>com.bugull.mongo.fs.UploadedFileServlet</servlet-class>
    <init-param>
        <param-name>forbidBucket</param-name>
        <param-value>personal</param-value>
    </init-param>
</servlet>
```
同一个UploadedFileServlet中，allowBucket、forbidBucket这2个参数，只能使用1个，不支持2个同时使用。

对于需要保护的文件，可能你还希望通过密码才能访问。你可以对该Servlet设置password属性，通过该Servlet访问文件的时候，必须提供相符合的密码。如：
```
<servlet>
    <servlet-name>UploadedFile</servlet-name>
    <servlet-class>com.bugull.mongo.fs.UploadedFileServlet</servlet-class>
    <init-param>
        <param-name>password</param-name>
        <param-value>abcd</param-value>
    </init-param>
</servlet>
```
那么，正确的获取文件的方式是：
```
http://www.domain.com/UploadedFile/2011070788888888.mp4?password=xxxxxx
```
其中，xxxxxx是密码abcd经过MD5加密后的字符串。

UploadedFileServlet有2个特点：

（1）对于"jpg", "jpeg", "png", "gif", "bmp"等格式的图片文件，通过该Servlet获取，浏览器能对它进行缓存。其它类型的文件，则不会进行缓存。

（2）支持断点续传和多线程下载。

### 4.2 AccessRestrictedServlet ###
该类扩展自UploadedFileServlet，用于限制对某些资源文件的并发访问数量。例如：
```
<servlet>
    <servlet-name>AccessRistrictedServlet</servlet-name>
    <servlet-class>com.bugull.mongo.fs.AccessRistrictedServlet</servlet-class>
    <init-param>
        <param-name>allowBucket</param-name>
        <param-value>videos</param-value>
    </init-param>
    <init-param>
        <param-name>resourceName</param-name>
        <param-value>videos</param-value>
    </init-param>
    <init-param>
        <param-name>maxAccess</param-name>
        <param-value>100</param-value>
    </init-param>
    <init-param>
        <param-name>redirectTo</param-name>
        <param-value>/too_many_access.html</param-value>
    </init-param>
</servlet>
```
该配置表示：只允许通过该Servlet访问videos这个bucket中的文件，并且最多只允许100个并发访问，当超过该数目时，不返回文件，而是跳转至/too\_many\_access.html这个网页。

如果不配置redirectTo参数，则访问请求不会被跳转，而是会进入等待队列。

如果你的应用中只有一个AccessRistrictedServlet，那么resourceName可以不配置，其默认值为bugu。resourceName的作用是：你可以通过它来获取当前还剩下多少个空闲的访问名额，以及等待队列中有多少个元素。如：
```
AccessCount ac = AccessCount.getInstance();
int available = ac.getAvailablePermits("videos");  //如果没有配置resourceName，则为ac.getCount("bugu");
int waiting = ac.getQueueLength("videos");
```