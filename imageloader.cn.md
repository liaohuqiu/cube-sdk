---
layout: default
title: Image Loader
lead: "简单，高效地加载图片 / 图片复用"
---
#非常简单的用法
---
<p class='lead'>一共三步：</p>

1. 首先，创建一个`ImageLoader`:

    ```java
    Context context;
    ImageLoader imageLoader = ImageLoaderFatory.create(context);
    ```

2. 用`findViewById()`找到要加载图片的`ImageView`：

    ```java
    CubeImageView imageView = (CubeImageView) view.findViewById(R.id.iv_item_image_list_big);
    ```

    这里留意一下：`CubeImageView` 是 `ImageView` 的子类, 在xml布局文件中应该这样写:

    ```xml
    <?xml version="1.0" encoding="utf-8"?>
    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:orientation="vertical" >

        <!-- 用定制的CubeImageView -->
        <in.srain.cube.image.CubeImageView 
            android:id="@+id/iv_item_image_list_big"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:scaleType="fitCenter" />

    </LinearLayout>
    ```

3. 加载图片。就一句代码：

    ```java
    imageView.loadImage(imageLoader, url);
    ```

#工作原理
---
<p class='lead'>整个加载图片的流程非常简单。</p>

##加载流程：

1.  `CubeImageView`会检查图片是否已经加载过，图片已经加载完成并已经在显示的重复请求直接忽略。
2.  检查图片是否在内存缓存中，如果再内存中，显示内存中的图片。 
3.  否则，创建一个 `ImageTask`，传递给`ImageLoader`，`ImageTaskExecutor`会处理这个任务。 
    
    `ImageTaskExecutor` 会用后台线程处理这个`ImageTask`.
    
4.  `ImageProvider`负责获取图片，如果图片在本地有文件缓存，那么直接从本地文件加载；否则从网络下载，存文件缓存。

5.  获取到图片之后，存内存缓存，通知加载完成。收到加载完成通知后，`ImageView`就可以显示图片了。

##组成

`ImageLoader`中包含了几个组件，让我们看看它的构造函数：

* `ImageLoader`

    ```java
    public ImageLoader(Context context, 
    
                            ImageProvider provider, 
                            ImageTaskExecutor executor, 
                            ImageResizer resizer, 
                            ImageLoadHandler loadHandler) {
        
    }
    ```

具体的是：

* `ImageProvider`

    管理 `ImageMemoryCache` 和 `ImageFileCache`，负责图片的内存缓存，文件缓存处理逻辑，图片不存在时，负责从网络下载。

* `ImageTaskExexutor`

    是一个接口，继承自 `java.util.concurrent.Executor`, 用来执 `LoadImageTask`, 它还允许设置任务不同的执行顺序：先加入先执行 / 后加入先执行。

    ```
void setTaskOrder(ImageTaskOrder order);
void execute(Runnable command);
    ```
* `ImageResizer`
    
    是一个接口，控制从文件读取图片时`BitmapFactory.encode()`时的 `BitmapFactory.Options.inSampleSize` 选项，同时可根据不同的请求尺寸，拼接不同的图片url。

    ```
int getInSampleSize(ImageTask imageTask);
String getResizedUrl(ImageTask imageTask);
    ```
* `ImageLoadHandler`

    是一个接口，定义如下：

    ```
void onLoading(ImageTask imageTask, CubeImageView cubeImageView);
void onLoadFinish(ImageTask imageTask, CubeImageView cubeImageView, BitmapDrawable drawable);
    ```

    当`LoadImageTask`被加入到`ImageTaskExecutor`时，`ImageLoader`调用`ImageLoadHandler.onLoading()`通知`ImageView`，这时`ImageView`可显示一个loading图。

    当图像加载完成, 调用`ImageLoadHandler.onLoadFinish()`，通知ImageView显示图像。

#扩展

<p class='lead'>这些接口，Cube-SDK都有一个默认的实现，你可以在你自己的App中，自己实现这些接口，实现特殊需求。</p>

###自定义loading图
自定义loading图有很多种办法：

1. 最深度的办法是实现 `ImageLoadHandler`，这可以实现你任何需求，当然一般情况下你不用这样大动干戈；

1. 用`ImageLoadHandler` 的默认实现 `DefaultImageLoadHandler`，他提供了几种方法用来设置loading图：

    ```java
    DefaultImageLoadHandler handler = new DefaultImageLoadHandler();

    // pick one of the following method
    handler.setLoadingBitmap(Bitmap loadingBitmap);
    handler.setLoadingResources(int loadingBitmap);
    handler.setLoadingImageColor(int color);
    handler.setLoadingImageColor(String colorString);
    ```

###图像加载完成之后，一些特殊效果

1.  实现`ImageLoadHandler.onLoadFinish()`，想要什么效果都行，`自己动手，丰衣足食`。

2.  `DefaultImageLoadHandler`内置了一些效果，你可以直接使用：
    * 淡入。**淡入** 默认启用，不需要的话，记得关掉。

        ```java
    setImageFadeIn(boolean fadeIn);
        ```
    * **圆角**

        ```java
    setImageRounded(boolean rouded, float cornerRadius);
        ```


###通过拼接不同的url，获得不同尺寸的图

如果你的服务器支持通过拼接不同的url，获取不同尺寸，不同质量的尺寸，你想在加载图片的时候，根据`ImageView`的尺寸大小，加载最合适的图，

非常简单：

```java
public class EtaoImageResizer extends DefaultResizer {

    public String getResizedUrl(ImageTask imageTask) {

            int w = imageTask.getRequestSize().x;
            int h = imageTask.getRequestSize().y;

            // return url according the request size
    }
}
```

#高级用法
###图片复用
如果你加载了一个大图(360x360)，随后你又要加载同一个图的较小尺寸(180x180)，这样做能节省网络带宽，不管是用户的，还是服务器的。

我们把大图叫做 `big_360`，小图叫做`small_180`，用`ImageReuseInfoManger`管理这些可复用的尺寸：

```java
// "big_360" 在 "small_180" 之前
private static final String[] sizeList = new String[] { "big_360", "small_180" };
public static final ImageReuseInfoManger sImageReuseInfoManger = new ImageReuseInfoManger(sizeList);
```

在加载大图的时候：

```java
ImageReuseInfo bigImageReuseInfo = sImageReuseInfoManger.create("big_360");
imageView.loadImage(imageLoader, url, bigImageReuseInfo);
```

在加载小图的时候：

```java
ImageReuseInfo smallImageReuseInfo = sImageReuseInfoManger.create("small_360");
imageView.loadImage(imageLoader, url, smallImageReuseInfo);
```

这样，在加载小图时，就会尝试复用大图了。
