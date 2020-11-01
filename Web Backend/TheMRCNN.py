import tensorflow as tf
import numpy as np
from tensorflow.keras.preprocessing import image
from flask import Flask, render_template, request,jsonify
import glob
import skimage 
from skimage import measure
import os
import sys
import json
import datetime
import skimage.draw
import cv2
from mrcnn.visualize import display_instances
import matplotlib.pyplot as plt
from mrcnn.config import Config
from tensorflow import keras
from mrcnn import model as modellib, utils


# initialization
app = Flask(__name__)

class CustomConfig(Config):
    NAME = "scratch"
    IMAGES_PER_GPU = 1
    NUM_CLASSES = 1 + 1  # Car Background + scratch
    STEPS_PER_EPOCH = 100
    DETECTION_MIN_CONFIDENCE = 0.9

class InferenceConfig(CustomConfig):

    GPU_COUNT = 1
    IMAGES_PER_GPU = 1

config = InferenceConfig()
model = modellib.MaskRCNN(mode="inference", config=config,model_dir=os.getcwd())
model.load_weights(os.getcwd()+"/mask_rcnn_scratch_final.h5", by_name=True)
graph = tf.get_default_graph()

@app.route('/ext_body_check', methods = ['GET', 'POST'])
def upload_fileMRCNN():        
    
    def color_splash(image, mask):
        status=0
        gray = skimage.color.gray2rgb(skimage.color.rgb2gray(image)) * 255
        mask = (np.sum(mask, -1, keepdims=True) >= 1)
        if mask.shape[0] > 0:
            splash = np.where(mask, image, gray).astype(np.uint8)
        else:
            splash = gray

        if np.sum(mask) > 0:
            status = 0
        else:
            status = 1
        return status


    def detect_and_color_splash(model, image_path):

        image = skimage.io.imread(image_path)
        r = model.detect([image], verbose=1)[0]
        status = color_splash(image, r['masks'])
        return status  
   
    if request.method == 'POST':
        files = [ request.form.get("filePath") ]
        sv_path=os.getcwd()
        for file in files:
            with graph.as_default():
                path=file
                #load&call
                config = InferenceConfig()
                cur_dir=os.getcwd()
                logs = cur_dir
                weights = cur_dir+"/mask_rcnn_scratch_final.h5"

                status=detect_and_color_splash(model, image_path=path)
            
    return jsonify({'result':status})


if __name__ == '__main__':
    app.run(port=8001)


