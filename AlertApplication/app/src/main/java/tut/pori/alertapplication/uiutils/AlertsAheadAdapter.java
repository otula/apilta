/*
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
package tut.pori.alertapplication.uiutils;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.Set;

import tut.pori.alertapplication.datatypes.Alert;

/**
 *
 */
public class AlertsAheadAdapter {
	private Context _context = null;
	private LinearLayout _layout = null;

	/**
	 *
	 * @param context
	 * @param layout
	 */
	public AlertsAheadAdapter(Context context, LinearLayout layout) {
		_context = context;
		_layout = layout;
	}

	/**
	 *
	 * @param alerts
	 */
	public void setAlerts(Set<Alert.AlertType> alerts) {
		_layout.removeAllViews();
		if(alerts != null){
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
			for(Alert.AlertType type : alerts){
				ImageView view = new ImageView(_context);
				view.setLayoutParams(params);
				view.setImageResource(type.getIconImageResource());
				_layout.addView(view);
			}
		}
	}
}
