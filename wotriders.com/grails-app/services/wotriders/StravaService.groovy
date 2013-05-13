package wotriders

import groovy.json.JsonSlurper

import java.util.concurrent.TimeUnit

import javax.annotation.PostConstruct

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.wotriders.HttpClient

class StravaService {
    def grailsApplication
    HttpClient couchHttp
    Cache cache
    
    @PostConstruct
    public setup() {
        couchHttp = new HttpClient()
        couchHttp.username = "wotrider"
        couchHttp.password = new File("${System.getProperty('user.home')}/wotrider.txt").text?.trim()
        couchHttp.log.error = { msg -> log.error(msg) }
        
        cache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .maximumSize(500)
            .build(new CacheLoader() {
                public Object load(Object key) {
                    switch(key) {
                        case "members":
                            return couchHttp.get("${grailsApplication.config.couchdb}/_design/wotriders/_view/members")?.json?.rows?.value
                            break
                        case "besttimes":
                            return couchHttp.get("${grailsApplication.config.couchdb}/_design/wotriders/_view/besttimes?group_level=2")?.json?.rows
                            break
                    }
                }
            }
        )
    }
    
    List members() { 
        return cache.get("members")
    }
    
    Map medals() {
        Map medals = [:]
        cache.get("besttimes")?.value?.each { value ->
            int medal = 0
            long lastTime = 0            
            value.eachWithIndex { athleteData, index ->
                String athlete = athleteData[0]
                long time = athleteData[1] as long
                if(!medals[athlete]) {
                    medals[athlete] = [0,0,0]
                }
                if(lastTime != time) {
                    medal = index
                }
                medals[athlete][medal]++
                lastTime = time
            }
        }
        return medals.sort { it.value[0] }
    }
    
    List besttimes() {
        return cache.get("besttimes")
    }
    
	void clearCache() {
		cache.invalidateAll()
	}
	
	static void main(String[] args) {
		String jsonText = '''
{"rows":[
{"key":[574115,"Mount Gravatt"],"value":[[1085072,466]]},
{"key":[608674,"Murarrie Crit Course"],"value":[[1153292,144],[1728968,261]]}
]}
'''
		def json = new JsonSlurper().parseText(jsonText)
		
	}
}
