import os

from flask import Flask, request, Response

app = Flask(__name__)

APP_ROOT = os.path.dirname(os.path.abspath(__file__))

@app.route("/images/upload", methods=["POST"])
def upload():

    target = os.path.join(APP_ROOT, 'images')
    print(target)
    if not os.path.isdir(target):
        os.mkdir(target)

    print(request.files)

    for file in request.files.getlist("image"):
        print(file)
        filename = file.filename
        destination = "/".join([target, filename])
        print("Accept incoming file:", filename)
        print(destination)
        file.save(destination)
    
    return Response("{'a':'b'}", status=201, mimetype='application/json')

if __name__ == "__main__":
    app.run(port=5000, debug=True)