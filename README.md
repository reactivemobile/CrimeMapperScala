# CrimeMapperScala [![Build Status](https://travis-ci.org/reactivemobile/CrimeMapperScala.svg?branch=master)](https://travis-ci.org/reactivemobile/CrimeMapperScala)
This is a simple Scala application for downloading and displaying crime report data from the UK Police. See https://data.police.uk/ for API documentation

The app is just a proof-of-concept / learning experience for the moment. 

There is NO error checking right now!

## Instructions

* Either build the project or download the jar file from releases directory
* To simply test that the project works, execute `java -jar crimemapper_1_0.jar` in the command line. This gets the latest crime reports from central London.
* To get the list of dates when crime reports are available execute `java -jar crimemapper_1_0.jar -availability`
* To get crimes for a specific location and date use the following ` java -jar crimemapper_1_0.jar -date [date] -location [latitude,longitude]`. For example `java -jar crimemapper_1_0.jar -date 2012-03 -location 51.494698,-0.015363`
