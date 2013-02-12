package wotriders

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.wotriders.HttpClient

class StravaService {
    def grailsApplication
    HttpClient couchHttp
    def cache
    
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
            value.eachWithIndex { athleteData, index ->
                def athlete = athleteData[0]
                if(!medals[athlete]) {
                    medals[athlete] = []
                }
                if(!medals[athlete][index]) {
                    medals[athlete][index] = 0
                }
                medals[athlete][index]++
            }
        }
        return medals.sort { it.value[0] }
    }
    
    List besttimes() {
        return cache.get("besttimes")
    }
    
}
