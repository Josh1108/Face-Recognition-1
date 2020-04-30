import flask
from flask import Flask,request,Response,jsonify
import jsonpickle
import cv2
import numpy as np
from PIL import Image
from numpy import asarray
import numpy as np
from scipy.spatial.distance import cosine
from mtcnn.mtcnn import MTCNN
from keras_vggface.vggface import VGGFace
from keras_vggface.utils import preprocess_input
from keras.models import load_model
import os
import base64
import io
import json
from matplotlib import pyplot
import pickle

app=Flask(__name__)
# extract a single face from a given photograph
def extract_face(filename, required_size=(224, 224)):
    # load image from file
    pixels = pyplot.imread(filename,0)
    # create the detector, using default weights
    detector = MTCNN()
    # detect faces in the image
    results = detector.detect_faces(pixels)
    # extract the bounding box from the first face
    x1, y1, width, height = results[0]['box']
    x2, y2 = x1 + width, y1 + height
    # extract the face
    face = pixels[y1:y2, x1:x2]
    # resize pixels to the model size
    image = Image.fromarray(face)
    image = image.resize(required_size)
    face_array = asarray(image)
    return face_array

# Take in base64 string and return PIL image


# convert PIL Image to an RGB image( technically a numpy array ) that's compatible with opencv
def toRGB(image):
    return cv2.cvtColor(np.array(image), cv2.COLOR_BGR2RGB)
def stringToImage(base64_string):
    imgdata = base64.b64decode(base64_string)
    image = Image.open(io.BytesIO(imgdata))
    img = toRGB(image)
    return img

def modelform():
    with open("embeddings.txt","rb") as f:
        embeddings = pickle.load(f)
    with open("identity.txt","rb") as f:
        iden = pickle.load(f)
    for f in os.listdir('./test/'):
        try:
            new_face = extract_face('./test/' + f)
        except:
            try:
                print("Face Not Found Trying recognition with whole image")
                faces = pyplot.imread('./test/' + f,0)
                faces = faces.resize((224, 224))
                new_face = asarray(faces)
            except:
                print("No Face Detected")
                b = []
                b.append("N")
                b.append("o")
                b.append("Fa")
                b.append("ce")
                b.append("Dete")
                b.append("cted")
                return b
        try:
            new_face = asarray(new_face, 'float32')
            new_face = preprocess_input(new_face, version=2)
            new_face = new_face.reshape(1,224,224,3)
            try:
                new_user_emb = model.predict(new_face)
            except:
                model = load_model('face_recog.h5')
                new_user_emb = model.predict(new_face)
            new_user_emb = new_user_emb.reshape(-1)
        except:
            print("MODEL not loading")
            b = []
            b.append("N")
            b.append("o")
            b.append("Fa")
            b.append("ce")
            b.append("Dete")
            b.append("cted")
            return b
    ans = []
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


@app.route('/', methods=['GET','POST'])
def facedetection():
    r=request
    var = r.json["imageString"]
    imgdata = base64.b64decode(var)
    filename = './test/test_img.png'  # I assume you have a way of picking unique filenames
    with open(filename, 'wb') as f:
        f.write(imgdata)
    index = modelform()
    response =[index[0],index[1],index[2],index[3],index[4],index[5]]
    print(response)
    for file in os.listdir('./test/'):
        if file.endswith('.png'):
            os.remove('./test/' + file) 
    return Response(response=response, status=200, mimetype="application/json")



if __name__ =="__main__":
    app.run('0.0.0.0',debug=True,port=8000)
