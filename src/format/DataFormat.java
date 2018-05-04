package format;

/**
 * headers:
 * format:
 * 1: String
 * 2: Files
 * 3: HTML
 * 4: END_SIGNAL
 * Created by TylerLiu on 2017/10/01.
 */

public class DataFormat {

    public static final byte NULL = 0;
    public static final byte FORMAT_COUNT = 1;
    public static final byte END_SIGNAL = 2;
    public static final byte MODE_SET = 3;
    public static final byte STRING = 4;
    public static final byte FILES = 5;
    public static final byte IMAGE = 6;
    public static final byte HTML = 7;
    public static final byte RTF = 8;
    public static final byte URL = 9;

}
