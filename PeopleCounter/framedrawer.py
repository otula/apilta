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

import numpy
import cv2
import time
import math
import gpshandler

FONT = cv2.FONT_HERSHEY_SIMPLEX

#
# handles creating lines/text overlay on top of a frame
#
class FrameDrawer:
	#
	# @param {int} frameHeight
	# @param {int} frameWidth
	# @param {string} imageFilePath
	#
	def __init__(self, frameHeight, frameWidth, imageFilePath):
		self.__imageFilePath = imageFilePath
		print("Saving files to: "+self.__imageFilePath)
		self.__lineDownColor = (255,0,0)
		self.__lineUpColor = (0,0,255)
		self.__timestamp = 0
		self.__frameWidth = frameWidth
		self.__frameHeight = frameHeight

	#
	def __del__(self):
		cv2.destroyAllWindows()

	#
	# @param {int} lineUp
	# @param {int} upLimit
	# @param {int} lineDown
	# @param {int} downLimit
	# @param {int} leftLimit
	# @param {int} rightLimit
	#
	def setLines(self, lineUp, upLimit, lineDown, downLimit, leftLimit, rightLimit):
		pt1 = [0, lineDown];
		pt2 = [self.__frameWidth, lineDown];
		self.__ptsL1 = numpy.array([pt1,pt2], numpy.int32)
		pt3 = [0, lineUp];
		pt4 = [self.__frameWidth, lineUp];
		self.__ptsL2 = numpy.array([pt3,pt4], numpy.int32)

		pt5 = [0, upLimit];
		pt6 = [self.__frameWidth, upLimit];
		self.__ptsL3 = numpy.array([pt5,pt6], numpy.int32)
		pt7 = [0, downLimit];
		pt8 = [self.__frameWidth, downLimit];
		self.__ptsL4 = numpy.array([pt7,pt8], numpy.int32)

		pt9 = [leftLimit, 0];
		pt10 = [leftLimit, self.__frameHeight];
		self.__ptsL5 = numpy.array([pt9,pt10], numpy.int32)
		pt11 = [rightLimit, 0];
		pt12 = [rightLimit, self.__frameHeight];
		self.__ptsL6 = numpy.array([pt11,pt12], numpy.int32)

	#
	# @param {???} frame
	# @param {???} cnt
	# @param {int} cx
	# @param {int} cy
	# @param {int} x
	# @param {int} w
	# @param {int} y
	# @param {int} h
	#
	def drawContours(self, frame, cnt, cx, cy, x, w, y, h):
		cv2.circle(frame,(cx,cy), 5, (0,0,255), -1)
		cv2.rectangle(frame,(x,y),(x+w,y+h),(0,255,0),2)
		cv2.drawContours(frame, cnt, -1, (0,255,0), 3)

	#
	# @param {???} frame
	#
	def drawLines(self, frame):
		cv2.polylines(frame, [self.__ptsL1], False, self.__lineDownColor, thickness=2)
		cv2.polylines(frame, [self.__ptsL2], False, self.__lineUpColor, thickness=2)

		cv2.polylines(frame, [self.__ptsL3], False, (255,255,255), thickness=1)
		cv2.polylines(frame, [self.__ptsL4], False, (255,255,255), thickness=1)
		cv2.polylines(frame, [self.__ptsL5], False, (255,255,255), thickness=1)
		cv2.polylines(frame, [self.__ptsL6], False, (255,255,255), thickness=1)

	#
	# @param {???} frame
	# @param {int} cntDown
	# @param {int} cntUp
	#
	def drawCounts(self, frame, cntDown, cntUp):
		strUp = 'UP: '+ str(cntUp)
		strDown = 'DOWN: '+ str(cntDown)
		cv2.putText(frame, strUp, (10,40), FONT, 0.5, (255,255,255), 2, cv2.LINE_AA)
		cv2.putText(frame, strDown, (10,90), FONT, 0.5, (255,255,255), 2, cv2.LINE_AA)

	#
	# @param {???} frame
	# @param {int} cntDown if not given or < 0, the value is not appended to the file name
	# @param {int} cntUp if not given or < 0, the value is not appended to the file name
	#
	def writeFrame(self, frame, cntDown=-1, cntUp=-1):
		path = self.__imageFilePath+"people_count_image_"
		if cntDown >= 0:
			path += str(cntDown)
		path += "_"
		if cntUp >= 0:
			path += str(cntUp)
		cv2.imwrite(path+".jpg", frame)

	#
	# @param {???} frame
	#
	def showFrame(self, frame):
		cv2.imshow('Frame', frame)

	#
	# prints statistics of the camera performance (frametime & fps)
	#
	# @param {Target} currentTarget
	# @param {LocationData} currentLocation
	#
	def printStatistics(self, currentTarget, currentLocation):
		tempTime = time.time()
		frameTime = tempTime - self.__timestamp
		if math.floor(tempTime) % 20 == 0:
			print("Frametime: ", frameTime, " FPS: ", 1/(frameTime))
			print("Fix timestamp: ", currentLocation.getFixTimestamp(), ", target: ", currentTarget.id)
		self.__timestamp = tempTime
