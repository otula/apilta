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

import sqlitehandler
import gpshandler
from math import radians, sin, cos, sqrt, asin

EARTH_RADIUS = 6372.8 # Earth radius in kilometers
LOCATION_THRESHOLD = 0.03 # distance threshold for resolving matching location coordinate, in kilometers
SPEED_THRESHOLD = 1.0
FEATURE_CAMERA = "sensor/camera"
FEATURE_LOCATION = "sensor/location"
FEATURE_COUNTER = "sensor/counter"

#
# class for handling targets, remember to call one of the initialization methods
#
class TargetHandler:
	#
	# @param {string} targetFilePath
	# @param {string} taskId
	# @throws ValueError on invalid or non-existent targets
	#
	def initializeFromFile(self, targetFilePath, taskId):
		self.targets = []
		targetId = 0
		with open(targetFilePath, "r") as file:
			for line in file:
				information = line.split()
				t = Target()
				print("Loaded target: "+information[0])
				t.latLonString = information[1]
				t.speed = float(information[2])
				information = information[1].split(",")
				t.latitude = float(information[0])
				t.longitude = float(information[1])
				t.taskId = taskId
				t.id = (targetId)
				targetId += 1
				t.output.append(FEATURE_LOCATION)
				t.output.append(FEATURE_COUNTER)
				self.targets.append(t)
				
		if not self.targets:
			raise ValueError("No valid targets defined in the given target file.")		

	#
	# @param {SqliteHandler} handler to use for retrieving target details
	# @throws ValueError on invalid or non-existent targets
	#
	def initializeFromDatabase(self, sqlitehandler):
		self.targets = sqlitehandler.loadTargets()
		if not self.targets:
			raise ValueError("No valid targets found in the database.")

	#
	# calculates the distance between two coordinates using the great circle distance
	#
	# @param {float} lat1 latitude for the first coordinate
	# @param {float} lon1 longitude for the first coordinate
	# @param {float} lat2 latitude for the second coordinate
	# @param {float} lon2 longitede for the second coordinate
	# @return {float} the distance between the two points in kilometers
	#
	def __haversine(self, lat1, lon1, lat2, lon2):
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
	# @param {LocationData} location
	# @return {Target} the matching Target object or None if nothing was found
	#
	def findTarget(self, location):
		for t in self.targets:
			if t.speed != None and abs(location.speed - t.speed) > SPEED_THRESHOLD: # skip non-matching speeds
				continue
			elif t.latitude and t.longitude and self.__haversine(location.latitude, location.longitude, t.latitude, t.longitude) > LOCATION_THRESHOLD: # skip if outside of area
				continue
			else:
				return t
		return None



############### class Target ################

#
# Details for a single location target
#
class Target:
	latitude = None
	longitude = None
	latLonString = None # coordinate in string format
	taskId = None # identifier for the task
	output = [] # output as requested by the task
	speed = None # travelling speed while around the target
	id = None # target identifier
	
