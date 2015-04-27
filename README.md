AisTrack
========

AisTrack receives continuous input of AIS data streams from different sources
and maintains a collection of 'targets' (aka tracks) with the most recent and consolidated information from each source.

AisTrack can be queried for track information by source and selected target
attributes using a Java API or a RESTful interface returning JSON.

## Prerequisites

* Java 8
* Maven 3
* (Docker)

## Building ##

	mvn clean install

## Configuring ##

Before launch an aisbus.xml file must be prepared
([example](https://github.com/dma-ais/AisTrack/blob/master/ais-track-common/src/main/resources/aisbus.xml)).

Aisbus.xml configures the AIS sources and input filtering in front of the tracker.


## Running ##

### Directly from the command line:

1. Go to a working directory where ais-track-rest-0.1-SNAPSHOT.jar resides; e.g.:
  
        $ cd ais-track-rest/target/

1. Make sure that aisbus.xml is located in data/aisbus.xml:

        $ ls -l data/
        total 8
        -rwxr-xr-x+ 1 tbsalling  staff  1317 27 Apr 09:00 aisbus.xml

1. Launch the tracker:

        $ java -jar ais-track-rest-0.1-SNAPSHOT.jar

### Using Docker:

1. Pull the latest Docker image:

        $ sudo docker pull dmadk/ais-track:latest

1. Make sure that aisbus.xml is located in some directory, say ~/tmp:

        $ ls -l ~/tmp/
        total 8
        -rwxr-xr-x+ 1 tbsalling  staff  1317 27 Apr 09:00 aisbus.xml

1. Run the docker image - with ~/tmp mounted as /data:

        $ sudo docker run -v ~/tmp:/data dmadk/ais-track:latest


## REST API ##

#### Query status information
Query for status information from the tracker:
	http://localhost:8080/
(currently response is text format; not JSON)

#### Query all targets
Return the most recent information known about any target regardless which source the information is received from:

	http://localhost:8080/tracks

Beware, that if tens or hundreds of thousands of targets are tracked, then this response can be quite large.

The set of returned targets can be limited through filtering. E.g. filtering to include only given MMSI numbers:

	http://localhost:8080/tracks?mmsi=244820404&mmsi=345070335

Or including only targets inside a given geographical bounding box:

	http://localhost:8080/tracks?area=52.3|4.8|52.5|4.9

Or including only targets inside any of a set of geographical bounding boxes:

	http://localhost:8080/tracks?area=52.3|4.8|52.5|4.9&area=20.0|100.0|21.0|110.0

Or including only targets matching given MMSI numbers OR given geographical bounding boxes:

	http://localhost:8080/tracks?mmsi=244820404&mmsi=345070335&area=52.0|4.0|52.5|5.0&area=20.0|100.0|21.0|110.0

#### Query all targets limited by source
The queries shown above all return the most recent information known about the
matching targets - regardless which source provided the information.

It is possible to take into considetaration only certain sources, by supplying a source filter expression to limit the source taken into account ([Read more about the source filter expressions](https://github.com/dma-ais/AisLib#filtering-on-packets-source)).

	http://localhost:8080/tracks?sourceFilter=s.country%20in%20(DK)
	http://localhost:8080/tracks?sourceFilter%3Ds.region%20%3D%20819

E.g. return latest information about MMSI 244820404 and MMSI 345070335 using only Danish sources:

	http://localhost:8080/tracks?mmsi=244820404&mmsi=345070335&sourceFilter=s.country%20in%20(DK)

#### Query a specific target by source
As a supplement to quering all targets and filtering them by MMSI, it is also possible to query the service for a single specific target using its MMSI no. as key:

	http://localhost:8080/track/257742710

This can also be combined with limiting the set of contributing sources:

	http://localhost:8080/track/257742710?sourceFilter=s.country%20in%20(DK%2C%20NO)

### JSON

The returned target information is in JSON format and complies with this format:

	{
	  "source" : {
	    "country" : "DK",
	    "bs" : 2190077
	  },
	  "target" : {
	    "mmsi" : 219007235,
	    "country" : "DK",
	    "lastReport" : "2015-04-27T08:23:13.707Z",
	    "created" : "2015-04-24T14:26:36.667Z",
	    "vesselStatic" : {
	      "mmsi" : 219007235,
	      "received" : "2015-04-24T14:26:36.670Z",
	      "sourceTimestamp" : "2015-04-27T08:23:13.707Z",
	      "created" : "2015-04-24T14:26:36.667Z",
	      "name" : "R67 KIKI LOUISE",
	      "callsign" : "XP5636",
	      "shipType" : 30,
	      "shipTypeCargo" : {
	        "shipType" : "FISHING",
	        "shipCargo" : "UNDEFINED"
	      },
	      "dimensions" : {
	        "dimBow" : 5,
	        "dimStern" : 7,
	        "dimPort" : 3,
	        "dimStarboard" : 1
	      }
	    },
	    "vesselPosition" : {
	      "mmsi" : 219007235,
	      "received" : "2015-04-24T14:26:36.670Z",
	      "sourceTimestamp" : "2015-04-27T08:23:32.613Z",
	      "created" : "2015-04-24T14:26:36.670Z",
	      "sog" : 0.3,
	      "cog" : 187.9,
	      "pos" : {
	        "lat" : 54.8387,
	        "lon" : 15.093032
	      },
	      "posAcc" : 0,
	      "utcSec" : 32,
	      "raim" : 1
	    },
	    "targetType" : "B"
	  }
	}
