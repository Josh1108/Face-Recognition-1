
from flask_admin.contrib.sqla import ModelView, view
from flask_admin import AdminIndexView,expose,BaseView
from flask_login import current_user
from flask import redirect, url_for, request,render_template
from .models import examinee
class UsersTable(ModelView):

    def is_accessible(self):
        return current_user.is_authenticated

    def inaccessible_callback(self, name, **kwargs):
        # redirect to login page if user doesn't have access
        return redirect(url_for('login', next=request.url))

class Tables(ModelView):

    def get_query(self):
        database = request.args.get('req', None) # pretending we have a GET parameter called "type"
        if database!=None:
            return self.session.query(self.model).filter(self.model.databasename==database)
        else:
            return self.session.query(self.model)
    def get_count_query(self):
        database = request.args.get('req',None)
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

class SeeTables(BaseView):
    @expose('/',methods=["POST","GET"])
    def index(self):

        query = examinee.query.with_entities(examinee.databasename).distinct()
        # print(query.all())
        tables = [row.databasename for row in query.all()]
        # print(tables)
        if request.method=="POST":
            req = request.form.get('database-select')
            print(req)
            # return redirect('/admin/table',database=req)
            return redirect(url_for('examinee.index_view', req=req))
        return self.render('admin/tables.html',tables=tables)

            