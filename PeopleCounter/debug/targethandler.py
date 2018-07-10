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

import gpshandler

FEATURE_CAMERA = "sensor/camera"
FEATURE_LOCATION = "sensor/location"
FEATURE_COUNTER = "sensor/counter" 

#
# This class is a drop-in debug replacement for the real TargetHandler
#
# This class can be used when real targets/position information is not available
#
# This class will always return a matching target
#
class TargetHandler:
	
	#
	def __init__(self):
		self.__taskId = "DEBUG_TASK_ID";
		print("Debug TargetHandler initialized.")
	
	#
	# @param {string} targetFilePath
	# @param {string} taskId
	#
	def initializeFromFile(self, targetFilePath, taskId):
		self.__taskId = taskId;
		print("initializeFromFile() called");		

	#
	# @param {SqliteHandler} handler to use for retrieving target details
	#
	def initializeFromDatabase(self, sqlitehandler):
		print("initializeFromDatabase() called");

	#
	# @param {LocationData} location
	# @return {Target} matching Target object
	#
	def findTarget(self, location):
		target = Target()
		target.latLonString = str(location.latitude)+","+str(location.longitude)
		target.latitude = location.latitude
		target.longitude = location.longitude
		target.speed = location.speed
		target.taskId = self.__taskId
		target.output.append(FEATURE_LOCATION)
		target.output.append(FEATURE_COUNTER)
		target.id = 0
		return target



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
	