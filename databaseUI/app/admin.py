
from flask_admin.contrib.sqla import ModelView, view
from flask_admin import AdminIndexView,expose,BaseView, form
from flask_login import current_user,logout_user
from flask import redirect, url_for, request,render_template,Markup,flash
from app.models import examinee
from app import db
from werkzeug.utils import secure_filename
ALLOWED_EXTENSIONS = {'pdf', 'png', 'jpg', 'jpeg'}
import flask
import os
import uuid
import app.prediction
imagedir = 'app/static/images'


class UsersTable(ModelView):

    def is_accessible(self):
        return current_user.is_authenticated

    def inaccessible_callback(self, name, **kwargs):
        # redirect to login page if user doesn't have access
        return redirect(url_for('login', next=request.url))


def _list_thumbnail(view, context, model, name):
    if not model.filename:
        return ''
    return Markup('<img src="{model.url}" style="width: 150px;">'.format(model=model))


def _imagename_uuid1_gen(obj, file_data):
    # _, ext = os.path.splitext(file_data.filename)
    return secure_filename(file_data.filename)
class Tables(ModelView):
    column_list = [
        'image', 'filename','databasename','RollNumber','attendance'
    ]

    column_formatters = {
        'image': _list_thumbnail
    }

    form_extra_fields = {
        'filename': form.ImageUploadField(
            'Image',
            base_path=imagedir,
            url_relative_path='images/',
            namegen=_imagename_uuid1_gen,
        )
    }
    def get_query(self):
        database = request.args.get('req',default = None) # pretending we have a GET parameter called "type"
        if database!=None:
            return self.session.query(self.model).filter(self.model.databasename==database)
        else:
            return self.session.query(self.model)
    def get_count_query(self):
        database = request.args.get('req', default = None)
        if database!=None:
            return self.session.query(view.func.count('*')).filter(self.model.databasename==database)
        else:
            return self.session.query(view.func.count('*'))
    
    def is_accessible(self):
        return current_user.is_authenticated

    def inaccessible_callback(self, name, **kwargs):
        # redirect to login page if user doesn't have access
        return redirect(url_for('login', next=request.url))

class MyAdminIndexView(AdminIndexView):
    def is_accessible(self):
        return current_user.is_authenticated

    def inaccessible_callback(self, name, **kwargs):
        # redirect to login page if user doesn't have access
        return redirect(url_for('login', next=request.url))

class MyBaseView(BaseView):
    def is_accessible(self):
        return current_user.is_authenticated

    def inaccessible_callback(self, name, **kwargs):
        # redirect to login page if user doesn't have access
        return redirect(url_for('login', next=request.url))
class SeeTables(MyBaseView):
    @expose('/',methods=["POST","GET"])
    def index(self):
        query = examinee.query.with_entities(examinee.databasename).distinct()
        # print(query.all())
        tables = [row.databasename for row in query.all()]
        # print(tables)
        if request.method=="POST":
            req = request.form.get('database-select')
            # print(req)
            # return redirect('/admin/table',database=req)
            return redirect(url_for('examinee.index_view', req=req))
        return self.render('admin/tables.html',tables=tables)
def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

class CreateDatabase(MyBaseView):
    @expose('/',methods=["POST","GET"])
    def index(self):
        if request.method=="POST":
            uploaded_files = flask.request.files.getlist("file[]")
            for file in uploaded_files:
                # if user does not select file, browser also
                # submit an empty part without filename
                if file.filename == '':
                    flash("file not selected")
                    continue
                req = request.form.get('dataname')
                if file and allowed_file(file.filename):
                    filename = secure_filename(file.filename)
                    file.save(os.path.join(imagedir, filename))
                    x = filename.rsplit('.', 1)[0]
                    print(x)
                ## adding to db
                    item = examinee(databasename=req,filename=filename,RollNumber=x)
                    db.session.add(item)
                    db.session.commit()
            return redirect(url_for('examinee.index_view', req = req))
        return self.render('admin/databaseform.html')

class TrainDatabase(MyBaseView):
    '''
        Train a Database
    '''
    @expose('/', methods = ['POST','GET'])
    def index(self):
        query = examinee.query.with_entities(examinee.databasename).distinct()
        tables = [row.databasename for row in query.all()]
        if request.method=="POST":
            database = request.form.get('database-select')
            # run API and show progress bar
            
            return redirect(url_for('training',database = database))
            


            # return redirect(url_for('examinee.index_view', req=req))
        return self.render('admin/train.html',tables=tables)
class Logout(MyBaseView):
    '''
    Logout
    '''
    @expose('/', methods = ['POST','GET'])
    def index(self):
        logout_user()
        flash('Successfully logged out')
        return redirect(url_for('login'))

class dailypass(ModelView):
    def is_accessible(self):
        return current_user.is_authenticated

    def inaccessible_callback(self, name, **kwargs):
        # redirect to login page if user doesn't have access
        return redirect(url_for('login', next=request.url))