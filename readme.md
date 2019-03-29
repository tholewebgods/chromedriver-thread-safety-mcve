
# MCVE to demonstrate multi threading issues with Chromedriver

# Problem description

Chromedriver and/or Chrome are not able to type into an input field and 
maintain the focus when multiple tests run in parallel.


## The test application

The test application implemented an input field which selects the text on 
focus (`focusin`). The implementation is documented in the related example 
file under [`src/test/data`](src/test/data/chromedriverthreadsafetymcve.html).


## The Selenium test

Tests run in parallel using TestNG's parallelism feature. Each thread creates 
its own Webdriver instance and thus Chrome session. Each test will click and 
type into the input field. One test is constantly typing into an input field 
while the other tests are pending. This is just to increase the likeliness of 
stealing the focus.


## The problem

Tests may occasionally fail with errors like this:

> expected [D4E4M4O4S4T4R4I4N4G4] but found [4O4S4T4R4I4N4G4]

One can observe that some characters are missing at the start of the string.
Sometimes more than one test will fail.


## Theory on the error cause

The fact that multiple Chrome instances type into input fields at the same 
time suggests the assumption that each window will steal another window's 
focus.

Since the application under test is selecting the text on focus the first 
typed characters get selected with the next character being typed and then 
erased.


## Tunable parameters

The likeliness of the error can be controlled by two factors, basically

- how many tests run in parallel (three by default, the more the likely)
- wait delay after the click in the `typeAndVerify()` method


## Note on real world test setup

This MCVE was created to reproduce this error with as little code as possible.
The real world application is much more complicated, only three tests run in 
parallel and the tests do other things as well (e.g. click buttons).


# Setup

- Java 1.8.0-181
- Chrome 73.0.3683.86
- Chromedriver 73.0.3683.68
- Firefox 65.0
- Geckodriver 0.24.0
- Selenium 3.4.0
- Maven 3.5.4

# Run

## Start Selenium with

```
/home/dev/opt/jdk8/bin/java \
	-Dwebdriver.chrome.driver=/home/dev/opt/selenium/drivers/chrome/chromedriver-73.0.3683.68/chromedriver \
	-Dwebdriver.gecko.driver=/home/dev/opt/selenium/drivers/gecko/geckodriver-0.24.0/geckodriver \
	-jar /home/dev/opt/selenium/selenium/selenium-server-standalone-3.4.0.jar
```

The Geckodriver can be omitted when testing against Chrome only.

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

## Testing with Firefox

You may want to test against Firefox to observe the tests running flawless.

```
/home/dev/opt/maven3/bin/mvn -Dusegecko=true clean test
```
