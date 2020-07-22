import flask 
from flask import Flask,request,Response,redirect,url_for,jsonify
import cv2
import numpy as np
from PIL import Image
from numpy import asarray
import numpy as np
from scipy.spatial.distance import cosine
from mtcnn.mtcnn import MTCNN
from keras_vggface.vggface import VGGFace
from keras_vggface.utils import preprocess_input
from tensorflow.keras.models import load_model
from keras.preprocessing.image import load_img
import os
import base64
import io
import json
from tqdm import tqdm
from matplotlib import pyplot
import pickle
import time
import tensorflow as tf
from app import app,db
from tensorflow import keras
global graph
graph = tf.get_default_graph()
model =load_model('app/model/face_recog.h5')
detector = MTCNN()
def extract_face(filename, required_size=(224, 224)):
    try:
        pixels=pyplot.imread(filename)
    except:
        print("error in reading file")

    results = detector.detect_faces(pixels)
    x1, y1, width, height = results[0]['box']
    x2, y2 = x1 + width, y1 + height
    face = pixels[y1:y2, x1:x2]
    image = Image.fromarray(face)
    image = image.resize(required_size)
    face_array = asarray(image)
    return face_array

def get_embeddings(filenames,iden):

    array = []
    for f in tqdm(filenames):
        try:
            faces = extract_face(f)
        except:
            print("face not detected in{}".format(f))
            continue

        samples = asarray(faces, 'float32')
        samples = preprocess_input(samples, version=2)
        samples = samples.reshape(1,224,224,3)
        yhat = model.predict(samples)
        iden.append(f)
        array.append(yhat)
    print(iden,"HERE WE ARE")
    return array,iden

@app.route('/training',methods=['GET'])
def training():
    database = request.args.get('database')
    # print(database)
    res = db.session.execute('SELECT * FROM examinee WHERE examinee.databasename=:val',{'val':database})
            # print(res)
    lst=[]
    for r in res:
        # print(r)
        lst.append(r[2])
    # print(lst)
    folder = os.getcwd()+'/app/static/images/'
    filenames=[]
    for item in lst:
        filenames.append(folder+item)
    # print(filenames)
    # model =load_model('app/model/face_recog.h5')
    iden =[]
    embeddings,iden = get_embeddings(filenames,iden)
    with open(os.getcwd()+"/app/static/embeddings/{}.txt".format(database),'wb') as f:
        pickle.dump(embeddings,f)
    with open(os.getcwd()+"/app/static/identity/{}.txt".format(database),'wb') as f:
        pickle.dump(iden,f)
    print("Yo, model trained?")
    return redirect(url_for('login'))


def modelform(database):
    for f in os.listdir('./test/'):
        print(f)
        try:
            new_face = extract_face('./test/' + f)
        except:
            a = {"result": "not_found"}
            return a
        new_face = asarray(new_face,'float32')
        new_face = preprocess_input(new_face, version=2)
        new_face = new_face.reshape(1,224,224,3)

        with graph.as_default():
            new_user_emb = model.predict(new_face)
        new_user_emb = new_user_emb.reshape(-1)
    ans = []
    with open(os.getcwd()+"/app/static/embeddings/{}.txt".format(database),"rb") as f:
        embeddings = pickle.load(f)
    print(embeddings)
    with open(os.getcwd()+"/app/static/identity/{}.txt".format(database),"rb") as f:
        iden = pickle.load(f)
    for emb in embeddings:
        ans.append(cosine(emb,new_user_emb))
    index = np.argsort(ans)
    print(index)

    d = (1-ans[index[0]])*100
    d ="{:.2f}".format(d)
    d = str(d) + "%"

    e = (1-ans[index[1]])*100
    e ="{:.2f}".format(e)
    e = str(e) + "%"

    n = (1-ans[index[2]])*100
    n ="{:.2f}".format(n)
    n = str(n) + "%"

    with open(os.getcwd()+"/app/static/images/{}".format(iden[index[0]].split("/")[-1]), "rb") as image_file:
        encoded_string1 = str(base64.b64encode(image_file.read()))
    with open(os.getcwd()+"/app/static/images/{}".format(iden[index[1]].split("/")[-1]), "rb") as image_file:
            encoded_string2 = str(base64.b64encode(image_file.read()))
    with open(os.getcwd()+"/app/static/images/{}".format(iden[index[2]].split("/")[-1]), "rb") as image_file:
        encoded_string3 = str(base64.b64encode(image_file.read()))
    encoded_string1=encoded_string1[1:-1]
    encoded_string2=encoded_string2[1:-1]
    encoded_string3=encoded_string3[1:-1]

    a ={"name": [iden[index[0]], iden[index[1]], iden[index[2]]], 
    	"percentage": [d,e,n],
        "imageString" : [encoded_string1,encoded_string2,encoded_string3]}
    return a
@app.route('/something', methods=['POST'])
def facedetection():
    # return(jsonify({"Party":"Party"}))
    # data = request.get_json(force = True)
    # print(data)
    # database = request.args.get('database', None)
    start=time.time()
    var = request.json["imageString"]
    print("Checking")
    # if 'imageString' not in data or 'dataset' not in data :
    #     return(jsonify({"Party":"Party"}))
    # var =data['imageString']
    database = request.json['dataset']
    print(database)
    imgdata = base64.b64decode(var)
    print("Time to get data and convert",time.time() - start)
    start=time.time()
    filename = './test/test_img.jpg'
    with open(filename, 'wb') as f:
        f.write(imgdata)
    print("Time to write:",time.time() - start)
    start=time.time()
    index = modelform(database)
    print("Time for model things",time.time() - start)
    # print(response)
    # for file in os.listdir('./test/'):
    #     if file.endswith('.png'):
    #         os.remove('./test/' + file) 
    return jsonify(index)
