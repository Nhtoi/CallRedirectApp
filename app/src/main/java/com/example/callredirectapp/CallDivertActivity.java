package com.example.callredirectapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import java.io.DataOutputStream;

public class CallDivertActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        Button answerButton = new Button(this);
        answerButton.setText("Answer");
        answerButton.setOnClickListener(v -> {
            answerCall();
            finish(); // Close app after answering
        });

        Button redirectButton = new Button(this);
        redirectButton.setText("Redirect");
        redirectButton.setOnClickListener(v -> {
            hangUpCall();
            finish(); // Close app after dismissing
        });

        Button hangupButton = new Button(this);
        hangupButton.setText("Hang-Up");
        hangupButton.setOnClickListener(v -> {
            hangUpCall();
            finish(); // Close app after dismissing
        });

        layout.addView(answerButton);
        layout.addView(redirectButton);
        layout.addView(hangupButton);

        setContentView(layout);
    }

    private void hangUpCall() {
        executeShellCommand("input keyevent 6"); // End Call
    }

    private void answerCall() {
        executeShellCommand("input keyevent 79"); // Answer Call
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
            e.printStackTrace();
        }
    }
}