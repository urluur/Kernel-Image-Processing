package com.urluur;

import java.awt.Color;
import java.awt.image.Kernel;
import java.util.stream.IntStream;
import java.awt.image.BufferedImage;
import java.util.concurrent.ForkJoinPool;

public class ImageProcessor {

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

    // For each pixel in the image
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        applyKernelToPixel(image, kernel, result, x, y);
      }
    }
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
  public static void applyKernelToPixel(BufferedImage image, Kernel kernel, BufferedImage result, int x, int y) {
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
        g += pixelColor.getGreen() * kernelValue;
        b += pixelColor.getBlue() * kernelValue;
      }
    }
    int newR = Math.min(Math.max((int) r, 0), 255);
    int newG = Math.min(Math.max((int) g, 0), 255);
    int newB = Math.min(Math.max((int) b, 0), 255);
    // Set the pixel to the new color
    Color newColor = new Color(newR, newG, newB);
    result.setRGB(x, y, newColor.getRGB());
  }

  /**
   * Apply the kernel to the image sequentially.
   * 
   * @param image  The image to apply the kernel to
   * @param kernel The kernel to apply
   * @return The image with the kernel applied sequentially
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
   * @return The image with the kernel applied in parallel
   */
  public static BufferedImage applyKernelParallel(BufferedImage image, Kernel kernel) {
    int width = image.getWidth();
    int height = image.getHeight();
    BufferedImage result = new BufferedImage(width, height, image.getType());

    // Create a ForkJoinPool that adapts to the number of available processors
    try (ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors())) {
      pool.submit(() -> {
        IntStream.range(0, width * height).parallel().forEach(i -> { // for each pixel
          int x = i % width;
          int y = i / width;
          applyKernelToPixel(image, kernel, result, x, y);
        });
      }).join();
    }

    return result;
  }

  /**
   * Apply the kernel to the image in parallel using blocks.
   * 
   * @param image  The image to apply the kernel to
   * @param kernel The kernel to apply
   * @return The image with the kernel applied in parallel
   */
  public static BufferedImage applyKernelParallelBlocks(BufferedImage image, Kernel kernel) {
    int width = image.getWidth();
    int height = image.getHeight();
    BufferedImage result = new BufferedImage(width, height, image.getType());

    final int BLOCK_SIZE = 32; // 32 because it is a common cache line size

    try (ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors())) {
      // number of blocks in both dimensions calculation
      int numBlocksX = (width + BLOCK_SIZE - 1) / BLOCK_SIZE;
      int numBlocksY = (height + BLOCK_SIZE - 1) / BLOCK_SIZE;

      // each task is one block
      pool.submit(() -> {
        IntStream.range(0, numBlocksX * numBlocksY).parallel().forEach(blockIndex -> {
          int blockX = blockIndex % numBlocksX;
          int blockY = blockIndex / numBlocksX;
          int startX = blockX * BLOCK_SIZE;
          int startY = blockY * BLOCK_SIZE;
          int endX = Math.min(startX + BLOCK_SIZE, width);
          int endY = Math.min(startY + BLOCK_SIZE, height);

          for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
              applyKernelToPixel(image, kernel, result, x, y);
            }
          }
        });
      }).join();
    }

    return result;
  }
}
