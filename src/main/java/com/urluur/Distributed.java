package com.urluur;

import mpi.*;
import java.io.IOException;
import java.awt.image.Kernel;
import java.awt.image.BufferedImage;

public class Distributed {

  private static int rank;
  private static int size;

  /**
   * Master method for distributed image processing.
   * Sends image chunks and kernel to workers, processes its own chunk, and
   * combines the results from workers.
   * 
   * @param image Image to process
   * @param kernel Kernel to apply to the image
   * @return Processed image
   */
  public static BufferedImage masterDistributed(BufferedImage image, Kernel kernel) {
    Distributed.rank = App.getRank();
    Distributed.size = App.getSize();

    // Convert Kernel to SerializableKernel
    SerializableKernel serializableKernel = new SerializableKernel(kernel);

    int width = image.getWidth();
    int height = image.getHeight();
    int chunkHeight = height / size;

    // Send each worker its chunk of the image and the kernel
    // TODO: broadcast the whole image to all workers
    for (int i = 1; i < size; i++) {
      int startY = i * chunkHeight;
      int endY = (i == size - 1) ? height : startY + chunkHeight;
      BufferedImage chunk = image.getSubimage(0, startY, width, endY - startY);

      byte[] chunkBytes = null;
      try {
        chunkBytes = ImageUtils.bufferedImageToByteArray(chunk, "png");
      } catch (IOException e) {
        e.printStackTrace();
        MPI.Finalize();
        return null;
      }

      Object[] buffer = new Object[] { chunkBytes, serializableKernel };
      MPI.COMM_WORLD.Send(buffer, 0, 2, MPI.OBJECT, i, 0);
    }

    // Master processes its chunk
    long startTime = System.currentTimeMillis();
    BufferedImage masterChunk = image.getSubimage(0, 0, width, chunkHeight);
    BufferedImage result = new BufferedImage(width, height, image.getType());
    BufferedImage processedMasterChunk = applyKernelDistributed(masterChunk, kernel);

    // Copy processed master chunk to the result
    for (int y = 0; y < chunkHeight; y++) {
      for (int x = 0; x < width; x++) {
        result.setRGB(x, y, processedMasterChunk.getRGB(x, y));
      }
    }
    long endTime = System.currentTimeMillis();
    System.out.println("Master done in " + (endTime - startTime) + "ms");

    // Combine results from workers
    for (int i = 1; i < size; i++) {
      int startY = i * chunkHeight;

      // Receive processed chunk from worker
      Object[] buffer = new Object[1];
      MPI.COMM_WORLD.Recv(buffer, 0, 1, MPI.OBJECT, i, 0);
      byte[] workerResultBytes = (byte[]) buffer[0];
      BufferedImage workerResult = null;
      try {
        workerResult = ImageUtils.byteArrayToBufferedImage(workerResultBytes);
      } catch (IOException e) {
        e.printStackTrace();
        MPI.Finalize();
        return null;
      }

      // Combine worker result into final result
      for (int y = 0; y < workerResult.getHeight(); y++) {
        for (int x = 0; x < width; x++) {
          result.setRGB(x, startY + y, workerResult.getRGB(x, y));
        }
      }
    }

    return result;
  }


  /**
   * Worker method for distributed image processing.
   * Receives image chunk and kernel from master, processes the chunk, and
   * sends the processed chunk back to the master.
   * 
   * @param args command line arguments
   */
  public static void workerDistributed(String[] args) {
    Distributed.rank = App.getRank();
    Distributed.size = App.getSize();

    // Receive the image chunk and kernel from the master
    Object[] buffer = new Object[2];
    MPI.COMM_WORLD.Recv(buffer, 0, 2, MPI.OBJECT, 0, 0);
    byte[] chunkBytes = (byte[]) buffer[0];
    BufferedImage chunk = null;
    try {
      chunk = ImageUtils.byteArrayToBufferedImage(chunkBytes);
    } catch (IOException e) {
      e.printStackTrace();
      MPI.Finalize();
      return;
    }
    SerializableKernel serializableKernel = (SerializableKernel) buffer[1];
    Kernel kernel = serializableKernel.toKernel();

    // Apply kernel to the chunk
    long startTime = System.currentTimeMillis();
    BufferedImage processedChunk = applyKernelDistributed(chunk, kernel);
    long endTime = System.currentTimeMillis();
    System.out.println("Rank " + Distributed.rank + " done in " + (endTime - startTime) + "ms");

    // Send the processed chunk back to the master
    byte[] resultBytes = null;
    try {
      resultBytes = ImageUtils.bufferedImageToByteArray(processedChunk, "png");
    } catch (IOException e) {
      e.printStackTrace();
      MPI.Finalize();
      return;
    }
    Object[] resultBuffer = new Object[] { resultBytes };
    MPI.COMM_WORLD.Send(resultBuffer, 0, 1, MPI.OBJECT, 0, 0);
  }

  public static BufferedImage applyKernelDistributed(BufferedImage image, Kernel kernel) {
    int width = image.getWidth();
    int height = image.getHeight();
    BufferedImage result = new BufferedImage(width, height, image.getType());

    // Apply kernel to the entire image
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        ImageProcessor.applyKernelToPixel(image, kernel, result, x, y);
      }
    }

    return result;
  }
}
