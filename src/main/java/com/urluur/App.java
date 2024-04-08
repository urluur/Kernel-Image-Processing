package com.urluur;

import mpi.*;
import java.awt.*;
import javax.swing.*;
import java.util.Map;
import java.util.HashMap;
import java.awt.image.Kernel;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

public class App {

  private static int rank;
  private static int size;

  public static int getRank() {
    return rank;
  }

  public static int getSize() {
    return size;
  }

  // kernels source: https://setosa.io/ev/image-kernels/
  private static final Map<String, Kernel> KERNELS = new HashMap<String, Kernel>() {
    {
      put("Blur",
          new Kernel(5, 5,
              new float[] { 1f / 25f, 1f / 25f, 1f / 25f, 1f / 25f, 1f / 25f, 1f / 25f, 1f / 25f, 1f / 25f, 1f / 25f,
                  1f / 25f, 1f / 25f, 1f / 25f, 1f / 25f, 1f / 25f, 1f / 25f, 1f / 25f, 1f / 25f, 1f / 25f, 1f / 25f,
                  1f / 25f, 1f / 25f, 1f / 25f, 1f / 25f, 1f / 25f, 1f / 25f }));
      put("Edge Detection", new Kernel(3, 3, new float[] { -1, -1, -1, -1, 8, -1, -1, -1, -1 }));
      put("Edge Detection 2", new Kernel(3, 3, new float[] { -1, 0, 1, -2, 0, 2, -1, 0, 1 }));
      put("Sharpen", new Kernel(3, 3, new float[] { 0, -1, 0, -1, 5, -1, 0, -1, 0 }));
      put("Laplacian", new Kernel(3, 3, new float[] { 0, 1, 0, 1, -4, 1, 0, 1, 0 }));
      put("Emboss", new Kernel(3, 3, new float[] { -2, -1, 0, -1, 1, 1, 0, 1, 2 }));
    }
  };

  public static void main(String[] args) {
    MPI.Init(args);
    rank = MPI.COMM_WORLD.Rank();
    size = MPI.COMM_WORLD.Size();

    if (rank == 0) {
      System.out.println("Master started");
      SwingUtilities.invokeLater(() -> {
        JFrame frame = new JFrame("Kernel image processor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GUI gui = new GUI(frame, KERNELS);
        gui.setupUI();

        frame.setPreferredSize(new Dimension(1200, 600));
        frame.pack();
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent e) {
            MPI.Finalize();
          }
        });
      });
    } else {
      System.out.println("Worker with rank " + rank + " started");
      while (true) {
        Distributed.workerDistributed(args);
      }
    }

  }
}