#!/bin/sh
 
PACKAGE_NAME="plantlightserver"
PACKAGE_VERSION="1.0.0"
SOURCE_DIR=$PWD
TEMP_DIR="/tmp"
 
mkdir -p $TEMP_DIR/debian/DEBIAN
mkdir -p $TEMP_DIR/debian/lib
mkdir -p $TEMP_DIR/debian/usr/bin
mkdir -p $TEMP_DIR/debian/usr/share/applications
mkdir -p $TEMP_DIR/debian/etc/modules-load.d/
mkdir -p $TEMP_DIR/debian/usr/share/$PACKAGE_NAME
mkdir -p $TEMP_DIR/debian/usr/lib/$PACKAGE_NAME
mkdir -p $TEMP_DIR/debian/usr/share/doc/$PACKAGE_NAME
mkdir -p $TEMP_DIR/debian/usr/share/common-licenses/$PACKAGE_NAME
mkdir -p $TEMP_DIR/debian/lib/modules/5.10.17-v7l+/kernel/extra/

echo "Package: $PACKAGE_NAME" > $TEMP_DIR/debian/DEBIAN/control
echo "Version: $PACKAGE_VERSION" >> $TEMP_DIR/debian/DEBIAN/control
cat control >> $TEMP_DIR/debian/DEBIAN/control
 
cp *.desktop $TEMP_DIR/debian/usr/share/applications/
cp copyright $TEMP_DIR/debian/usr/share/common-licenses/$PACKAGE_NAME/ # results in no copyright warning
cp copyright $TEMP_DIR/debian/usr/share/doc/$PACKAGE_NAME/ # results in obsolete location warning
 
cp *.jar $TEMP_DIR/debian/usr/share/$PACKAGE_NAME/
cp *.ko $TEMP_DIR/debian/lib/modules/5.10.17-v7l+/kernel/extra/
cp *.conf $TEMP_DIR/debian/etc/modules-load.d/
cp postinst $TEMP_DIR/debian/DEBIAN/
cp $PACKAGE_NAME $TEMP_DIR/debian/usr/bin/
 
echo "$PACKAGE_NAME ($PACKAGE_VERSION) trusty; urgency=low" > changelog
echo "  * Rebuild" >> changelog
echo " -- Zsofia Adam <adamzsofi@edu.bme.hu>  `date -R`" >> changelog
gzip -9c changelog > $TEMP_DIR/debian/usr/share/doc/$PACKAGE_NAME/changelog.gz
 
PACKAGE_SIZE=`du -bs $TEMP_DIR/debian | cut -f 1`
PACKAGE_SIZE=$((PACKAGE_SIZE/1024))
echo "Installed-Size: $PACKAGE_SIZE" >> $TEMP_DIR/debian/DEBIAN/control
 
chown -R root $TEMP_DIR/debian/
chgrp -R root $TEMP_DIR/debian/
 
cd $TEMP_DIR/
dpkg --build debian
mv debian.deb $SOURCE_DIR/$PACKAGE_NAME-$PACKAGE_VERSION.deb
rm -r $TEMP_DIR/debian 
