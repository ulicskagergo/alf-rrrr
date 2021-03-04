## Description
As the GPIO and Hardware part of the light device driver is still under progress, a "mock" driver has been put together, so the Spring server can be developed in the meantime.

## Functionality
The mock driver is a character device driver kernel module (named ldrChar).
- It gives back a random number in a 0-1024 range (this might change later!), as to emulate reading the current sensor value.
- If a 0 or 1 is written in it, it logs "Light on" and "Light off" to the KERNEL_INFO level, as to emulate turning the LED light on and off. It logs "invalid command" to everything else.

## Usage
The kernel module is meant to be used on a raspberry pi (although it would probably work on any architecture, but make sure to change the ARCH in the Makefile in this case).
1. Copy the content of the directory `components/driver/` to the Raspberry Pi
2. `make` 
3. There is a simple C program beside the module source, with which the module can be easily tested, compile this (something like `gcc test_ldr.c -o ldr_test`)
4. Load the module: `sudo insmod light_module.ko` (Other useful commands: `lsmod`, `modinfo light_module.ko`, `sudo rmmod light_module.ko`)
5. If you want to test it, run the test program and check out the log with `tail -n 50 -f kern.log` afterwards

## More info
http://derekmolloy.ie/writing-a-linux-kernel-module-part-1-introduction/
http://derekmolloy.ie/writing-a-linux-kernel-module-part-2-a-character-device/