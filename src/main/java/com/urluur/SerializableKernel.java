package com.urluur;

import java.io.Serializable;
import java.awt.image.Kernel;

public class SerializableKernel implements Serializable {
    private float[] kernelData;
    private int width;
    private int height;

    /**
     * Create a serializable kernel from a kernel.
     * It will be used to send the kernel to workers.
     * 
     * @param kernel The kernel to serialize
     */
    public SerializableKernel(Kernel kernel) {
        this.kernelData = kernel.getKernelData(null);
        this.width = kernel.getWidth();
        this.height = kernel.getHeight();
    }

    /**
     * Convert the serializable kernel back to a kernel.
     * 
     * @return The kernel object
     */
    public Kernel toKernel() {
        return new Kernel(width, height, kernelData);
    }
}