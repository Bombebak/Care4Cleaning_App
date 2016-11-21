package com.example.den_f.mandatoryassignment_care4cleaning;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

import static android.content.Context.MODE_PRIVATE;


/**
 * Created by Den_F on 24-10-2016.
 */

public class UploadToServer extends AsyncTask<String, Integer, String> {

    Context context;
    RequestQueue queue;
    public AsyncResponse delegate = null;
    public final static String URL_BASE = "https://185.93.195.194/";
    public final static String URL_CREATEUSER = "https://185.93.195.194/createuser.php";
    public final static String URL_UPLOADIMAGE = "https://185.93.195.194/uploadpic.php";
    public static String errorMessage = "";
    private ProgressDialog dialog;
    private MainActivity activity;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition tryAgain = lock.newCondition();
    private volatile boolean finished = false;

    public UploadToServer(Context c, AsyncResponse delegate)
    {
        context = c;
        dialog = new ProgressDialog(context);
        this.delegate = delegate;
    }

    public UploadToServer(Context c, MainActivity activity)
    {
        context = c;
        dialog = new ProgressDialog(context);
        this.activity = activity;
    }


    protected void onPreExecute() {
        super.onPreExecute();
        dialog.setMessage(activity.getDialogMessage());
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setIndeterminate(true);
        dialog.setProgress(0);
        dialog.show();
    }


    @Override
    protected String doInBackground(String... params) {
        try {
            lock.lockInterruptibly();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        do {
            //Gets the command string
            String command = params[0];
            int i = 0;
            //Checks if the command is imageUpload or createUser
            if (command == "imageUpload") {
                try {
                    Thread.sleep(6000);
                    dialog.setProgress(i);
                    //Retrieves all the parameters and executes the uploadPicture method
                    String caseIdStr = params[1];
                    int caseID = Integer.parseInt(caseIdStr);
                    String bitmap = params[2];
                    String token = params[3];
                    String description = params[4];
                    String imageName = params[5];
                    uploadPicture(bitmap, token, caseID, description, imageName);
                    publishProgress(100);
                    return "The picture is uploaded";
                }
                catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else if (command == "createUser") {
                try {
                    Thread.sleep(5000);
                    dialog.setProgress(i);
                    //Retrieves all the parameters and executes the createUser method
                    String username = params[1];
                    String token = params[2];
                    CreateUser(username, token);
                    publishProgress(100);
                    return "The user is uploaded";
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            try {
                tryAgain.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while(!finished);
        lock.unlock();


        return "Something went wrong";
    }


    public class CustomHostnameVerifier implements HostnameVerifier
    {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }


    public void setupSSLCertificate()
    {
        String ur = URL_BASE;
        System.out.println("setting up ssl ca");
        URL url = null;
        java.security.cert.Certificate ca = null;
        HttpsURLConnection.setDefaultHostnameVerifier(new CustomHostnameVerifier());

        HttpsURLConnection urlConnection= null;

        try {
            url = new URL(ur);
        }
        catch (MalformedURLException e)
        {
            Log.d("MalformedURL",e.toString());
        }

        // Load CAs from an InputStream
// (could be from a resource or ByteArrayInputStream or ...)
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            // new FileInputStream(R.raw.cacert);
            InputStream caInput = context.getResources().openRawResource(
                    context.getResources().getIdentifier("cacert",
                            "raw", context.getPackageName()));

            // InputStream caInput = new BufferedInputStream(new FileInputStream(R.raw.cacert));
            try {
                ca = cf.generateCertificate(caInput);
                System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
            } finally {
                caInput.close();
            }
        } catch (Exception e)
        {
            Log.d("CertificationError",e.toString());
        }
        System.out.println("factory done!");


// Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

        }
        catch (Exception e)
        {
            Log.d("KeyStoreException",e.toString());
        }
        System.out.println("KeyStore done!");

// Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

// Create an SSLContext that uses our TrustManager

            SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, tmf.getTrustManagers(), null);

// Tell the URLConnection to use a SocketFactory from our SSLContext
            //   URL url = new URL("https://certs.cac.washington.edu/CAtest/");
            urlConnection =
                    (HttpsURLConnection) url.openConnection();
            urlConnection.setSSLSocketFactory(sslcontext.getSocketFactory());
        }
        catch (Exception e)
        {
            Log.d("TrustManagerException",e.toString());
        }
        System.out.println("Trustmanager done!");
        if ("https".equals(url.getProtocol())) {
            // ((HttpsURLConnection)urlConnection).setSSLSocketFactory(mSslSocketFactory);
            urlConnection.setHostnameVerifier(new CustomHostnameVerifier());
        }

        queue = Volley.newRequestQueue(context, new HurlStack(null, urlConnection.getSSLSocketFactory()));

    }

    public void uploadPicture(final String bitmap,final String token,
                              final int caseId, final String description,
                              final String imageName)
    {
        String url = URL_UPLOADIMAGE;
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d("Response", response);
                        activity.clearFields();
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setMessage(response)
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                        //Toast.makeText(context,response,Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
             @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse response = error.networkResponse;
                if(response != null && response.data != null){
                    switch(response.statusCode){
                        case 400:
                            errorMessage = new String(response.data);
                            // json2 = trimMessage(json2, "message");
                            if(errorMessage != null) {
                                Log.d("ErrorServerUpload", errorMessage);
                                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                builder.setMessage(errorMessage)
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });
                                AlertDialog alert = builder.create();
                                alert.show();
                                //Toast.makeText(context,errorMessage,Toast.LENGTH_LONG).show();
                            }
                            break;

                    }
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                JSONObject json = new JSONObject();

                try {
                    json.put("name", imageName);
                    json.put("token", token);
                    json.put("description", description);
                    json.put("case_id", caseId);
                    json.put("base64",bitmap);
                }
                catch (JSONException e)
                {
                    Log.d("JsonException",e.getMessage());
                }
                map.put("json", json.toString());
                //System.out.println("JSON:"+json.toString());
                return map;
            }
        };

        int socketTimeout = 20000;//20 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        // Add the request to the RequestQueue.

        queue.add(stringRequest);


    }

    public void CreateUser(final String userName, final String token) {
        String url = URL_CREATEUSER;
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d("Response", response);
                        SharedPreferences prefs = context.getSharedPreferences("myPrefs", MODE_PRIVATE);
                        //save the preferences.
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("token",token);
                        editor.commit();
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setMessage(response)
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                        //Toast.makeText(context,response,Toast.LENGTH_SHORT).show();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ErrorResponseServer",error.toString());
                if (error.getMessage()!=null) {
                    Log.d("ErrorResponseServer", error.getMessage().toString());
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setMessage(errorMessage)
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                    //Toast.makeText(context,error.getMessage().toString(),Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                JSONObject json = new JSONObject();

                try {
                    json.put("token", token);
                    json.put("username",userName);
                }
                catch (JSONException e)
                {
                    Log.d("JsonException",e.getMessage().toString());
                }
                map.put("json", json.toString());
                //System.out.println("JSON:"+json.toString());
                return map;
            }
        };

        int socketTimeout = 20000;//20 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        // Add the request to the RequestQueue.

        queue.add(stringRequest);

    }

    @Override
    protected void onProgressUpdate(Integer... values) {

    }

    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        dialog.hide();
        dialog.dismiss();

    }
}
