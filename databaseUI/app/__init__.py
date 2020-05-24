from flask import Flask, render_template
from config import Config
from flask_sqlalchemy import SQLAlchemy
from flask_migrate import Migrate
from flask_login import LoginManager
from flask_admin import Admin
from flask_login import current_user
from flask import redirect, url_for, request,render_template
from flask_admin import AdminIndexView,expose


app = Flask(__name__)
app.config.from_object(Config)
login=LoginManager(app)
login.login_view = 'login'
db = SQLAlchemy(app)
migrate = Migrate(app, db)
from app import routes, models
from app.admin import UsersTable, Tables,MyAdminIndexView,SeeTables

admin = Admin(app, name='facerecog', template_mode='bootstrap3',index_view=MyAdminIndexView())
admin.add_view(UsersTable(models.User, db.session))
admin.add_view(Tables(models.Table,db.session,endpoint='testadmin'))
admin.add_view(SeeTables('selectdb', url='/selectdb'))