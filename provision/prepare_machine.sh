#!/bin/sh
echo Provisioning the machine

PROJECT_HOME=/project
PROGRAMS_HOME=/opt

echo ******************************************************************
echo ** Setting up tools                                             **
echo ******************************************************************
echo

echo preparing package sources..
#from http://www.scala-sbt.org/0.13/tutorial/Installing-sbt-on-Linux.html
echo "deb http://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
sudo apt-get update

echo installing JDK
# --no-install-recommends avoids installing X11 stuff.
apt-get -y --no-install-recommends install openjdk-7-jdk

DOWNLOAD_DIR=/tmp/download
mkdir -p $DOWNLOAD_DIR

echo installing sbt
#--force-yes - for WARNING: The following packages cannot be authenticated!
sudo apt-get -y --force-yes --no-install-recommends install sbt

FUSEKI_HOME=$PROGRAMS_HOME/apache-jena-fuseki-2.0.0
echo installing Fuseki to $FUSEKI_HOME
if [ -e $FUSEKI_HOME ]
then
    echo already installed, skip
else
    FUSEKI_DIST=apache-jena-fuseki-2.0.0.tar.gz
    wget -P $DOWNLOAD_DIR http://apache.cp.if.ua/jena/binaries/$FUSEKI_DIST

    tar -xvzf $DOWNLOAD_DIR/$FUSEKI_DIST -C $PROGRAMS_HOME
    rm $DOWNLOAD_DIR/$FUSEKI_DIST
    cd $FUSEKI_HOME

    mkdir $FUSEKI_HOME/run
    cp $PROJECT_HOME/provision/config/fuseki-shiro.ini $FUSEKI_HOME/run/shiro.ini

    ./fuseki-server --update --mem /ds
fi



echo ******************************************************************
echo ** Finalizing setup                                             **
echo ******************************************************************
echo

echo setting up environment

ENV_FILE=/etc/profile.d/openreveal-env.sh
echo "#!/bin/sh
export PROGRAMS_HOME=$PROGRAMS_HOME
export SBT_HOME=$MAVEN_HOME
export FUSEKI_HOME=$FUSEKI_HOME
export PATH=$PATH:\$SBT_HOME/bin:\$FUSEKI_HOME/bin
" > $ENV_FILE

echo ******************************************************************
echo ** Provisioning complete, ready to build and deploy projects    **
echo ******************************************************************


