
from flask_admin.contrib.sqla import ModelView
from flask_admin import AdminIndexView,expose,BaseView
from flask_login import current_user
from flask import redirect, url_for, request,render_template
from .models import Table
class UsersTable(ModelView):

    def is_accessible(self):
        return current_user.is_authenticated

    def inaccessible_callback(self, name, **kwargs):
        # redirect to login page if user doesn't have access
        return redirect(url_for('login', next=request.url))

class Tables(ModelView):

    # def get_query(self):
    #     return super(Tables, self).get_query().filter(User.username == current_user.username)
    def is_accessible(self):
        return current_user.is_authenticated

    def inaccessible_callback(self, name, **kwargs):
        # redirect to login page if user doesn't have access
        return redirect(url_for('login', next=request.url))
    # def on_model_change(self, form, model, is_created):
    #     if is_created:
    #         sqlalchemy_create_table(model)

    # def on_model_delete(self, model):
    #     sqlalchemy_droptable(model)
# class SelectTable(ModelView):
class MyAdminIndexView(AdminIndexView):
    def is_accessible(self):
        return current_user.is_authenticated

    def inaccessible_callback(self, name, **kwargs):
        # redirect to login page if user doesn't have access
        return redirect(url_for('login', next=request.url))
    
class UserView(ModelView):
    """
    Restrict only the current user can see his/her own profile
    """
    def get_query(self):
        return self.session.query(self.model).filter(self.model.id==current_user.id)
    def get_count_query(self):
        return self.session.query(func.count('*')).filter(self.model.id==current_user.id)

class SeeTables(BaseView):
    @expose('/',methods=["POST","GET"])
    def index(self):

        query = Table.query.with_entities(Table.databasename).distinct()
        # print(query.all())
        tables = [row.databasename for row in query.all()]
        # print(tables)
        if request.method=="POST":
            req = request.form.get('database-select')
            print(req)
            # return redirect('/admin/table',database=req)
            return url_for('testadmin.index_view')
        return self.render('admin/tables.html',tables=tables)

            