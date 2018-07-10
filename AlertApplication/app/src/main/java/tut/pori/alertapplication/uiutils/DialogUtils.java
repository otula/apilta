/*
 * Copyright 2014 Tampere University of Technology, Pori Department
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

import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import tut.pori.alertapplication.R;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.util.Log;

/**
 *
 */
public final class DialogUtils {
	private static final String CLASS_NAME = DialogUtils.class.toString();

	/**
	 *
	 */
	private DialogUtils(){
		// nothing needed
	}

	/**
	 * 
	 * @param activity caller activity
	 * @param resId description string resource id
	 * @param abort if true, the activity will be aborted after the user has clicked OK
	 */
	public static void showErrorDialog(final Activity activity, final int resId, final boolean abort){
		Builder builder = new Builder(activity);
		builder.setMessage(resId).setTitle(R.string.title_dialog_error);
		builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				if(abort){
					activity.finish();
				}
			}
		});
		builder.setCancelable(false);
		builder.create().show();
	}

	/**
	 * 
	 * @param activity
	 * @param resId description resource id
	 * @param listener
	 * @param showCancel
	 * @param tag tag that defines this dialog
	 * @return
	 */
	public static void showConfirmationDialog(final Activity activity, final int resId, final DialogListener listener, boolean showCancel, final int tag){
		Builder builder = new Builder(activity){

		};
		builder.setTitle(R.string.title_dialog_confirmation);
		builder.setMessage(resId);
		builder.setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {		
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				listener.dialogClosed(DialogListener.ConfirmationStatus.ACCEPTED, tag);
			}
		});
		builder.setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {		
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				listener.dialogClosed(DialogListener.ConfirmationStatus.REJECTED, tag);
			}
		});
		if(showCancel){
			builder.setNeutralButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					listener.dialogClosed(DialogListener.ConfirmationStatus.CANCELLED, tag);
				}
			});
		}

		builder.setCancelable(false);
		builder.show();
	}
	
	/**
	 * 
	 * @param activity
	 * @param values list of values, in the form of <IDENTIFIER_OBJECT, value>, the list of values will always be shown in an alphabetical order
	 * @param listener
	 * @param selectedItem if null, the first value of the list will be chosen by default
	 * @param tag tag that defines this dialog
	 */
	public static <T> void showSelectionDialog(final Activity activity, final Map<T,String> values, final DialogListener listener, T selectedItem, final int tag){
		if(values == null || values.isEmpty()){
			Log.w(CLASS_NAME, "Empty value list given for selection.");
			listener.selectionClosed(null, null, tag);
			return;
		}
		
		Builder sel = new Builder(activity);
		sel.setTitle(R.string.title_dialog_selection);
		
		final LinkedList<T> idList = new LinkedList<>();
		final LinkedList<String> valueList = new LinkedList<>();
		for(Entry<T,String> e : values.entrySet()){	// separate the map and sort by value
			String value = e.getValue();
			for(int i=0,count=valueList.size();i<count;++i){
				if(value.compareTo(valueList.get(i)) < 0){
					valueList.add(i, value);
					idList.add(i,e.getKey());
					value = null;
					break;
				}
			}
			if(value != null){
				valueList.add(value);
				idList.add(e.getKey());
			}
		}
		
		sel.setSingleChoiceItems(valueList.toArray(new String[values.size()]), (selectedItem == null ? 0 : idList.indexOf(selectedItem)), new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				listener.selectionClosed(idList.get(which),valueList.get(which), tag);
			}
		});
		sel.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {		
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				listener.selectionClosed(null, null, tag);
			}
		});
		sel.create().show();
	}

	/**
	 * 
	 *
	 */
	public interface DialogListener{
		/**
		 * 
		 *
		 */
		public enum ConfirmationStatus{
			ACCEPTED,
			REJECTED,
			CANCELLED
		}

		/**
		 * 
		 * @param status
		 * @param tag tag that defines this dialog
		 */
		public void dialogClosed(ConfirmationStatus status, int tag);
		/**
		 * 
		 * @param id the selected id or null if dialog cancelled
		 * @param text the selected text or null if dialog cancelled
		 * @param tag tag that defines this dialog
		 */
		public <T> void selectionClosed(T id, String text, int tag);
	}
}
