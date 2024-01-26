# Kernel Image Processing

<img alt="Static Badge" src="https://img.shields.io/badge/Graphics-5a3aa1">
<img alt="Static Badge" src="https://img.shields.io/badge/Computing-0c4866">

This project is a part of the course [Programming III](https://prog3.student.famnit.upr.si/) at [UP FAMNIT](https://www.famnit.upr.si/en/).

It is a simple image processing program that uses a kernel to process the image. The program is written in Java and uses [Swing](https://docs.oracle.com/javase/tutorial/uiswing/) for the GUI.

![screenshot_cli](https://github.com/urluur/Kernel-Image-Processing/blob/main/img/screenshots/screenshot.jpg?raw=true)

## Guidelines 

[Link](https://prog3.student.famnit.upr.si/#projects/KernelImageProcessing/) to guidelines posted by Famnit.

## What is it doing?

The program is using a kernel, which is a small matrix that is applied to every pixel of the image. It calculates the new value of the pixel by multiplying the kernel with the surrounding pixels and summing them up.

![image](https://upload.wikimedia.org/wikipedia/commons/1/19/2D_Convolution_Animation.gif)

By [Michael Plotke](https://commons.wikimedia.org/w/index.php?curid=24288958) - Own work, CC BY-SA 3.0

This process can take a lot of time if doen sequentially, so we can use parallel processing to speed it up. The program measures the time it takes to process the image both sequentially and in parallel.

## Usage

- **Drag and drop** an image to the left panel or choose a sample image from the dropdown menu
- Choose `Sequential` or `Parallel` button to start the processing of the image
- The result will be visible on the right panel
- The time it took to process the image will be displayed in the according label

### To-do:

1. Running the program:
  - [x] Sequential
  - [x] Parallel
  - [ ] Distributed
  - [x] User can specify the input image.
  - [x] The program measures run-time needed to complete.

2. Problem specific implementation requirements
  - [x] There should be a default kernel supplied
  - [ ] The implementation must adapt automatically to the hardware it is being ran on (Physical CPU's, Cores, Memory, etc..);
  - [x] The project must include a few sample images of different sizes.
  - [ ] The user should be able to specify the kernel and its size
