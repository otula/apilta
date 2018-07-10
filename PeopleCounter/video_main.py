#!/usr/bin/python
#
# Copyright 2018 Tampere University of Technology, Pori Department
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

# tracking codes adapted from http://www.femb.com.mx/people-counter/

import sys
# enable debug gps/target handlers by running the application
# python3 counter_main.py debug <path-to-debug-handlers>
if (len(sys.argv) == 3) and (sys.argv[1] == "debug"):
	print("Using DEBUG GPS handlers from: ", sys.argv[2] + "/debug")
	sys.path.insert(0, sys.argv[2]+"/debug")
else:
	print("Resume normal operation.")

import numpy
import cv2
import Person
import time
import sqlitehandler
import gpshandler
import targethandler
# import framedrawer
import confighandler

PROPERTY_FILE = "/var/opt/raspi/config.ini"
FPS_LIMIT = 15
CAP_OPEN_CHECK_INTERVAL = 1 # how often the capture open status is checked, in seconds
DATA_KEY_COUNTER_UP = targethandler.FEATURE_COUNTER+"/up"
DATA_KEY_COUNTER_DOWN = targethandler.FEATURE_COUNTER+"/down"
DATA_KEY_CAMERA_POSITION = "camera/position"
BACKGROUND_SUBTRACTOR_HISTORY = FPS_LIMIT * 10
MORPH_OPENING = 5
MORPH_CLOSING = 9
COUNTER_DEBUG = True
COUNTER_INTERVAL = 10

#
# people counter class
#
class PeopleCounter:
	#
	#
	def __init__(self):
		self.cntUp = 0
		self.cntDown = 0

		configHandler = confighandler.ConfigHandler(PROPERTY_FILE)
		self.__cap = cv2.VideoCapture(-1)
		self.__cap.set(cv2.CAP_PROP_FPS, FPS_LIMIT)
		frameWidth = self.__cap.get(3)
		frameHeight = self.__cap.get(4)
		frameArea = frameHeight*frameWidth
		self.__areaTHMin = frameArea * configHandler.getThresholdMin() / 100
		self.__areaTHMax = frameArea * configHandler.getThresholdMax() / 100

		print("Area Thresholds - frameArea: "+str(frameArea)+", areaTHMin: "+str(self.__areaTHMin)+", areaTHMax: "+str(self.__areaTHMax))

		# left/right side limits
		self.__leftLimit = configHandler.getLineLeftLimit()
		self.__rightLimit = configHandler.getLineRightLimit()

		# entrance/exit lines
		self.__lineUp = configHandler.getLineUp()
		self.__upLimit = configHandler.getLineUpLimit()
		self.__lineDown = configHandler.getLineDown()
		self.__downLimit = configHandler.getLineDownLimit()

		print("Blue (down) line y: "+str(self.__lineDown))
		print("Red (up) line y: "+str(self.__lineUp))

		self.__cropLimits = configHandler.getCropLimits()
		if self.__cropLimits:
			print("Cropping limits...")
			self.__lineUp = self.__lineUp-self.__upLimit
			self.__lineDown = self.__lineDown-self.__upLimit
		else:
			print("Left limit: "+str(self.__leftLimit)+", right limit: "+str(self.__rightLimit))
			print("Blue (down) limit: "+str(self.__downLimit)+", red (up) limit: "+str(self.__upLimit))

		# foreground/background subtractor
		self.__fgbg = None

		# operators for the morphoperhonen filter
		# https://docs.opencv.org/3.0-beta/doc/py_tutorials/py_imgproc/py_morphological_ops/py_morphological_ops.html
		# Opening is just another name of erosion followed by dilation. It is useful in removing noise, as we explained above. Here we use the function, cv2.morphologyEx()
		self.__kernelOpening = cv2.getStructuringElement(cv2.MORPH_RECT,(MORPH_OPENING, MORPH_OPENING)) #also (3,3) ???
		#	cv2.getStructuringElement(cv2.MORPH_ELLIPSE,(MORPH_OPENING, MORPH_OPENING))
		#	elliptical kernel really kills performance!
		#	also: bigger the kernel, slower the operation
		#		opening 7; closing 11 and RECT: FPS around 8
		#		opening 7; closing 11 and ELLIPSE: FPS around 2
		#		opening 7; no closing: FPS around 12
		# Closing is reverse of Opening, Dilation followed by Erosion. It is useful in closing small holes inside the foreground objects, or small black points on the object.
		self.__kernelClosing = cv2.getStructuringElement(cv2.MORPH_RECT,(MORPH_CLOSING, MORPH_CLOSING))

		self.__personId = 0
		self.__persons = []

		self.__gpsHandler = gpshandler.GPSHandler()
		self.__gpsHandler.setServerAddress(configHandler.getGpsdHostName(), configHandler.getGpsdPort())

		self.__sqliteHandler = sqlitehandler.SQLiteHandler(configHandler.getDatabaseFilePath())

		self.__targetHandler = targethandler.TargetHandler()
		self.__targetHandler.initializeFromFile(configHandler.getTargetFilePath(), configHandler.getTaskId())
		# self.__TargetHandler.initializeFromDatabase(self.__sqliteHandler)

		self.__currentTarget = None
		self.__currentLocation = None

		self.__imageFilePath = configHandler.getImageFilePath()
		self.__cameraLocation = configHandler.getCameraPosition()
		# self.__fd = framedrawer.FrameDrawer(frameHeight, frameWidth, configHandler.getImageFilePath())
		# self.__fd.setLines(self.__lineUp, self.__upLimit, self.__lineDown, self.__downLimit, self.__leftLimit, self.__rightLimit)
		self.__videoFile = None
		self.__videoFramesWritten = 0

	#
	def __del__(self):
		self.__cap.release()
		del self.__sqliteHandler
		# del self.__fd

	#
	# Reset counter and current target, save measurement (if there are changes)
	#
	def __resetAndSave(self):
		self.cntUp = 0
		self.cntDown = 0
		self.__currentTarget = None
		self.__currentLocation = None
		self.__persons.clear()
		self.__fgbg = None #reset the background substractor just in case
		
		if self.__videoFile:
			self.__videoFile.release()
			self.__videoFile = None
		self.__videoFramesWritten = 0

	#
	# Store approximately every n-th passenger (it does not matter if they go up or down)
	#
	def __storeDebugPhoto(self, targetId, photo, foreground, processedForeground):
		passengerCount = self.cntUp + self.cntDown

	#
	# start counter
	#
	# @return {boolean} true if count finished successfully, false on error
	#
	def count(self):
		try:
			self.__gpsHandler.startListen()
			lastTime = time.time()

			while not self.__cap.isOpened():
				print("Capture is not open, waiting for ", CAP_OPEN_CHECK_INTERVAL, " second(s).")
				time.sleep(CAP_OPEN_CHECK_INTERVAL)

			while True: # we could also check for __cap.isOpened() here, but note that if the raspberry camera gets "broken" or disconnected for whatever reason, this check will still pass
				location = self.__gpsHandler.getCurrentLocation()
				if not location: # location was lost
					self.__resetAndSave()
					time.sleep(gpshandler.POLL_INTERVAL)
					continue

				target = self.__targetHandler.findTarget(location)
				if not target: # location was resolved, but we are not at any target
					self.__resetAndSave()
					time.sleep(gpshandler.POLL_INTERVAL)
					continue
				elif self.__currentTarget and self.__currentTarget.id != target.id: # the target has changed
					self.__resetAndSave()
					continue

				self.__currentTarget = target
				self.__currentLocation = location
				
				if not self.__videoFile:
					timestamp = self.__currentLocation.getFixTimestamp()
					fName = self.__imageFilePath + "../videos/bussivideo" + '_ts'+str(timestamp) + '_targetT'+str(self.__currentTarget.id) + '.avi'
					# Define the codec and create VideoWriter object
					fourcc = cv2.VideoWriter_fourcc(*'XVID')
					self.__videoFile = cv2.VideoWriter(fName, fourcc, FPS_LIMIT, (640,480))
				
				ret, frame = self.__cap.read()
				self.__videoFile.write(frame)
				
				#tempTime = time.time()
				#print("frametime", tempTime-lastTime, "fps", 1/(tempTime-lastTime))
				#lastTime = tempTime
			# // while
		finally:
			self.__gpsHandler.stopListen()

		return True


################ MAIN ###############
counter = PeopleCounter()
try:
	counter.count()
finally:
	del counter
