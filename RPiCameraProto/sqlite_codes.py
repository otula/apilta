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
import sqlite3 # from https://docs.python.org/2/library/sqlite3.html and python-pysqlite2 in debian
import uuid

import time # imported for testing timestamps
from datetime import datetime, tzinfo, timezone

connection = sqlite3.connect('/tmp/file.db')
cursor = connection.cursor()
nmeaFormat = "%H%M%S.%f,%d,%m,%Y,%z" # requires Python 3.3+
epoch = datetime(1970, 1, 1, 0, 0, 0, 0, tzinfo=timezone.utc)

# Insert the basic measurement details
#
# @param {string} taskId identifier for the processed task
# @return {string} identifier for the created measurement
# 
def createMeasurement(taskId):
    measurementId = str(uuid.uuid4())
    cursor.execute("INSERT INTO measurements (measurement_id, task_id, sent) VALUES (?,?,0)", (measurementId, taskId))
    return measurementId

# insert file details to the database
#
# @param {string} measurementId identifier for the measurement
# @param {string} path the file path (e.g. /tmp/file.jpg)
# @param {long} timestamp unix timestamp
#
def insertFileData(measurementId, path, timestamp):
    cursor.execute("INSERT INTO measurement_files (measurement_id, path, guid, timestamp) VALUES (?,?,NULL,?)", (measurementId, path, timestamp))

# insert measurement data to the database
#
# @param {string} measurementId identifier for the measurement
# @param {string} key data type (e.g. for GPS, "sensor/location")
# @param {string} value the measurement value
# @param {long} timestamp unix timestamp
#
def insertMeasurementData(measurementId, key, value, timestamp):
    cursor.execute("INSERT INTO measurement_data (measurement_id, key, value, timestamp) VALUES (?,?,?,?)", (measurementId, key, value, timestamp))


# Parse GPS datetime
#
# @param {string} zda NMEA ZDA format
# @return {long} unix timestamp
#
def parseZDA(zda):
	parts = zda.split(",")
	if parts[1].find(".") < 0:
		parts[1] += ".0"	#add milliseconds to part[1]
	
	tempdate = parts[1] + "," + parts[2] + "," + parts[3] + "," + parts[4]
	if len(parts[5]) < 1: #no timezone data
		tempdate += ",+0000"
	elif (parts[5].find("-")==0) or (parts[5].find("+")==0): #timezone is negative/positive
		tempdate += "," + parts[5]+parts[6].split("*")[0]
	else: #timezone is (most probably) positive without plus sign
		tempdate += ",+" + parts[5]+parts[6].split("*")[0]

	dt = datetime.strptime(tempdate, nmeaFormat)
	return int((dt - epoch).total_seconds()*1000)
		

# testing method
def test():
    measurementId = createMeasurement("1") # using hard-coded task identifier
    timestamp = parseZDA("$GPZDA,201530.46,04,07,2002,-03,00*60")
    insertFileData(measurementId, "/tmp/file.jpg", timestamp)
    insertMeasurementData(measurementId, "sensor/location", "61.4926,21.7980", timestamp)
    
    cursor.execute("SELECT * FROM measurements")
    print(cursor.fetchall())
    cursor.execute("SELECT * FROM measurement_files")
    print(cursor.fetchall())
    cursor.execute("SELECT * FROM measurement_data")
    print(cursor.fetchall())

test()
connection.commit() # commit in one-go, could also commit after each cursor.execute(...)
connection.close()
