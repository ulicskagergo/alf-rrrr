## Description
The "ldr" or light driver kernel module is a Linux kernel module for Raspbian (Raspberry Pi 4 B), using 2 GPIO pins, an interrupt and a character device.
It's purpose is to be able to measure environmental light and to be able to turn the plant light on and off.

## Functionality
The ldr ([light_module.c](../components/driver/light_module.c)) is a character device driver kernel module (named ldrChar).
- When read, it takes a measurement on it's sensor and outputs this value in a form of a long value. These values are usually around 1 - 10 000, for more check out [hardware.md](./hardware.md)
- When written, it takes 0 and 1 as valid commands and turns the plant lights off/on accordingly.

Beside the module there is a [mock module](../components/driver/mock_light_module.c), which can be used to test the character device without GPIO usage. It works the following way:
- It gives back a random number in a 0-1024 range (this might change later!), as to emulate reading the current sensor value.
- If a 0 or 1 is written in it, it logs "Light on" and "Light off" to the KERNEL_INFO level, as to emulate turning the LED light on and off. It logs "invalid command" to everything else.

## Usage
The kernel module is meant to be used on a raspberry pi (although it would probably work on any architecture, but make sure to change the ARCH in the Makefile in this case and adjust the GPIO parts in the code).
1. Copy the content of the directory `components/driver/` to the Raspberry Pi
2. `make` 
3. There is a simple C program beside the module source, with which the module can be easily tested. Compile this test program (something like `gcc test_ldr.c -o ldr_test`)
4. Load the module: `sudo insmod light_module.ko` (Other useful commands: `lsmod`, `modinfo light_module.ko`, `sudo rmmod light_module.ko`)
    *You might want to give additional group rights with chmod to /dev/ldrchar, e.g. something like `chmod 777 /dev/ldrchar`*
5. If you want to test it, run the test program and check out the log with `tail -n 50 -f kern.log` afterwards

## More info
http://derekmolloy.ie/writing-a-linux-kernel-module-part-1-introduction/
http://derekmolloy.ie/writing-a-linux-kernel-module-part-2-a-character-device/
