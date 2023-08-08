from flask import Flask
import logging
import time
import threading

logging.basicConfig(level=logging.ERROR, format='%(asctime)s ERROR 1 --- [scheduling-1] c.h.server1.service.SchedulerService : %(message)s')
logger = logging.getLogger()

def log_errors():
    while True:
        logger.error('Error occurred during database connection: Connection refused; Failed attempts: 7; Last host attempted: 192.168.2.15; Cause: Network configuration mismatch')
        time.sleep(5)

app = Flask(__name__)

@app.route('/')
def home():
    return 'DB Error Simulator', 200

if __name__ == '__main__':
    t = threading.Thread(target=log_errors)
    t.start()
    app.run(host='0.0.0.0', port=8080)
