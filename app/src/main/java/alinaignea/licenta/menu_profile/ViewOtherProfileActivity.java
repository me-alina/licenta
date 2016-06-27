package alinaignea.licenta.menu_profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.AsyncTask;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import alinaignea.licenta.helper.AppConfig;
import alinaignea.licenta.MainActivity;
import alinaignea.licenta.R;
import alinaignea.licenta.helper.SQLiteHandler;
import alinaignea.licenta.helper.SessionManager;

/**
 * Created by Alina Ignea on 6/27/2016.
 */
public class ViewOtherProfileActivity extends Activity{

    protected Bitmap bitmap;
    private Button btnEdit;
    private TextView txtName;
    private TextView txtEmail;
    private TextView txtPhone;
    private TextView txtCity;
    private TextView txtAbout;
    private ProgressDialog pDialog;
    protected RatingBar ratingVal;
    private ImageView picture;
    private String driverId;
    String myJSON;
    private SessionManager session;
    private SQLiteHandler db;
    int flag = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);
        Bundle b = getIntent().getExtras();
        driverId = b.getString("driverId");

        // btnEdit = (Button) findViewById(R.id.btnEdit);
        txtName = (TextView) findViewById(R.id.txtname);
        txtEmail = (TextView) findViewById(R.id.txtemail);
        txtPhone = (TextView) findViewById(R.id.txtphone);
        txtCity = (TextView) findViewById(R.id.txtcity);
        txtAbout = (TextView) findViewById(R.id.aboutInfo);
        ratingVal = (RatingBar) findViewById(R.id.ratingBar);
        picture = (ImageView) findViewById(R.id.pic);


        if (!driverId.isEmpty())
            getUserData(driverId);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_menu, menu);

        return true;

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.view:
                startActivity(new Intent(this, ViewProfileActivity.class));
                return true;
            case R.id.edit:
                startActivity(new Intent(this, EditProfileActivity.class));
                return true;
            case R.id.main:
                startActivity(new Intent(this, MainActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void getUserData(final String driverId) {

        class GetDataJSON extends AsyncTask<String, Void, String>{

            @Override
            protected String doInBackground(String... params) {



                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(AppConfig.URL_OPROFILE);

                // Depends on your web service
                httppost.setHeader("Content-type", "application/x-www-form-urlencoded");
                try {
                    ArrayList<NameValuePair> nameValuePairs;
                    nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("uid", driverId));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                } catch (UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                }
                InputStream inputStream = null;
                String result = null;
                try {
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();

                    inputStream = entity.getContent();
                    // json is UTF-8 by default
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                    StringBuilder sb = new StringBuilder();

                    String line = null;
                    while ((line = reader.readLine()) != null)
                    {
                        sb.append(line + "\n");
                    }
                    result = sb.toString();
                    Log.d("php", result);
                } catch (Exception e) {
                    // Oops
                }
                finally {
                    try{if(inputStream != null)inputStream.close();}catch(Exception squish){}
                }
                return result;
            }

            @Override
            protected void onPostExecute(String result){

                try {
                    JSONObject jObj = new JSONObject(result);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        // user profile successfully fetched

                        // Now show profile
                        String uid = jObj.getString("uid");

                        JSONObject user = jObj.getJSONObject("user");

                        String created_at = user.getString("created_at");
                        String phone = user.getString("phone");
                        String city = user.getString("city");
                        String about = user.getString("about");
                        String rating_cnt = user.getString("rating_cnt");
                        String rating_val = user.getString("rating_val");
                        String avatar = user.getString("avatar");
                        float rating_v = Float.parseFloat(rating_val);

                        txtPhone.setText(phone);
                        txtCity.setText (city);
                        txtAbout.setText(about);

                        ratingVal.setStepSize((float)0.1);
                        ratingVal.setRating(rating_v);

                        ratingVal.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                            public void onRatingChanged(RatingBar ratingBar, float rating,
                                                        boolean fromUser) {
                                if (flag==0) {
                                    final String rate = String.valueOf(rating);
                                    new AlertDialog.Builder(ViewOtherProfileActivity.this)
                                            .setTitle("Rating")
                                            .setMessage("Are you sure you want to rate this user with " + String.valueOf(rating) + " stars?")
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {

                                                    rate(driverId, rate);

                                                }
                                            })
                                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    // nimic
                                                }
                                            })
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .show();
                                    // Toast.makeText(getApplicationContext(), tripsList.get(arg2).get(TAG_UID) , Toast.LENGTH_LONG).show();
                                flag=1;
                                    ratingVal.setClickable(false);
                                    ratingVal.setIsIndicator(true);
                                }
                            }

                        });


                        new LoadImage().execute(avatar);

                    }

                    else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                //    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute();
    }


    private class LoadImage extends AsyncTask<String, String, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ViewOtherProfileActivity.this);
            pDialog.setMessage("Loading Image ....");
            pDialog.show();

        }
        protected Bitmap doInBackground(String... args) {
            try {
                bitmap = BitmapFactory.decodeStream((InputStream)new URL(args[0]).getContent());

            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap image) {

            if(image != null){
                picture.setImageBitmap(image);
                pDialog.dismiss();

            }else{

                pDialog.dismiss();
                Toast.makeText(ViewOtherProfileActivity.this, "Image Does Not exist or Network Error", Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void rate(final String driverId, final String rate){
        class GetDataJSON extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {

                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(AppConfig.URL_RATE);

                // Depends on your web service
                httppost.setHeader("Content-type", "application/x-www-form-urlencoded");
                try {
                    ArrayList<NameValuePair> nameValuePairs;
                    nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("uid", driverId));
                    nameValuePairs.add(new BasicNameValuePair("rate", rate));


                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                } catch (UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                }
                InputStream inputStream = null;
                String result = null;
                try {
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();

                    inputStream = entity.getContent();
                    // json is UTF-8 by default
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                    StringBuilder sb = new StringBuilder();

                    String line = null;
                    while ((line = reader.readLine()) != null)
                    {
                        sb.append(line + "\n");
                    }
                    result = sb.toString();
                    Log.d("php", result);
                } catch (Exception e) {
                    // Oops
                }
                finally {
                    try{if(inputStream != null)inputStream.close();}catch(Exception squish){}
                }
                return result;
            }

            @Override
            protected void onPostExecute(String result){

                try {
                    JSONObject jObj = new JSONObject(result);
                    boolean error = jObj.getBoolean("error");
                    String errorMsg = jObj.getString("message");
                    Toast.makeText(getApplicationContext(),
                            errorMsg, Toast.LENGTH_LONG).show();

                    // Check for error node in json
                    if (!error) {
                        // user profile successfully updated
                        // Now show profile
                        getUserData(driverId);


                    }

                    else {
                        // Error in updating. Get the error message
                        getUserData(driverId);

                    }
                    Toast.makeText(getApplicationContext(),
                            errorMsg, Toast.LENGTH_LONG).show();

                } catch (JSONException e) {
                    // JSON error
                    getUserData(driverId);
                    e.printStackTrace();
                  //  Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute();
    }





}