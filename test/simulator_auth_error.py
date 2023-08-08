from flask import Flask
import logging
import time
import threading

logging.basicConfig(level=logging.ERROR, format='%(asctime)s ERROR 3 --- [scheduling-3] c.h.server3.service.ApiService : %(message)s')
logger = logging.getLogger()

def log_errors():
    while True:
        logger.error('Authentication failure during API call to "https://api.example.com/v2/payments"; User ID: 389201; Token expired or invalid; Attempted at: 2023-08-07T13:49:19.765Z')
        time.sleep(5)

app = Flask(__name__)

@app.route('/')
def home():
    return 'Auth Error Simulator', 200

if __name__ == '__main__':
    t = threading.Thread(target=log_errors)
    t.start()
    app.run(host='0.0.0.0', port=8080)
