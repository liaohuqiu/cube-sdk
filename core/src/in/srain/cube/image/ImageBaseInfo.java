package in.srain.cube.image;

/**
 * Created by xufu.lg on 2015/3/30.
 */
public class ImageBaseInfo {
    public String url;
    public int requestWidth;
    public int requestHeight;
    public int priority;

    public ImageBaseInfo(){

    }

    public ImageBaseInfo(String url, int requestWidth, int requestHeight, int priority){
        this.url = url;
        this.requestHeight = requestHeight;
        this.requestWidth = requestWidth;
        this.priority = priority;
    }
}
