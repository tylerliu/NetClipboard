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
    public static final byte HTML = 5;
    public static final byte RTF = 6;
    public static final byte URL = 7;
    public static final byte IMAGE = 8;
    public static final byte FILES = 9;
    public static final byte GENERAL_STRING = 10;
    public static final byte BYTEBUFFER = 11;

    public static final javafx.scene.input.DataFormat[] FXFormats = new javafx.scene.input.DataFormat[] {
            null,
            null,
            null,
            null,
            javafx.scene.input.DataFormat.PLAIN_TEXT,
            javafx.scene.input.DataFormat.HTML,
            javafx.scene.input.DataFormat.RTF,
            javafx.scene.input.DataFormat.URL,
            javafx.scene.input.DataFormat.IMAGE,
            javafx.scene.input.DataFormat.FILES

    };

    public static byte getFormat(javafx.scene.input.DataFormat dataFormat) {
        for (byte i = 4; i < FXFormats.length; i ++) {
            if (dataFormat.equals(FXFormats[i])) return i;
        }
        return DataFormat.NULL;
    }
}
