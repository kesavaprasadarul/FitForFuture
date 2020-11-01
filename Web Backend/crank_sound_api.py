#API
import sys
import wave
import numpy as np
import matplotlib.pyplot as plt
from pydub import AudioSegment
from scipy.signal import find_peaks


import numpy as np

from flask import Flask, render_template, request,jsonify
import glob
import os
import skimage 
from skimage import measure
import cv2
import os
app = Flask(__name__)

@app.route('/crank_audio_check', methods = ['GET', 'POST'])

def upload_file1():
    
    def check_crank(path):

        sound = AudioSegment.from_wav(path)
        os.remove(path)
        sound = sound.set_channels(1)
        sound.export(path, format="wav")
        wav=wave.open(path,"r")
        raw = wav.readframes(-1)
        raw=np.frombuffer(raw,"Int16")
        sampleRate=wav.getframerate()
        duration = len(raw)/sampleRate
        time = np.arange(0,duration,1/sampleRate) #time vector
        #check decible
        check=[]
        for i in range (len(raw)):
            if(abs(raw[i])>5000):
                check.append(1)
            else:
                check.append(0)
        #get time index,time 
        x=0.5
        limit=[]
        t=[]
        for i in range (len(time)):  
            if(time[i]==x):
                limit.append(i)
                t.append(time[i])
                x=x+0.5
        #get sum       
        r=[]
        x=0
        for j in range (len(limit)):
            s=0
            d_s=0

            while(x<=limit[j]):
                s=s+check[x]
                x=x+1

            r.append(s)
            x=limit[j]
        time_series=r
        indices = find_peaks(time_series)[0]
        peaks= [time_series[j] for j in indices]
        start_point=0
        end_point=0
        for i in range (len(time_series)):
            if(time_series[i]>200):
                start_point=i
                break
        for i in range (len(time_series)):
            if(time_series[i]==peaks[0]):
                end_point=i
                break
        crank_duration=t[end_point]-t[start_point]
        if(crank_duration<3):
            status= 1
        else:
            status=0
        return(status)

    
    if request.method == 'POST':
        if 'files[]' not in request.files:
            print('not_found')
        files = request.files.getlist('files[]')
        sv_path=os.getcwd()
        for file in files:
            path=os.path.join(sv_path,file.filename)
            
            file.save(path)
            status= check_crank(path)
#             os.remove("mono.wav")
            os.remove(path)
            
    return jsonify({'crank_status':status})

if __name__ == '__main__':
       app.run(port=5000)