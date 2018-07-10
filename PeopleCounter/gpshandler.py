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

import threading
import socket
import json
from time import sleep
import dateutil.parser # python-dateutil
from datetime import tzinfo, datetime, timezone

MSGLEN = 4096
START_WATCH = ('?WATCH={"enable":true,"json":false};').encode()
POLL = ('?POLL;').encode()
POLL_INTERVAL = 1 # location poll interval in seconds
EPOCH = datetime(1970, 1, 1, 0, 0, 0, 0, tzinfo=timezone.utc)

#
# GPS handler class
#
class GPSHandler:
	#
	# @param {Boolean} checkFix check for fix validity
	#
	def __init__(self, checkFix=True):
		self.__lastKnownLocation = None
		self.__stopListening = True
		self.__thread = None
		self.__lock = threading.Lock()
		self.__hasFix = False
		self.__checkFix = checkFix
		self.__serverAddress = ('localhost', 2947) #default values
	#
	def __del__(self):
		self.stopListen()
		
	#
	# @param {???} sky JSON object as returned NMEA
	# @return {Boolean} True if there was a valid Fix
	#
	def __checkSkyFix(self, sky):
		if not sky:
			self.__hasFix = False
			return False
			
		sky = sky[0]
		satellites = sky.get("satellites", None)
		inUse = 0
		if satellites: # check fix status by counting used satellites. Note: with gpsd/raspbian, the fix timestamp do not work reliably, making it impossible to figure out when the fix was achieved
			for s in satellites:
				if s.get("used", False):
					inUse +=1
				# // if
			# // for
		# // if
		if inUse > 1:	# assume that we have a fix when more than 1 satellite is in use
			self.__hasFix = True
		else:
			self.__hasFix = False
			
		return self.__hasFix

	#
	# GPS information poller method
	#
	def __poll(self):
		sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		try:
			sock.connect(self.__serverAddress)
			sock.sendall(START_WATCH)
			
			while not self.__stopListening:
				sock.sendall(POLL)
				
				data = sock.recv(MSGLEN).decode()
		
				for j in data.split("\n"): # check all messages in the buffer
					if j:
						try:
							nmeajson = json.loads(j)
							if nmeajson.get("class", None) != "POLL": # process only POLL responses
								continue
							
							if self.__checkFix and not self.__checkSkyFix(nmeajson.get("sky", None)): # if required, check that we have a valid fix
								continue
							
							ref = nmeajson.get("tpv", None)
							if not ref:
								continue
							
							ref = ref[0]
							
							time = ref.get("time", None)
							latitude = ref.get("lat", None)
							longitude = ref.get("lon", None)
							speed = ref.get("speed", None)
							
							if latitude and longitude and speed and time:
								self.__lastKnownLocation = LocationData(latitude, longitude, speed, time)
								break # no need to search for other poll responses
							
						except json.decoder.JSONDecodeError as ex:
							print(ex) # ignore JSON errors, sometimes the socket contains messages that do not have all required data
					# // if
				# // for
							
				sleep(POLL_INTERVAL)
			# // while
		finally:
			sock.close()
			
	#
	# sets address details for the GPSD server
	#
	def setServerAddress(self, hostname, port):
		self.__serverAddress = (hostname, port)

	#
	# start listening for new GPS data, if already listening, calling this method does nothing
	#
	def startListen(self):
		self.__lock.acquire()
		try:
			if self.__thread:
				print("Already listening...")
				return
			self.__stopListening = False
			self.__thread = threading.Thread(target=self.__poll)
			self.__thread.start()
		finally:
			self.__lock.release()

	#
	# stop listening for new GPS as soon as possible, if not listening, this does nothing
	#
	def stopListen(self):
		self.__lock.acquire()
		try:
			self.__stopListening = True
			
			if not self.__thread:
				print("Not listening...")
				return
			self.__thread.join()
			self.__thread = None
		finally:
			self.__lock.release()

	#
	# @return {LocationData} return the current location or None if current location is not known
	#
	def getCurrentLocation(self):
		if self.__lastKnownLocation and self.__hasFix:
			return self.__lastKnownLocation # NOTE: if this creates issues with threading, lock & copy
		else:
			return None


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
		self.__fixTime = fixTime
	
	#
	# @return {int} fix as UNIX timestamp (in ms)
	#
	def getFixTimestamp(self):
		dt = dateutil.parser.parse(self.__fixTime)
		return (int((dt - EPOCH).total_seconds())*1000)
