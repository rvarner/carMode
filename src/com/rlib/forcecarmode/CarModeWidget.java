/**
 * 
 */
package com.rlib.forcecarmode;

import android.app.PendingIntent;
import android.app.Service;
import android.app.UiModeManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * @author reid
 *
 */
public class CarModeWidget extends AppWidgetProvider {
	
	private static final String TAG = "CarModeWidget";
	public static String CAR_MODE_BUTTON_PRESS = "CarModeButtonPress";
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,int[] appWidgetIds) {
		Log.d(TAG,"Starting the UpdateService");
		context.startService(new Intent(context,UpdateService.class));
    }
	

	/* (non-Javadoc)
	 * @see android.appwidget.AppWidgetProvider#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG,"Intent recv'd: URI:" + intent.toURI());
		
		if(intent.getAction().equals(CAR_MODE_BUTTON_PRESS)) {
			toggleCarMode(context);
		}
		
		super.onReceive(context, intent);
	}
	
	private void toggleCarMode(Context context) {
		Log.d(TAG, "Toggling Car Mode");
        
		Object uiModeManagerObj = context.getSystemService(Context.UI_MODE_SERVICE);
		if(uiModeManagerObj == null) {
			Log.e(TAG, "Could not retrieve UIModeManager");
			return;
		}
		
		// Cast the ui manager
		UiModeManager uiManager = (UiModeManager) uiModeManagerObj;
		
		// Get the current mode
		int currentMode = uiManager.getCurrentModeType();
		
		// Log the current mode
		Log.d(TAG,"Current mode is: " + currentMode);

		switch(currentMode) {
		case Configuration.UI_MODE_TYPE_CAR:
			Log.i(TAG, "Disabling car mode");
			uiManager.disableCarMode(0);
			break;
		default:
			Log.i(TAG, "Enabling car mode");
			uiManager.enableCarMode(UiModeManager.ENABLE_CAR_MODE_GO_CAR_HOME);
		}
	}

	
	
	public static class UpdateService extends Service {
		
        @Override
        public void onStart(Intent intent, int startId) {
            Log.d(TAG, "onStart()");

            // Build the widget update for today
            RemoteViews updateViews = buildUpdate(this);
            Log.d(TAG, "update built");
            
            // Push update for this widget to the home screen
            ComponentName thisWidget = new ComponentName(this, CarModeWidget.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            manager.updateAppWidget(thisWidget, updateViews);
            Log.d(TAG, "widget updated");
        }
		
		private RemoteViews buildUpdate(Context context) {
			
			// Get the remote views
			RemoteViews views = new RemoteViews(context.getPackageName(),R.layout.main);
			
			// Set the image we're going to use
			//views.setImageViewResource(R.id.toggle_button, enabled ? R.drawable.fourg_icon_on : R.drawable.fourg_icon_off);
			
			// Create an intent
			Intent i = new Intent(context,CarModeWidget.class);
			i.setAction(CAR_MODE_BUTTON_PRESS);
			
			// Create a pending intent
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
			
			// Set the pending intent
			views.setOnClickPendingIntent(R.id.toggle_button, pendingIntent);
			
			Intent nfcI = new Intent(context,CarModeWidget.class);
			nfcI.setAction("DockMode");

			// Tell myself about it
			Log.d(TAG,"Finished building update");
			
			return views;
		}

		@Override
		public IBinder onBind(Intent i) {
			return null;
		}
	}
	
}
