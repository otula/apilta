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
COUNTER_DEBUG = False
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

	#
	def __del__(self):
		self.__cap.release()
		del self.__sqliteHandler
		# del self.__fd

	#
	# Reset counter and current target, save measurement (if there are changes)
	#
	def __resetAndSave(self):
		if self.__currentTarget and (self.cntUp > 0 or self.cntDown > 0):
			timestamp = self.__currentLocation.getFixTimestamp()
			print(timestamp, "Saving changes for target id:", self.__currentTarget.id, ", persons went down:", self.cntDown, "and went up:", self.cntUp)

			measurementId = self.__sqliteHandler.createMeasurement(self.__currentTarget.taskId)
			self.__sqliteHandler.insertMeasurementData(measurementId, targethandler.FEATURE_LOCATION, self.__currentTarget.latLonString, timestamp)
			self.__sqliteHandler.insertMeasurementData(measurementId, DATA_KEY_CAMERA_POSITION, self.__cameraLocation, timestamp)
			if self.cntUp > 0:
				self.__sqliteHandler.insertMeasurementData(measurementId, DATA_KEY_COUNTER_UP, str(self.cntUp), timestamp)
			if self.cntDown > 0:
				self.__sqliteHandler.insertMeasurementData(measurementId, DATA_KEY_COUNTER_DOWN, str(self.cntDown), timestamp)

		self.cntUp = 0
		self.cntDown = 0
		self.__currentTarget = None
		self.__currentLocation = None
		self.__persons.clear()
		self.__fgbg = None #reset the background substractor just in case

	#
	# Store approximately every n-th passenger (it does not matter if they go up or down)
	#
	def __storeDebugPhoto(self, targetId, photo, foreground, processedForeground):
		passengerCount = self.cntUp + self.cntDown
		if COUNTER_DEBUG and ((passengerCount + 1) % COUNTER_INTERVAL == 0):
			timestamp = self.__currentLocation.getFixTimestamp()
			scaledAndConverted = cv2.cvtColor(photo, cv2.COLOR_BGR2GRAY)
			#stack images vertically
			concatenated = numpy.concatenate((scaledAndConverted, foreground, processedForeground), axis=0)
			filename = self.__imageFilePath + 'debug' + '_ts'+str(timestamp) + '_targetT'+str(targetId) + '_pc'+str(passengerCount) + '.png'
			print("Saving a file to", filename)
			cv2.imwrite(filename, concatenated)

	#
	# start counter
	#
	# @return {boolean} true if count finished successfully, false on error
	#
	def count(self):
		try:
			self.__gpsHandler.startListen()

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
					time.sleep(0.5)
					continue
				elif self.__currentTarget and self.__currentTarget.id != target.id: # the target has changed
					self.__resetAndSave()
					continue

				if not self.__fgbg:
					self.__fgbg = cv2.createBackgroundSubtractorMOG2(history = BACKGROUND_SUBTRACTOR_HISTORY, varThreshold = 20, detectShadows = False)

				self.__currentTarget = target
				self.__currentLocation = location

				ret, frame = self.__cap.read()
				if self.__cropLimits:
					frame = frame[self.__upLimit:self.__downLimit, self.__leftLimit:self.__rightLimit]
				# cv::BackgroundSubtractorMOG2::apply ( image, learningRate = -1 )
				# learningRate	The value between 0 and 1 that indicates how fast the background model is learnt. Negative parameter value makes the algorithm to use some automatically chosen learning rate. 0 means that the background model is not updated at all, 1 means that the background model is completely reinitialized from the last frame. 
				fgmask = self.__fgbg.apply(frame)
				ret,imBin = cv2.threshold(fgmask, 200, 255, cv2.THRESH_BINARY)
				mask = cv2.morphologyEx(imBin, cv2.MORPH_OPEN, self.__kernelOpening)
				mask = cv2.morphologyEx(mask, cv2.MORPH_CLOSE, self.__kernelClosing)

				# RETR_EXTERNAL returns only extreme outer flags. All child contours are left behind.
				_, contours0, hierarchy = cv2.findContours(mask,cv2.RETR_EXTERNAL,cv2.CHAIN_APPROX_SIMPLE)
				for cnt in contours0:
					area = cv2.contourArea(cnt)
					if area > self.__areaTHMin and area < self.__areaTHMax:
						x,y,w,h = cv2.boundingRect(cnt)

						M = cv2.moments(cnt)
						cx = int(M['m10']/M['m00'])
						cy = int(M['m01']/M['m00'])

						if not self.__cropLimits and (cx < self.__leftLimit or cx > self.__rightLimit):
							#check whether the object is between the defined "corridor" (left and right boundaries)
							continue

						new = True
						persons = self.__persons[:] # slice array copy for looping
						for person in persons:
							#checks that the "new" object is close enought the old stored object
							if abs(cx-person.cx) <= w and abs(cy-person.cy) <= h:
								new = False
								#print("person was previously detected >>", "up: ", person.overUp, " down: ", person.underDown)

								if person.overUp: # this person has been over the up line
									if cy > self.__lineDown: # person travelled up -> down
										self.cntDown += 1
										print("Down: ", self.cntDown, ", target: ", self.__currentTarget.id, ", person id: ", person.id)
										self.__persons.remove(person)
										self.__storeDebugPhoto(self.__currentTarget.id, frame, fgmask, mask)
								elif person.underDown: # this person has been under the down line
									if cy < self.__lineUp: # person travelled down -> up
										self.cntUp += 1
										print("Up: ", self.cntUp, ", target: ", self.__currentTarget.id, ", person id: ", person.id)
										self.__persons.remove(person)
										self.__storeDebugPhoto(self.__currentTarget.id, frame, fgmask, mask)
								elif cy > self.__lineDown: # the person is under down line
									person.underDown = True
									#print("down", person.id)
								elif cy < self.__lineUp: # the person is over up line
									person.overUp = True
									#print("up", person.id)

								person.updated(cx, cy)
								break
							elif person.isExpired():
								self.__persons.remove(person)
						# // for

						if new:
							#print("new")
							p = Person.Person(self.__personId, cx, cy)
							self.__persons.append(p)
							self.__personId += 1
						# // if
					# // if
				# // for

				# self.__fd.drawLines(frame)
				# self.__fd.drawCounts(frame, self.cntDown, self.cntUp)
				# self.__fd.writeFrame(frame, self.cntDown, self.cntUp)
				# self.__fd.showFrame(frame)
				# self.__fd.printStatistics(self.__currentTarget, self.__currentLocation)
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
