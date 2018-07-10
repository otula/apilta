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
import android.widget.Toast;

import java.util.Set;

import tut.pori.alertapplication.R;
import tut.pori.alertapplication.datatypes.Alert;
import tut.pori.alertapplication.utils.SoundHandler;

/**
 *
 */
public class AlertsInRangeAdapter {
	private Set<Alert.AlertType> _alerts = null;
	private Context _context = null;
	private LinearLayout _layout = null;
	private SoundHandler _soundHandler = null;

	/**
	 *
	 * @param context
	 * @param layout
	 * @param handler
	 */
	public AlertsInRangeAdapter(Context context, LinearLayout layout, SoundHandler handler) {
		_context = context;
		_layout = layout;
		_soundHandler = handler;
	}

	/**
	 *
	 * @param alerts
	 */
	public void setAlerts(Set<Alert.AlertType> alerts) {
		if(alerts != null){
            if(_alerts != null && _alerts.containsAll(alerts)){ //new list is the same as old one
                return;
            }
            _layout.removeAllViews();
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			for(Alert.AlertType type : alerts){
				ImageView view = new ImageView(_context);
				view.setLayoutParams(params);
				view.setImageResource(type.getListImageResource());
				view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				_layout.addView(view);
			}

			_soundHandler.play(SoundHandler.EventType.NEW_ALERTS_IN_RANGE);
			Toast.makeText(_context, R.string.notification_new_alerts, Toast.LENGTH_SHORT).show();
		}else if(_alerts == null) { // the previous list was empty, and the new list is empty as well
            return;
        }else{
            _layout.removeAllViews();
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			ImageView view = new ImageView(_context);
			view.setLayoutParams(params);
			view.setImageResource(R.drawable.no_active);
			view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			_layout.addView(view);
			_soundHandler.play(SoundHandler.EventType.NO_ALERTS_IN_RANGE);
			Toast.makeText(_context, R.string.notification_no_alerts, Toast.LENGTH_SHORT).show();
		}
		_alerts = alerts;
	}
}
