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
package otula.apilta.raspi.config.datatypes;

import com.google.gson.annotations.SerializedName;

/**
 * 
 * 
 */
public class RaspiIni {
	@SerializedName(value=Definitions.JSON_CROP_LIMITS)
	private Boolean _cropLimits = null;
	@SerializedName(value=Definitions.JSON_LINE_DOWN)
	private Integer _lineDown = null;
	@SerializedName(value=Definitions.JSON_LINE_DOWN_LIMIT)
	private Integer _lineDownLimit = null;
	@SerializedName(value=Definitions.JSON_LINE_LEFT_LIMIT)
	private Integer _lineLeftLimit = null;
	@SerializedName(value=Definitions.JSON_LINE_RIGHT_LIMIT)
	private Integer _lineRightLimit = null;
	@SerializedName(value=Definitions.JSON_LINE_UP)
	private Integer _lineUp = null;
	@SerializedName(value=Definitions.JSON_LINE_UP_LIMIT)
	private Integer _lineUpLimit = null;
	@SerializedName(value=Definitions.JSON_AREA_THRESHOLD_MIN)
	private Integer _areaThresholdMin = null;
	@SerializedName(value=Definitions.JSON_AREA_THRESHOLD_MAX)
	private Integer _areaThresholdMax = null;
	@SerializedName(value=Definitions.JSON_TASK_ID)
	private String _taskId = null;
	
	/**
	 * @return the lineDown
	 */
	public Integer getLineDown() {
		return _lineDown;
	}
	
	/**
	 * @param lineDown the lineDown to set
	 */
	public void setLineDown(Integer lineDown) {
		_lineDown = lineDown;
	}
	
	/**
	 * @return the lineDownLimit
	 */
	public Integer getLineDownLimit() {
		return _lineDownLimit;
	}
	
	/**
	 * @param lineDownLimit the lineDownLimit to set
	 */
	public void setLineDownLimit(Integer lineDownLimit) {
		_lineDownLimit = lineDownLimit;
	}
	
	/**
	 * @return the lineUp
	 */
	public Integer getLineUp() {
		return _lineUp;
	}
	
	/**
	 * @param lineUp the lineUp to set
	 */
	public void setLineUp(Integer lineUp) {
		_lineUp = lineUp;
	}
	
	/**
	 * @return the lineUpLimit
	 */
	public Integer getLineUpLimit() {
		return _lineUpLimit;
	}
	
	/**
	 * @param lineUpLimit the lineUpLimit to set
	 */
	public void setLineUpLimit(Integer lineUpLimit) {
		_lineUpLimit = lineUpLimit;
	}
	
	/**
	 * @return the areaThresholdMin
	 */
	public Integer getAreaThresholdMin() {
		return _areaThresholdMin;
	}
	
	/**
	 * @param areaThresholdMin the areaThresholdMin to set
	 */
	public void setAreaThresholdMin(Integer areaThresholdMin) {
		_areaThresholdMin = areaThresholdMin;
	}
	
	/**
	 * @return the areaThresholdMax
	 */
	public Integer getAreaThresholdMax() {
		return _areaThresholdMax;
	}
	
	/**
	 * @param areaThresholdMax the areaThresholdMax to set
	 */
	public void setAreaThresholdMax(Integer areaThresholdMax) {
		_areaThresholdMax = areaThresholdMax;
	}
	
	/**
	 * @return the taskId
	 */
	public String getTaskId() {
		return _taskId;
	}
	
	/**
	 * @param taskId the taskId to set
	 */
	public void setTaskId(String taskId) {
		_taskId = taskId;
	}
	
	/**
	 * @return the lineLeftLimit
	 */
	public Integer getLineLeftLimit() {
		return _lineLeftLimit;
	}

	/**
	 * @param lineLeftLimit the lineLeftLimit to set
	 */
	public void setLineLeftLimit(Integer lineLeftLimit) {
		_lineLeftLimit = lineLeftLimit;
	}

	/**
	 * @return the lineRightLimit
	 */
	public Integer getLineRightLimit() {
		return _lineRightLimit;
	}

	/**
	 * @param lineRightLimit the lineRightLimit to set
	 */
	public void setLineRightLimit(Integer lineRightLimit) {
		_lineRightLimit = lineRightLimit;
	}

	/**
	 * @return the cropLimits
	 */
	public Boolean getCropLimits() {
		return _cropLimits;
	}

	/**
	 * @param cropLimits the cropLimits to set
	 */
	public void setCropLimits(Boolean cropLimits) {
		_cropLimits = cropLimits;
	}

	/**
	 * for sub-classing, use static
	 * 
	 * @return true if valid
	 */
	protected boolean isValid() {
		return (_lineDown != null || _lineDownLimit != null || _lineUpLimit != null || _lineUp != null || _areaThresholdMax != null || _areaThresholdMin != null || _taskId != null || _lineLeftLimit != null || _lineRightLimit != null || _cropLimits != null); // at least one configuration value must be given
	}
	
	/**
	 * 
	 * @param ini
	 * @return true if valid and not null
	 */
	public static boolean isValid(RaspiIni ini) {
		return (ini != null && ini.isValid());
	}
}
