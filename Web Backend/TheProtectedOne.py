#!/usr/bin/env python
import os
import time
from flask import Flask, abort, request, jsonify, g, url_for
from flask_sqlalchemy import SQLAlchemy
from flask_httpauth import HTTPBasicAuth
import jwt
from werkzeug.security import generate_password_hash, check_password_hash

#Custom Additions
import glob
import os
import skimage 
from skimage import measure
import cv2
import os
from PIL import Image
import pytesseract
import pytesseract as pyt
import cv2
import re
import pytesseract as tess
import dateparser
import datetime
#import tensorflow as tf
import numpy as np
from tensorflow.keras.preprocessing import image
import glob
from tensorflow.keras.preprocessing import image
import skimage.draw
from mrcnn.visualize import display_instances
from mrcnn.config import Config
from mrcnn import model as modellib, utils
import tensorflow.compat.v1 as tf
tf.disable_v2_behavior()
from flask import Flask, render_template, request,jsonify
import requests
import json
import sys
import wave
import numpy as np
import matplotlib.pyplot as plt
from pydub import AudioSegment
from scipy.signal import find_peaks



# initialization
app = Flask(__name__)
app.config['SECRET_KEY'] = 'sandaila kizhiyaatha satta enga iruku'
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///db.sqlite'
app.config['SQLALCHEMY_COMMIT_ON_TEARDOWN'] = True

# extensions
db = SQLAlchemy(app)
auth = HTTPBasicAuth()


class User(db.Model):
    __tablename__ = 'users'
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(32), index=True)
    password_hash = db.Column(db.String(128))
    displayname = db.Column(db.String(32))

    def hash_password(self, password):
        self.password_hash = generate_password_hash(password)

    def verify_password(self, password):
        return check_password_hash(self.password_hash, password)

    def generate_auth_token(self, expires_in=600):
        return jwt.encode(
            {'id': self.id, 'exp': time.time() + expires_in},
            app.config['SECRET_KEY'], algorithm='HS256')

    @staticmethod
    def verify_auth_token(token):
        try:
            data = jwt.decode(token, app.config['SECRET_KEY'],
                              algorithms=['HS256'])
        except:
            return
        return User.query.get(data['id'])


@auth.verify_password
def verify_password(username_or_token, password):
    # first try to authenticate by token
    user = User.verify_auth_token(username_or_token)
    if not user:
        # try to authenticate with username/password
        user = User.query.filter_by(username=username_or_token).first()
        if not user or not user.verify_password(password):
            return False
    g.user = user
    return True


@app.route('/api/users', methods=['POST'])
def new_user():
    username = request.form.get('username')
    displayname = request.form.get('displayname')
    password = request.form.get('password')
    if username is None or password is None:
        abort(400)    # missing arguments
    if User.query.filter_by(username=username).first() is not None:
        abort(400)    # existing user
    user = User(username=username, displayname=displayname)
    user.hash_password(password)

    db.session.add(user)
    db.session.commit()
    return (jsonify({'username': user.username}), 201,
            {'Location': url_for('get_user', id=user.id, _external=True)})


@app.route('/api/users/<int:id>')
def get_user(id):
    user = User.query.get(id)
    if not user:
        abort(400)
    return jsonify({'username': user.username, 'displayname': user.displayname})


@app.route('/api/token')
@auth.login_required
def get_auth_token():
    token = g.user.generate_auth_token(600)
    return jsonify({'token': token.decode('ascii'), 'duration': 600})



@app.route('/api/resource')
@auth.login_required
def get_resource():
    return jsonify({'data': 'Hello, %s!' % g.user.username})


@app.route("/")
def home():
    return "<h4 style=\"font-family:'Segoe UI'\">Rocket Science, please refrain from entering</h4>"


@app.route('/api/ext_body_check', methods = ['GET', 'POST'])
@auth.login_required
def upload_fileMRCNN():   
    if request.method == 'POST':
        if 'files[]' not in request.files:
            print('not_found')
        files = request.files.getlist('files[]')
        sv_path=os.getcwd()
        for file in files:
            path=os.path.join(sv_path,file.filename)
            file.save(path)
            #load&call
            status = requests.post("http://localhost:8001/ext_body_check", {"filePath" : path})
            os.remove(path)
            
    return status.json()


@app.route('/api/save', methods = ['GET', 'POST'])
@auth.login_required
def upload_fileSave():    
    if request.method == 'POST':
        if 'files[]' not in request.files:
            print('not_found')
        files = request.files.getlist('files[]')
        sv_path=os.getcwd()
        for file in files:
            path=os.path.join(sv_path,file.filename)
            file.save(path)

@app.route('/api/crank_audio_check', methods = ['GET', 'POST'])
@auth.login_required
def upload_fileCrank():    
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

@app.route('/api/horn_check', methods = ['GET', 'POST'])
@auth.login_required
def upload_fileHorn():    
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
        
        for i in range (len(time_series)):
            if(time_series[i]!=0):
                start_point=time_series[i]
                break

        print(start_point,peaks[0])  

        lm=4000
        um=15000

        if(start_point>lm and peaks[0]<um):
            status=1
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
            
    return jsonify({'horn_status':status})

@app.route('/api/chassis_check', methods = ['GET', 'POST'])
@auth.login_required
def upload_fileCChk():
    if request.method == 'POST':
        if 'files[]' not in request.files:
            print('not_found')
        files = request.files.getlist('files[]')
        filenames=[]
        sv_path=os.getcwd()
        for file in files:
            path=os.path.join(sv_path,file.filename)
            filenames.append(path)
            print(filenames)
            file.save(path)
    
    
    def test_all(chassis):       
        #chassis number
        #im = Image.open(chassis)        
        text = pyt.image_to_string(chassis)
        #a=re.findall("[A-Za-z]{2}\d[A-Za-z]{4}\d[A-Za-z]\d{2} \d{6}",text)
        a=re.findall("[A-Za-z]{2}\d[A-Za-z]{4}\d[A-Za-z]\w{2} \d{6}",text)
        print("FoundText:",a,"FullText: ",text)
        chassis_no=a[0]
        chassis_no=chassis_no.replace(" ", "") 
        return(chassis_no)
    
    
    #Database
    chassis_no=test_all(filenames[0])
    os.remove(filenames[0])  
    return jsonify({'chassis_number_check':chassis_no})

@app.route('/api/eng_bay_check', methods = ['GET', 'POST'])
@auth.login_required
def upload_fileEbChk():
    def tyre_ch(path):
        model = tf.keras.models.load_model('eng_bay_model.h5')
        img = image.load_img(path, target_size=(128, 128))
        x = image.img_to_array(img)
        x = np.expand_dims(x, axis=0)
        images = np.vstack([x])
        classes = model.predict(images)
        if classes[0]<0.5:
            r=0
        else:
            r=1
        return r
    
    if request.method == 'POST':
        if 'files[]' not in request.files:
            print('not_found')
        files = request.files.getlist('files[]')
        sv_path=os.getcwd()
        for file in files:
            path=os.path.join(sv_path,file.filename)
            file.save(path)
            status= tyre_ch(path)
            os.remove(path)
    
    return jsonify({'eng_bay_status':status})
            

@app.route('/api/wiper_check', methods = ['GET', 'POST'])
@auth.login_required
def upload_WP():
    
    def cmpr_img(p1,p2):
        imageA = cv2.imread(p1)
        imageA = cv2.resize(imageA, (480, 540)) 
        imageB = cv2.imread(p2)
        imageB = cv2.resize(imageB, (480, 540)) 
        imageA = cv2.cvtColor(imageA, cv2.COLOR_BGR2GRAY)
        imageB = cv2.cvtColor(imageB, cv2.COLOR_BGR2GRAY)
        m = skimage.metrics.mean_squared_error(imageA, imageB)
        s = skimage.metrics.structural_similarity(imageA, imageB)
        return m,s 
    
    filenames=[]
    if request.method == 'POST':
        if 'files[]' not in request.files:
            print('not_found')
        files = request.files.getlist('files[]')
        sv_path=os.getcwd()
        for file in files:
            path=os.path.join(sv_path,file.filename)
            filenames.append(path)
            file.save(path)
             
    length=int(len(filenames)/2)
    error=[]
    sim_idx=[]
    st=0
    for i in range(length):
        m,s=cmpr_img(filenames[i],filenames[i+1])
        error.append(m)
        sim_idx.append(s)
   
    for i in range(len(error)):
        if(error[i]>1000 and sim_idx[i]<0.70):
            st=st+1
        else:
            st=0
    if(st>0):
        status=1
    else:
        status=0
    for f in filenames:
        os.remove(f)
    return jsonify({'wiper_status':status})

@app.route('/api/tyre_check', methods = ['GET', 'POST'])
@auth.login_required
def upload_fileTC():
    def tyre_ch(path):
        model = tf.keras.models.load_model('tyre_model.h5')
        img = image.load_img(path, target_size=(128, 128))
        x = image.img_to_array(img)
        x = np.expand_dims(x, axis=0)
        images = np.vstack([x])
        classes = model.predict(images)
        if classes[0]<0.5:
            r=0
        else:
            r=1
        return r
    
    if request.method == 'POST':
        if 'files[]' not in request.files:
            print('not_found')
        files = request.files.getlist('files[]')
        sv_path=os.getcwd()
        for file in files:
            path=os.path.join(sv_path,file.filename)
            file.save(path)
            status= tyre_ch(path)
            os.remove(path)
            
    return jsonify({'tyre_status':status})

@app.route('/api/poll_cert_check', methods = ['GET', 'POST'])
@auth.login_required
def upload_filePollu():
    if request.method == 'POST':
        if 'files[]' not in request.files:
            print('not_found')
        files = request.files.getlist('files[]')
        filenames=[]
        sv_path=os.getcwd()
        for file in files:
            path=os.path.join(sv_path,file.filename)
            filenames.append(path)
            file.save(path)
    
    
    def poll(path):
        image = cv2.imread(path)
        text = tess.image_to_string(image)
        text = text.split("\n")
        dates=[]   
        for line in text:
            m=re.findall(r'\d{2}\-\d{2}\-\d{4}', line)
            if m:
                print(m)
                dates.append(m[0])
        v_date=min(dates)
        return v_date
    
    status_pc=poll(filenames[0]) 
    os.remove(filenames[0])
    return jsonify({'pollution_certificate_check':status_pc})

@app.route('/api/number_plate_check', methods = ['GET', 'POST'])
@auth.login_required
def upload_fileNP():
    if request.method == 'POST':
        if 'files[]' not in request.files:
            print('not_found')
        files = request.files.getlist('files[]')
        filenames=[]
        sv_path=os.getcwd()
        for file in files:
            path=os.path.join(sv_path,file.filename)
            filenames.append(path)
            file.save(path)
    
    
    def test_all(number_plate):

        #number plate
        image = cv2.imread(number_plate)
        gray_image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        canny_edge = cv2.Canny(gray_image, 170, 200)
        contours, new  = cv2.findContours(canny_edge.copy(), cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)
        contours=sorted(contours, key = cv2.contourArea, reverse = True)[:30]
        contour_with_license_plate = None
        license_plate = None
        x = None
        y = None
        w = None
        h = None

        for contour in contours:        
                perimeter = cv2.arcLength(contour, True)
                approx = cv2.approxPolyDP(contour, 0.01 * perimeter, True)
                if len(approx) == 4: 
                    contour_with_license_plate = approx
                    x, y, w, h = cv2.boundingRect(contour)
                    license_plate = gray_image[y:y + h, x:x + w]
                    break


        license_plate = cv2.bilateralFilter(license_plate, 11, 17, 17)
        (thresh, license_plate) = cv2.threshold(license_plate, 150, 180, cv2.THRESH_BINARY)
        text = pytesseract.image_to_string(license_plate, config="--psm 7")  
        s = ''.join(filter(str.isalnum, text))
        license_plate=s
        license_plate=license_plate.replace(" ", "") 
        return(license_plate)
    
 
    license_plate=test_all(filenames[0])
    os.remove(filenames[0])
    return jsonify({'number_plate_check':license_plate})



if __name__ == '__main__':
    if not os.path.exists('db.sqlite'):
        db.create_all()
    app.run(debug=False, host="0.0.0.0", ssl_context=('cert.pem', 'key.pem'))


