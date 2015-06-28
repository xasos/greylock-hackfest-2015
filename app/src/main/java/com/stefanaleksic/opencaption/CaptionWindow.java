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

import static wei.mark.standout.StandOutWindow.*;

/**
 * Created by stefan on 6/27/15.
 */
public class CaptionWindow extends StandOutWindow{

    protected static AudioManager mAudioManager;
    protected static SpeechRecognizer mSpeechRecognizer;
    protected static Intent mSpeechRecognizerIntent;
    protected final Messenger mServerMessenger = new Messenger(new IncomingHandler(this));

    protected static boolean mIsListening;
    protected static volatile boolean mIsCountDownOn;
    private static boolean mIsStreamSolo;
    private static TextView captionView;

    static final int MSG_RECOGNIZER_START_LISTENING = 1;
    static final int MSG_RECOGNIZER_CANCEL = 2;

    @Override
    public void onCreate()
    {
        super.onCreate();

    }

    protected static class IncomingHandler extends Handler
    {
        private WeakReference<CaptionWindow> mtarget;

        IncomingHandler(CaptionWindow target)
        {
            mtarget = new WeakReference<CaptionWindow>(target);
        }


        @Override
        public void handleMessage(Message msg)
        {
            final CaptionWindow target = mtarget.get();

            switch (msg.what)
            {
                case MSG_RECOGNIZER_START_LISTENING:

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    {
                        // turn off beep sound
                        if (!mIsStreamSolo)
                        {
                            mAudioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, true);
                            mIsStreamSolo = true;
                        }
                    }
                    if (!target.mIsListening)
                    {
                        target.mSpeechRecognizer.startListening(target.mSpeechRecognizerIntent);
                        target.mIsListening = true;
                        //Log.d(TAG, "message start listening"); //$NON-NLS-1$
                    }
                    break;

                case MSG_RECOGNIZER_CANCEL:
                    if (mIsStreamSolo)
                    {
                        mAudioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, false);
                        mIsStreamSolo = false;
                    }
                    target.mSpeechRecognizer.cancel();
                    target.mIsListening = false;
                    //Log.d(TAG, "message canceled recognizer"); //$NON-NLS-1$
                    break;
            }
        }
    }

    protected class SpeechRecognitionListener implements RecognitionListener
    {

        @Override
        public void onBeginningOfSpeech()
        {
            // speech input will be processed, so there is no need for count down anymore
            if (mIsCountDownOn)
            {
                mIsCountDownOn = false;
                mNoSpeechCountDown.cancel();
            }
            //Log.d(TAG, "onBeginingOfSpeech"); //$NON-NLS-1$
        }

        @Override
        public void onBufferReceived(byte[] buffer)
        {

        }

        @Override
        public void onEndOfSpeech()
        {
            //Log.d(TAG, "onEndOfSpeech"); //$NON-NLS-1$
        }

        @Override
        public void onError(int error)
        {
            if (mIsCountDownOn)
            {
                mIsCountDownOn = false;
                mNoSpeechCountDown.cancel();
            }
            mIsListening = false;
            Message message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
            try
            {
                mServerMessenger.send(message);
            }
            catch (RemoteException e)
            {

            }
            //Log.d(TAG, "error = " + error); //$NON-NLS-1$
        }

        @Override
        public void onEvent(int eventType, Bundle params)
        {

        }

        @Override
        public void onPartialResults(Bundle partialResults)
        {
            ArrayList<String> results =
                    partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            String b = "";
            if (results != null) {
                if(results.size() > 6){
                    results.clear();
                }
                for (String p : results) {
                    b += p;
                }
            }
            captionView.setText(b);

        }

        @Override
        public void onReadyForSpeech(Bundle params)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            {
                mIsCountDownOn = true;
                mNoSpeechCountDown.start();

            }
        }

        @Override
        public void onResults(Bundle results)
        {
            //Log.d(TAG, "onResults"); //$NON-NLS-1$

        }

        @Override
        public void onRmsChanged(float rmsdB)
        {

        }

    }
    protected CountDownTimer mNoSpeechCountDown = new CountDownTimer(5000, 5000)
        {

            @Override
            public void onTick(long millisUntilFinished)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void onFinish()
            {
                mIsCountDownOn = false;
                Message message = Message.obtain(null, MSG_RECOGNIZER_CANCEL);
                try
                {
                    mServerMessenger.send(message);
                    message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
                    mServerMessenger.send(message);
                }
                catch (RemoteException e)
                {

                }
            }
        };

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
        captionView = (TextView)inflater.inflate(R.layout.simple, frame, true);
        // Count down timer for Jelly Bean work around

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizer.setRecognitionListener(new SpeechRecognitionListener());
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());

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
