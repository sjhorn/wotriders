package wotriders

class HomeController {
    StravaService stravaService
    
    def index() { 
        def members = stravaService.members()
        def membersMap = members.inject([:]) { map, item ->
            map[item.strava_id] = item.name
            return map
        }
        def besttimes = stravaService.besttimes()
        Set riderCounts = besttimes.collect {
            it.value.size()
        } 
        
        
        return [
            members: members,
            membersMap : membersMap,
            medals: stravaService.medals(),
            besttimes: besttimes,
            riderCounts: (riderCounts as List).reverse()
        ]    
    }
    
    def member() {
        if(params.strava_id) {
            def members = stravaService.members()
            def membersMap = members.inject([:]) { map, item ->
                map[item.strava_id] = item.name
                return map
            }
            def besttimes = stravaService.besttimes().findAll {  time ->
                time.value[0][0].toString() == params.strava_id || 
                    ( time.value.size() > 1 && time.value[1][0].toString() == params.strava_id ) || 
                    ( time.value.size() > 2 && time.value[2][0].toString() == params.strava_id )
            }
            
            return [
                membersMap : membersMap,
                besttimes: besttimes,
                medal: [ "Gold", "Silver", "Bronze"],
                strava_id: params.strava_id
            ]
        } else {
            redirect("/")
        }
    }
}
