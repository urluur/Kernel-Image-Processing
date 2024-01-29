package com.urluur;

import java.io.File;
import java.awt.Image;
import javax.swing.JFrame;
import java.io.IOException;
import java.awt.Graphics2D;
import javax.imageio.ImageIO;
import java.awt.RenderingHints;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ImageUtils {

  /**
   * Get a scaled version of the image.
   * 
   * @param srcImg       The image to scale
   * @param targetWidth  The target width
   * @param targetHeight The target height
   * @return The scaled image
   */
  public static Image getScaledImage(Image srcImg, int targetWidth, int targetHeight) {
    double ratio = Math.min(targetWidth / (double) srcImg.getWidth(null),
        targetHeight / (double) srcImg.getHeight(null));

    int width = (int) (srcImg.getWidth(null) * ratio);
    int height = (int) (srcImg.getHeight(null) * ratio);

    // Create a new transparent image (set the size)
    BufferedImage resizedImg = new BufferedImage(
        targetWidth,
        targetHeight,
        BufferedImage.TRANSLUCENT);

    Graphics2D g2 = resizedImg.createGraphics();
    g2.setRenderingHint(
        RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BILINEAR);

    g2.drawImage(srcImg, (targetWidth - width) / 2, (targetHeight - height) / 2, width, height, null);
    g2.dispose();

    return resizedImg;
  }

  public static void saveImage(Image processedImage, JFrame frame) {
    if (processedImage == null) {
      JOptionPane.showMessageDialog(frame, "No processed image to save", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Specify a file to save");

    // Add file filters for .png and .jpg
    FileNameExtensionFilter pngFilter = new FileNameExtensionFilter("PNG images", "png");
    FileNameExtensionFilter jpgFilter = new FileNameExtensionFilter("JPEG images", "jpg");
    fileChooser.addChoosableFileFilter(pngFilter);
    fileChooser.addChoosableFileFilter(jpgFilter);
    fileChooser.setFileFilter(jpgFilter); // Set default file filter

    int userSelection = fileChooser.showSaveDialog(frame);

    if (userSelection == JFileChooser.APPROVE_OPTION) {
      File fileToSave = fileChooser.getSelectedFile();
      String selectedFileExtension = ((FileNameExtensionFilter) fileChooser.getFileFilter()).getExtensions()[0];

      // Append file extension if not present
      if (!fileToSave.getName().toLowerCase().endsWith("." + selectedFileExtension)) {
        fileToSave = new File(fileToSave.toString() + "." + selectedFileExtension);
      }

      try {
        ImageIO.write((RenderedImage) processedImage, selectedFileExtension, fileToSave);
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }

}
