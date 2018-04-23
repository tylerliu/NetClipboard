package format;

/**
 * a class output stream that can transfer multiple format of file
 * <p>
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
    public static final byte STRING = 1;
    public static final byte FILES = 2;
    public static final byte HTML = 3;
    public static final byte END_SIGNAL = 4;
}
