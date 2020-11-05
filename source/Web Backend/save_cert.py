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

@app.route('/save', methods = ['GET', 'POST'])

def upload_file1():
    
    if request.method == 'POST':
        if 'files[]' not in request.files:
            print('not_found')
        files = request.files.getlist('files[]')
        sv_path=os.getcwd()
        for file in files:
            path=os.path.join(sv_path,file.filename)
            file.save(path)
          
            
    return 'success'

if __name__ == '__main__':
       app.run(port=5000)