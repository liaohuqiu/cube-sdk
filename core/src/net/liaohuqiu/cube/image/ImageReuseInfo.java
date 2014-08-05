package net.liaohuqiu.cube.image;

public class ImageReuseInfo {

    private String mIdentitySize;
    private String[] mReuseSize;

    public ImageReuseInfo(String indentitySize, String[] reuseSize) {
        mIdentitySize = indentitySize;
        mReuseSize = reuseSize;
    }

    public String getIdentitySize() {
        return mIdentitySize;
    }

    public String[] getReuseSizeList() {
        return mReuseSize;
    }
}