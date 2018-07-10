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

import time

#
# This class is a drop-in debug replacement for the real GPSHandler
#
# This class can be used when real targets/position information is not available
#
# This class will always return a matching target
#
class GPSHandler:
	#
	# @param {Boolean} checkFix check for fix validity
	#
	def __init__(self, checkFix=True):
		print("Debug GPSHandler initialized.")

	#
	# sets address details for the GPSD server
	#
	def setServerAddress(self, hostname, port):
		print("setServerAddress() called with params: ", hostname, port)

	#
	# start listening for new GPS data, if already listening, calling this method does nothing
	#
	def startListen(self):
		print("startListen() called")

	#
	# stop listening for new GPS as soon as possible, if not listening, this does nothing
	#
	def stopListen(self):
		print("stopListen() called");

	#
	# @return {LocationData} returns location
	#
	def getCurrentLocation(self):
		return LocationData(61.492474, 21.800476, 0.1, None)


############################## LocationData ###############################

#
# location data, as received from GPS
#
class LocationData:
	#
	# @param {double} latitude
	# @param {double} longitude
	# @param {double} speed
	# @param {string} fixTime in ISO8601 format
	#
	def __init__(self, latitude, longitude, speed, fixTime):
		self.latitude = latitude
		self.longitude = longitude
		self.speed = speed
		self.__fixTime = int(time.time()*1000) # in this debug module, simply ignore the given fixTime and use current time
	
	#
	# @return {int} fix as UNIX timestamp (in ms)
	#
	def getFixTimestamp(self):
		return self.__fixTime
