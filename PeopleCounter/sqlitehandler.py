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

import sqlite3 # from https://docs.python.org/2/library/sqlite3.html and python-pysqlite2 in debian
import uuid
import targethandler

CONDITION_LOCATION_POINT = "location/point"
CONDITION_SPEED = "speed"

# class for accessing sqlite database
class SQLiteHandler:
	#
	# initialize using the given database file path
	#
	# @param {string} databaseFile
	#
	def __init__(self, databaseFile):
		self.__connection = sqlite3.connect(databaseFile)
		self.__cursor = self.__connection.cursor()

	#
	# cleanup
	#
	def __del__(self):
		if self.__connection:
			self.__connection.close()

	# Insert the basic measurement details
	#
	# @param {string} taskId identifier for the processed task
	# @return {string} identifier for the created measurement
	#
	def createMeasurement(self, taskId):
		measurementId = str(uuid.uuid4())
		self.__cursor.execute("INSERT INTO measurements (measurement_id, task_id, sent) VALUES (?,?,0)", (measurementId, taskId))
		self.__connection.commit()
		return measurementId

	# insert file details to the database
	#
	# @param {string} measurementId identifier for the measurement
	# @param {string} path the file path (e.g. /tmp/file.jpg)
	# @param {long} timestamp unix timestamp
	#
	def insertFileData(self, measurementId, path, timestamp):
		self.__cursor.execute("INSERT INTO measurement_files (measurement_id, path, guid, timestamp) VALUES (?,?,NULL,?)", (measurementId, path, timestamp))
		self.__connection.commit()

	# insert measurement data to the database
	#
	# @param {string} measurementId identifier for the measurement
	# @param {string} key data type (e.g. for GPS, "sensor/location")
	# @param {string} value the measurement value
	# @param {long} timestamp unix timestamp
	#
	def insertMeasurementData(self, measurementId, key, value, timestamp):
		self.__cursor.execute("INSERT INTO measurement_data (measurement_id, key, value, timestamp) VALUES (?,?,?,?)", (measurementId, key, value, timestamp))
		self.__connection.commit()

	#
	# read targets from the provided sqlite database file
	#
	# @return {Array[Target]} list of targets
	#
	def loadTargets(self):
		targets = []
		self.__cursor.execute("SELECT task_id, key, value, condition_id FROM task_conditions ORDER BY condition_id ASC")
		cRows = self.__cursor.fetchall()
		if len(cRows) < 1:
			return targets
		
		currentConditionId = None
		currentTarget = None
		targetId = 0
		for cRow in cRows:
			conditionId = cRow[3] # column condition_id
			if conditionId != currentConditionId: # create one target per condition
				currentConditionId = conditionId
				taskId = cRow[0] # column task_id
				
				self.__cursor.execute("SELECT feature FROM task_outputs where task_id=?", [taskId])
				oRows = self.cursor.fetchall()
				if len(oRows) < 1:
					print("The task, id: "+taskId+" does not contain valid output parameters...")
					continue
				for oRow in oRows:
					currentTarget.output.append(oRow[0])
					
				currentTarget = targethandler.Target()
				currentTarget.taskId = taskId
				currentTarget.id = targetId
				targetId += 1
				targets.append(currentTarget)
			# // if
			
			condition = cRow[1] # column key
			if condition == CONDITION_LOCATION_POINT:
				currentTarget.latLonString = cRow[2] # column value
				information = currentTarget.latLonString.split(",")
				currentTarget.latitude = float(information[0])
				currentTarget.longitude = float(information[1])
			elif condition == CONDITION_SPEED:
				currentTarget.speed = cRow[2] # column value
		# // for
			
		self.__connection.commit()
		return targets
