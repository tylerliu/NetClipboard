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
    public static final byte GENERAL_STRING = 4;
    public static final byte BYTEBUFFER = 5;
    public static final byte SERIALIZABLE = 6;
    public static final byte IMAGE = 7;
    public static final byte FILES = 8;

    public static final javafx.scene.input.DataFormat[] FXFormats = new javafx.scene.input.DataFormat[] {
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            javafx.scene.input.DataFormat.IMAGE,
            javafx.scene.input.DataFormat.FILES
    };

    public static byte getFormat(javafx.scene.input.DataFormat dataFormat) {
        for (byte i = 7; i < FXFormats.length; i ++) {
            if (dataFormat.equals(FXFormats[i])) return i;
        }
        return DataFormat.NULL;
    }
}
