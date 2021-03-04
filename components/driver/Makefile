obj-m += light_module.o
all:
	make ARCH=arm -C /lib/modules/$(shell uname -r)/build/ M=$(PWD) modules
clean:
	make ARCH=arm -C /lib/modules/$(shell uname -r)/build/ M=$(PWD) clean 
