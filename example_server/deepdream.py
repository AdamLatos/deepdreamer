import os

from flask import Flask, request, Response, send_file
import time

app = Flask(__name__, static_url_path='/static')

APP_ROOT = os.path.dirname(os.path.abspath(__file__))


def random_name():
  from hashlib import md5
  from time import localtime
  return "%s.jpg" % (md5(str(localtime()).encode('utf-8')).hexdigest())

@app.route('/<generated_image>', methods=["GET"])
def generated_image_page(generated_image):
    print(f"serving {generated_image}")
    fullpath = "./images/" + generated_image
    return send_file(fullpath, mimetype="image/jpeg")


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
    
        # generate filename for file
        generated_img_name = random_name()
        print(generated_img_name)
        # generate new image
        time.sleep(2)

    resp = {"{\"path\": \"testing.jpg\"}"}

    return Response(resp, status=201, mimetype='application/json')

if __name__ == "__main__":
    app.run(port=5000, debug=True)