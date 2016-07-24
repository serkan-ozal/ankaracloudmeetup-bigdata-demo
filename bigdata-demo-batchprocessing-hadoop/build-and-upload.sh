mvn clean install
mvn clean install package assembly:assembly
mvn beanstalk:upload-source-bundle
