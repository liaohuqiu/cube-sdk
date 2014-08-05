package in.srain.cube.util;

import android.util.Log;

public class CLog {
    public static boolean DEBUG_IMAGE = false;
    public static boolean DEBUG_LIST = false;
    public static boolean DEBUG_SCROLL_HEADER_FRAME = false;

    public static final int LEVEL_NONE = -1;
    public static final int LEVEL_VERBOSE = 0;
    public static final int LEVEL_DEBUG = 1;
    public static final int LEVEL_INFO = 2;
    public static final int LEVEL_WARNING = 3;
    public static final int LEVEL_ERROR = 4;
    public static final int LEVEL_FATAL = 5;

    private static int sLevel = LEVEL_VERBOSE;

    public void setLogLevel(int level) {
        sLevel = level;
    }

    public static void v(String tag, String msg) {
        if (sLevel >= LEVEL_VERBOSE) {
            return;
        }
        Log.v(tag, msg);
    }

    public static void v(String tag, String msg, Object... args) {
        if (sLevel >= LEVEL_VERBOSE) {
            return;
        }
        if (args.length > 0) {
            msg = String.format(msg, args);
        }
        v(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (sLevel >= LEVEL_DEBUG) {
            return;
        }
        Log.d(tag, msg);
    }

    public static void d(String tag, String msg, Object... args) {
        if (sLevel >= LEVEL_DEBUG) {
            return;
        }
        if (args.length > 0) {
            msg = String.format(msg, args);
        }
        Log.d(tag, msg);
    }

    public static void i(String tag, String msg) {
        if (sLevel >= LEVEL_INFO) {
            return;
        }
        Log.i(tag, msg);
    }

    public static void i(String tag, String msg, Object... args) {
        if (sLevel >= LEVEL_INFO) {
            return;
        }
        if (args.length > 0) {
            msg = String.format(msg, args);
        }
        i(tag, msg);
    }

    public static void w(String tag, String msg) {
        if (sLevel >= LEVEL_WARNING) {
            return;
        }
        Log.w(tag, msg);
    }

    public static void w(String tag, String msg, Object... args) {
        if (sLevel >= LEVEL_WARNING) {
            return;
        }
        if (args.length > 0) {
            msg = String.format(msg, args);
        }
        w(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (sLevel >= LEVEL_ERROR) {
            return;
        }
        Log.e(tag, msg);
    }

    public static void e(String tag, String msg, Object... args) {
        if (sLevel >= LEVEL_ERROR) {
            return;
        }
        if (args.length > 0) {
            msg = String.format(msg, args);
        }
        e(tag, msg);
    }

    public static void f(String tag, String msg) {
        if (sLevel >= LEVEL_FATAL) {
            return;
        }
        Log.wtf(tag, msg);
    }

    public static void f(String tag, String msg, Object... args) {
        if (sLevel >= LEVEL_FATAL) {
            return;
        }
        if (args.length > 0) {
            msg = String.format(msg, args);
        }
        f(tag, msg);
    }
}
