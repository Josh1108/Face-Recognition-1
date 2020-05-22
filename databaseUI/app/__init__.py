from flask import Flask
from config import Config
from flask_sqlalchemy import SQLAlchemy
from flask_migrate import Migrate
from flask_login import LoginManager
from flask_admin import Admin
from app.admin import UsersTable, Tables,MyAdminIndexView


app = Flask(__name__)
app.config.from_object(Config)
login=LoginManager(app)
login.login_view = 'login'
db = SQLAlchemy(app)
migrate = Migrate(app, db)
from app import routes, models
admin = Admin(app, name='facerecog', template_mode='bootstrap3',index_view=MyAdminIndexView())
admin.add_view(UsersTable(models.User, db.session))
admin.add_view(Tables(models.Table,db.session))