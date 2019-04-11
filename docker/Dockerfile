FROM ubuntu:16.04
RUN sed -i s@/archive.ubuntu.com/@/mirrors.aliyun.com/@g /etc/apt/sources.list\
    && apt-get update\
    && apt-get install -y g++ gcc time\
    && apt-get clean\
