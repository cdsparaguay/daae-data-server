#Version 1 de Dockerfile
FROM    ubuntu:16.04
LABEL   description="This is a custom pentaho kettle container image" \
        author="Hector Martinez <hmartinez.py@gmail.com>"


# APT-GET UPDATE
RUN apt-get update

# Install OS utils
RUN apt-get install -y \
        vim \
        zip \
        unzip \
        telnet \
        wget \
        mlocate

RUN apt-get update
RUN apt-get -y install default-jre

ADD https://downloads.sourceforge.net/project/pentaho/Data%20Integration/7.1/pdi-ce-7.1.0.0-12.zip /opt/
RUN cd /opt/ && unzip pdi-ce-7.1.0.0-12.zip

WORKDIR /opt/data-integration

#EXPOSE 8181

#CMD ["./carte.sh", "0.0.0.0", "8181"]
