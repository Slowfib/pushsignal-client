package com.pushsignal.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.pushsignal.Constants;
import com.pushsignal.NotificationDisplay;
import com.pushsignal.R;
import com.pushsignal.adapters.EventListAdapter;
import com.pushsignal.asynctasks.RestCallAsyncTask;
import com.pushsignal.observers.AppObservable;
import com.pushsignal.observers.EventListObserver;
import com.pushsignal.rest.RestClient;
import com.pushsignal.xml.simple.EventDTO;

public class EventListActivity extends Activity {
	private static final int PROGRESS_DIALOG = 0;

	private List<EventDTO> eventList;

	private EventListAdapter adapter;

	private ListView eventListView;

	private ProgressDialog progressDialog;

	private final Handler handleEventsChanged = new Handler() {
		@Override
		public void handleMessage(final Message msg) {
			adapter.notifyDataSetChanged();
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_list);

		// Obtain handles to UI objects
		eventListView = (ListView) findViewById(R.id.eventList);

		// Register handler for UI elements
		eventListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(final AdapterView<?> parent, final View view, final int position,
					final long id) {
				Log.d(Constants.CLIENT_LOG_TAG, "mEventList clicked");
				launchEventViewer(eventList.get(position));
			}
		});

		new RefreshListAsyncTask(this).execute();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.refresh_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.refresh:
			Log.d(Constants.CLIENT_LOG_TAG, "refresh menu button clicked");
			new RefreshListAsyncTask(this).execute();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected Dialog onCreateDialog(final int id) {
		switch(id) {
		case PROGRESS_DIALOG:
			progressDialog = new ProgressDialog(this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setMessage("Loading events...");
			return progressDialog;
		default:
			return null;
		}
	}

	/**
	 * Launches the EventViewer activity to show information about a particular event.
	 */
	private void launchEventViewer(final EventDTO event) {
		final Intent i = new Intent(this, EventViewerActivity.class);
		i.putExtra("event", event);
		startActivity(i);
	}

	private class RefreshListAsyncTask extends RestCallAsyncTask<Void> {

		public RefreshListAsyncTask(final Context context) {
			super(context);
		}

		@Override
		protected void onPreExecute() {
			// Show progress dialog
			showDialog(PROGRESS_DIALOG);
		}

		@Override
		protected void doRestCall(final RestClient restClient, final Void... params) throws Exception {
			eventList = new ArrayList<EventDTO>(restClient.getAllEvents().getEvents());
		}

		@Override
		protected void onSuccess(final Context context) {
			adapter = new EventListAdapter(context, R.layout.event_list_item, eventList, getLayoutInflater());
			eventListView.setAdapter(adapter);
			AppObservable.getInstance().addObserver(new EventListObserver(handleEventsChanged, eventList));
			dismissDialog(PROGRESS_DIALOG);
		}

		@Override
		protected void onException(final Context context, final Exception ex) {
			Log.e(Constants.CLIENT_LOG_TAG, ex.getMessage());
			NotificationDisplay.showError(context, ex.getMessage());
			dismissDialog(PROGRESS_DIALOG);
		}
	}
}