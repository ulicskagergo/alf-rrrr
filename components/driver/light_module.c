#include <linux/init.h>           // Macros used to mark up functions e.g. __init __exit
#include <linux/module.h>         // Core header for loading LKMs into the kernel
#include <linux/device.h>         // Header to support the kernel Driver Model
#include <linux/kernel.h>         // Contains types, macros, functions for the kernel
#include <linux/fs.h>             // Header for the Linux file system support
#include <linux/uaccess.h>        // Required for the copy to user function
#include <linux/string.h>
#include <linux/random.h>
#include <linux/gpio.h>
#include <linux/ktime.h>
#include <linux/param.h>
#include <linux/delay.h>
#include <linux/interrupt.h>

#define  DEVICE_NAME "ldrchar"    
#define  CLASS_NAME  "ldr"        // light driver (ldr)

MODULE_LICENSE("GPL");
MODULE_AUTHOR("Zsofi Adam");
MODULE_DESCRIPTION("Light and light sensor driver of the smart plant light project");
MODULE_VERSION("0.01");

// light gpio
static unsigned int gpioLights = 6;

// sensor gpio
static unsigned int gpioSensor = 26;
static long start, end;         // jiffie values, used when measuring the elapsed time (to get the sensor value)
static unsigned int measurement_ended;  // flag, used when measuring the sensor time
static unsigned int irqNumber;          ///< Used to share the IRQ number within this file
static irqreturn_t sensorgpio_irq_handler(unsigned int irq, void *dev_id, struct pt_regs *regs);

static int    majorNumber;                  // Stores the device number -- determined automatically
static struct class*  ldrcharClass  = NULL; // The device-driver class struct pointer
static struct device* ldrcharDevice = NULL; // The device-driver device struct pointer

// The prototype functions for the character driver -- must come before the struct definition
static int     dev_open(struct inode *, struct file *);
static int     dev_release(struct inode *, struct file *);
static ssize_t dev_read(struct file *, char *, size_t, loff_t *);
static ssize_t dev_write(struct file *, const char *, size_t, loff_t *);
 
/** @brief Devices are represented as file structure in the kernel. The file_operations structure from
 *  /linux/fs.h lists the callback functions that you wish to associated with your file operations
 *  using a C99 syntax structure. char devices usually implement open, read, write and release calls
 */

// set the permissions of the character device
static int permissions_dev_uevent(struct device *dev, struct kobj_uevent_env *env)
{
    add_uevent_var(env, "DEVMODE=%#o", 0666);
    return 0;
}

static struct file_operations fops =
{
   .open = dev_open,
   .read = dev_read,
   .write = dev_write,
   .release = dev_release,
   .owner = THIS_MODULE,
};

static int __init ldr_init(void) // This will run when loaded
{
    printk(KERN_INFO "Initializing light driver..."); 

    // Try to dynamically allocate a major number for the device
    majorNumber = register_chrdev(0, DEVICE_NAME, &fops);
    if (majorNumber<0){
        printk(KERN_ALERT "ldrChar failed to register a major number\n");
        return majorNumber;
    }
    printk(KERN_INFO "ldrChar: registered correctly with major number %d\n", majorNumber);

    // Register the device class
    ldrcharClass = class_create(THIS_MODULE, CLASS_NAME);
    if (IS_ERR(ldrcharClass)){                // Check for error and clean up if there is
        unregister_chrdev(majorNumber, DEVICE_NAME);
        printk(KERN_ALERT "Failed to register device class\n");
        return PTR_ERR(ldrcharClass);          // Correct way to return an error on a pointer
    }
    ldrcharClass->dev_uevent = permissions_dev_uevent;
    printk(KERN_INFO "ldrChar: device class registered correctly\n");

    // Register the device driver
    ldrcharDevice = device_create(ldrcharClass, NULL, MKDEV(majorNumber, 0), NULL, DEVICE_NAME);
    if (IS_ERR(ldrcharDevice)){               // Clean up if there is an error
        class_destroy(ldrcharClass);           // Repeated code but the alternative is goto statements
        unregister_chrdev(majorNumber, DEVICE_NAME);
        printk(KERN_ALERT "Failed to create the device\n");
        return PTR_ERR(ldrcharDevice);
    }
    printk(KERN_INFO "ldrChar: device class created correctly\n"); // Made it! device was initialized
    printk(KERN_INFO "HW Initialization: Lights off");

    if (!gpio_is_valid(gpioLights)){
        printk(KERN_INFO "GPIO_LIGHTS: invalid Light GPIO\n");
        return -ENODEV;
    }
    gpio_request(gpioLights, "sysfs");          // request gpio of lights
    gpio_direction_output(gpioLights, false);   // Set the gpio to be in output mode and off
    gpio_export(gpioLights, false);            // Causes the gpio number of the lights to appear in /sys/class/gpio
			                                // the bool argument prevents the direction from being changed

    measurement_ended = 1;                  // no measurement in progress

    if (!gpio_is_valid(gpioSensor)){
        printk(KERN_INFO "GPIO_SENSOR: invalid Sensor GPIO\n");
        return -ENODEV;
    }
    gpio_request(gpioSensor, "sysfs");          // request gpio of sensor
    gpio_direction_output(gpioSensor, true);   // Set the gpio to be in output mode and on
    gpio_export(gpioSensor, false);

    // GPIO numbers and IRQ numbers are not the same! This function performs the mapping
    irqNumber = gpio_to_irq(gpioSensor);
    printk(KERN_INFO "GPIO_SENSOR: The sensor input is mapped to IRQ: %d\n", irqNumber);

    int result = 0;
    result = request_irq(irqNumber,             // The interrupt number requested
                    (irq_handler_t) sensorgpio_irq_handler, // The pointer to the handler function below
                    IRQF_TRIGGER_FALLING,   // Interrupt on rising edge (button press, not release)
                    "ldr_sensor_gpio_handler",    // Used in /proc/interrupts to identify the owner
                    NULL);                 // The *dev_id for shared interrupt lines, NULL is okay
    return 0;
}

static void __exit ldr_exit(void)// This will run when unloaded
{
    printk(KERN_INFO "Exiting driver...");
    printk(KERN_INFO "HW Deinit: Lights off"); 
    gpio_set_value(gpioLights, false);              // Turn the LED off
    gpio_unexport(gpioLights);                  // Unexport the LED GPIO

    free_irq(irqNumber, NULL);               // Free the IRQ number, no *dev_id required in this case
    device_destroy(ldrcharClass, MKDEV(majorNumber, 0));     // remove the device
    class_unregister(ldrcharClass);                          // unregister the device class
    class_destroy(ldrcharClass);                             // remove the device class
    unregister_chrdev(majorNumber, DEVICE_NAME);             // unregister the major number
}

///// GPIO functionality

static struct timespec64 time;

// the mock is a random int, but I'm not sure what type the GPIO value will be, so:
// return type might change in the future!
static long sensor_current_value(void) {
    measurement_ended = 0;            // starting measurement
    gpio_direction_input(gpioSensor); // Set the sensor GPIO to be an input (so the condensator discharges)
    ktime_get_real_ts64(&time);
    start = time.tv_nsec;

    int successful = 1;
    long current_jiffy, time_passed;
    while(!measurement_ended) {
        ktime_get_real_ts64(&time);
        current_jiffy = time.tv_nsec;
        time_passed = (current_jiffy - start)/1000; // time passed in microsec - "watchdog"
        if(time_passed>=1000000) { successful = 0; break; } // timed out - measurement should only take around 10-1000ms long
        msleep(10);
    } // waiting for interrupt to set the end time
    gpio_direction_output(gpioSensor, true);   // Set the gpio to be in output mode and on (condensator is charged again)

    if(successful) {
        long diff = end - start;
        long result = diff / 1000; // convert it to microsec 
        printk(KERN_INFO "GPIO_SENSOR: Measurement is %lu microsec\n", result);
        return result;
    } else {
        printk(KERN_INFO "GPIO_SENSOR: Measurement timed out\n");
        return -1; // error value
    }
    // for testing without GPIO usage:
    // int value; 
    // get_random_bytes(&value, sizeof(value));
    // return value%1024;
}

static irqreturn_t sensorgpio_irq_handler(unsigned int irq, void *dev_id, struct pt_regs *regs) {
    ktime_get_real_ts64(&time);
    end = time.tv_nsec;
    printk(KERN_INFO "GPIO_SENSOR: Interrupt! Measurement is done\n");
    measurement_ended = 1;                   // measurement is done
    return (irqreturn_t) IRQ_HANDLED;      // Announce that the IRQ has been handled correctly
}

// turn light off
static void light_turn_off(void) {
    gpio_set_value(gpioLights, false);
    printk(KERN_INFO "ldrChar: Lights off");
}

// turn light on
static void light_turn_on(void) {
    gpio_set_value(gpioLights, true);
    printk(KERN_INFO "ldrChar: Lights on");
}

/////

static int dev_open(struct inode *inodep, struct file *filep){
   printk(KERN_INFO "ldrChar: Device successfully opened\n");
   return 0;
}

static ssize_t dev_read(struct file *filep, char *buffer, size_t len, loff_t *offset){
    int error_count = 0;

    long value = sensor_current_value();
    printk(KERN_INFO "ldrChar: Sensor value: %ld\n", value);

    size_t size_of_message = snprintf(NULL, 0, "%ld\n", value);
    char message[size_of_message];
    snprintf(message, size_of_message+1, "%ld\n", value);
    printk(KERN_INFO "ldrChar: Sensor value message: %s\n", message);
    
    if(len>=size_of_message) {
        error_count = copy_to_user(buffer, message, size_of_message);
        if (error_count==0){            // if true then have success
            printk(KERN_INFO "EBBChar: Sent %d characters to the user\n", size_of_message);
            return size_of_message;  // clear the position to the start and return 0
        }
    }
	
    printk(KERN_INFO "EBBChar: Failed to send %d characters to the user\n", error_count);
    return -EFAULT;              // Failed -- return a bad address message (i.e. -14)
}

// Receives a command to turn the light off (0) or on (1)
static ssize_t dev_write(struct file *filep, const char *buffer, size_t len, loff_t *offset){
    char message[len+1];
    snprintf(message, len+1, "%s\0", buffer);
    printk(KERN_INFO "ldrChar: Received %zu characters from the user\n", len);
    printk(KERN_INFO "ldrChar: Received message: %s\n", message);

    if (strcmp(message, "1") == 0) {
        light_turn_on();
    } else if (strcmp(message, "0") == 0) {
        light_turn_off();
    } else {
        printk(KERN_INFO "ldrChar: Invalid command received: %s\n", message);
    }

    return len;
}

static int dev_release(struct inode *inodep, struct file *filep){
   printk(KERN_INFO "ldrChar: Device successfully closed\n");
   return 0;
}

module_init(ldr_init);
module_exit(ldr_exit);
