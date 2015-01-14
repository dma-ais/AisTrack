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

