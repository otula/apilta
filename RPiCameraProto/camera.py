#!/usr/bin/python
#
# Copyright 2017 Tampere University of Technology, Pori Department
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
import serial
import pynmea2
import sqlite3 # from https://docs.python.org/2/library/sqlite3.html and python-pysqlite2 in debian
import uuid
import subprocess
from math import radians, sin, cos, sqrt, asin
from time import sleep
import time
import datetime as datetime
from picamera import PiCamera
import picamera

# constants
CONDITION_LOCATION = "location/point" # coordinate location condition for a task
DATABASE_FILE = "/home/pi/file.db" # path to the sqlite database file
EARTH_RADIUS = 6372.8 # Earth radius in kilometers
ERROR_RETRY_MAX = 5 # maximum retries on error
ERROR_WAIT_DURATION = 2 # how long to wait between errors, in seconds
FEATURE_CAMERA = "sensor/camera"
FEATURE_LOCATION = "sensor/location" # key for data point coordinates
LOCATION_THRESHOLD = 0.025 # distance threshold for resolving matching location coordinate, in kilometers
PHOTO_DIRECTORY = "/home/pi/target/" # directory where the photos are stored
PHOTO_COUNT = 4
PHOTO_WAIT_DURATION = 6 # how long is the wait between taking photos, in seconds
TARGETS_FILE = "/home/pi/targets" # path to the file where the list of GPS targets are stored
TASK_ID = "ca24ff28-4ec6-47f5-a590-e59e9fbd13fe"

#
# Details for a single location target
#
class Target:
	latitude = 0.0
	longitude = 0.0
	location = ""
	latLonString = "" # coordinate in string format
	taskId = "" # identifier for the task
	output = [] # output as requested by the task

#
# calculates the distance between two coordinates using the great circle distance
#
# @param {float} lat1 latitude for the first coordinate
# @param {float} lon1 longitude for the first coordinate
# @param {float} lat2 latitude for the second coordinate
# @param {float} lon2 longitede for the second coordinate
# @return {float} the distance between the two points in kilometers
#
def haversine(lat1, lon1, lat2, lon2):
	dLat = radians(lat2-lat1)
	dLon = radians(lon2-lon1)
	lat1 = radians(lat1)
	lat2 = radians(lat2)

	a = sin(dLat/2)**2 + cos(lat1)*cos(lat2)*sin(dLon/2)**2
	c = 2*asin(sqrt(a))
	return EARTH_RADIUS * c

#
# Checks if the list of known targets contains coordinates near the given latitude/longitude pair
#
# If multiple coordinates are in close vicinity, the the first match in the targets list is returned
#
# @param {float} lat latitude
# @param {float} lon longitude
# @param {Array[String, String]} targets coordinate-location name mapping
# @return {Target} the matching Target object or None if nothing was found
#
def findCoordinateMatch(lat, lon, targets):
	for element in targets:
		if haversine(lat, lon, element.latitude, element.longitude) <= LOCATION_THRESHOLD:
			return element
	return None

# Insert the basic measurement details
#
# @param {cursor} cursor connection cursor used to perform the sqlite operation
# @param {string} taskId identifier for the processed task
# @return {string} identifier for the created measurement
#
def createMeasurement(cursor, taskId):
    measurementId = str(uuid.uuid4())
    cursor.execute("INSERT INTO measurements (measurement_id, task_id, sent) VALUES (?,?,0)", (measurementId, taskId))
    return measurementId

# insert file details to the database
#
# @param {cursor} cursor connection cursor used to perform the sqlite operation
# @param {string} measurementId identifier for the measurement
# @param {string} path the file path (e.g. /tmp/file.jpg)
# @param {long} timestamp unix timestamp
#
def insertFileData(cursor, measurementId, path, timestamp):
    cursor.execute("INSERT INTO measurement_files (measurement_id, path, guid, timestamp) VALUES (?,?,NULL,?)", (measurementId, path, timestamp))

# insert measurement data to the database
#
# @param {cursor} cursor connection cursor used to perform the sqlite operation
# @param {string} measurementId identifier for the measurement
# @param {string} key data type (e.g. for GPS, "sensor/location")
# @param {string} value the measurement value
# @param {long} timestamp unix timestamp
#
def insertMeasurementData(cursor, measurementId, key, value, timestamp):
    cursor.execute("INSERT INTO measurement_data (measurement_id, key, value, timestamp) VALUES (?,?,?,?)", (measurementId, key, value, timestamp))

#
# read the targets file and generate target mapping
#
# Ignored the location name in the file (the name is only printed for debuging), uses a hard-coded task identifier and output
#
# @return {Array[Target]} list of targets
#
def loadTargetsFromFile():
	targets = []
	with open(TARGETS_FILE, "r") as file:
		for line in file:
			information = line.split()
			target = Target()
			print("Loaded target: "+information[0])
			target.latLonString = information[0]
			target.location = information[1]
			information = information[0].split(",")
			target.latitude = float(information[0])
			target.longitude = float(information[1])
			target.taskId = TASK_ID
			target.output.append("sensor/camera")
			target.output.append("sensor/location")
			targets.append(target)
	file.close()
	return targets

#
# read targets from the provided sqlite database file
#
# @param {cursor} cursor connection cursor used to perform the sqlite operation
# @return {Array[Target]} list of targets
#
def loadTargetsFromDatabase(cursor):
	targets = []
	cursor.execute("SELECT task_id, value FROM task_conditions WHERE key=\""+CONDITION_LOCATION+"\"") # technically speaking, there could be other conditions, but currently only process the location, also assume there are no impossible conditions (e.g. two location/point values with AND relation => the device cannot be in two places at the same time)
	cRows = cursor.fetchall()
	for cRow in cRows:
		target = Target()
		target.taskId = cRow[0]
		target.latLonString = cRow[1]
		information = target.latLonString.split(",")
		target.latitude = float(information[0])
		target.longitude = float(information[1])
		cursor.execute("SELECT feature FROM task_outputs where task_id=?", [target.taskId])
		oRows = cursor.fetchall()
		for oRow in oRows:
			target.output.append(oRow[0])
		if FEATURE_CAMERA in target.output:
			targets.append(target)
		else:
			print("The task, id: "+target.taskId+" does not contain valid output parameters...")
	return targets

#
# start listening for GPS
#
def startListen():
	#serialStream = serial.Serial("/dev/ttyS0", 9600)
	#try:
		#connection = sqlite3.connect(DATABASE_FILE)
	try:
		connection = sqlite3.connect(DATABASE_FILE)
		camera = picamera.PiCamera()
		cursor = connection.cursor()
		targets = loadTargetsFromFile()
		if not targets:
			print("No valid targets defined, aborting...")
			return

		errorCount = 0
		while True:
			try:
				serialStream = serial.Serial("/dev/ttyS0", 9600)
				sentence = serialStream.readline().decode("ascii") # this randomly throws an exception, but as it is somewhat difficult to know what state the serial is after an error, so let it abort the application
				if sentence.find("GGA") > 0:
					data = pynmea2.parse(sentence)
					print (str(data.latitude)+","+str(data.longitude))
					errorCount = 0 # reset error count after successful read
					target = findCoordinateMatch(data.latitude, data.longitude, targets)
					if target is not None:
						print("Target found: "+target.latLonString)
						timestamp = int(time.time()*1000) # get time from system time
						measurementId = createMeasurement(cursor, target.taskId)
						for i in range(PHOTO_COUNT):
							camera.annotate_text = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
							filename = PHOTO_DIRECTORY + target.location + "_" + str(timestamp) + "_" + str(i) + ".jpg"
							camera.capture_sequence([filename])
							insertFileData(cursor, measurementId, filename, timestamp)
							time.sleep(2)
						if FEATURE_LOCATION in target.output:
							insertMeasurementData(cursor, measurementId, FEATURE_LOCATION, target.latLonString, timestamp)
						connection.commit() # commit in one-go, could also commit after each cursor.execute(...)

						sleep(PHOTO_WAIT_DURATION)
				serialStream.close() # for some reason the serial stream has issues if left open, so close it here even if it is somewhat illogal

			except KeyboardInterrupt:
				raise # do not catch keyboard interrupt
			except Exception as ex: # catch everything else, this is not really the proper way of handling exceptions, but as we have no clue what errors readline() and pynmea's parse() function really cause, we'll use this approach anyway
				errorCount+=1
				if errorCount > ERROR_RETRY_MAX:
					raise # raise the same error to abort the application after retry max has been reached
				else:
					print(ex)
					sleep(ERROR_WAIT_DURATION)
	finally:
		connection.close()

# main execution block:
try:
	startListen()
except KeyboardInterrupt:
	print ("\nAborted by keyboard.")
