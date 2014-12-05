package in.srain.cube.util;

public class Env {

    private static final String ENV_PROD = "prod";
    private static final String ENV_PRE = "pre";
    private static final String ENV_DEV = "dev";

    private static String sEnvTag = ENV_DEV;

    public static boolean isProd() {
        return ENV_PROD.equals(sEnvTag);
    }

    public static boolean isPre() {
        return ENV_PRE.equals(sEnvTag);
    }

    public static boolean isDev() {
        return ENV_DEV.equals(sEnvTag);
    }

    public static String getEnvTag() {
        return sEnvTag;
    }

    public static void setEnvTag(String tag) {
        sEnvTag = tag;
    }
}
