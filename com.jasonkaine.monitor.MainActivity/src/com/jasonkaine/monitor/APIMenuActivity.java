package com.jasonkaine.monitor;

import com.jasonkaine.monitor.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class APIMenuActivity extends Activity {
	public static final String PREFS_NAME = "MyPrefsFile";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_account);
        
        EditText api_key = (EditText) findViewById(R.id.api_key);
        
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        api_key.setHint(settings.getString("api_key",null));
     

        
    }
    
    public void submit(View view) {
    	EditText api_key = (EditText) findViewById(R.id.api_key);
    
   
    String string = api_key.getText().toString();
    	if(!(string.length() == 0)){
    		
    	
        
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("api_key", string);
        editor.putBoolean("first", false);
        editor.commit();
        
        Toast.makeText(getApplicationContext(), "Key is now: " + string, Toast.LENGTH_SHORT).show();
    	}
      
    }

}
