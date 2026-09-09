FROM ubuntu:latest
LABEL authors="gusta"

ENTRYPOINT ["top", "-b"]