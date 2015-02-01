package in.srain.cube.request;

public class FailData {

    public static final int TYPE_DATA_FORMAT = 1;
    public int type = TYPE_DATA_FORMAT;
    public String responseContent;

    public FailData(String responseContent) {
        this.responseContent = responseContent;
    }
}
