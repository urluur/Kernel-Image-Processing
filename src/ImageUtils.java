import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

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

}
