package com.urluur;

import mpi.*;
import java.io.IOException;
import java.awt.image.Kernel;
import java.awt.image.BufferedImage;

public class Distributed {

  private static int rank;
  private static int size;

  public static BufferedImage masterDistributed(BufferedImage image, Kernel kernel) {
    // method for the master computer
    Distributed.rank = App.getRank();
    Distributed.size = App.getSize();
    // Convert image to byte array
    byte[] imageBytes = null;
    try {
      imageBytes = ImageUtils.bufferedImageToByteArray(image, "png");
    } catch (IOException e) {
      e.printStackTrace();
      MPI.Finalize();
      return null;
    }

    // Convert Kernel to SerializableKernel
    SerializableKernel serializableKernel = new SerializableKernel(kernel);

    // Send the image and kernel to each worker
    for (int i = 1; i < size; i++) {
      Object[] buffer = new Object[] { imageBytes, serializableKernel };
      MPI.COMM_WORLD.Send(buffer, 0, 2, MPI.OBJECT, i, 0);
    }

    return applyKernelDistributed(image, kernel);
  }

  public static void workerDistributed(String[] args) {
    // main method for the computers who are not masters

    Distributed.rank = App.getRank();
    Distributed.size = App.getSize();

    // receive the image and kernel from the master
    Object[] buffer = new Object[2];
    MPI.COMM_WORLD.Recv(buffer, 0, 2, MPI.OBJECT, 0, 0);
    byte[] imageBytes = (byte[]) buffer[0];
    BufferedImage image = null;
    try {
      image = ImageUtils.byteArrayToBufferedImage(imageBytes);
    } catch (IOException e) {
      e.printStackTrace();
      MPI.Finalize();
      return;
    }
    SerializableKernel serializableKernel = (SerializableKernel) buffer[1];
    Kernel kernel = serializableKernel.toKernel();

    // Apply kernel to the image
    long startTime = System.currentTimeMillis();
    applyKernelDistributed(image, kernel);
    long endTime = System.currentTimeMillis();
    System.out.println("Rank " + MPI.COMM_WORLD.Rank() + " done in " + (endTime - startTime) + "ms");

    // MPI.Finalize();
  }

  public static BufferedImage applyKernelDistributed(BufferedImage image, Kernel kernel) {

    int width = image.getWidth();
    int height = image.getHeight();
    BufferedImage result = new BufferedImage(width, height, image.getType());

    // Divide image into chunks
    int chunkSize = height / size;
    int startY = rank * chunkSize;
    int endY = (rank == size - 1) ? height : startY + chunkSize;

    // Apply kernel to the chunk
    for (int y = startY; y < endY; y++) {
      for (int x = 0; x < width; x++) {
        ImageProcessor.applyKernelToPixel(image, kernel, result, x, y);
      }
    }

    if (rank == 0) { // get all results from workers
      for (int i = 1; i < size; i++) {
        // Receive result from worker i
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

        // Combine all result into final
        for (int y = i * chunkSize; y < (i == size - 1 ? height : (i + 1) * chunkSize); y++) {
          for (int x = 0; x < width; x++) {
            result.setRGB(x, y, workerResult.getRGB(x, y));
          }
        }
      }
    } else { // we are worker
      // Send the result to master
      byte[] resultBytes = null;
      try {
        resultBytes = ImageUtils.bufferedImageToByteArray(result, "png");
      } catch (IOException e) {
        e.printStackTrace();
        MPI.Finalize();
        return null;
      }
      Object[] buffer = new Object[] { resultBytes };
      MPI.COMM_WORLD.Send(buffer, 0, 1, MPI.OBJECT, 0, 0);
    }

    return result; // return final result
  }
}