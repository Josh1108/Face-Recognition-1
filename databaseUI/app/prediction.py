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
global graph
graph = tf.get_default_graph()

def extract_face(filename, required_size=(224, 224)):
    pixels = pyplot.imread(filename)
    detector = MTCNN()
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
    for f in filenames:
        iden.append(f)
        try:
            faces = extract_face('./dataset/' + f)
        except:
            print('Face not detected taking whole image {}'.format(f))
            try:
                faces = pyplot.imread('./dataset/' + f)
                faces = faces.resize((224, 224))
                faces = asarray(image)
            except:
                print("Whole image couldn't be taken.{} is skipped".format(f))
                continue

        samples = asarray(faces, 'float32')

        samples = preprocess_input(samples, version=2)

        samples = samples.reshape(1,224,224,3)
        yhat = model.predict(samples)
        array.append(yhat)
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
    print(filenames)
    model =load_model('app/model/face_recog.h5')
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
        try:
            new_face = extract_face('./test/' + f)
        except:
            print("Can't find face using whole image")
            new_face = pyplot.imread('./test/' + f,0)
            new_face = Image.fromarray(new_face)
            new_face = image.resize((224,224))
            new_face = asarray(new_face)
        new_face = asarray(new_face,'float32')
        new_face = preprocess_input(new_face, version=2)
        new_face = new_face.reshape(1,224,224,3)
        with graph.as_default():
            new_user_emb = model.predict(new_face)
        new_user_emb = new_user_emb.reshape(-1)
    ans = []
    with open(os.getcwd()+"/app/static/embeddings/{}.txt".format(database),"rb") as f:
        embeddings = pickle.load(f)
    with open(os.getcwd()+"/app/static/identity/{}.txt".format(database),"rb") as f:
        iden = pickle.load(f)
    for emb in embeddings:
        ans.append(cosine(emb,new_user_emb))
    index = np.argsort(ans)
    a =[]
    a.append(iden[index[0]])
    a.append(iden[index[1]])
    a.append(iden[index[2]])
    d = (1-ans[index[0]])*100
    print(d)
    d ="{:.2f}".format(d)
    e = str(d) + "%"
    a.append(e)
    d = (1-ans[index[1]])*100
    print(d)
    d ="{:.2f}".format(d)
    e = str(d) + "%"
    a.append(e)
    d = (1-ans[index[2]])*100
    print(d)
    d ="{:.2f}".format(d)
    e = str(d) + "%"
    a.append(e)
    return a

    
@app.route('/something', methods=['GET','POST'])
def facedetection():
    # return(jsonify({"Party":"Party"}))
    data = request.get_json() or {}
    # database = request.args.get('database', None)
    start=time.time()
    # var = request.json["imageString"]
    if 'imageString' not in data or 'dataset' not in data :
        return(jsonify({"Party":"Party"}))
    var =data['imageString']
    database = data['database']
    imgdata = base64.b64decode(var)
    print("Time to get data and convert",time.time() - start)
    start=time.time()
    filename = './test/test_img.png'
    with open(filename, 'wb') as f:
        f.write(imgdata)
    print("Time to write:",time.time() - start)
    start=time.time()
    index = modelform(database)
    print("Time for model things",time.time() - start)
    response =[index[0],index[1],index[2],index[3],index[4],index[5]]
    print(response)
    # for file in os.listdir('./test/'):
    #     if file.endswith('.png'):
    #         os.remove('./test/' + file) 
    return Response(response=response, status=200, mimetype="application/json")
