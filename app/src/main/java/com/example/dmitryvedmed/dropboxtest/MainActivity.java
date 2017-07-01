package com.example.dmitryvedmed.dropboxtest;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session;
import com.dropbox.client2.session.TokenPair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;


public class MainActivity extends AppCompatActivity {

    final static private String APP_KEY = "7yoho9g2mk2n4h6";
    final static private String APP_SECRET = "7yoho9g2mk2n4h6";
    private final static String DROPBOX_NAME = "dropbox_prefs";

    final static public String DROPBOX_APP_KEY = "AppKey";


    final static public String DROPBOX_APP_SECRET = "AppSecret";

    private DropboxAPI<AndroidAuthSession> mDBApi;


    final static public Session.AccessType ACCESS_TYPE = Session.AccessType.DROPBOX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session;

        SharedPreferences prefs = getSharedPreferences(DROPBOX_NAME, 0);
        String key = prefs.getString(DROPBOX_APP_KEY, null);
        String secret = prefs.getString(DROPBOX_APP_SECRET, null);

        if (key != null && secret != null) {
            AccessTokenPair tokenPair = new AccessTokenPair(key, secret);
            session = new AndroidAuthSession(appKeys, tokenPair);
        } else {
            session = new AndroidAuthSession(appKeys);
        }

        mDBApi = new DropboxAPI<AndroidAuthSession>(session);
    }

    public void onClick(View v) throws FileNotFoundException, DropboxException {
        switch (v.getId()) {
            case R.id.btnLogin:
                mDBApi.getSession().startOAuth2Authentication(MainActivity.this);
                break;
            case R.id.btnUpload:
                new Upload().execute();
                break;
        }
    }

    protected void onResume() {
        super.onResume();

        AndroidAuthSession session = mDBApi.getSession();
        if (session.authenticationSuccessful()) {
            try {
                // Required to complete auth, sets the access token on the session
                session.finishAuthentication();

                String accessToken = mDBApi.getSession().getOAuth2AccessToken();
                Log.i("TAG", accessToken);

                TokenPair tokens = session.getAccessTokenPair();
                if (tokens != null)
                    storeKeys(tokens.key, tokens.secret);
            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }
        }
    }

    private void storeKeys(String key, String secret) {
        SharedPreferences prefs = getSharedPreferences(DROPBOX_NAME, 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(DROPBOX_APP_KEY, key);
        edit.putString(DROPBOX_APP_SECRET, secret);
        edit.commit();
    }

    public class Upload extends AsyncTask<Void, Long, String> {

        @Override
        protected String doInBackground(Void... voids) {
            DropboxAPI.Entry response = null;

            try {
                File file = new File("./sdcard/img.jpg");
                FileInputStream fileInputStream = new FileInputStream(file);

                response = mDBApi.putFile("/image12.jpg", fileInputStream,
                        file.length(), null, null);


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (DropboxException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(s!=null && s.isEmpty() == false);
            Toast.makeText(getApplicationContext(), "File uploaded", Toast.LENGTH_SHORT).show();
        }
    }
}
