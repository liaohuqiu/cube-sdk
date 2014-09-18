package in.srain.cube.image;

public class ImagePerformanceStatistics {

    private static int sSAMPLE_NUM = 0;

    public static void setSample(int num) {
        sSAMPLE_NUM = num;
    }

    public static boolean sample(int id) {
        return sSAMPLE_NUM != 0 && id % sSAMPLE_NUM == 0;
    }
}