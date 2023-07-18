FROM python:3.8-slim-bullseye

WORKDIR /gptcache-korean

COPY ./ ./

RUN apt update
RUN apt install -y libatlas-base-dev libgomp1
RUN pip install -r requirements.txt

WORKDIR ./src

RUN python3 init.py

CMD ["uvicorn", "server:app", "--host", "0.0.0.0", "--port", "80"]