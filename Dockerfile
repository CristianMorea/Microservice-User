FROM ubuntu:latest
LABEL authors="crist"

ENTRYPOINT ["top", "-b"]