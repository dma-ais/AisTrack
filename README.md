AisTrack
========

Live monitoring of AIS vessel targets

## Prerequisites

* Java 8
* Maven 3


## Building ##

	mvn clean install

## Running ##

	java -jar target/ais-track-0.1-SNAPSHOT.jar -bus aisbus.xml -conf aistrack.properties
	
## REST API ##

#### Vessel target information

	http://locahost:8080/target/vessel/{mmsi}
	
#### Vessel target list

	http://locahost:8080/target/vessel/list
	
The following GET arguments can be supplied for filtering 

  * `ttlLive` - Time to live for live targets (format: https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html#parse-java.lang.CharSequence)
  * `ttlSat` - Time to live for sat targets (format: https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html#parse-java.lang.CharSequence)
  * `mmsi` - Multiple mmsi number can be given
  * `geo` - Filter on geography, multiple arguments on the form: `circle,lat,lon,radius(m)` or `bb,lat1,lon1,lat2,lon2`  (bounding box) 

#### Vessel target count

	http://locahost:8080/target/vessel/count
	
Same arguments as list

#### Vessel track

	http://locahost:8080/target/vessel/track/{mmsi}

Past track for the given vessel. The following GET arguments can be supplied for trimming the output  

  * `minDist` (meters) - Samples the past track. The minimum distance between
	positions will be `minDist`. This argument can greatly reduce the number of track points for vessels at berth or anchor.
  * `age` - How long back to get past track for (format: https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html#parse-java.lang.CharSequence)

#### Vessel max speed

	http://locahost:8080/target/vessel/maxspeed/{mmsi}
	
The maximum speed encountered for the given vessel.

#### Metrics

A complete set of metrics for system can be retrieved here

	http://localhost:8080/metrics/all
	
To monitor the rate of a meter or timer the following call can be used

	http://localhost:8080/metrics/flow/{meter}[?expected=flow]
	
The argument `expected` can be used to indicate what is the expected flow. Everything else is an error. Default is 0.0.
Example

	http://localhost:8080/metrics/flow/messages?expected=300

Output

	{
		rate: "688.3288056391231",
		expexted: "300.0",
		status: "ok"
	}

The used properties can be inspected using the following call

	http://localhost:8080/metrics/properties
