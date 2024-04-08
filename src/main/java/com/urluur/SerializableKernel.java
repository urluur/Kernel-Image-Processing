package com.urluur;

import java.io.Serializable;
import java.awt.image.Kernel;

public class SerializableKernel implements Serializable {
    private float[] kernelData;
    private int width;
    private int height;

    public SerializableKernel(Kernel kernel) {
        this.kernelData = kernel.getKernelData(null);
        this.width = kernel.getWidth();
        this.height = kernel.getHeight();
    }

    public Kernel toKernel() {
        return new Kernel(width, height, kernelData);
    }
}