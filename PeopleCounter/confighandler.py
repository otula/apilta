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

import configparser

SECTION_LINES = "lines"
SECTION_AREA = "area"
SECTION_TARGETS = "targets"
SECTION_DATABASE = "database"
SECTION_TASKS = "tasks"
SECTION_IMAGES = "images"
SECTION_CONFIGURATION = "configuration"

#
# handler for configuration files
#
class ConfigHandler:
    #
    # @param {string} propertyFilePath
    #
    def __init__(self, propertyFilePath):
        self.__parser = configparser.RawConfigParser()
        self.__propertyFilePath = propertyFilePath
        self.loadConfig()

    #
    # called automatically by __init__()
    #
    def loadConfig(self):
        self.__parser.read(self.__propertyFilePath)

    #
    # @return {int} line down y
    #
    def getLineDown(self):
        return self.__parser.getint(SECTION_LINES, "down")

    #
    # @return {int} line down limit
    #
    def getLineDownLimit(self):
        return self.__parser.getint(SECTION_LINES, "down_limit")

    #
    # @return {int} line up y
    #
    def getLineUp(self):
        return self.__parser.getint(SECTION_LINES, "up")

    #
    # @return {int} line up y
    #
    def getLineUpLimit(self):
        return self.__parser.getint(SECTION_LINES, "up_limit")

    #
    # @return {int} line left x
    #
    def getLineLeftLimit(self):
        return self.__parser.getint(SECTION_LINES, "left_limit")

    #
    # @return {int} line right x
    #
    def getLineRightLimit(self):
        return self.__parser.getint(SECTION_LINES, "right_limit")

    #
    # @return {boolean} true if frames should be cropped to limits
    #
    def getCropLimits(self):
        return self.__parser.getboolean(SECTION_LINES, "crop_limits")

    #
    # @return {int} threshold area min percentage
    #
    def getThresholdMin(self):
        return self.__parser.getint(SECTION_AREA, "threshold_min")

    #
    # @return {int} threshold area max percentage
    #
    def getThresholdMax(self):
        return self.__parser.getint(SECTION_AREA, "threshold_max")

    #
    # @return {string} target file path
    #
    def getTargetFilePath(self):
        return self.__parser.get(SECTION_TARGETS, "file_path")

    #
    # @return {string} database file path
    #
    def getDatabaseFilePath(self):
        return self.__parser.get(SECTION_DATABASE, "file_path")

    #
    # @return {string} task id
    #
    def getTaskId(self):
        return self.__parser.get(SECTION_TASKS, "task_id")

    #
    # @return {string} image file path
    #
    def getImageFilePath(self):
        return self.__parser.get(SECTION_IMAGES, "file_path")

    #
    # @return {boolean} true if this host is running the gpsd service
    #
    def getGpsdMaster(self):
        return self.__parser.getboolean(SECTION_CONFIGURATION, "gpsd_master")

    #
    # @return {string} host name of gpsd service in the case gpsd_master was false
    #
    def getGpsdHostName(self):
        if self.getGpsdMaster():
            return "localhost"
        else:
            print("Info: using gpsd host name from config file")
            return self.__parser.get(SECTION_CONFIGURATION, "gpsd_address")
       
    #
    # @return {int} port of the gpsd service
    #
    def getGpsdPort(self):
        return self.__parser.getint(SECTION_CONFIGURATION, "gpsd_port")

    #
    # @return {string} text describing the location of the installed camera
    #
    def getCameraPosition(self):
        return self.__parser.get(SECTION_CONFIGURATION, "camera_position")
