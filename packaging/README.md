## How to create a debian package for Raspbian of the backend
*Disclaimer: a package was already created, check out our releases. Only build yourself, if there are incompatibilities with that one.*
*Note: The backend was built and tested on a Raspberry PI 4B with the Raspbian lite version released pm ?arch 4th 2021. Any changes in kernel versions or GPIO numberings need to be addressed at the appropriate parts of the project.*

## Required binaries
You need to build the spring server and the kernel module, as they will be packaged in a binary format. 
To do that, check out the components folder in the repository. 

When you are done, put the below listed 2 files in the debplantlightserver directory:
- `server-0.0.1-SNAPSHOT.jar`
- `light_module.ko`

## Packaging
Check out `packagedb.sh` if the directory names (especially the kernel directory) are right.
Run packagedb.sh, it will create the changelog and the `.deb` package

## Installing & Usage
Copy the package to your raspberry pi and install it with `apt-get`.
The postinst script will take care of the driver (the driver will start on every boot from then on).
After the installation is done, you can start the server with the command `plantlightserver`.
