package com.example.callredirectapp;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.ImageButton;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CallDivertActivity extends Activity {

    private static final String TAG = "CallDivertActivity";
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

        // Allow network operations on main thread (for debugging, remove in production)
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.HONEYCOMB_MR2) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build());
        }

        String userId = getUserIdFromLoginApp();
        Log.d(TAG, "Fetched USER_ID from login app: " + userId);

        ImageButton answerButton = findViewById(R.id.answerButton);
        ImageButton redirectButton = findViewById(R.id.redirectButton);
        ImageButton hangupButton = findViewById(R.id.rejectButton);

        executor = Executors.newSingleThreadExecutor();  // Create a single-thread executor

        answerButton.setOnClickListener(v -> {
            answerCall();
            finish();
        });

        redirectButton.setOnClickListener(v -> {
            hangUpCall();
            if (userId != null) {
                executor.submit(() -> postRedirectCall(userId));  // Execute the POST request on background thread
            } else {
                Log.e(TAG, "User ID is null, cannot post redirect call.");
            }
            finish();
        });

        hangupButton.setOnClickListener(v -> {
            hangUpCall();
            finish();
        });
    }

    private String getUserIdFromLoginApp() {
        try {
            Context loginAppContext = createPackageContext("com.example.myapplication", Context.CONTEXT_IGNORE_SECURITY);
            File file = new File(loginAppContext.getFilesDir(), "userid.txt");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String userId = reader.readLine();
            reader.close();
            return userId;
        } catch (Exception e) {
            Log.e(TAG, "Error reading user ID from file", e);
            return null;
        }
    }

    private void hangUpCall() {
        executeShellCommand("input keyevent 6");
    }

    private void answerCall() {
        executeShellCommand("input keyevent 79");
    }

    private void executeShellCommand(String command) {
        try {
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(su.getOutputStream());
            os.writeBytes(command + "\n");
            os.flush();
            os.close();
            su.waitFor();
        } catch (Exception e) {
            Log.e(TAG, "Error executing shell command", e);
        }
    }

    private void postRedirectCall(String userId) {
        try {
            URL url = new URL("https://scam-scam-service-185231488037.us-central1.run.app/api/v1/app/post-call");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setDoOutput(true);

            JSONObject payload = new JSONObject();
            payload.put("ownedBy", userId);

            OutputStream os = conn.getOutputStream();
            os.write(payload.toString().getBytes("UTF-8"));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            Log.d(TAG, "POST response code: " + responseCode);

            if (responseCode == 200 || responseCode == 201) {
                Scanner scanner = new Scanner(conn.getInputStream()).useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";
                scanner.close();
                Log.d(TAG, "POST successful. Response: " + response);
            } else {
                Log.e(TAG, "POST failed with code: " + responseCode);
            }

            conn.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "Error during POST request", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}
