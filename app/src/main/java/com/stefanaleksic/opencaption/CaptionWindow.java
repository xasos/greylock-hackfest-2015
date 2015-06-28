package com.stefanaleksic.opencaption;

import wei.mark.standout.StandOutWindow;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static wei.mark.standout.StandOutWindow.*;

/**
 * Created by stefan on 6/27/15.
 */
public class CaptionWindow extends StandOutWindow{
    TextView captionView;
    public Handler handler;
    public void onCreate() {
        handler = new Handler();
        super.onCreate();
    }
    @Override
    public String getAppName() {
        return "SimpleWindow";
    }

    @Override
    public int getAppIcon() {
        return android.R.drawable.ic_menu_close_clear_cancel;
    }

    @Override
    public void createAndAttachView(int id, FrameLayout frame) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View v = inflater.inflate(R.layout.simple, frame, true);
        captionView = (TextView) v.findViewById(R.id.textView2);
        // Count down timer for Jelly Bean work around
        final SpeechRecognizer speechRec = SpeechRecognizer.createSpeechRecognizer(this);
        final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "VoiceIME");
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 10000L);

        speechRec.startListening(intent);
        final RecognitionListener someListener = new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {

                String someString = "";
                List<String> listOfWords = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);


                someString = listOfWords.get(0);
                try {

                    someString = URLEncoder.encode(someString, "UTF-8");

                } catch (Exception e) {
                    e.printStackTrace();
                }


                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                speechRec.stopListening();
                speechRec.setRecognitionListener(this);
                speechRec.startListening(intent);
            }

            public void onPartialResults(Bundle partialResults) {
                // WARNING: The following is specific to Google Voice Search
                ArrayList<String> results =
                        partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                String b = "";
                if (results != null) {
                    if (results.size() > 6) {
                        results.clear();
                    }
                    for (String p : results) {
                        b += p;
                    }
                }
                captionView.setText(b);

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        };
        speechRec.setRecognitionListener(someListener);

//        new Timer().scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        speechRec.stopListening();
//                        speechRec.setRecognitionListener(someListener);
//                        speechRec.startListening(intent);
//                    }
//                });
//
//            }
//        }, 0, 5000);


        }

    private void runOnUiThread(Runnable runnable){
        handler.post(runnable);
    }

    // the window will be centered
    @Override
    public StandOutLayoutParams getParams(int id, Window window) {
        return new StandOutLayoutParams(id, 1100, 200,
                StandOutLayoutParams.CENTER, StandOutLayoutParams.CENTER);
    }

    // move the window by dragging the view
    @Override
    public int getFlags(int id) {
        return super.getFlags(id) | StandOutFlags.FLAG_BODY_MOVE_ENABLE
                | StandOutFlags.FLAG_WINDOW_FOCUSABLE_DISABLE;
    }

    @Override
    public String getPersistentNotificationMessage(int id) {
        return "Click to close the SimpleWindow";
    }

    @Override
    public Intent getPersistentNotificationIntent(int id) {
        return StandOutWindow.getCloseIntent(this, CaptionWindow.class, id);
    }
}
