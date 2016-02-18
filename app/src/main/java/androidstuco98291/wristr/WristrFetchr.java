package androidstuco98291.wristr;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

public class WristrFetchr extends AppCompatActivity {

    private ProgressDialog mStartupDialog;
    private TextView mPName;
    private JSONArray mJSONArr = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPName = (TextView)this.findViewById(R.id.pname);

        /* We run this AsyncTask to make our API calls */
    	/* The AsyncTask also populates our TextView and handles the heavy lifting */

        new LongOperation().execute();

        setContentView(R.layout.activity_wristr_fetchr);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_wristr_fetchr, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* We make API calls in this class */
    private class LongOperation extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            /* When the app is loading data, we show the user a progress dialog box
             * Shouldn't take more than 2 seconds if connected to internet
             */
            super.onPreExecute();
            mStartupDialog = ProgressDialog.show(WristrFetchr.this, "",
                    getString(R.string.loading), true);
            mStartupDialog.show();
        }

        @Override
        protected void onPostExecute(String returned) {
            /* When the app is done loading everything, dismiss the
             * progress dialog box
             */
            if (mStartupDialog != null && mStartupDialog.isShowing()) {
                mStartupDialog.dismiss();
            }

            ConnectivityManager cm =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo == null || !(netInfo.isConnectedOrConnecting())) {
                new AlertDialog.Builder(WristrFetchr.this)
                        .setTitle(R.string.network_issue_title)
                        .setMessage(R.string.network_issue_body)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }

            mPName.setText(mJSONArr.toString());

        }

        @Override
        protected String doInBackground(String... params) {
            JSONArray returnedJSON = new JSONArray();
            AtomicReference<BufferedReader> readIn;
            readIn = new AtomicReference<>();
            String result;

            try {
                // Make a well-formed HTTP GET request
                URL url = new URL("http://privacygrade.org/api/v1/apps.json?q=com.facebook.katana");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(1000);
                connection.setRequestMethod("GET");
                connection.connect();

                // input stream is where received information goes
                InputStream inputStream = connection.getInputStream();

                result = getThingsFromInternet(readIn, inputStream, false);

                try {
                    returnedJSON = new JSONArray(result);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mJSONArr = returnedJSON;
            return returnedJSON.toString();
        }

        private String getThingsFromInternet(AtomicReference<BufferedReader> readIn, InputStream inputStream, boolean b) {
            String result;
            if (inputStream != null) {
                readIn.set(new BufferedReader(new InputStreamReader(inputStream)));
            }
            StringBuilder buffer = new StringBuilder("");
            String line;
            String newLine = System.getProperty("line.separator");

            try {
                while((line = readIn.get().readLine()) != null) {
                    buffer.append(line).append(newLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                readIn.get().close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            result = buffer.toString();
            return result;
        }
    }
}
