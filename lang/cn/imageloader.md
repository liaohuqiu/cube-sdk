---
layout: default
title: Image Loader
lead: "简单，高效地加载图片"
---
#ImageLoader
---
<p class='lead'><code>ImageLoader</code> 是图片加载组件的核心。</p>

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
        <com.srain.cube.image.CubeImageView 
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
    
    `ImageTaskExecutor` has an work queue, it has some threads to do the work in backgroud. If all of the threads are busy with current task, this task will put into the work queue.
    
4.  If the image is stored in file cache, it will be decoded, or else `ImageProvider` will fetch it from remote server then store it to file cache.

5.  After decoded, the process is complete. The ImageView will be notified to display this image.

##Components

There are four components used in `ImageLoader` to load image. As you can see in the construct method in the `ImageLoader`:

* `ImageLoader`

    ```java
    public ImageLoader(Context context, 
    
                            ImageProvider provider, 
                            ImageTaskExecutor executor, 
                            ImageResizer resizer, 
                            ImageLoadHandler loadHandler) {
        
    }
    ```

They are:

* `ImageProvider`

    It managers the `ImageMemoryCache` and `ImageFileCache`; request the image from remote server; decode bitmap from file.

* `ImageTaskExexutor`

    It is an interface which extends `ava.util.concurrent.Executor`. An `ImageTaskExecutor` can execute `LoadImageTask`, it allows the task can be executed in different order.

    ```
void setTaskOrder(ImageTaskOrder order);
void execute(Runnable command);
    ```
* `ImageResizer`
    
    It controls the `BitmapFactory.Options.inSampleSize` when decode bitmap from file and builds the network url according the request size.

    ```
int getInSampleSize(ImageTask imageTask);
String getResizedUrl(ImageTask imageTask);
    ```
* `ImageLoadHandler`

    ```
void onLoading(ImageTask imageTask, CubeImageView cubeImageView);
void onLoadFinish(ImageTask imageTask, CubeImageView cubeImageView, BitmapDrawable drawable);
    ```

    When the image is loading in the backgroud thread, `onLoading()` will be called to notify the ImageView. The ImageView can display a loading image place hodler. 

    Once image is loaded, `onLoadFinish()` will be called to notify the ImageView to display the image.

#Customize

<p class='lead'>Most of the components interface has a default implementions, you can implement for your own application.</p>

###A speical loading image
There are lots of ways:

1. Implement the interface `ImageLoadHandler`;

1. Use the `DefaultImageLoadHandler` which implements `ImageLoadHandler`:

    ```java
    DefaultImageLoadHandler handler = new DefaultImageLoadHandler();

    // pick one of the following method
    handler.setLoadingBitmap(Bitmap loadingBitmap);
    handler.setLoadingResources(int loadingBitmap);
    handler.setLoadingImageColor(int color);
    handler.setLoadingImageColor(String colorString);
    ```

###Some speical effect after the image is loaded

1.  You can also implement the `ImageLoadHandler`, in the `onLoadFinish()` method, you can add whatever effect you want, as long as they are based on `Drawable`.

2.  In the `DefaultImageLoadHandler`, there are some effects;
    * **Fade in** by default, this effect is on.

        ```java
    setImageFadeIn(boolean fadeIn);
        ```
    * **Rounded corner**

        ```java
    setImageRounded(boolean rouded, float cornerRadius);
        ```


###Diffrent url for diffrent size

If you have a thumbnail web service which can return multiple size image according the url, you can implements this method to return the specified url according the request size.

That is easy:

```java
public class EtaoImageResizer extends DefaultResizer {

    public String getResizedUrl(ImageTask imageTask) {

            int w = imageTask.getRequestSize().x;
            int h = imageTask.getRequestSize().y;

            // return url according the request size
    }
}
```
