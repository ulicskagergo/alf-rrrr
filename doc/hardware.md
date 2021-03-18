## Description
## Sensor
As the raspberry pi has no analog pins, only digital GPIO, we use a "trick" to measure the values of the photoresistor. There is a capacitor in paralell with the photoresistor. The pin used for measurements is initially set up as an output pin (turned on) - this charges the capacitor. When we take the measurement, we set this pin to be an input pin, which causes the capacitor to discharge in a few microseconds - the exact amount changes based on the resistance of the photoresistor. 

Using this we'll measure the time until the input read on the now input pin changes to 0. We do this by requesting a falling edge interrupt on this pin and saving the time when the interrupt handler was called. Then we can just subtract the start time of the measurement from this.

The values measured are nanoseconds, but the kernel module outputs microseconds. The outputted values are usually between 1-10 000 ms.

## Lights
We use a few LED strips and a 12V adapter, combined with an n-channel MOSFET, which is connected to the GPIO output pin used to turn the lights on and off.
