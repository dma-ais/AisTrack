FROM java:8

RUN mkdir -p /data
RUN wget --progress=bar "https://dma.ci.cloudbees.com/job/AisTrack/lastSuccessfulBuild/dk.dma.ais.track\$ais-track-rest/artifact/dk.dma.ais.track/ais-track-rest/0.1-SNAPSHOT/ais-track-rest-0.1-SNAPSHOT.jar"
EXPOSE 8080
ADD ./start.sh /start.sh
RUN chmod u+x /start.sh

ENV DIRECTORY /data

CMD ["/bin/bash", "/start.sh"]

