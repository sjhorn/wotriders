package com.wotriders

import groovy.json.*

class Athletes {

    final File log

    public Athletes() {
        log = new File("execution.log")
        if(log.exists()) log.delete() // always create one fresh for the current execution
    }


    static main(args) {

        Athletes athletes = new Athletes()

        def clubAthletes = athletes.getContent("http://app.strava.com/api/v1/clubs/18485/members")
        def json = new JsonSlurper().parseText(clubAthletes)
        def club = json.club
        def members = json.members

        // this will run every time since will get all my new rides
        members.each { def member ->
            athletes.log "fetching ${member.name} rides"

            def file = new File("members/${member.name.replace(" ", "_").replace(".", "").toLowerCase()}.json") //thanks my long name we have to replace the dots
            def content = athletes.getContent("http://app.strava.com/api/v1/rides?athleteId=${member.id}")
            if (content) {
                file.text = content
            }
        }

        new File('members').eachFile { File file ->
            if (file.isFile()) {
                def name = file.name.split("_").first()
                def rider = new JsonSlurper().parseText(file.text)

                rider.rides.each { def ride ->
                    def rideFile = new File("rides/${name}_${ride.id}.json")

                    athletes.log "fetching ${name} segment id ${ride.id}"
                    if (!rideFile.exists()) { // we only need to fetch the new rides, since the previous ones wont ever change

                        def content = athletes.getContent("http://www.strava.com/api/v2/rides/${ride.id}/efforts")
                        if (content) {
                            rideFile.text = content
                        }
                    } else {
                        athletes.log "existent segment file: ${rideFile.name}"
                    }
                }
            }
        }

        // we will fetch all the segments even if only one of us have ridden, after having it all we can play with the data
        new File('rides').eachFile { File file ->
            if (file.isFile()) {
                def ride = new JsonSlurper().parseText(file.text)

                ride.efforts.each { def effort ->
                    def segmentFile = new File("segments/${effort.segment.id}.json")
                    // we only need to fetch the new efforts/segments, since the previous ones wont ever change
                    // and if already fetched from previous athlete do not do it again.
                    athletes.log "fetching leaderboard for ${effort.segment.name} - ${effort.segment.id}"

                    if (! segmentFile.exists()) {
                        def content = athletes.getContent("http://app.strava.com/api/v1/segments/${effort.segment.id}/efforts?clubId=18485&best=true")
                        if (content) {
                            segmentFile.text = content
                        }
                    } else {
                        athletes.log "existent leaderboard file: ${segmentFile.name}"
                    }
                }
            }
        }

    }

    String getContent(String uri) {
        String content = null
        try{
            URL url = uri?.toURL()
            URLConnection connection = url.openConnection()
            if(connection.responseCode >= 200 && connection.responseCode <= 300) {
                content = connection.content.text
            } else {
                log "${connection.responseCode}: ${uri}"
            }
        } catch(Exception e) {
            log "exception ocurrend: ${e.cause}"
        } finally {
            return content
        }
    }

    void log(String line) {
        log << "${line}\n"
    }

}
