import javax.swing.*;

public class CustomDialog extends JDialog {
  public CustomDialog(JFrame frame, String message) {
    super(frame, "", true);
    JLabel loadingLabel = new JLabel(message, SwingConstants.CENTER);
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    setAlwaysOnTop(true);
    setSize(100, 100);
    setResizable(false);
    setLocationRelativeTo(frame);
    add(loadingLabel);
  }
}