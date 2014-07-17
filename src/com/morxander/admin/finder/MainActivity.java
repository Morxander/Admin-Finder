package com.morxander.admin.finder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;


 // Copyright 2014 Morad Edwar
 // www.morad-edwar.com
 // Admin Finder for android is free software: you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation, either version 3 of the License, or
 // (at your option) any later version.
 // Admin Finder for android is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 // You should have received a copy of the GNU General Public License
 // along with Admin Finder for android.  If not, see <http://www.gnu.org/licenses/>.
 
public class MainActivity extends Activity {
	ArrayList<String> admins;
	EditText txt_box_url;
	ScrollView scroll;
	Button bt_find;
	TextView txt_log;
	String[] paths;
	String current_url;
	int num;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initializeVars();
		setOnClick();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	// initialize vars & ui components
	private void initializeVars() {
		setContentView(R.layout.activity_main);
		txt_box_url = (EditText) findViewById(R.id.autoCompleteTextView1);
		bt_find = (Button) findViewById(R.id.button1);
		txt_log = (TextView) findViewById(R.id.textView1);
		scroll = (ScrollView) findViewById(R.id.scroll);
		admins = new ArrayList<String>();
		num = 0;
		current_url = new String();
		paths = getResources().getStringArray(R.array.php);
	}

	// setting the onclick listener
	private void setOnClick() {
		bt_find.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!txt_box_url.getText().toString().equals(""))
				{
					if (!txt_box_url.getText().toString().startsWith("http://")
							&& !txt_box_url.getText().toString()
									.startsWith("https://")) {
						txt_box_url.setText("http://"
								+ txt_box_url.getText().toString());
					}
					bt_find.setEnabled(false);
					new RequestTask().execute(String.valueOf(txt_box_url.getText()
							.toString() + "/" + paths[num]));
				}else if(!haveNetworkConnection()){
					txt_log.setText("You don't have an Internet Connection");
				}else{
					txt_log.setText("Please Enter a URL");
				}
			}
		});
	}

	// Method For Internet Checking
	private boolean haveNetworkConnection() {
		boolean haveConnectedWifi = false;
		boolean haveConnectedMobile = false;

		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] netInfo = cm.getAllNetworkInfo();
		for (NetworkInfo ni : netInfo) {
			if (ni.getTypeName().equalsIgnoreCase("WIFI"))
				if (ni.isConnected())
					haveConnectedWifi = true;
			if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
				if (ni.isConnected())
					haveConnectedMobile = true;
		}
		return haveConnectedWifi || haveConnectedMobile;
	}
	
	// the AsynTask class which will make the http requests to avoid
	// NetworkOnMainThreadException
	class RequestTask extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... uri) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response;
			String responseString = null;
			String status = null;
			try {
				response = httpclient.execute(new HttpGet(uri[0]));
				StatusLine statusLine = response.getStatusLine();
				current_url = uri[0];
				if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					response.getEntity().writeTo(out);
					out.close();
					responseString = out.toString();
					status = "#200";
					admins.add(uri[0]);
				} else {
					// Closes the connection.
					response.getEntity().getContent().close();
					status = "#404";
					throw new IOException(statusLine.getReasonPhrase());
				}
			} catch (ClientProtocolException e) {
				// TODO Handle problems..
			} catch (IOException e) {
				// TODO Handle problems..
			}
			return status;
		}

		@Override
		protected void onPostExecute(String result) {
			num++;
			txt_log.append(String.valueOf(Calendar.getInstance().get(
					Calendar.MINUTE)
					+ ":" + Calendar.getInstance().get(Calendar.SECOND))
					+ " ~$ " + current_url + " : " + result + "\n");
			scroll.smoothScrollTo(0, txt_log.getHeight());
			if (num < 149) {
				new RequestTask().execute(String.valueOf(txt_box_url.getText()
						.toString() + "/" + paths[num]));
			} else {
				txt_log.setText(String.valueOf("Pages Found :" + admins.size()) + "\n");
				
				for(int i=0;i<admins.size();i++)
				{
					txt_log.append(admins.get(i) + "\n");
				}
				// clear everything back
				scroll.smoothScrollTo(0, txt_log.getHeight());
				bt_find.setEnabled(true);
				txt_box_url.setText("");
				admins.clear();
				current_url = "";
				num = 0;
			}
			super.onPostExecute(result);

		}
	}
}
