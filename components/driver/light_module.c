#include <linux/init.h>           // Macros used to mark up functions e.g. __init __exit
#include <linux/module.h>         // Core header for loading LKMs into the kernel
#include <linux/device.h>         // Header to support the kernel Driver Model
#include <linux/kernel.h>         // Contains types, macros, functions for the kernel
#include <linux/fs.h>             // Header for the Linux file system support
#include <linux/uaccess.h>          // Required for the copy to user function
#include <linux/string.h>
#include <linux/random.h>

#define  DEVICE_NAME "ldrchar"    
#define  CLASS_NAME  "ldr"        // light driver (ldr)

MODULE_LICENSE("GPL");
MODULE_AUTHOR("Zsofi Adam");
MODULE_DESCRIPTION("Light and light sensor driver of the smart pot project");
MODULE_VERSION("0.01");

static int    majorNumber;                  ///< Stores the device number -- determined automatically
static struct class*  ldrcharClass  = NULL; ///< The device-driver class struct pointer
static struct device* ldrcharDevice = NULL; ///< The device-driver device struct pointer

// The prototype functions for the character driver -- must come before the struct definition
static int     dev_open(struct inode *, struct file *);
static int     dev_release(struct inode *, struct file *);
static ssize_t dev_read(struct file *, char *, size_t, loff_t *);
static ssize_t dev_write(struct file *, const char *, size_t, loff_t *);
 
/** @brief Devices are represented as file structure in the kernel. The file_operations structure from
 *  /linux/fs.h lists the callback functions that you wish to associated with your file operations
 *  using a C99 syntax structure. char devices usually implement open, read, write and release calls
 */
static struct file_operations fops =
{
   .open = dev_open,
   .read = dev_read,
   .write = dev_write,
   .release = dev_release,
};

static int __init lkm_example_init(void) // This will run when loaded
{
    printk(KERN_INFO "Initializing light driver..."); 

    // Try to dynamically allocate a major number for the device -- more difficult but worth it
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
    return 0;
}

static void __exit lkm_example_exit(void)// This will run when unloaded
{
    printk(KERN_INFO "Exiting driver...");
    device_destroy(ldrcharClass, MKDEV(majorNumber, 0));     // remove the device
    class_unregister(ldrcharClass);                          // unregister the device class
    class_destroy(ldrcharClass);                             // remove the device class
    unregister_chrdev(majorNumber, DEVICE_NAME);             // unregister the major number
    printk(KERN_INFO "HW Deinit: Lights off"); 
}

///// GPIO functionality

// the mock is a random int, but I'm not sure what type the GPIO value will be, so:
// return type might change in the future!
static int sensor_current_value(void) {
    int value; 
    get_random_bytes(&value, sizeof(value));
    return value%1024;
}

// turn light off
static void light_turn_off(void) {
    printk(KERN_INFO "ldrChar: Lights off");
}

// turn light on
static void light_turn_on(void) {
    printk(KERN_INFO "ldrChar: Lights on");
}

/////

static int dev_open(struct inode *inodep, struct file *filep){
   printk(KERN_INFO "ldrChar: Device successfully opened\n");
   return 0;
}

// Gives back sensor value, if read
static ssize_t dev_read(struct file *filep, char *buffer, size_t len, loff_t *offset){
    int error_count = 0;

    int value = sensor_current_value();
    printk(KERN_INFO "ldrChar: Sensor value: %d\n", value);

    char message[5];
    snprintf(message, 5, "%d", value);
    int size_of_message = sizeof(message);

    error_count = copy_to_user(buffer, message, size_of_message);

    if (error_count==0){            // if true then have success
        printk(KERN_INFO "EBBChar: Sent %d characters to the user\n", size_of_message);
        return (size_of_message=0);  // clear the position to the start and return 0
    }
    else {
        printk(KERN_INFO "EBBChar: Failed to send %d characters to the user\n", error_count);
        return -EFAULT;              // Failed -- return a bad address message (i.e. -14)
    }
}

// Receives a command to turn the light off (0) or on (1)
static ssize_t dev_write(struct file *filep, const char *buffer, size_t len, loff_t *offset){
    char message[len];
    snprintf(message, len, "%s", buffer);
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

module_init(lkm_example_init);
module_exit(lkm_example_exit);