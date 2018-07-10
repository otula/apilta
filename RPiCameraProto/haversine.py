from math import radians, sin, cos, sqrt, asin
from decimal import Decimal

locationThreshold = 0.05 # distance threshold for resolving matching location coordinate, in kilometers
earthRadius = 6372.8 # Earth radius in kilometers

targets = {}
with open("/tmp/targets", "r") as file:
	for line in file:
		information = line.split()
		targets[information[0]] = information[1]  	
file.close()

#
# calculates the distance between two coordinates using the great circle distance
#
# @param {float} lat1 latitude for the first coordinate
# @param {float} lon1 longitude for the first coordinate
# @param {float} lat2 latitude for the second coordinate
# @param {float} lon2 longitede for the second coordinate
# @return {float} the distance between the two points in kilometers
#
def haversine(lat1, lon1, lat2, lon2):
	dLat = radians(lat2-lat1)
	dLon = radians(lon2-lon1)
	lat1 = radians(lat1)
	lat2 = radians(lat2)
 
	a = sin(dLat/2)**2 + cos(lat1)*cos(lat2)*sin(dLon/2)**2
	c = 2*asin(sqrt(a))
	return earthRadius * c

#
# Checks if the list of known targets contains coordinates near the given latitude/longitude pair
#
# If multiple coordinates are in close vicinity, the the first match in the targets list is returned
#
# @param {float} lat latitude
# @param {float} lon longitude
# @return {string} the matching coordinate or null if nothing was found
#
def findCoordinateMatch(lat, lon):
	for key, value in targets.items():
		pair = key.split(",")
		if haversine(lat,lon, float(pair[0]), float(pair[1])) <= locationThreshold:
			return key
	return None

#
# method for testing haversine algorithm
#
# can compare, e.g. with 61.455529,21.839194 paikka
#
def test():
	lat = float(Decimal(61.455748)) # convertin decimal to float seems to work OK, not really needed if already float (or decimal...)
	lon = float(Decimal(21.838932))
	match = findCoordinateMatch(lat, lon)
	if match is not None: # check if match was found
		print(str(lat)+", "+str(lon)+" was within the threshold boundary of "+match+", "+targets.get(match))
	lat = 61.458609
	lon = 21.837302
	match = findCoordinateMatch(lat, lon)
	if match is not None: # check if match was found
		print(str(lat)+", "+str(lon)+" was within the threshold boundary of "+match+", "+targets.get(match))
	lat = 61.455857
	lon = 21.838788
	match = findCoordinateMatch(lat, lon)
	if match is not None: # check if match was found
		print(str(lat)+", "+str(lon)+" was within the threshold boundary of "+match+", "+targets.get(match))

test() # call test method
