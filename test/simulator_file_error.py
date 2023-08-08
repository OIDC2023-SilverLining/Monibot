from flask import Flask
import logging
import time
import threading

logging.basicConfig(level=logging.ERROR, format='%(asctime)s ERROR 2 --- [scheduling-2] c.h.server2.service.FileService : %(message)s')
logger = logging.getLogger()

def log_errors():
    while True:
        logger.error('Error occurred during file processing: File /data/input.csv not found; Required by: DataProcessingJob; Job ID: 12345; User: user1')
        time.sleep(5)

app = Flask(__name__)

@app.route('/')
def home():
    return 'File Error Simulator', 200

if __name__ == '__main__':
    t = threading.Thread(target=log_errors)
    t.start()
    app.run(host='0.0.0.0', port=8080)
