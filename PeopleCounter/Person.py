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

EXPIRATION_TIME = 5 # when the person becomes expired, in seconds

#
# detected person
#
class Person:
	#
	# @param id
	# @param cx
	# @param cy
	#
	def __init__(self, id, cx, cy):
		self.__updated = time.time()
		self.overUp = False
		self.underDown = False
		self.cx = cx
		self.cy = cy
		self.id = id

	#
	# @return {Boolean} True if this person has expired
	#
	def isExpired(self):
		return ((time.time() - self.__updated) > EXPIRATION_TIME)

	#
	# @param cx
	# @param cy
	#
	def updated(self, cx, cy):
		self.cx = cx
		self.cy = cy
		self.__updated = time.time()
