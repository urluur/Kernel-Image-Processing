# Kernel Image Processing

<img alt="Static Badge" src="https://img.shields.io/badge/Graphics-5a3aa1"> <img alt="Static Badge" src="https://img.shields.io/badge/Computing-0c4866">

This project is a part of the course [Programming III](https://prog3.student.famnit.upr.si/) at [UP FAMNIT](https://www.famnit.upr.si/en/).

It is a simple image processing program that uses a kernel to process the image. It is written in Java and uses [Swing](https://docs.oracle.com/javase/tutorial/uiswing/) for the GUI.

![screenshot_cli](https://github.com/urluur/Kernel-Image-Processing/blob/main/screenshots/screenshot.jpg?raw=true)

## Guidelines 

[Link](https://prog3.student.famnit.upr.si/#projects/KernelImageProcessing/) to guidelines posted by Famnit.

## What is it doing?

The program is using a kernel, which is a small matrix that is applied to every pixel of the image. It calculates the new value of the pixel by multiplying the kernel with the surrounding pixels and summing them up.

![image](https://upload.wikimedia.org/wikipedia/commons/1/19/2D_Convolution_Animation.gif)

Photo by [Michael Plotke](https://commons.wikimedia.org/w/index.php?curid=24288958) - Own work, CC BY-SA 3.0

This process can take a lot of time if done sequentially, so we can use parallel processing to speed it up. The program measures the time it takes to process the image both sequentially and in parallel.

## Usage

- **Drag and drop** an image to the left panel or choose a sample image from the `Demo images` dropdown menu
- Choose the **Kernel** from the dropdown menu
- Choose `Sequential` or `Parallel` button to start the processing of the image
- Processed image will be visible on the right panel
- The time it took to process the image will be displayed in the according label
- You can **Save** the processed image by clicking the `Save` button on the top right corner
- Click the `Reset` button or drag and drop a new image if you want to process another image

## Distributed computing setup

1. Download MPJ Express v0.44 from [SourceForge](https://sourceforge.net/projects/mpjexpress/files/releases/mpj-v0_44.zip/download)
2. Move to the project repository: `cd Kernel-Image-Processing/`
2. Create libs folder: `mkdir libs`
3. Move extracted folder to the libs folder
4. Install MPJ Express as dependancy:
    ```sh
    mvn install:install-file -Dfile=libs/mpj-v0_44/lib/mpj.jar -DgroupId=com.googlecode.mpj-express -DartifactId=mpj-v0_44 -Dversion=0.44 -Dpackaging=jar
    ```

## To-do:

1. Running the program:
  - [x] Sequential
  - [x] Parallel
  - [ ] Distributed
  - [x] User can specify the input image.
  - [x] The program measures run-time needed to complete.

2. Problem specific implementation requirements
  - [x] There should be a default kernel supplied
  - [x] The implementation must adapt automatically to the hardware it is being ran on (Physical CPU's, Cores, Memory, etc..);
  - [x] The project must include a few sample images of different sizes.
  - [x] The user should be able to choose the kernel
  - [ ] The user can set a custom kernel with a GUI
