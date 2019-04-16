# Scala K-means example in Jupyter Notebook
In the example has been used algorithm k-means from library [Smile](https://haifengl.github.io/smile). You can run the scala notebook in Jupyter with support BeakerX. For simple start you can use docker image:
```bash
git clone https://github.com/AntonYurchenko/habr.git /tmp/habr

docker run -it \
    -p 8888:8888 \
    -v /tmp/habr/scala-smile-clustering:/workspace/scala-smile-clustering \
    entony/jupyter-scala:1.4.1
```
