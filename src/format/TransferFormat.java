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

public class TransferFormat {

    public static final byte NULL = 0;
    public static final byte FORMAT_COUNT = 1;
    public static final byte END_SIGNAL = 2;
    public static final byte MODE_SET = 3;
    public static final byte GENERAL_STRING = 4;
    public static final byte BYTE_BUFFER = 5;
    public static final byte SERIALIZABLE = 6;
    public static final byte IMAGE = 7;
    public static final byte FILES = 8;
}
