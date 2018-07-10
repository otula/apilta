#!/bin/bash
# start the wifi script and the python gps script if they are not already running
#
# add to crontab, e.g. 0,15,30,45 * * * * /home/pi/start.sh or */15 * * * * /home/pi/start.sh to execute every 15 minutes
#

pgrep -x "wifi.sh" > /dev/null
if [ $? != 0 ]
then
    echo "Starting wifi script..."
    /home/pi/wifi.sh >> /home/pi/wifi.log &
else
    echo "Wifi script seems to be running..."
fi

pgrep -f "/usr/bin/python3 /home/pi/camera_new.py" > /dev/null
if [ $? != 0 ]
then
    echo "Starting camera script..."
    /usr/bin/python3 /home/pi/camera.py >> /home/pi/java.txt &
else
    echo "Camera script seems to be running..."
fi
