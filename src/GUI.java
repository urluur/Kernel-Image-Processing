import java.awt.*;
import java.io.File;
import javax.swing.*;
import java.awt.dnd.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.awt.image.*;
import javax.imageio.ImageIO;
import javax.swing.border.Border;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.datatransfer.DataFlavor;

public class GUI {
  private JLabel originalImageLabel;
  private JLabel processedImageLabel;
  private JButton sequentialButton;
  private JButton parallelButton;
  private JLabel sequentialTimeLabel;
  private JLabel parallelTimeLabel;

  private BufferedImage originalImage;
  private BufferedImage processedImage;

  private Map<String, Kernel> kernels;
  private static Kernel selectedKernel;

  JComboBox<String> demoImagesComboBox;

  public GUI(Map<String, Kernel> kernels) {
    this.kernels = kernels;
    selectedKernel = this.kernels.get("Edge Detection");
  }

  public void setupUI(JFrame frame) {
    frame.setLayout(new BorderLayout());

    Border black_border = BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2);

    originalImageLabel = new JLabel("Drag and drop image here...", SwingConstants.CENTER);
    processedImageLabel = new JLabel("Result:", SwingConstants.CENTER);
    sequentialButton = new JButton("Sequential →");
    parallelButton = new JButton("Parallel →");
    sequentialTimeLabel = new JLabel("Sequential time: 0 ms");
    parallelTimeLabel = new JLabel("Parallel time: 0 ms");

    // Override paintComponent for image labels
    originalImageLabel = new JLabel() {
      @Override
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

    processedImageLabel = new JLabel() {
      @Override
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

    // Set border and alignment for image labels
    originalImageLabel.setBorder(black_border);
    processedImageLabel.setBorder(black_border);
    originalImageLabel.setHorizontalAlignment(JLabel.CENTER);
    processedImageLabel.setHorizontalAlignment(JLabel.CENTER);

    // Set up drag and drop functionality
    new DropTarget(originalImageLabel, new DropTargetAdapter() {
      @Override
      public void drop(DropTargetDropEvent dtde) {
        try {
          dtde.acceptDrop(DnDConstants.ACTION_COPY);
          @SuppressWarnings("unchecked")
          List<File> files = (List<File>) dtde.getTransferable()
              .getTransferData(DataFlavor.javaFileListFlavor);
          File file = files.get(0);

          CustomDialog loading = new CustomDialog(frame, "Loading...");

          SwingWorker<BufferedImage, Void> worker = new SwingWorker<>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
              return ImageIO.read(file);
            }

            @Override
            protected void done() {
              try {
                originalImage = get();

                originalImageLabel.repaint();
                sequentialTimeLabel.setText("Sequential time: 0 ms");
                parallelTimeLabel.setText("Parallel time: 0 ms");

                processedImage = null;
                processedImageLabel.repaint();

                demoImagesComboBox.setSelectedIndex(0);

                loading.dispose();
              } catch (Exception ex) {
                ex.printStackTrace();
              }
            }
          };

          worker.execute();
          loading.setVisible(true);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });

    // Set up ActionListener for Sequential button
    sequentialButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (originalImage == null) {
          JOptionPane.showMessageDialog(frame, "No image loaded", "Error", JOptionPane.ERROR_MESSAGE);
          return;
        }

        CustomDialog processing = new CustomDialog(frame, "Processing...");

        SwingWorker<BufferedImage, Void> worker = new SwingWorker<>() {
          @Override
          protected BufferedImage doInBackground() throws Exception {
            long startTime = System.currentTimeMillis();
            BufferedImage result = ImageProcessor.applyKernelSequential(originalImage, selectedKernel);
            long endTime = System.currentTimeMillis();
            sequentialTimeLabel.setText("Sequential time: " + (endTime - startTime) + " ms");
            return result;
          }

          @Override
          protected void done() {
            try {
              processedImage = get();
              processedImageLabel.repaint();
              processing.dispose();
            } catch (Exception ex) {
              ex.printStackTrace();
            }
          }
        };

        worker.execute();
        processing.setVisible(true);
      }
    });

    // Set up ActionListener for Parallel button
    parallelButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (originalImage == null) {
          JOptionPane.showMessageDialog(frame, "No image loaded", "Error", JOptionPane.ERROR_MESSAGE);
          return;
        }

        CustomDialog processing = new CustomDialog(frame, "Processing...");

        SwingWorker<BufferedImage, Void> worker = new SwingWorker<>() {
          @Override
          protected BufferedImage doInBackground() throws Exception {
            long startTime = System.currentTimeMillis();
            BufferedImage result = ImageProcessor.applyKernelParallel(originalImage, selectedKernel);
            long endTime = System.currentTimeMillis();
            parallelTimeLabel.setText("Parallel time: " + (endTime - startTime) + " ms");
            return result;
          }

          @Override
          protected void done() {
            try {
              processedImage = get();
              processedImageLabel.repaint();
              processing.dispose();
            } catch (Exception ex) {
              ex.printStackTrace();
            }
          }
        };

        worker.execute();
        processing.setVisible(true);
      }
    });

    // Set preferred size for buttons
    Dimension buttonSize = new Dimension(100, 40);
    sequentialButton.setPreferredSize(buttonSize);
    parallelButton.setPreferredSize(buttonSize);

    JButton resetButton = new JButton("Reset");
    resetButton.setPreferredSize(new Dimension(80, 30));
    resetButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        originalImage = null;
        processedImage = null;
        originalImageLabel.setIcon(null);
        processedImageLabel.setIcon(null);

        sequentialTimeLabel.setText("Sequential time: 0 ms");
        parallelTimeLabel.setText("Parallel time: 0 ms");

        originalImageLabel.repaint();
        processedImageLabel.repaint();

        demoImagesComboBox.setSelectedIndex(0);
      }
    });

    // Get the images
    File imgDir = new File("img");
    File[] imgFiles = imgDir
        .listFiles((dir, name) -> name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg"));

    // create a dropdown with images
    String[] imgFileNames = Arrays.stream(imgFiles).map(File::getName).toArray(String[]::new);
    demoImagesComboBox = new JComboBox<>(imgFileNames);

    demoImagesComboBox.insertItemAt("-- Demo images --", 0);
    demoImagesComboBox.setSelectedIndex(0);

    demoImagesComboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String selectedImageFileName = (String) demoImagesComboBox.getSelectedItem();
        if (!selectedImageFileName.equals("-- Demo images --")) {

          CustomDialog loading = new CustomDialog(frame, "Loading...");

          SwingWorker<BufferedImage, Void> worker = new SwingWorker<>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
              File selectedImageFile = new File(imgDir, selectedImageFileName);
              return ImageIO.read(selectedImageFile);
            }

            @Override
            protected void done() {
              try {
                originalImage = get();

                originalImageLabel.repaint();
                sequentialTimeLabel.setText("Sequential time: 0 ms");
                parallelTimeLabel.setText("Parallel time: 0 ms");

                processedImage = null;
                processedImageLabel.repaint();

                loading.dispose();
              } catch (Exception ex) {
                ex.printStackTrace();
              }
            }
          };

          worker.execute();
          loading.setVisible(true);
        }
      }
    });

    // Set up layout for top panel
    JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    topPanel.add(resetButton);
    topPanel.add(demoImagesComboBox);

    frame.add(topPanel, BorderLayout.NORTH);

    // Set up layout for center panel
    JPanel centerPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 0.5;
    gbc.weighty = 1.0;
    centerPanel.add(originalImageLabel, gbc);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
    buttonPanel.add(Box.createVerticalGlue());

    sequentialButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    buttonPanel.add(sequentialButton);

    parallelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    buttonPanel.add(parallelButton);

    sequentialTimeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    buttonPanel.add(sequentialTimeLabel);

    parallelTimeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    buttonPanel.add(parallelTimeLabel);

    buttonPanel.add(Box.createVerticalGlue());

    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.VERTICAL;
    gbc.weightx = 0;
    gbc.weighty = 1.0;
    centerPanel.add(buttonPanel, gbc);

    gbc.gridx = 2;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 0.5;
    gbc.weighty = 1.0;
    centerPanel.add(processedImageLabel, gbc);

    frame.add(centerPanel, BorderLayout.CENTER);
  }
}
