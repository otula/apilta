/**
 * Copyright 2018 Tampere University of Technology, Pori Department
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tut.pori.shockapplication.datatypes;

/**
 *
 */
public class ShockMeasurement {
	private AccelerometerData _accelerometerData = null;
	private String _dataVisibility = null;
	private GyroData _gyroData = null;
	private Float _heading = null;
	private Double _latitude = null;
	private Integer _level = null;
	private Double _longitude = null;
	private String _measurementId = null;
	private RotationData _rotationData = null;
	private Float _speed = null;
	private Long _timestamp = null;

	/**
	 *
	 * @param accelerometerData
	 * @param gyroData
	 * @param heading
	 * @param latitude
	 * @param longitude
	 * @param rotationData
	 * @param speed
	 * @param timestamp
	 */
	public ShockMeasurement(AccelerometerData accelerometerData, String dataVisibility, GyroData gyroData, Float heading, Double latitude, Double longitude, RotationData rotationData, Float speed, Long timestamp) {
		_accelerometerData = accelerometerData;
		_gyroData = gyroData;
		_heading = heading;
		_latitude = latitude;
		_longitude = longitude;
		_rotationData = rotationData;
		_speed = speed;
		_timestamp = timestamp;
		_dataVisibility = dataVisibility;
	}

	public String getDataVisibility() {
		return _dataVisibility;
	}

	public AccelerometerData getAccelerometerData() {
		return _accelerometerData;
	}

	public GyroData getGyroData() {
		return _gyroData;
	}

	public Double getLatitude() {
		return _latitude;
	}

	public Double getLongitude() {
		return _longitude;
	}

	public void setMeasurementId(String measurementId) {
		_measurementId = measurementId;
	}

	public String getMeasurementId() {
		return _measurementId;
	}

	public RotationData getRotationData() {
		return _rotationData;
	}

	public Float getSpeed() {
		return _speed;
	}

	public Long getTimestamp() {
		return _timestamp;
	}

	public Integer getLevel() {
		return _level;
	}

	public void setLevel(Integer level) {
		_level = level;
	}

	public Float getHeading() {
		return _heading;
	}

	/**
	 *
	 */
	public static class AccelerometerData {
		private Float _systematicError = null;
		private float _xAcceleration = 0;
		private float _xyzAcceleration = 0;
		private float _yAcceleration = 0;
		private float _zAcceleration = 0;
		private Long _timestamp = null;

		/**
		 *
		 * @param xAcceleration
		 * @param yAcceleration
		 * @param zAcceleration
		 * @param timestamp
		 */
		public AccelerometerData(float xAcceleration, float yAcceleration, float zAcceleration, Long timestamp) {
			_xAcceleration = xAcceleration;
			_yAcceleration = yAcceleration;
			_zAcceleration = zAcceleration;
			_timestamp = timestamp;
		}

		public float getxAcceleration() {
			return _xAcceleration;
		}

		public float getyAcceleration() {
			return _yAcceleration;
		}

		public float getzAcceleration() {
			return _zAcceleration;
		}

		public Long getTimestamp() {
			return _timestamp;
		}

		public float getxyzAcceleration() {
			return _xyzAcceleration;
		}

		public void setxyzAcceleration(float xyzAcceleration) {
			_xyzAcceleration = xyzAcceleration;
		}

		public Float getSystematicError() {
			return _systematicError;
		}

		public void setSystematicError(Float systematicError) {
			_systematicError = systematicError;
		}
	} // class AccelerometerData

	/**
	 *
	 */
	public static class GyroData {
		private float _xSpeed = 0;
		private float _ySpeed = 0;
		private float _zSpeed = 0;
		private Long _timestamp = null;

		/**
		 *
		 * @param xSpeed
		 * @param ySpeed
		 * @param zSpeed
		 * @param timestamp
		 */
		public GyroData(float xSpeed, float ySpeed, float zSpeed, Long timestamp) {
			_xSpeed = xSpeed;
			_ySpeed = ySpeed;
			_zSpeed = zSpeed;
			_timestamp = timestamp;
		}

		public float getxSpeed() {
			return _xSpeed;
		}

		public float getySpeed() {
			return _ySpeed;
		}

		public float getzSpeed() {
			return _zSpeed;
		}

		public Long getTimestamp() {
			return _timestamp;
		}
	} // class GyroData

	/**
	 *
	 */
	public static class RotationData {
		private float _xSin = 0;
		private float _ySin = 0;
		private float _zSin = 0;
		private float _cos = 0;
		private float _accuracy = 0;
		private Long _timestamp = null;

		/**
		 *
		 * @param xSin
		 * @param ySin
		 * @param zSin
		 * @param cos
		 * @param accuracy
		 * @param timestamp
		 */
		public RotationData(float xSin, float ySin, float zSin, float cos, float accuracy, Long timestamp) {
			_xSin = xSin;
			_ySin = ySin;
			_zSin = zSin;
			_cos = cos;
			_accuracy = accuracy;
			_timestamp = timestamp;
		}

		public float getxSin() {
			return _xSin;
		}

		public float getySin() {
			return _ySin;
		}

		public float getzSin() {
			return _zSin;
		}

		public float getCos() {
			return _cos;
		}

		public float getAccuracy() {
			return _accuracy;
		}

		public Long getTimestamp() {
			return _timestamp;
		}
	} // class RotationData
}
