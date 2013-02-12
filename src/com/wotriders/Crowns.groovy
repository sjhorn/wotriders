package com.wotriders

class Crowns {

    static main(args) {
        HttpClient couchHttp = new HttpClient()
        couchHttp.username = "wotrider"
        couchHttp.password = new File("${System.getProperty('user.home')}/wotrider.txt").text?.trim()
        
        Map crowns = [:]
        def json = couchHttp.get("http://localhost:5984/wotriders/_design/wotriders/_view/besttimes?group_level=2").json
        json.rows.value.each { value ->
            value.eachWithIndex { athleteData, index ->
                def athlete = athleteData[0]
                if(!crowns[athlete]) {
                    crowns[athlete] = [0, 0, 0]
                }
                crowns[athlete][index]++
            }
        }
        crowns = crowns.sort { it.value[0] }
        
        def totalgold = 0
        crowns.reverseEach { k, v ->
            println "${k} -> ${v[0]} gold, ${v[1]} silver, ${v[2]} bronze"
            totalgold +=  v[0]
        }
        println "Total gold ${totalgold}"
    }

}
