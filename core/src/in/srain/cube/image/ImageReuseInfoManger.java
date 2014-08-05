package in.srain.cube.image;

import java.util.ArrayList;

public class ImageReuseInfoManger {

    private String[] mSizeList;

    /**
     * Create Reuse Info Manger, the smaller size is in the front of the size array.
     *
     * @param size new String[] { "small_180", "big_360", "big_720" }
     */
    public ImageReuseInfoManger(String[] size) {
        mSizeList = size;
    }

    /**
     * Find out the size list can be re-sued.
     *
     * @param thisSize if "small_180" is input
     * @return {"big_360", "big_720"} will be returned.
     */
    public ImageReuseInfo create(String thisSize) {
        ArrayList<String> list = new ArrayList<String>();
        boolean canBeReused = false;
        for (int i = 0; i < mSizeList.length; i++) {
            String size = mSizeList[i];

            if (!canBeReused && thisSize.equals(size)) {
                canBeReused = true;
                continue;
            }
            if (canBeReused && !thisSize.equals(size)) {
                list.add(size);
            }
        }
        if (list.size() == 0) {
            return new ImageReuseInfo(thisSize, null);
        } else {
            String[] sizeList = new String[list.size()];
            list.toArray(sizeList);
            return new ImageReuseInfo(thisSize, sizeList);
        }
    }
}