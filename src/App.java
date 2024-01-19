import java.awt.*;
import java.io.File;
import javax.swing.*;
import java.awt.dnd.*;
import java.util.List;
import java.awt.image.*;
import javax.imageio.ImageIO;
import javax.swing.border.Border;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.datatransfer.DataFlavor;

public class App {
    private static JLabel originalImageLabel = new JLabel("Drag and drop image here...", SwingConstants.CENTER);
    private static JLabel processedImageLabel = new JLabel("Result:", SwingConstants.CENTER);
    private static JButton sequentialButton = new JButton("Sequential");
    private static JButton parallelButton = new JButton("Parallel");

    private static JLabel sequentialTimeLabel = new JLabel("Sequential time: 0 ms");
    private static JLabel parallelTimeLabel = new JLabel("Parallel time: 0 ms");

    private static final float[] KERNEL_BLUR = {
            1f / 25f, 1f / 25f, 1f / 25f, 1f / 25f, 1f / 25f,
            1f / 25f, 1f / 25f, 1f / 25f, 1f / 25f, 1f / 25f,
            1f / 25f, 1f / 25f, 1f / 25f, 1f / 25f, 1f / 25f,
            1f / 25f, 1f / 25f, 1f / 25f, 1f / 25f, 1f / 25f,
            1f / 25f, 1f / 25f, 1f / 25f, 1f / 25f, 1f / 25f,
    };

    private static BufferedImage originalImage;
    private static BufferedImage processedImage;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Kernel image processor");
        frame.setLayout(new BorderLayout());

        // create a black border that will be placed around images
        Border black_border = BorderFactory.createLineBorder(Color.BLACK, 3);

        // setup the label that will display the original image
        originalImageLabel = new JLabel() {
            @Override // override the paintComponent method to display the scaled image
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (originalImage != null) {
                    Image scaledImage = ImageUtils.getScaledImage(originalImage, getWidth(), getHeight());
                    g.drawImage(scaledImage, 0, 0, this);
                } else {
                    g.drawString("Drag and drop image here...", 10, 20);
                }
            }
        };

        // setup the label that will display the processed image
        processedImageLabel = new JLabel() {
            @Override // override the paintComponent method to display the scaled image
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (processedImage != null) {
                    Image scaledImage = ImageUtils.getScaledImage(processedImage, getWidth(), getHeight());
                    g.drawImage(scaledImage, 0, 0, this);
                } else {
                    g.drawString("Result:", 10, 20);
                }
            }
        };

        // set the border around two images and set them to be centered
        originalImageLabel.setBorder(black_border);
        processedImageLabel.setBorder(black_border);
        originalImageLabel.setHorizontalAlignment(JLabel.CENTER);
        processedImageLabel.setHorizontalAlignment(JLabel.CENTER);

        JPanel resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));

        resultsPanel.add(sequentialTimeLabel);
        resultsPanel.add(parallelTimeLabel);

        // setup the drag and drop functionality for the original image label
        new DropTarget(originalImageLabel, new DropTargetAdapter() {
            @Override // override the drop method to load the image
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY); // Accept the drop operation

                    @SuppressWarnings("unchecked")
                    List<File> files = (List<File>) dtde.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);
                    File file = files.get(0);
                    originalImage = ImageIO.read(file);

                    originalImageLabel.repaint(); // Repaint the label to update the image

                    sequentialTimeLabel.setText("Sequential time: 0 ms");
                    parallelTimeLabel.setText("Parallel time: 0 ms");

                    processedImage = null;
                    processedImageLabel.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        sequentialButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (originalImage == null) {
                    JOptionPane.showMessageDialog(frame, "No image loaded", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                float[] data = KERNEL_BLUR;
                Kernel kernel = new Kernel(5, 5, data);

                long startTime = System.currentTimeMillis();
                processedImage = ImageProcessor.applyKernelSequential(originalImage, kernel);
                long endTime = System.currentTimeMillis();
                sequentialTimeLabel.setText("Sequential time: " + (endTime - startTime) + " ms");

                processedImageLabel.repaint();
            }
        });

        parallelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (originalImage == null) {
                    JOptionPane.showMessageDialog(frame, "No image loaded", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                float[] data = KERNEL_BLUR;
                Kernel kernel = new Kernel(5, 5, data);

                long startTime = System.currentTimeMillis();
                processedImage = ImageProcessor.applyKernelParallel(originalImage, kernel);
                long endTime = System.currentTimeMillis();
                parallelTimeLabel.setText("Parallel time: " + (endTime - startTime) + " ms");

                processedImageLabel.repaint();
            }
        });

        // Set the preferred size of the Sequential and Parallel buttons
        Dimension buttonSize = new Dimension(100, 40);
        sequentialButton.setPreferredSize(buttonSize);
        parallelButton.setPreferredSize(buttonSize);

        JButton resetButton = new JButton("Reset");
        resetButton.setPreferredSize(new Dimension(80, 30));
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Clear the images
                originalImage = null;
                processedImage = null;
                // Clear the labels
                originalImageLabel.setIcon(null);
                processedImageLabel.setIcon(null);

                sequentialTimeLabel.setText("Sequential time: 0 ms");
                parallelTimeLabel.setText("Parallel time: 0 ms");

                // Repaint the labels to update the display
                originalImageLabel.repaint();
                processedImageLabel.repaint();
            }
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(resetButton);

        frame.add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Add originalImageLabel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0;
        centerPanel.add(originalImageLabel, gbc);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(sequentialButton);
        buttonPanel.add(parallelButton);
        buttonPanel.add(resultsPanel);
        buttonPanel.add(Box.createVerticalGlue());

        // Add buttonPanel
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.weightx = 0;
        gbc.weighty = 1.0;
        centerPanel.add(buttonPanel, gbc);

        // Add processedImageLabel
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0;
        centerPanel.add(processedImageLabel, gbc);

        frame.add(centerPanel, BorderLayout.CENTER);

        frame.setPreferredSize(new Dimension(1200, 600));
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}