package com.urluur;

import java.awt.*;
import java.io.File;
import javax.swing.*;
import java.util.Map;
import java.awt.dnd.*;
import java.util.List;
import java.awt.image.*;
import java.util.ArrayList;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import java.io.BufferedReader;
import javax.swing.border.Border;
import java.io.InputStreamReader;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.datatransfer.DataFlavor;

public class GUI {
  private JLabel originalImageLabel, processedImageLabel;
  private JLabel dimensionsLabel;
  private JLabel sequentialTimeLabel, parallelTimeLabel;

  private JButton sequentialButton, parallelButton;
  private JButton resetButton;
  private JButton saveButton;

  private BufferedImage originalImage, processedImage;

  private Map<String, Kernel> kernels;
  private static Kernel selectedKernel;

  private JFrame frame;

  private JComboBox<String> demoImagesComboBox;
  private JComboBox<String> kernelsComboBox;

  public GUI(JFrame frame, Map<String, Kernel> kernels) {
    this.frame = frame;
    this.kernels = kernels;
  }

  public void setupUI() {
    frame.setLayout(new BorderLayout());
    setupImageLabels();
    setupDragAndDrop();
    setupButtons();
    setupDemoImages();
    setupKernels();
    setupPanels();
  }

  private void updateDimensionsLabel() {
    if (originalImage != null) {
      dimensionsLabel.setText("Dimensions: " + originalImage.getWidth() + "x" + originalImage.getHeight());
    } else {
      dimensionsLabel.setText("");
    }
  }

  private void setupImageLabels() {
    originalImageLabel = new JLabel("Drag and drop image here...", SwingConstants.CENTER);
    processedImageLabel = new JLabel("Result:", SwingConstants.CENTER);
    sequentialTimeLabel = new JLabel("Sequential: 0 ms");
    parallelTimeLabel = new JLabel("Parallel: 0 ms");
    dimensionsLabel = new JLabel();

    updateDimensionsLabel();

    // Create a border with padding
    Border paddingBorder = BorderFactory.createEmptyBorder(5, 15, 0, 15);
    Border black_border = BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2);

    // Add the border to the labels
    sequentialTimeLabel.setBorder(paddingBorder);
    parallelTimeLabel.setBorder(paddingBorder);

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
  }

  private void setupDragAndDrop() {
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

          CustomDialog loading = new CustomDialog(frame, "Loading image...");

          SwingWorker<BufferedImage, Void> worker = new SwingWorker<>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
              return ImageIO.read(file);
            }

            @Override
            protected void done() {
              try {
                originalImage = get();
                updateDimensionsLabel();

                originalImageLabel.repaint();
                sequentialTimeLabel.setText("Sequential: 0 ms");
                parallelTimeLabel.setText("Parallel: 0 ms");

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
  }

  private void setupButtons() {
    sequentialButton = new JButton("Sequential →");
    parallelButton = new JButton("Parallel →");

    // Set up ActionListener for Sequential button
    sequentialButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (originalImage == null) {
          JOptionPane.showMessageDialog(frame, "No image loaded", "Error", JOptionPane.ERROR_MESSAGE);
          return;
        }

        CustomDialog processing = new CustomDialog(frame, "Processing image sequentially...");

        SwingWorker<BufferedImage, Void> worker = new SwingWorker<>() {
          @Override
          protected BufferedImage doInBackground() throws Exception {
            long startTime = System.currentTimeMillis();
            BufferedImage result = ImageProcessor.applyKernelSequential(originalImage, selectedKernel);
            long endTime = System.currentTimeMillis();
            sequentialTimeLabel.setText("Sequential: " + (endTime - startTime) + " ms");
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

        CustomDialog processing = new CustomDialog(frame, "Processing image in parallel...");

        SwingWorker<BufferedImage, Void> worker = new SwingWorker<>() {
          @Override
          protected BufferedImage doInBackground() throws Exception {
            long startTime = System.currentTimeMillis();
            BufferedImage result = ImageProcessor.applyKernelParallel(originalImage, selectedKernel);
            long endTime = System.currentTimeMillis();
            parallelTimeLabel.setText("Parallel: " + (endTime - startTime) + " ms");
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

    resetButton = new JButton("Reset");
    resetButton.setPreferredSize(new Dimension(80, 30));
    resetButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        originalImage = null;
        processedImage = null;
        originalImageLabel.setIcon(null);
        processedImageLabel.setIcon(null);

        sequentialTimeLabel.setText("Sequential: 0 ms");
        parallelTimeLabel.setText("Parallel: 0 ms");

        originalImageLabel.repaint();
        processedImageLabel.repaint();

        updateDimensionsLabel();

        demoImagesComboBox.setSelectedIndex(0);
      }
    });

    // Create a Save button
    saveButton = new JButton("Save");
    saveButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        ImageUtils.saveImage(processedImage, frame);
      }
    });
  }

  private void setupDemoImages() {
    // TODO: works in vscode, but not in jar
    try (InputStream in = getClass().getResourceAsStream("/img");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {

      List<String> imgFileNames = new ArrayList<>();
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.toLowerCase().endsWith(".png") || line.toLowerCase().endsWith(".jpg")) {
          imgFileNames.add(line);
        }
      }
      // create a dropdown with images
      demoImagesComboBox = new JComboBox<>(imgFileNames.toArray(new String[0]));
    } catch (IOException e) {
      e.printStackTrace();
    }

    demoImagesComboBox.insertItemAt("-- Demo images --", 0);
    demoImagesComboBox.setSelectedIndex(0);

    demoImagesComboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String selectedImageFileName = (String) demoImagesComboBox.getSelectedItem();
        if (!selectedImageFileName.equals("-- Demo images --")) {

          CustomDialog loading = new CustomDialog(frame, "Loading image...");

          SwingWorker<BufferedImage, Void> worker = new SwingWorker<>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
              InputStream is = getClass().getResourceAsStream("/img/" + selectedImageFileName);
              return ImageIO.read(is);
            }

            @Override
            protected void done() {
              try {
                originalImage = get();
                updateDimensionsLabel();

                originalImageLabel.repaint();
                sequentialTimeLabel.setText("Sequential: 0 ms");
                parallelTimeLabel.setText("Parallel: 0 ms");

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
  }

  private void setupKernels() {
    String[] kernelNames = kernels.keySet().toArray(new String[0]); // get all kernels
    kernelsComboBox = new JComboBox<>(kernelNames); // add to combobox
    selectedKernel = kernels.get(kernelNames[0]); // set default kernel

    kernelsComboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String selectedKernelName = (String) kernelsComboBox.getSelectedItem();
        selectedKernel = kernels.get(selectedKernelName);
      }
    });
  }

  private void setupPanels() {
    // Set up layout for top panel
    JPanel topPanel = new JPanel(new BorderLayout());
    JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

    leftPanel.add(resetButton);
    leftPanel.add(demoImagesComboBox);
    leftPanel.add(kernelsComboBox);

    rightPanel.add(dimensionsLabel);
    rightPanel.add(saveButton);

    topPanel.add(leftPanel, BorderLayout.WEST);
    topPanel.add(rightPanel, BorderLayout.EAST);

    frame.add(topPanel, BorderLayout.NORTH);

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
