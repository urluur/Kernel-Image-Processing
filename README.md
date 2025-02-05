# Kernel Image Processing

<img alt="Static Badge" src="https://img.shields.io/badge/Graphics-5a3aa1"> <img alt="Static Badge" src="https://img.shields.io/badge/Computing-0c4866">

This project was created as part of the [Programming III](https://prog3.student.famnit.upr.si/) course at [UP FAMNIT](https://www.famnit.upr.si/en/).

It is a simple image processing program that uses a kernel to process the image. It is written in Java and uses [Swing](https://docs.oracle.com/javase/tutorial/uiswing/) for the GUI.

![screenshot_cli](https://github.com/urluur/Kernel-Image-Processing/blob/main/screenshots/screenshot.jpg?raw=true)

## Guidelines 

[Link](https://prog3.student.famnit.upr.si/#projects/KernelImageProcessing/) to guidelines posted by Famnit.

## What is it doing?

The program is using a kernel, which is a small matrix that is applied to every pixel of the image. It calculates the new value of the pixel by multiplying the kernel with the surrounding pixels and summing them up.

![image](https://upload.wikimedia.org/wikipedia/commons/1/19/2D_Convolution_Animation.gif)

Photo by <a href="https://commons.wikimedia.org/wiki/File:2D_Convolution_Animation.gif">Michael Plotke</a>, <a href="https://creativecommons.org/licenses/by-sa/3.0">CC BY-SA 3.0</a>, via Wikimedia Commons

This process can take a lot of time if done sequentially, so we can use parallel processing to speed it up. The program measures the time it takes to process the image both sequentially and in parallel.

## Usage

- **Drag and drop** an image to the left panel or choose a sample image from the `Demo images` dropdown menu
- Choose the **Kernel** from the dropdown menu
- Choose `Sequential`, `Parallel` or `Distributed` button to start the processing of the image
- Processed image will be visible on the right panel
- The time it took to process the image will be displayed in the according label
- You can **Save** the processed image by clicking the `Save` button on the top right corner
- Click the `Reset` button or drag and drop a new image if you want to process another image

## Distributed computing setup (macOS/Linux)

>[!important]
>Code on this branch only works if MPI is set up in your environment.
>Simplified non-distributed version is available on [no-mpi](https://github.com/urluur/Kernel-Image-Processing/tree/no-mpi) branch.

1. Download MPJ Express v0.44 from [SourceForge](https://sourceforge.net/projects/mpjexpress/files/releases/mpj-v0_44.zip/download)
2. Move to the project repository: `cd Kernel-Image-Processing/`
2. Create libs folder: `mkdir libs`
3. Move extracted folder to the libs folder
4. Install MPJ Express as dependancy:
    ```sh
    mvn install:install-file -Dfile=libs/mpj-v0_44/lib/mpj.jar -DgroupId=com.googlecode.mpj-express -DartifactId=mpj-v0_44 -Dversion=0.44 -Dpackaging=jar
    ```
5. Add MPJ to path:
    ```sh
    export MPJ_HOME=libs/mpj-v0_44
    export PATH=$MPJ_HOME/bin:$PATH
    ```
6. Compile the project:
    ```sh
    mvn clean install
    ```
7. Run the project:
    ```sh
    libs/mpj-v0_44/bin/mpjrun.sh -np 4 target/kernel-image-processing-1.0-SNAPSHOT.jar
    ```
