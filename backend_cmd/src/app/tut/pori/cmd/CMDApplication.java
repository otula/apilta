/**
 * Copyright 2017 Tampere University of Technology, Pori Department
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
package app.tut.pori.cmd;

import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import app.tut.pori.cmd.datatypes.SqliteFile;
import otula.backend.tasks.TaskClient;
import otula.backend.tasks.datatypes.SensorTask;
import otula.backend.tasks.datatypes.SensorTaskList;
import service.tut.pori.apilta.files.datatypes.FileDetails;
import service.tut.pori.apilta.sensors.Definitions;
import service.tut.pori.apilta.sensors.datatypes.DataPoint;
import service.tut.pori.apilta.sensors.datatypes.Measurement;
import service.tut.pori.apilta.sensors.datatypes.MeasurementList;
import service.tut.pori.tasks.datatypes.TaskBackend;
import service.tut.pori.tasks.datatypes.TaskBackend.Status;

/**
 * Command line application for accessing Task and Sensor services
 * 
 */
public class CMDApplication implements Closeable {
	/** the requested operation finished without an error */
	public static final int CODE_OK = 0;
	/** failed to parse command line arguments */
	public static final int ERROR_CMD_LINE_ARGUMENTS = -1;
	/** failed to process database file */
	public static final int ERROR_DATABASE_FILE = -2;
	/** failed to send file */
	public static final int ERROR_FILE_SEND = -3;
	/** measurement send was requested, but there was nothing to send */
	public static final int ERROR_NOTHING_TO_SENT = -4;
	/** could not retrieve new tasks */
	public static final int ERROR_TASK_RETRIEVAL = -5;
	private static final String CMD_LINE_SYNTAX = "CMDApplication";
	private static final String OPTION_CREATE_DATABASE = "create-database";
	private static final String OPTION_CONFIGURATION_FILE = "configuration";
	private static final String OPTION_RETRIEVE_TASKS = "retrieve-tasks";
	private static final String OPTION_SEND_MEASUREMENTS = "send-measurements";
	private static final Logger LOGGER = Logger.getLogger(CMDApplication.class);
	private TaskClient _client = null;
	private CMDConfiguration _configuration = null;
	private SQLiteHandler _sqliteHandler = null;

	/**
	 * 
	 * @param cmd
	 * @return non-zero error code on failure
	 */
	private int initialize(CommandLine cmd) {
		_configuration = new CMDConfiguration();
		_configuration.initialize(cmd.getOptionValue(OPTION_CONFIGURATION_FILE));
		
		_sqliteHandler = new SQLiteHandler();
		
		boolean createDatabase = cmd.hasOption(OPTION_CREATE_DATABASE);
		if(createDatabase){
			LOGGER.debug("Creating new database...");
		}
		if(!_sqliteHandler.initialize(_configuration.getDatabaseFilePath(), createDatabase)){
			return ERROR_DATABASE_FILE;
		}
		
		_client = new TaskClient(_configuration);
		return 0;
	}
	
	/**
	 * execute the application based on the given arguments
	 * 
	 * @param args
	 * @return 0 on success, non-zero error code on failure
	 * @throws IllegalArgumentException
	 */
	public int execute(String[] args) throws IllegalArgumentException{
		CommandLine cmd = parseCMDOptions(args);
		if(cmd == null){
			return ERROR_CMD_LINE_ARGUMENTS;
		}
		
		int retval = initialize(cmd);
		if(retval != CODE_OK){
			return retval;
		}
		
		if(cmd.hasOption(OPTION_RETRIEVE_TASKS) && (retval = retrieveTasks()) != CODE_OK){
			return retval;
		}
		
		if(cmd.hasOption(OPTION_SEND_MEASUREMENTS)){
			retval = sendResults();
		}
		
		return retval;
	}
	
	/**
	 * 
	 * @return non-zero error code on error
	 */
	private int retrieveTasks() {
		LOGGER.debug("Retrieving tasks...");
		Long backendId = _configuration.getBackendId();
		SensorTaskList tasks = _client.getTasks(backendId, service.tut.pori.tasks.Definitions.TASK_STATE_ACTIVE, null, null);
		if(tasks == null){
			LOGGER.error("Failed to retrieve tasks, keeping old ones...");
			return ERROR_TASK_RETRIEVAL;
		}
		
		if(SensorTaskList.isEmpty(tasks)){
			LOGGER.debug("No "+service.tut.pori.tasks.Definitions.TASK_STATE_ACTIVE+" tasks available.");
			if(!_sqliteHandler.deleteTasks()){
				LOGGER.warn("Failed to delete tasks.");
			}
			return CODE_OK;
		}
		
		List<SensorTask> taskList = tasks.getTasks();
		ArrayList<SensorTask> taskDetails = new ArrayList<>(taskList.size());
		for(SensorTask t : taskList){
			String taskId = t.getTaskIds().iterator().next();
			SensorTask details = _client.queryTaskDetails(backendId, taskId, null);
			if(details == null){
				LOGGER.error("Failed to retrieve task details for task, id: "+taskId+", using old details (if known)...");
				return ERROR_TASK_RETRIEVAL;
			}
			taskDetails.add(details);
		}
		
		if(!_sqliteHandler.deleteTasks()){
			LOGGER.warn("Failed to delete old tasks...");
		}
		
		for(SensorTask details : taskDetails){
			if(!_sqliteHandler.saveTaskDetails(details)){
				LOGGER.warn("Failed to save details for task, id: "+details.getTaskIds().iterator().next());
			}
		}
		
		return CODE_OK;
	}
	
	/**
	 * 
	 * @return non-zero error code on error
	 */
	private int sendResults() {
		LOGGER.debug("Sending files and results...");
		int status = sendFiles();
		if(status != CODE_OK) { // send all files
			LOGGER.error("Failed to send files to server.");
			return status;
		}
		
		Map<String, List<String>> unsent = _sqliteHandler.getUnsentMeasurementIds();
		if(unsent == null){
			LOGGER.debug("No unsent measurements in the database.");
			return ERROR_NOTHING_TO_SENT;
		}
		
		ArrayList<TaskBackend> backends = new ArrayList<>(1);
		TaskBackend backend = new TaskBackend();
		Long backendId = _configuration.getBackendId();
		backend.setBackendId(backendId);
		backend.setStatus(Status.EXECUTING); // if the task is really fully completed, we could also set COMPLETED status, but for now, keep it in executing state
		backends.add(backend);
		for(Entry<String, List<String>> e : unsent.entrySet()){
			String taskId = e.getKey();
			LOGGER.debug("Processing measurements for task, id: "+taskId);
			String callbackUri = _sqliteHandler.getCallbackURI(taskId);
			if(StringUtils.isBlank(callbackUri)){
				LOGGER.error("Callback uri: "+callbackUri+" is invalid for task, id: "+taskId);
				continue;
			}
			
			SensorTask task = new SensorTask();
			task.setBackends(backends);
			task.setTaskIds(Arrays.asList(taskId));
			task.setCallbackUri(callbackUri);
			
			List<String> measurementIds = e.getValue();
			List<Measurement> measurements = _sqliteHandler.getMeasurements(measurementIds);
			if(measurements == null){
				LOGGER.debug("No measurements resolved for task, id: "+taskId);
				measurements = new ArrayList<>();
			}	
			
			List<SqliteFile> files = _sqliteHandler.getFiles(true, measurementIds);
			if(files == null){
				LOGGER.debug("No files for task, id: "+taskId);
			}else{
				for(SqliteFile file : files){
					DataPoint dp = convert(file);		
					String measurementId = file.getMeasurementId();
					boolean missing = true;
					for(Measurement measurement : measurements){ // check if the measurement is already added
						if(measurement.getMeasurementId().equals(measurementId)){
							measurement.addDataPoint(dp);
							missing = false;
							break;
						}
					}
					if(missing){ // no matching measurement found, create a new one
						Measurement measurement = new Measurement();
						measurement.setMeasurementId(measurementId);
						measurement.addDataPoint(dp);
					}
				}
			}
			
			if(measurements.isEmpty()){
				LOGGER.debug("No new measurements for task, id: "+taskId);
				continue;
			}
			
			for(Measurement measurement : measurements){
				measurement.setBackendId(backendId); // set back end identifier for every measurement
			}
			
			MeasurementList list = new MeasurementList();
			list.setMeasurements(measurements);
			task.setMeasurements(list);
			
			if(!_client.taskFinished(task)){
				LOGGER.error("Failed to send task finished for task, id: "+taskId);
				continue;
			}
			
			if(_configuration.isKeepData()){
				if(!_sqliteHandler.setMeasurementsSent(measurementIds, taskId)){
					LOGGER.warn("Failed to set measurements sent for task, id: "+taskId);
				}
			}else{
				LOGGER.debug("Deleting data...");
				if(!_sqliteHandler.deleteMeasurements(measurementIds)){
					LOGGER.warn("Failed to delete data for task, id: "+taskId);
				}
			}
		}
		
		return CODE_OK;
	}
	
	/**
	 * helper method for converting sqlite file details to a measurement data point
	 * 
	 * @param file
	 * @return the passed file as data point
	 */
	private DataPoint convert(SqliteFile file) {
		DataPoint dp = new DataPoint();
		dp.setCreated(file.getTimestamp());
		dp.setKey(Definitions.DATA_POINT_KEY_FILE_GUID);
		dp.setValue(file.getGUID());
		return dp;
	}
	
	/**
	 * sends all unsent ("guidless") files
	 * @throws IllegalArgumentException on bad data
	 */
	private int sendFiles() throws IllegalArgumentException {
		List<SqliteFile> files = _sqliteHandler.getFiles(false, null);
		if(files == null){
			LOGGER.debug("No new files to send...");
			return CODE_OK;
		}
		
		Long backendId = _configuration.getBackendId();
		for(SqliteFile sqliteFile : files){
			String path = sqliteFile.getPath();
			File file = new File(path);
			FileDetails details = _client.createFile(backendId, file);
			if(details == null) {
				LOGGER.warn("Failed to send file to server, path: "+path);
				return ERROR_FILE_SEND;
			}else{
				String guid = details.getGUID();
				sqliteFile.setGUID(guid);
				if(!_sqliteHandler.setFileGUID(path, guid)){
					throw new IllegalArgumentException("Invalid file path: "+path+" or guid: "+guid);
				}
				
				if(!_configuration.isKeepFiles()){
					LOGGER.debug("Deleting file, path: "+path);
					if(!file.delete()){
						LOGGER.warn("Failed to delete file, path: "+path);
					}
				} // if
			} // else
		} // for
		return CODE_OK;
	}
	
	/**
	 * 
	 * @param args 
	 * @return initialized command line options available for this application
	 */
	private CommandLine parseCMDOptions(String[] args) {
		Options options = new Options();
		
		Option option = new Option("c", OPTION_CONFIGURATION_FILE, true, "Configuration file path");
		option.setRequired(true);
		option.setArgs(1);
		options.addOption(option);

		option = new Option("r", OPTION_RETRIEVE_TASKS, false, "Retrieve tasks");
		options.addOption(option);
		
		option = new Option("d", OPTION_CREATE_DATABASE, false, "Create database");
		options.addOption(option);
		
		option = new Option("s", OPTION_SEND_MEASUREMENTS, false, "Send measurements");
		options.addOption(option);
		CommandLineParser parser = new DefaultParser();
		
		CommandLine cmd = null;
		try {
			cmd =  parser.parse(options, args);
			if(!cmd.hasOption(OPTION_CREATE_DATABASE) && !cmd.hasOption(OPTION_SEND_MEASUREMENTS) && !cmd.hasOption(OPTION_RETRIEVE_TASKS)){ // at least one execute option must be given
				new HelpFormatter().printHelp(CMD_LINE_SYNTAX, options);
				cmd = null;
			}
		} catch (ParseException ex) {
			LOGGER.error(ex, ex);
			new HelpFormatter().printHelp(CMD_LINE_SYNTAX, options);
		}
		return cmd;
	}

	@Override
	public void close() {
		if(_client != null){
			_client.close();
			_client = null;
		}
		if(_sqliteHandler != null){
			_sqliteHandler.close();
			_sqliteHandler = null;
		}
	}
}
