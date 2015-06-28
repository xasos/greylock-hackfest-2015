#!/usr/bin/env python3
from Tkinter import *
from threading import Timer
import time
import pyaudio, wave, sys
import requests
import speech_recognition as sr

r =  sr.Recognizer()

chunk =  1024
FORMAT = pyaudio.paInt16
CHANNELS = 1
RATE = 44100
RECORD_SECONDS = 8
WAVE_OUTPUT_FILENAME = 'audio.wav'
API_URL = 'http://open-caption-dank.mybluemix.net/'

p = pyaudio.PyAudio()
channel_map = (0, 1)

stream_info = pyaudio.PaMacCoreStreamInfo(
    flags = pyaudio.PaMacCoreStreamInfo.paMacCorePlayNice,
    channel_map = channel_map)

while True:
    stream = p.open(format = FORMAT,
                    rate = RATE,
                    input = True,
                    input_host_api_specific_stream_info = stream_info,
                    channels = CHANNELS)

    all = []
    for i in range(0, RATE / chunk * RECORD_SECONDS):
            data = stream.read(chunk)
            all.append(data)
    stream.close()
    p.terminate()

    data = ''.join(all)
    wf = wave.open(WAVE_OUTPUT_FILENAME, 'wb')
    wf.setnchannels(CHANNELS)
    wf.setsampwidth(p.get_sample_size(FORMAT))
    wf.setframerate(RATE)
    wf.writeframes(data)
    wf.close()

    root = Tk()
    root.wm_title("openCaption")
    root.geometry('2800x100')
    root.attributes("-topmost", True)

    var = StringVar()
    label = Label( root, textvariable=var, relief=RAISED)


    with sr.WavFile("audio.wav") as source:
        audio = r.record(source)

    try:
        list = r.recognize(audio,True)
        print("Possible transcriptions")
        for prediction in list:
            print(" " + prediction["text"] + " (" + str(prediction["confidence"]*100) + "%)")
            var.set(prediction["text"])
    except LookupError:
        print("Could not understand audio")

    label.pack()
    root.mainloop()
    threading.Timer(0.1, f).start()
