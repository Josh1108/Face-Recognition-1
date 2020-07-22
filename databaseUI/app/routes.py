from flask import render_template, flash, redirect, url_for,jsonify,make_response
from werkzeug.urls import url_parse
from app import app,db
from app.forms import LoginForm
from flask_login import current_user, login_user,logout_user
from app.models import User,examinee,dailypass
from flask import request
@app.route('/index')
def index():
    return render_template('index.html', title='Home')

@app.route('/',methods=['GET','POST'])
@app.route('/login', methods=['GET', 'POST'])
def login():
    if current_user.is_authenticated:
        return redirect(url_for('admin.index'))
    form = LoginForm()
    if form.validate_on_submit():
        user = User.query.filter_by(username=form.username.data).first()
        if user is None or not user.check_password(form.password.data):
            flash('Invalid username or password')
            return redirect(url_for('login'))
        login_user(user, remember=form.remember_me.data)
        next_page = request.args.get('next')
        if not next_page or url_parse(next_page).netloc != '':
            next_page=url_for('admin.index')
        flash('Successfully logged in')
        return redirect(url_for('admin.index'))
    return render_template('login.html', title='Sign In', form=form)
@app.route('/logout')
def logout():
    logout_user()
    flash('Successfully logged out')
    return redirect(url_for('login'))
@app.route('/api/databases', methods=["GET"])
def get_databases():
    result = db.session.execute('SELECT DISTINCT databasename FROM examinee;')
    lst=[]
    for r in result:
        lst.append(r[0])
    return jsonify({"databases": lst})

@app.route('/api/database/<name>',methods=["GET"])
def get_records(name):
    res = db.session.execute('SELECT * FROM examinee WHERE examinee.databasename=:val',{'val':name})
    lst=[]
    for r in res:
        l=[]
        # print(r)
        for item in r:
            # print(item)
            l.append(item)
        lst.append(l)
    # print(lst)
    return make_response(jsonify({'examinees': lst}))

@app.route('/api/databases/attendance',methods=["GET","POST"])
def mark_attendance():
    database= request.json["database"]
    rnum = request.json["attendance"]
    try:
        att = examinee.query.filter_by(databasename=database,RollNumber=rnum).first()
        att.attendance=True
        db.session.commit()
    except AttributeError:
        return make_response(jsonify({'result': 'attribute error'}))
    # res = db.session.execute('UPDATE examinee SET attendance = 1 WHERE databasename=:val AND RollNumber=:rnum',{'val':database,'rnum':rnum})
    return make_response(jsonify({'result': 'done'}))


@app.route('/creds',methods=["POST"])
def logincreds():
    creds = request.json["ButterChicken"]
    password =""
    try:
        res = db.session.execute('SELECT * FROM dailypass')
        for r in res:
            a,b = r
        password = b
        print(password)
    except AttributeError:
        return make_response(jsonify({'creds': 'keyerrorindatabase'}),150)
    if "ButterChicken" ==creds:
        return make_response(jsonify({'creds':"{}".format(password)}))
    else: return make_response(jsonify({'creds': 'fail'}),100) # fails   

@app.errorhandler(404)
def not_found(error):
    return make_response(jsonify({'error': 'Not found'}), 404)





#addlogin_required for protected pages, add decorater below app.route
