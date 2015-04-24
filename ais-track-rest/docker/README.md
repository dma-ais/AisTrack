To launch the AisTrack REST server:

```bash
sudo docker run -v ~/tmp:/data dmadk/ais-track:latest
```

Where ~/tmp is replaced by a directory on the host machine containing a valid aisbus.xml file. An example of such a file is found [here](https://github.com/dma-ais/AisTrack/blob/master/ais-track-common/src/main/resources/aisbus.xml).
