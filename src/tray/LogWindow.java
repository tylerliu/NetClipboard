package tray;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

class LogWindow extends JFrame {
    private static LogWindow window = null;

    private JTextArea textArea;
    private JScrollPane pane;

    public static LogWindow getLogWindow() {
        if (window == null) window = new LogWindow("Warning", 500, 300);
        return window;
    }

    public LogWindow(String title, int width, int height) {
        super(title);
        setSize(width, height);
        textArea = new JTextArea();
        textArea.setEditable(false);
        pane = new JScrollPane(textArea);
        getContentPane().add(pane);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

    public void toggle() {
        if (hasFocus()) setVisible(false);
        else {
            setVisible(true);
            requestFocus();
        }
    }

    /**
     * This method appends the data to the text area.
     *
     * @param data
     *            the Logging information data
     */
    void showInfo(String data) {
        textArea.append(data);
        this.getContentPane().validate();
    }

    void showError(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        showInfo(sw.toString());
    }
}
