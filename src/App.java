import javax.swing.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.image.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.border.Border;

public class App {
    private static JLabel originalImageLabel = new JLabel("Drag and drop image here...", SwingConstants.CENTER);
    private static JLabel processedImageLabel = new JLabel("Result:", SwingConstants.CENTER);
    private static JButton sequentialButton = new JButton("Sequential");
    private static JButton parallelButton = new JButton("Parallel");

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

        originalImageLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (originalImage != null) {
                    Image scaledImage = getScaledImage(originalImage, getWidth(), getHeight());
                    g.drawImage(scaledImage, 0, 0, this);
                } else {
                    g.drawString("Drag and drop image here...", 10, 20);
                }
            }
        };

        // create and apply a border around images
        Border black_border = BorderFactory.createLineBorder(Color.BLACK, 3);

        originalImageLabel.setBorder(black_border);
        originalImageLabel.setHorizontalAlignment(JLabel.CENTER);

        processedImageLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (processedImage != null) {
                    Image scaledImage = getScaledImage(processedImage, getWidth(), getHeight());
                    g.drawImage(scaledImage, 0, 0, this);
                } else {
                    g.drawString("Result:", 10, 20);
                }
            }
        };

        processedImageLabel.setBorder(black_border); // Set the same border as originalImageLabel
        processedImageLabel.setHorizontalAlignment(JLabel.CENTER);

        new DropTarget(originalImageLabel, new DropTargetAdapter() {
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY); // Accept the drop operation

                    @SuppressWarnings("unchecked")
                    List<File> files = (List<File>) dtde.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);
                    File file = files.get(0);
                    originalImage = ImageIO.read(file);

                    originalImageLabel.repaint(); // Repaint the label to update the image
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

                processedImage = applyKernelSequential(originalImage, kernel);
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

                processedImage = applyKernelParallel(originalImage, kernel);
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

    /**
     * Get a scaled version of the image.
     * 
     * @param srcImg       The image to scale
     * @param targetWidth  The target width
     * @param targetHeight The target height
     * @return The scaled image
     */
    private static Image getScaledImage(Image srcImg, int targetWidth, int targetHeight) {
        double ratio = Math.min(targetWidth / (double) srcImg.getWidth(null),
                targetHeight / (double) srcImg.getHeight(null));

        int width = (int) (srcImg.getWidth(null) * ratio);
        int height = (int) (srcImg.getHeight(null) * ratio);

        BufferedImage resizedImg = new BufferedImage(targetWidth, targetHeight, BufferedImage.TRANSLUCENT);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg, (targetWidth - width) / 2, (targetHeight - height) / 2, width, height, null);
        g2.dispose();

        return resizedImg;
    }

    /**
     * Apply the kernel to the image.
     * 
     * @param image  The image to apply the kernel to
     * @param kernel The kernel to apply
     * @param result The image to store the result in
     */
    private static void applyKernel(BufferedImage image, Kernel kernel, BufferedImage result) {
        int width = image.getWidth();
        int height = image.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                applyKernelToPixel(image, kernel, result, x, y);
            }
        }
    }

    /**
     * Apply the kernel to the image sequentially.
     * 
     * @param image  The image to apply the kernel to
     * @param kernel The kernel to apply
     * @return The image with the kernel applied
     */
    public static BufferedImage applyKernelSequential(BufferedImage image, Kernel kernel) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        applyKernel(image, kernel, result);
        return result;
    }

    /**
     * Apply the kernel to the image in parallel.
     * 
     * @param image  The image to apply the kernel to
     * @param kernel The kernel to apply
     * @return The image with the kernel applied
     */
    public static BufferedImage applyKernelParallel(BufferedImage image, Kernel kernel) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, image.getType());

        try (ForkJoinPool pool = new ForkJoinPool()) {
            pool.submit(() -> {
                IntStream.range(0, height).parallel().forEach(y -> {
                    for (int x = 0; x < width; x++) {
                        applyKernelToPixel(image, kernel, result, x, y);
                    }
                });
            }).join();
        }

        return result;
    }

    /**
     * Apply the kernel to the pixel at position (x, y) in the image.
     * 
     * @param image  The image to apply the kernel to
     * @param kernel The kernel to apply
     * @param result The image to store the result in
     * @param x      The x position of the pixel
     * @param y      The y position of the pixel
     */
    private static void applyKernelToPixel(BufferedImage image, Kernel kernel, BufferedImage result, int x, int y) {
        int width = image.getWidth();
        int height = image.getHeight();

        int kernelWidth = kernel.getWidth();
        int kernelHeight = kernel.getHeight();
        float[] kernelData = kernel.getKernelData(null);

        float r = 0, g = 0, b = 0;
        for (int ky = -kernelHeight / 2; ky <= kernelHeight / 2; ky++) { // For each kernel row
            for (int kx = -kernelWidth / 2; kx <= kernelWidth / 2; kx++) { // For each kernel column

                // Calculate the adjacent pixel's position
                int pixelX = Math.min(Math.max(x + kx, 0), width - 1);
                int pixelY = Math.min(Math.max(y + ky, 0), height - 1);

                Color pixelColor = new Color(image.getRGB(pixelX, pixelY)); // Get the adjacent pixel's color

                // Get the kernel value
                float kernelValue = kernelData[(ky + kernelHeight / 2) * kernelWidth + (kx + kernelWidth / 2)];

                // Multiply the pixel's color with the kernel value
                r += pixelColor.getRed() * kernelValue;
                g += pixelColor.getGreen() * kernelValue; // Multiply the pixel's color with the kernel value
                b += pixelColor.getBlue() * kernelValue; // Multiply the pixel's color with the kernel value
            }
        }
        int newR = Math.min(Math.max((int) r, 0), 255);
        int newG = Math.min(Math.max((int) g, 0), 255);
        int newB = Math.min(Math.max((int) b, 0), 255);
        // Set the pixel to the new color
        Color newColor = new Color(newR, newG, newB);
        result.setRGB(x, y, newColor.getRGB());
    }
}