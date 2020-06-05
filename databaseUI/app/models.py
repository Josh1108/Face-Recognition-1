from app import db,login,app
from werkzeug.security import generate_password_hash, check_password_hash
from flask_login import UserMixin
from flask_uploads import UploadSet, IMAGES, configure_uploads,patch_request_class
images = UploadSet('images', IMAGES)
patch_request_class(app, 16 * 1024 * 1024)
configure_uploads(app, (images))
class User(UserMixin,db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(64), index=True, unique=True)
    password_hash = db.Column(db.String(128))
    def set_password(self, password):
        self.password_hash = generate_password_hash(password)

    def check_password(self, password):
        return check_password_hash(self.password_hash, password)
    def __repr__(self):
        return '<User {}>'.format(self.username)    
@login.user_loader
def load_user(id):
    return User.query.get(int(id))

class examinee(UserMixin,db.Model):
    id = db.Column(db.Integer, primary_key=True)
    databasename = db.Column(db.String(64), index=True)
    # image = db.Column(db.LargeBinary)
    # name = db.Column(db.String(64), unique=True) 
    filename =db.Column(db.String(128))
    RollNumber=db.Column(db.String(64), index=True, unique = True)
    attendance =db.Column(db.Boolean,default =False)
    @property
    def url(self):
        return images.url(self.filename)

    @property
    def filepath(self):
        if self.filename is None:
            return
        return images.path(self.filename)