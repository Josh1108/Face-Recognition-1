import flask
from flask import Flask,request
import jsonpickle
import cv2
import numpy as np

app=Flask(__name__)

@app.route('/facedetection', methods=['GET','POST'])
def facedetection():
    r = request
    nparr=np.fromstring(r.data, np.uint8)
    img = cv2.imdecode(nparr,cv2.IMREAD_COLOR)
    response = {'message': 'image received. size={}x{}'.format(img.shape[1], img.shape[0])}
    response_pickled = jsonpickle.encode(response)

    return Response(response=response_pickled, status=200, mimetype="application/json")



if __name__ =="__main__":
    app.run(debug=True)