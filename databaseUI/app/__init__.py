from flask import Flask, render_template,Markup
from config import Config
from flask_sqlalchemy import SQLAlchemy
from flask_migrate import Migrate
from flask_login import LoginManager,UserMixin,current_user
from flask_admin import Admin,form
from flask import redirect, url_for, request,render_template
from flask_admin import AdminIndexView,expose
from flask_admin.contrib.sqla import ModelView, view
from flask_uploads import UploadSet, IMAGES, configure_uploads,patch_request_class
import os
from sqlalchemy import event
import uuid
from werkzeug.utils import secure_filename

app = Flask(__name__)
app.config.from_object(Config)
app.config['UPLOADED_IMAGES_DEST'] = imagedir = '/app/static/images'
app.config['UPLOADED_IMAGES_URL'] = '/static/images/'
login=LoginManager(app)
login.login_view = 'login'
db = SQLAlchemy(app)
migrate = Migrate(app, db)

# images = UploadSet('images', IMAGES)
# configure_uploads(app, (images))
# patch_request_class(app, 16 * 1024 * 1024)
from app import routes, models,prediction
from app.admin import UsersTable, Tables,MyAdminIndexView,SeeTables,CreateDatabase,TrainDatabase
from app.models import examinee

admin = Admin(app, name='facerecog', template_mode='bootstrap3',index_view=MyAdminIndexView())
admin.add_view(UsersTable(models.User, db.session))
admin.add_view(Tables(models.examinee,db.session))
admin.add_view(SeeTables('selectdb', url='/selectdb'))
admin.add_view(CreateDatabase('CreateDatabase', url= '/createdb'))
admin.add_view(TrainDatabase('Train Database', url= '/traindb'))

@event.listens_for(examinee, 'after_delete')
def del_image(mapper, connection, target):
    if target.filepath is not None:
        try:
            os.remove(target.filepath)
        except OSError:
            pass




