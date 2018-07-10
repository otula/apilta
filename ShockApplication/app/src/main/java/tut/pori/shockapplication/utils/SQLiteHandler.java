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
package tut.pori.shockapplication.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.Closeable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import tut.pori.shockapplication.datatypes.ShockMeasurement;

/**
 * Tables:
 *
 *
 * {@value #TABLE_MEASUREMENTS}
 * ----------------------------
 * {@value #COLUMN_MEASUREMENT_ID} TEXT
 * {@value #COLUMN_LATITUDE} REAL
 * {@value #COLUMN_LONGITUDE} REAL
 * {@value #COLUMN_SPEED} REAL
 * {@value #COLUMN_TIMESTAMP} INTEGER
 * {@value #COLUMN_SENT} INTEGER
 * {@value #COLUMN_DATA_VISIBILITY} TEXT
 *
 * {@value #TABLE_MEASUREMENTS_ACCELEROMETER}
 * ----------------------------------------
 * {@value #COLUMN_MEASUREMENT_ID} TEXT
 * {@value #COLUMN_X_ACCELERATION} REAL
 * {@value #COLUMN_Y_ACCELERATION} REAL
 * {@value #COLUMN_Z_ACCELERATION} REAL
 * {@value #COLUMN_TIMESTAMP} INTEGER
 *
 * {@value #TABLE_MEASUREMENTS_GYRO}
 * ---------------------------------
 * {@value #COLUMN_MEASUREMENT_ID} TEXT
 * {@value #COLUMN_X_SPEED} REAL
 * {@value #COLUMN_Y_SPEED} REAL
 * {@value #COLUMN_Z_SPEED} REAL
 * {@value #COLUMN_TIMESTAMP} INTEGER
 *
 * {@value #TABLE_MEASUREMENTS_ROTATION}
 * ---------------------------------
 * {@value #COLUMN_MEASUREMENT_ID} TEXT
 * {@value #COLUMN_X_SIN} REAL
 * {@value #COLUMN_Y_SIN} REAL
 * {@value #COLUMN_Z_SIN} REAL
 * {@value #COLUMN_COS} REAL
 * {@value #COLUMN_ACCURACY} REAL
 * {@value #COLUMN_TIMESTAMP} INTEGER
 */
public class SQLiteHandler implements Closeable {
	private static final String TAG = SQLiteHandler.class.toString();
	private static final String DATABASE_NAME = "sensor_db";
	private static final int DATABASE_VERSION = 20180418_1;
	/* tables */
	private static final String TABLE_MEASUREMENTS = "measurements";
	private static final String TABLE_MEASUREMENTS_ACCELEROMETER = "measurements_accelerometer";
	private static final String TABLE_MEASUREMENTS_GYRO = "measurements_gyro";
	private static final String TABLE_MEASUREMENTS_ROTATION = "measurements_rotation";
	/* columns */
	private static final String COLUMN_X_ACCELERATION = "x_accleration";
	private static final String COLUMN_Y_ACCELERATION = "y_accleration";
	private static final String COLUMN_Z_ACCELERATION = "z_accleration";
	private static final String COLUMN_X_SPEED = "x_speed";
	private static final String COLUMN_Y_SPEED = "y_speed";
	private static final String COLUMN_Z_SPEED = "z_speed";
	private static final String COLUMN_X_SIN= "x_sin";
	private static final String COLUMN_Y_SIN = "y_sin";
	private static final String COLUMN_Z_SIN = "z_sin";
	private static final String COLUMN_COS = "cos";
	private static final String COLUMN_ACCURACY = "accuracy";
	private static final String COLUMN_MEASUREMENT_ID = "measurement_id";
	private static final String COLUMN_TIMESTAMP = "timestamp";
	private static final String COLUMN_SPEED = "speed";
	private static final String COLUMN_HEADING = "heading";
	private static final String COLUMN_LATITUDE = "latitude";
	private static final String COLUMN_LONGITUDE = "longitude";
	private static final String COLUMN_SENT = "sent";
	private static final String COLUMN_DATA_VISIBILITY = "data_visibility";
	/* sql clauses */
	private static final String SQL_COUNT_MEASUREMENTS = "SELECT COUNT(*) FROM "+TABLE_MEASUREMENTS;
	private static final String SQL_WHERE_MEASUREMENTS_BY_TIMESTAMP = COLUMN_TIMESTAMP+">? AND "+COLUMN_TIMESTAMP+"<?";
	private static final String SQL_WHERE_MEASUREMENTS_SENT = COLUMN_SENT+"=1";
	private static final String SQL_WHERE_MEASUREMENTS_UNSENT = COLUMN_SENT+"=0";
	private static final String SQL_WHERE_MEASUREMENT_ID = COLUMN_MEASUREMENT_ID+"=?";
	private SQLiteDatabase _database = null;

	@Override
	public void close() {
		_database.close();
	}

	/**
	 *
	 * @param context
	 */
	public SQLiteHandler(Context context) {
		_database = new DBHelper(context).getWritableDatabase();
	}

	/**
	 *
	 * @param sent only count values with the given sent parameter, if null, both sent and unsent are counted
	 * @return the total amount of measurements in the database
	 */
	public int countMeasurements(Boolean sent) {
		StringBuilder sql = new StringBuilder(SQL_COUNT_MEASUREMENTS);
		if(sent != null){
			sql.append(" WHERE ");
			if(sent){
				sql.append(SQL_WHERE_MEASUREMENTS_SENT);
			}else{
				sql.append(SQL_WHERE_MEASUREMENTS_UNSENT);
			}
		}

		try (Cursor c = _database.rawQuery(sql.toString(), null)) {
			c.moveToFirst();
			return c.getInt(0);
		}
	}

	/**
	 *
	 * @param measurement
	 * @return UUID for the generated measurement
	 */
	public String createMeasurement(ShockMeasurement measurement) {
		String measurementId = null;
		_database.beginTransaction();
		try {
			measurementId = insertMeasurement(measurement);

			ShockMeasurement.AccelerometerData aData = measurement.getAccelerometerData();
			if(aData != null){
				insertAccelerometerData(measurementId, aData);
			}

			ShockMeasurement.GyroData gData = measurement.getGyroData();
			if(gData != null) {
				insertGyroData(measurementId, gData);
			}

			ShockMeasurement.RotationData rData = measurement.getRotationData();
			if(rData != null) {
				insertRotationData(measurementId, rData);
			}

			_database.setTransactionSuccessful();
		} finally {
			_database.endTransaction();
		}
		return measurementId;
	}

	/**
	 *
	 * @param measurementId
	 * @param data
	 */
	private void insertAccelerometerData(String measurementId, ShockMeasurement.AccelerometerData data) {
		ContentValues values = new ContentValues(5);
		values.put(COLUMN_MEASUREMENT_ID, measurementId);
		values.put(COLUMN_X_ACCELERATION, data.getxAcceleration());
		values.put(COLUMN_Y_ACCELERATION, data.getyAcceleration());
		values.put(COLUMN_Z_ACCELERATION, data.getzAcceleration());
		values.put(COLUMN_TIMESTAMP, data.getTimestamp());
		_database.insertOrThrow(TABLE_MEASUREMENTS_ACCELEROMETER, null, values);
	}

	/**
	 *
	 * @param measurementId
	 * @param data
	 */
	private void insertGyroData(String measurementId, ShockMeasurement.GyroData data) {
		ContentValues values = new ContentValues(5);
		values.put(COLUMN_MEASUREMENT_ID, measurementId);
		values.put(COLUMN_X_SPEED, data.getxSpeed());
		values.put(COLUMN_Y_SPEED, data.getySpeed());
		values.put(COLUMN_Z_SPEED, data.getzSpeed());
		values.put(COLUMN_TIMESTAMP, data.getTimestamp());
		_database.insertOrThrow(TABLE_MEASUREMENTS_GYRO, null, values);
	}

	/**
	 *
	 * @param measurementId
	 * @param data
	 */
	private void insertRotationData(String measurementId, ShockMeasurement.RotationData data) {
		ContentValues values = new ContentValues(7);
		values.put(COLUMN_MEASUREMENT_ID, measurementId);
		values.put(COLUMN_X_SIN, data.getxSin());
		values.put(COLUMN_Y_SIN, data.getySin());
		values.put(COLUMN_Z_SIN, data.getzSin());
		values.put(COLUMN_COS, data.getCos());
		values.put(COLUMN_ACCURACY, data.getAccuracy());
		values.put(COLUMN_TIMESTAMP, data.getTimestamp());
		_database.insertOrThrow(TABLE_MEASUREMENTS_ROTATION, null, values);
	}

	/**
	 *
	 * @param measurement
	 * @return UUID created for the measurement
	 */
	private String insertMeasurement(ShockMeasurement measurement) {
		String measurementId = UUID.randomUUID().toString();
		ContentValues values = new ContentValues(7);
		values.put(COLUMN_MEASUREMENT_ID, measurementId);
		values.put(COLUMN_LATITUDE, measurement.getLatitude());
		values.put(COLUMN_LONGITUDE, measurement.getLongitude());
		values.put(COLUMN_SPEED, measurement.getSpeed());
		values.put(COLUMN_TIMESTAMP, measurement.getTimestamp());
		values.put(COLUMN_DATA_VISIBILITY, measurement.getDataVisibility());
		values.put(COLUMN_HEADING, measurement.getHeading());
		_database.insertOrThrow(TABLE_MEASUREMENTS, null, values);

		measurement.setMeasurementId(measurementId);
		return measurementId;
	}

	/**
	 *
	 * @param measurementIds list of measurements to delete
	 */
	public void delete(Collection<String> measurementIds) {
		_database.beginTransaction();
		String[] whereArgs = new String[1];
		try {
			for(String measurementId : measurementIds) {
				whereArgs[0] = measurementId;
				_database.delete(TABLE_MEASUREMENTS, SQL_WHERE_MEASUREMENT_ID, whereArgs);
				_database.delete(TABLE_MEASUREMENTS_ACCELEROMETER, SQL_WHERE_MEASUREMENT_ID, whereArgs);
				_database.delete(TABLE_MEASUREMENTS_GYRO, SQL_WHERE_MEASUREMENT_ID, whereArgs);
				_database.delete(TABLE_MEASUREMENTS_ROTATION, SQL_WHERE_MEASUREMENT_ID, whereArgs);
			}
			_database.setTransactionSuccessful();
		} finally {
			_database.endTransaction();
		}
	}

	/**
	 * return measurements within the given time interval
	 *
	 * @param startTimestamp
	 * @param endTimestamp
	 * @return list of measurements or null if none was found
	 */
	public List<ShockMeasurement> getMeasurements(long startTimestamp, long endTimestamp) {
		_database.beginTransaction();
		List<ShockMeasurement> measurements = null;
		try (Cursor c = _database.query(TABLE_MEASUREMENTS, null, SQL_WHERE_MEASUREMENTS_BY_TIMESTAMP, new String[]{String.valueOf(startTimestamp), String.valueOf(endTimestamp)}, null, null, null)) {
			measurements = extractMeasurements(c);
			_database.setTransactionSuccessful();
		} finally {
			_database.endTransaction();
		}

		return measurements;
	}

	/**
	 * return measurements marked as "unsent"
	 *
	 * @param limit
	 * @return list of measurements or null if none was found
	 */
	public List<ShockMeasurement> getUnsentMeasurements(int limit) {
		_database.beginTransaction();
        List<ShockMeasurement> measurements = null;
		try (Cursor c = _database.query(TABLE_MEASUREMENTS, null, SQL_WHERE_MEASUREMENTS_UNSENT, null, null, null, null, String.valueOf(limit))) {
			measurements = extractMeasurements(c);
			_database.setTransactionSuccessful();
		} finally {
			_database.endTransaction();
		}

		return measurements;
	}

	/**
	 * helper method for extracting measurements for cursor
	 *
	 * @param c
	 * @return measurements for the cursor, or null if none
	 */
	private LinkedList<ShockMeasurement> extractMeasurements(Cursor c) {
        LinkedList<ShockMeasurement> measurements = null;
		if(c.moveToFirst()){
			measurements = new LinkedList<>();
			int indexMeasurementId = c.getColumnIndex(COLUMN_MEASUREMENT_ID), indexLatitude = c.getColumnIndex(COLUMN_LATITUDE), indexLongitude = c.getColumnIndex(COLUMN_LONGITUDE), indexSpeed = c.getColumnIndex(COLUMN_SPEED), indexTimestamp = c.getColumnIndex(COLUMN_TIMESTAMP), indexDataVisibility = c.getColumnIndex(COLUMN_DATA_VISIBILITY), indexHeading = c.getColumnIndex(COLUMN_HEADING);
			do{
				String measurementId = c.getString(indexMeasurementId);
				ShockMeasurement.AccelerometerData aData = getAccelerometerData(measurementId);
				ShockMeasurement.GyroData gData = getGyroData(measurementId);
				ShockMeasurement.RotationData rData = getRotationData(measurementId);
				ShockMeasurement measurement = new ShockMeasurement(aData, c.getString(indexDataVisibility), gData, c.getFloat(indexHeading), c.getDouble(indexLatitude), c.getDouble(indexLongitude), rData, c.getFloat(indexSpeed), c.getLong(indexTimestamp));
				measurement.setMeasurementId(measurementId);
				measurements.add(measurement);
			}while(c.moveToNext());
		}
		return measurements;
	}

	/**
	 * set the given list of measurements as sent, non-existing or duplicate ids are ignored
	 *
	 * @param measurementIds
	 */
	public void setSent(Collection<String> measurementIds) {
		if(measurementIds == null || measurementIds.isEmpty()){
			Log.w(TAG, "Attempted to set sent with null or empty collection of ids.");
			return;
		}

		_database.beginTransaction();
		try {
			ContentValues values = new ContentValues(1);
			values.put(COLUMN_SENT, 1);
			String[] whereArgs = new String[1];
			for(String measurementId : measurementIds) {
				whereArgs[0] = measurementId;
				_database.update(TABLE_MEASUREMENTS, values, SQL_WHERE_MEASUREMENT_ID, whereArgs);
			}
			_database.setTransactionSuccessful();
		} finally {
			_database.endTransaction();
		}
	}

	/**
	 *
	 * @param measurementId
	 * @return accelerometer data or null if nothing was found
	 */
	private ShockMeasurement.AccelerometerData getAccelerometerData(String measurementId) {
		try (Cursor c = _database.query(TABLE_MEASUREMENTS_ACCELEROMETER, null, SQL_WHERE_MEASUREMENT_ID, new String[]{measurementId}, null, null, null, "1")) {
			if(c.moveToFirst()){
				return new ShockMeasurement.AccelerometerData(c.getFloat(c.getColumnIndex(COLUMN_X_ACCELERATION)),c.getFloat(c.getColumnIndex(COLUMN_Y_ACCELERATION)), c.getFloat(c.getColumnIndex(COLUMN_Z_ACCELERATION)), c.getLong(c.getColumnIndex(COLUMN_TIMESTAMP)));
			}
		}
		return null;
	}

	/**
	 *
	 * @param measurementId
	 * @return gyro data or null if nothing was found
	 */
	private ShockMeasurement.GyroData getGyroData(String measurementId) {
		try (Cursor c = _database.query(TABLE_MEASUREMENTS_GYRO, null, SQL_WHERE_MEASUREMENT_ID, new String[]{measurementId}, null, null, null, "1")) {
			if(c.moveToFirst()){
				return new ShockMeasurement.GyroData(c.getFloat(c.getColumnIndex(COLUMN_X_SPEED)),c.getFloat(c.getColumnIndex(COLUMN_Y_SPEED)), c.getFloat(c.getColumnIndex(COLUMN_Z_SPEED)), c.getLong(c.getColumnIndex(COLUMN_TIMESTAMP)));
			}
		}
		return null;
	}

	/**
	 *
	 * @param measurementId
	 * @return rotation data or null if nothing was found
	 */
	private ShockMeasurement.RotationData getRotationData(String measurementId) {
		try (Cursor c = _database.query(TABLE_MEASUREMENTS_ROTATION, null, SQL_WHERE_MEASUREMENT_ID, new String[]{measurementId}, null, null, null, "1")) {
			if(c.moveToFirst()){
				return new ShockMeasurement.RotationData(c.getFloat(c.getColumnIndex(COLUMN_X_SIN)),c.getFloat(c.getColumnIndex(COLUMN_Y_SIN)), c.getFloat(c.getColumnIndex(COLUMN_Z_SIN)), c.getFloat(c.getColumnIndex(COLUMN_COS)), c.getFloat(c.getColumnIndex(COLUMN_ACCURACY)), c.getLong(c.getColumnIndex(COLUMN_TIMESTAMP)));
			}
		}
		return null;
	}

	/**
	 *
	 */
	private class DBHelper extends SQLiteOpenHelper {

		/**
		 *
		 * @param context
		 */
		public DBHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase sqLiteDatabase) {
			Log.d(TAG, "Re-creating tables if necessary...");

			sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_MEASUREMENTS+" (" +
					COLUMN_MEASUREMENT_ID + " TEXT NOT NULL, " +
					COLUMN_LATITUDE + " REAL NOT NULL, " +
					COLUMN_LONGITUDE + " REAL NOT NULL, " +
					COLUMN_HEADING + " REAL DEFAULT NULL, " +
					COLUMN_SPEED + " REAL DEFAULT NULL, " +
					COLUMN_SENT + " INTEGER DEFAULT 0, " +
					COLUMN_DATA_VISIBILITY + " TEXT NOT NULL, " +
					COLUMN_TIMESTAMP + " INTEGER NOT NULL);"
				);
			sqLiteDatabase.execSQL("CREATE INDEX sent_INDEX ON "+TABLE_MEASUREMENTS+"("+COLUMN_SENT+");");

			sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS "+ TABLE_MEASUREMENTS_ACCELEROMETER +" (" +
					COLUMN_MEASUREMENT_ID + " TEXT NOT NULL, " +
					COLUMN_X_ACCELERATION + " REAL NOT NULL, " +
					COLUMN_Y_ACCELERATION + " REAL NOT NULL, " +
					COLUMN_Z_ACCELERATION + " REAL NOT NULL, " +
					COLUMN_TIMESTAMP + " INTEGER NOT NULL);"
				);

			sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_MEASUREMENTS_GYRO+" (" +
					COLUMN_MEASUREMENT_ID + " TEXT NOT NULL, " +
					COLUMN_X_SPEED + " REAL NOT NULL, " +
					COLUMN_Y_SPEED + " REAL NOT NULL, " +
					COLUMN_Z_SPEED + " REAL NOT NULL, " +
					COLUMN_TIMESTAMP + " INTEGER NOT NULL);"
			);

			sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_MEASUREMENTS_ROTATION+" (" +
					COLUMN_MEASUREMENT_ID + " TEXT NOT NULL, " +
					COLUMN_X_SIN + " REAL NOT NULL, " +
					COLUMN_Y_SIN + " REAL NOT NULL, " +
					COLUMN_Z_SIN + " REAL NOT NULL, " +
					COLUMN_COS + " REAL NOT NULL, " +
					COLUMN_ACCURACY + " REAL, " +
					COLUMN_TIMESTAMP + " INTEGER NOT NULL);"
			);
		}

		@Override
		public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
			Log.d(TAG, "Dropping all tables...");
			sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_MEASUREMENTS+";");
			sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ TABLE_MEASUREMENTS_ACCELEROMETER +";");
			sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_MEASUREMENTS_GYRO+";");
			sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_MEASUREMENTS_ROTATION+";");
			onCreate(sqLiteDatabase);
		}
	}
}
