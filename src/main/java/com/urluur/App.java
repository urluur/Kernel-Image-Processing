package com.urluur;

import java.awt.*;
import javax.swing.*;
import java.util.Map;
import java.util.HashMap;
import java.awt.image.Kernel;

public class App {

    private static final Kernel KERNEL_BLUR = new Kernel(5, 5, new float[] {
            1f / 25f, 1f / 25f, 1f / 25f, 1f / 25f, 1f / 25f,
            1f / 25f, 1f / 25f, 1f / 25f, 1f / 25f, 1f / 25f,
            1f / 25f, 1f / 25f, 1f / 25f, 1f / 25f, 1f / 25f,
            1f / 25f, 1f / 25f, 1f / 25f, 1f / 25f, 1f / 25f,
            1f / 25f, 1f / 25f, 1f / 25f, 1f / 25f, 1f / 25f,
    });

    private static final Kernel KERNEL_EDGE_DETECTION = new Kernel(3, 3, new float[] {
            -1, -1, -1,
            -1, 8, -1,
            -1, -1, -1
    });

    private static final Kernel KERNEL_EDGE_DETECTION_2 = new Kernel(3, 3, new float[] {
            -1, 0, 1,
            -2, 0, 2,
            -1, 0, 1
    });

    private static final Kernel KERNEL_SHARPEN = new Kernel(3, 3, new float[] {
            0, -1, 0,
            -1, 5, -1,
            0, -1, 0
    });

    private static final Kernel KERNEL_LAPLACIAN = new Kernel(3, 3, new float[] {
            0, 1, 0,
            1, -4, 1,
            0, 1, 0
    });

    private static final Kernel KERNEL_EMBOSS = new Kernel(3, 3, new float[] {
            -2, -1, 0,
            -1, 1, 1,
            0, 1, 2
    });

    private static final Map<String, Kernel> KERNELS = new HashMap<String, Kernel>() {
        {
            put("Blur", KERNEL_BLUR);
            put("Edge Detection", KERNEL_EDGE_DETECTION);
            put("Sharpen", KERNEL_SHARPEN);
            put("Edge Detection 2", KERNEL_EDGE_DETECTION_2);
            put("Laplacian", KERNEL_LAPLACIAN);
            put("Emboss", KERNEL_EMBOSS);
        }
    };

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Kernel image processor");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            GUI gui = new GUI(frame, KERNELS);
            gui.setupUI();

            frame.setPreferredSize(new Dimension(1200, 600));
            frame.pack();
            frame.setVisible(true);
        });
    }
}