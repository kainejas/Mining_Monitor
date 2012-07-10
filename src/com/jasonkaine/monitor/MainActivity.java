package com.jasonkaine.monitor;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class MainActivity extends Activity {
	private AdView adView;
	private final String MY_AD_UNIT_ID = "a14ff78e6c7ed6c";
	public static final String PREFS_NAME = "MyPrefsFile";
	private String account, hash_rate, estimated, confirmed, total, unconfirmed, alive, api, duration;
	private String[] worker_information;
	int worker_amount = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        adView = (AdView) findViewById(R.id.adView);
        adView.loadAd(new AdRequest());
       
       
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        if(first_time_check()) {
        	api = "";
        	Toast.makeText(getApplicationContext(), "Account Not Set Up! Go to Settings!", Toast.LENGTH_LONG).show();
        }
        {
        	api = settings.getString("api_key", null);
        }
      
       
        
       
        		
        //This runs a separate thread from the main thread to fetch the information from the pool.
        new Thread(new Runnable() {
	        public void run() {
	        	
	        	DownloadFromUrl("https://mining.bitcoin.cz/accounts/profile/json/" + api);
	            
	            }
	 }).start();
        

        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
    	Intent intent = new Intent(this, APIMenuActivity.class);
        switch (item.getItemId()) {
            case R.id.menu_settings:
                startActivity(intent);
                return true;
         
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void onDestroy() {
      if (adView != null) {
        adView.destroy();
      }
      super.onDestroy();
    }
  


    public void DownloadFromUrl(String DownloadUrl) {
    		String jString = "";
    		String stats_string = "";
    	   try {
    		   /*
    	           File root = android.os.Environment.getExternalStorageDirectory();               

    	           File dir = new File (root.getAbsolutePath() + "/xmls");
    	           if(dir.exists()==false) {
    	                dir.mkdirs();
    	           }

*/
    	           URL url = new URL(DownloadUrl); 
    	           URL stats = new URL("http://mining.bitcoin.cz/stats/json/");

    	           long startTime = System.currentTimeMillis();
    	           Log.d("DownloadManager", "download begining");
    	           Log.d("DownloadManager", "download url:" + url);
    	         
    	           /* Open a connection to that URL. */
    	           URLConnection ucon =  url.openConnection();
    	           URLConnection statcon = stats.openConnection();

    	           /*
    	            * Define InputStreams to read from the URLConnection.
    	            */
    	           InputStream is = ucon.getInputStream();
    	           BufferedInputStream bis = new BufferedInputStream(is);
    	           
    	           InputStream stat_is = statcon.getInputStream();
    	           BufferedInputStream stat_buf = new BufferedInputStream(stat_is);

    	           /*
    	            * Read bytes to the Buffer until there is nothing more to read(-1).
    	            */
    	           ByteArrayBuffer baf = new ByteArrayBuffer(5000);
    	           ByteArrayBuffer stat_baf = new ByteArrayBuffer(5000);
    	           int current = 0;
    	           while ((current = bis.read()) != -1) {
    	              baf.append((byte) current);
    	           }
    	           
    	           int stat_current = 0;
    	           while((stat_current = stat_buf.read()) != -1) {
    	        	   stat_baf.append((byte) stat_current);
    	           }
    	           
    	           /* Convert the Bytes read to a String. */
    	           stats_string = new String(stat_baf.toByteArray());
    	           jString= new String(baf.toByteArray());
    	       
    	           Log.d("DownloadManager", "download ready in" + ((System.currentTimeMillis() - startTime) / 1000) + " sec");
       	           Log.d("StatsManager", stats_string);
    
    	           
    	   } catch (IOException e) {
    	       Log.d("DownloadManager", "Error: " + e);
    	   }
    	   
    	   //Parse the JSON File
    	   try{
           parse(jString, stats_string);
    	   }
    	   catch(Exception e) {
    		   Log.d("ParserManager" , "Exception: " + e);
    	   }
    	}
    public void onResume() {
    	super.onResume();
    	adView.loadAd(new AdRequest());
    	 //This runs a separate thread from the main thread to fetch the information from the pool.
        new Thread(new Runnable() {
	        public void run() {
	        	
	        	DownloadFromUrl("https://mining.bitcoin.cz/accounts/profile/json/" + api);
	            
	            }
	 }).start();
    }
    
    private void parse(String jString, String stats_string) throws Exception {
     	

    	//Create a JSONObject from String
    	JSONObject jObject = new JSONObject(jString);
    	JSONObject stat_jObject = new JSONObject(stats_string);
    	JSONObject workers = new JSONObject(jObject.getJSONObject("workers").toString());
    	
    	//Obtain the names of the workers
    	JSONArray worker_jArray = workers.names();
    	String[] worker_names = getNames(worker_jArray); 
    	worker_amount = worker_names.length;
    	
    	//Store worker info
    	JSONObject[] worker_array = new JSONObject[worker_names.length];
    	String workers_alive[] = new String[worker_names.length];
    	String workers_hashrate[] = new String[worker_names.length];
    	int total_hashrate = 0;
    	String[] worker_info = new String[worker_names.length];
    	
    	//Go through each worker and store each worker's information
    	for(int i = 0; i < worker_names.length; i++) {
    		worker_array[i] = new JSONObject(workers.getJSONObject(worker_names[i]).toString());
    		workers_alive[i] = worker_array[i].getString("alive").toString();
    		workers_hashrate[i] = worker_array[i].getString("hashrate");
    		total_hashrate+= Integer.parseInt(workers_hashrate[i]);
    		worker_info[i] = worker_names[i]+ ":    Alive: " + workers_alive[i] + "   Hashrate: " + workers_hashrate[i] + " MHash/s";
    		//Log.d("WORKERINFO", worker_names[i]+ ":    Alive:" + workers_alive[i] + "   Hashrate: " + workers_hashrate[i] + " MHashes/s");
    	}
    	worker_information = worker_info;
    	
    	//Analyze JSON
    	account = jObject.getString("username").toString();
     	hash_rate = total_hashrate + "";
     	estimated = jObject.getString("estimated_reward").toString();
     	confirmed = jObject.getString("confirmed_reward").toString();
     	unconfirmed = jObject.getString("unconfirmed_reward").toString();
     	alive = (Boolean.parseBoolean(workers_alive[0]))?"On":"Off";
     	duration = stat_jObject.getString("round_duration");
     	
     	//Format the total BTC to 8 decimal places to fit the same format as the currency
     	DecimalFormat df = new DecimalFormat();
     	df.setMaximumFractionDigits(8);
     	total = Double.valueOf(df.format(Double.parseDouble(confirmed) + Double.parseDouble(unconfirmed))) + "";

    	//Set text of TextViews
    	runOnUiThread(new Runnable() {
       
   	    
		public void run() {
   	    	//Instantiate all of the TextViews in the UI needed to be changed
   	    	TextView username = (TextView) findViewById(R.id.username);
   	     	TextView hashrate = (TextView) findViewById(R.id.hashrate);
   	     	TextView estimated_reward = (TextView) findViewById(R.id.estimated_reward);
   	     	TextView total_reward= (TextView) findViewById(R.id.total_reward);
   	     	TextView worker = (TextView) findViewById(R.id.worker);
   	     	
   	     	//Set the text to be put in the TextView that shows the workers
   	     	String worker_text ="";
   	     	for(int i = 0; i < worker_amount; i++ ) {
   	    	 
   	     		worker_text+= (i == worker_amount)?worker_information[i]:worker_information[i]+"\n";
   	     		worker.setText(worker_text);
   	     }
   	     
   	     	//Set text of the rest of the TextViews
   	    	username.setText("Account: " + account + "\t Round duration: " + duration);
   	    	hashrate.setText("Hashrate: " + hash_rate);
   	    	estimated_reward.setText("Estimated: " + estimated + " BTC");
   	    	total_reward.setText("Total: " + total + " BTC");
   	   
   	    	Toast.makeText(getApplicationContext(), "Refresh successful!", Toast.LENGTH_SHORT).show();
   	    	Log.d("ADMANAGER", adView.toString());
		}
   	});
    
    
   
    }
    
    //This method is used to get the Names of all of the workers from the JSONArray
    public String[] getNames(JSONArray array) {
    	String[] actual_names = new String[array.length()];
    	try{
    	for(int i = 0; i < array.length(); i++) {
    		actual_names[i] = (String)array.get(i);
    	//	Log.d("Names_Manager",actual_names[i]);
    	}
    	}
    	catch(Exception e) {
    		Log.d("getNames", e.toString());
    	}
    	return actual_names;
    }
    

    
    public void refresh(View view) {
    	 SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    	 api = settings.getString("api_key", null);
    	
    	 new Thread(new Runnable() {
    	        public void run() {
    	        	DownloadFromUrl("https://mining.bitcoin.cz/accounts/profile/json/" + api);
    	            
    	            }
    	 }).start();
    	
    	
    
    }
    
    private boolean first_time_check() {
        /* 
         * Checking Shared Preferences if the user had pressed 
         * the remember me button last time he logged in
         * */
    	 SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        boolean first = settings.contains("first");
        if((first)){
            return false;
        }
        else 
            return true;
    }

    		}


    
   
    

