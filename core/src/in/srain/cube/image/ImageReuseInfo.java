package in.srain.cube.image;

public class ImageReuseInfo {

    private String mIdentitySize;
    private String[] mReuseSize;

    public ImageReuseInfo(String identitySize, String[] reuseSize) {
        mIdentitySize = identitySize;
        mReuseSize = reuseSize;
    }

    public String getIdentitySize() {
        return mIdentitySize;
    }

    public String[] getReuseSizeList() {
        return mReuseSize;
    }
}