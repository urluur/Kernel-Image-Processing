import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class CustomDialog extends JDialog {

  private final int PADDING = 25;

  public CustomDialog(JFrame frame, String message) {
    super(frame, "", true);
    JLabel loadingLabel = new JLabel(message, SwingConstants.CENTER);
    loadingLabel.setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    setAlwaysOnTop(true);
    setResizable(false);
    add(loadingLabel);
    pack();
    setLocationRelativeTo(frame);
  }
}