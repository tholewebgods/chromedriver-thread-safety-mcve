
# MCVE to demonstrate multi threading issues with Chromedriver

# Setup

- Java 1.8.0-181
- Chrome 73.0.3683.86
- Chromedriver 73.0.3683.68
- Selenium 3.4.0
- Maven 3.5.4

# Run

## Start Selenium with

```
/home/dev/opt/jdk8/bin/java \
	-Dwebdriver.chrome.driver=/home/dev/opt/selenium/drivers/chrome/chromedriver-73.0.3683.68/chromedriver \
	-jar /home/dev/opt/selenium/selenium/selenium-server-standalone-3.4.0.jar
```

## Start the server

```
cd src/test/data/ ; python -m SimpleHTTPServer 8080 ; cd -
```

## Start the test with

```
/home/dev/opt/maven3/bin/mvn clean test
```

It might be necessary to run the tests multiple times until a failure occurs.

```
for n in $( seq 1 10 ); do /home/dev/opt/maven3/bin/mvn clean test; done
```
