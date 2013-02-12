package com.wotriders

import groovy.json.*

class Strava2Couch {
    static COUCHDB_HOST = new File("${System.getProperty('user.home')}/wotrider_couchdb.txt").text?.trim()
    HttpClient stravaHttp
    HttpClient couchHttp
    int newMembers = 0
    int newRides = 0
    int bestRides = 0
    
    static class Athlete {
        String id
        String name
    }

    static void main(String[] args) {
        Date start = new Date()
        println "Started processing at ${start}"
        HttpClient stravaHttp = new HttpClient()
        stravaHttp.connectTimeout = 5000
        stravaHttp.followRedirects = true

        HttpClient couchHttp = new HttpClient()
        couchHttp.username = "wotrider"
        couchHttp.password = new File("${System.getProperty('user.home')}/wotrider.txt").text?.trim()
        couchHttp.log.error = { msg -> }

        Strava2Couch s2c = new Strava2Couch(stravaHttp:stravaHttp, couchHttp: couchHttp)
        
        List<Athlete> athletes = s2c.updateAndGetAthletes()
        /*
        athletes?.each { Athlete athlete ->
            s2c.updateRides(athlete)
        }
        */
        
        s2c.updateBestTimes()
        
        Date end = new Date()
        println "Completed processing at ${end}, ${s2c.newMembers} new members, ${s2c.newRides} new rides, ${s2c.bestRides} best rides, processing time ${end.time - start.time} ms"
    }
    
    
    List<Athlete> updateAndGetAthletes() {
        def res = stravaHttp.get("http://app.strava.com/api/v1/clubs/18485/members")
        if(res.code == 200) {
            def json = res.json
            res = couchHttp.post("${COUCHDB_HOST}/_all_docs", new JsonBuilder([keys: json.members.collect { "member_${it.id}" } ]).toString())
            if(res.code == 200) {
                def matches = res.json
                def newMemberIds = matches.rows.findAll { it?.error || it?.value?.deleted }?.key
                newMembers = newMemberIds.size()
                json.members.findAll { "member_${it.id}".toString() in newMemberIds }.each {
                    Map doc = [
                        strava_id: it.id,
                        name: it.name,
                        doc_type: "member",
                        avatar: "http://d26ifou2tyrp3u.cloudfront.net/assets/avatar/athlete/large-162a16b208e0364313ec8ce239ffd7a8.png"
                    ]
                    res = couchHttp.put("${COUCHDB_HOST}/member_${it.id}", new JsonBuilder(doc).toString())
                    if(res.code != 201) {
                        println "Failed to add new member ${it.name} ${res.body}"
                    } 
                }
            } else {
                println "Failed to retrieve existing users from the couchdb, ${res.body}"
            }
            return json.members.collect { new Athlete(id:it.id, name: it.name) }
        } else {
            println "Failed to retreive members from Strava ${res.body}"
        }
        return null
    }
    
    void updateRides(Athlete athlete) {
        def offset = 0
        while(true) {
            println "Getting http://app.strava.com/api/v1/rides?athleteId=${athlete.id}&offset=${offset}"
            def res = stravaHttp.get("http://app.strava.com/api/v1/rides?athleteId=${athlete.id}&offset=${offset}")
            if(res.code == 200 && res.json?.rides?.size()) {
                def json = res.json
                res = couchHttp.post("${COUCHDB_HOST}/_all_docs", new JsonBuilder([keys: json.rides.collect { "ride_${athlete.id}_${it.id}" } ]).toString())
                if(res.code == 200) {
                    def matches = res.json
                    def newRideIds = matches.rows.findAll { it?.error || it?.value?.deleted }?.key
                    newRides += newRideIds.size()
                    json.rides.findAll { "ride_${athlete.id}_${it.id}".toString() in newRideIds }.each { ride -> 
                        res = stravaHttp.get("http://www.strava.com/api/v2/rides/${ride.id}/efforts")
                        if(res.code == 200) {
                            def doc = res.json
                            def docId = "ride_${athlete.id}_${doc.id}"
                            doc.doc_type = "ride"
                            doc.athlete_id = athlete.id
                            doc.athlete_name = athlete.name
                            doc.ride_id = doc.id
                            doc.remove("id")
                            res = couchHttp.put("${COUCHDB_HOST}/${docId}", new JsonBuilder(doc).toString())
                            if(res.code != 201) {
                                println "Failed to add new ride ${docId}, ${res.body}"
                            }
                        } else {
                            println "Failed to grab new ride with id ${ride.id}, ${res.body}"
                        }
                    }
                } else {
                    println "Problem retrieving matches from couchdb, ${res.body}"
                    return
                }
            } else {
                println "No more rides, done here"
                return
            }
            offset += 50
        }
    }
    
    void updateBestTimes() {
        def res = couchHttp.get("${COUCHDB_HOST}/_design/wotriders/_view/segmentids?group=true")
        if(res.code == 200) {
            println res.json.rows
            res.json.rows.key.eachWithIndex { it, index ->
                println "${index}. Getting best times for ${it}"
                res = stravaHttp.get("http://app.strava.com/api/v1/segments/${it}/efforts?clubId=18485&best=true")
                if(res.code == 200) {
                    def best_doc = res.json
                    best_doc.id = "best_${it}"
                    best_doc.doc_type = "best"
                    res = couchHttp.get("${COUCHDB_HOST}/${best_doc.id}")
                    if(res.code == 200) {
                        best_doc._rev = res.json._rev
                    }
                    res = couchHttp.put("${COUCHDB_HOST}/${best_doc.id}", new JsonBuilder(best_doc).toString())
                    if(res.code != 201) {
                        println "Failed to add new best_doc ${best_doc.id}, ${res.body}"
                    }
                }
                bestRides++
            }
        } else {
            println res.body
        }
    }
    
}