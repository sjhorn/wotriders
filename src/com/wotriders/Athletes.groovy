package com.wotriders

import groovy.json.*

class Athletes {

    static main(args) {
        
        //new File("athlete_1300808.json").text = "http://app.strava.com/api/v1/rides?athleteId=1300808".toURL().text
        // Get Rides
//        def json = new JsonSlurper().parseText(new File("athlete_1300808.json").text)
//        
//        
//        
//        json.rides.each {
//              def efforts = new JsonSlurper().parseText(new File("ride_${it.id}").text) //=  "http://www.strava.com/api/v2/rides/${it.id}/efforts".toURL().text
//              it.efforts = efforts.efforts
//        }
//        
//        new File("athlete.json").text =  JsonOutput.prettyPrint(JsonOutput.toJson(json))
        
        def athlete = new JsonSlurper().parseText(new File("athlete.json").text)
        
        def segments = [:]
        (athlete.rides.efforts.segment).flatten().each {
            segments[it.id] = it.name
        }
        
        segments.each { id, name ->
            println "$id -> $name"
            if(! (new File("segments/${id}.json")).exists() ) {
                println "Getting ${name}"
                try {
                    new File("segments/${id}.json").text = "http://app.strava.com/api/v1/segments/${id}/efforts?clubId=18485&best=true".toURL().text
                } catch(e) {
                    System.err.println("Oops "+e.message);
                }
            }
        }
    }

}
