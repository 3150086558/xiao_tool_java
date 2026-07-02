FROM python:3.12-slim
WORKDIR /app
COPY requirements.txt /app/requirements.txt
RUN pip install --no-cache-dir -r requirements.txt
COPY . /app
EXPOSE 8000
ENV APP_HOST=0.0.0.0
ENV APP_PORT=8000
CMD ["python", "app.py"]
