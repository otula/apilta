#!/bin/bash

SERVER=8.8.8.8

while :
do
	echo "sleeping for 3 seconds..."
	sleep 180
	ping -c2 ${SERVER} &> /dev/null
	if [ $? == 0 ]
	then
		echo "executing raspbe..."
		/usr/bin/java -jar /home/pi/raspbe.jar -c /home/pi/backend.properties -s &> /home/pi/java.log
	else
		/sbin/ifup --force wlan0 &> /home/pi/wifi.log
	fi
done

