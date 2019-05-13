#!/bin/sh

if [ $# != 3 ]; then
  echo "usage: mkdmg.sh volname vers srcdir"
  exit 0
fi

VOL="$1"
VER="$2"
FILES="$3"

DMG="tmp-$VOL.dmg"

# create temporary disk image and format, ejecting when done
hdiutil create "$DMG" -megabytes 5 -ov -type UDIF
DISK=`hdid -nomount "$DMG" | sed -ne ' /Apple_partition_scheme/ s|^/dev/\([^ ]*\).*$|\1|p'`
newfs_hfs -v "$VOL" /dev/r${DISK}s2
hdiutil eject $DISK

# mount and copy files onto volume 
hdid "$DMG"
cp -R "${FILES}"/* "/Volumes/$VOL"
hdiutil eject $DISK
#osascript -e "tell application \"Finder\" to eject disk \"$VOL\"" && \

# convert to compressed image, delete temp image
rm -f "${VOL}-${VER}.dmg"
hdiutil convert "$DMG" -format UDZO -o "${VOL}-${VER}.dmg"
rm -f "$DMG"

