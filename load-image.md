---
layout: default
title: Image Loader
lead: "Make Loading Image Efficiently & Easily."
---
#ImageLoader
---
<p class='lead'>The <code>ImageLoader</code>, it is...</p>

#Simple Usage
---
<p class='lead'>It is very simple to use <code>ImageLoader</code>.</p>

1. First, create an `ImageLoader`:

    ```
    Context context;
    ImageLoader imageLoader = ImageLoaderFatory.create(context);
    ```

2. Find the ImageView in which the image will be loaded.

    ```
    CubeImageView imageView = (CubeImageView) view.findViewById(R.id.iv_item_image_list_big);
    ```

    `CubeImageView` is a subclass of `ImageView`, the layout file maybe like:

    ```
    <?xml version="1.0" encoding="utf-8"?>
    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:orientation="vertical" >

        <com.srain.cube.image.CubeImageView
            android:id="@+id/iv_item_image_list_big"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:scaleType="fitCenter" />

    </LinearLayout>
    ```

3. Use the ImageView and the ImageLoader to load image

    ```
    String url = '';
    imageView.loadImage(imageLoader, url);
    ```

#How does it work
---
<p class='lead'>The process of loading a bitmap is also very simple.</p>

##Workflow

1.  If image has been loaded, it will not be loaded again.
2.  Check if the image is in memory cache, if yes, display it.
3.  An `ImageTask` is created, then added to `ImageLoader`. The `ImageTaskExecutor` will take it.
    
    `ImageTaskExecutor` has an work queue, it has some threads to do the work in backgroud. If all of the threads are busy with current task, this task will put into the work queue.
    
4.  If the image is stored in file cache, it will be decoded, or else `ImageProvider` will fetch it from remote server then store it to file cache.

5.  After decoded, the process is complete. The ImageView will be notified to display this image.

##Components

There are four components used in `ImageLoader` to load image. As you can see in the construct method in the `ImageLoader`:

* `ImageLoader`

    ```
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

###Customize the loading image
There are lots of ways:

1. Implement the interface `ImageLoadHandler`;

2. Use the `DefaultImageLoadHandler`:

    ```
    DefaultImageLoadHandler handler = new DefaultImageLoadHandler();
    handler.set();
    ```

###Some speical effect after the image is loaded

1.  In the `DefaultImageLoadHandler`, there is a fade in effect when the image is loaded, you can use this. This is on by default, you can call `setFadeIn(false)` to turn it off.

2.  You can also implement the `ImageLoadHandler`, in the `onLoadFinish()` method, you can add whatever effect you want, as long as they are based on `Drawable`.

###Diffrent url for diffrent size?

If you have a thumbnail web service which can return multiple size image according the url, you can implements this method to return the specified url according the request size.

That is easy:

```
public class EtaoImageResizer extends DefaultResizer {

    public String getResizedUrl(ImageTask imageTask) {

            int w = imageTask.getRequestSize().x;
            int h = imageTask.getRequestSize().y;

            // return url according the request size
    }
```



###A webp format?
