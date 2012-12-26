package com.wotriders
import groovy.time.*
import groovy.json.JsonSlurper

class GenerateHTML {

    static void main(String[] args) {
        boolean iframeDone = false
        // inversely sorts the tree map
        def comparator = [ compare: { first, second-> first.equals(second) ? 0: first > second  ? -1 : 1 } ] as Comparator
        TreeMap map = new TreeMap(comparator)

        new File("index.html").withWriter { writer ->
            writer.write """
<!DOCTYPE html>
<!--[if lt IE 7]>      <html class="lt-ie9 lt-ie8 lt-ie7"> <![endif]-->
<!--[if IE 7]>         <html class="lt-ie9 lt-ie8"> <![endif]-->
<!--[if IE 8]>         <html class="lt-ie9"> <![endif]-->
<!--[if gt IE 8]><!--> <html> <!--<![endif]-->
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
        <title>Wot Riders</title>
        <meta name="viewport" content="width=device-width">
        <link href='http://fonts.googleapis.com/css?family=Montserrat+Subrayada' rel='stylesheet' type='text/css'>
        <link rel="stylesheet" href="css/normalize.css">
        <link rel="stylesheet" href="css/bootstrap.min.css">
        <link rel="stylesheet" href="css/main.css">
    </head>
<body>

    <div class="container">

        <header>
            <h1>Wot Riders</h1>
            <p>30km/h average or bust</p>
            <span>Last updated ${new Date().format("d MMM")}</span>
        </header>
        """

            new File("segments").eachFile { def file ->
                if(file.isFile()) {
                    def segment = new JsonSlurper().parseText(file.text)

                    // get sorted efforts
                    def effortsSorted = segment.efforts.sort{ it.elapsedTime }

                    def best = effortsSorted[0]
                    def rup = effortsSorted[1]

                    // sorts out how many people have ridden the segment segment and add into the key X(times ridden) a list of the needed map data
                    // map = [5:[[id:123445, name:"CORO DRIVE TT", athlete: "Marcos", time:"120"], [id:778688, name:"GO BETWEEN BRIDGE", athlete: "Scott", time:"600"], ...], 4: [..]]
                    if(! map.containsKey(segment.efforts?.size())) { // generate the key
                        map << [ (segment.efforts?.size()) : []]
                    }

                    // add the data to the list
                    // safe navigation operator '?' rocks!
                    map[(segment.efforts?.size())] << [
                        id: segment.segment.id, name: 
                        segment.segment.name,
                        athlete: best.athlete.name, 
                        time: best.elapsedTime,
                        rupAthlete: rup?.athlete?.name,
                        rupTime: rup?.elapsedTime
                    ]
                }
            }

            map.each { riddenByXRiders, segments ->
                if(!iframeDone) {
                    iframeDone = true
                    writer.write """
                    <iframe name="embedme" height='337' width='100%' frameborder='0' allowtransparency='true' scrolling='no' src="http://app.strava.com/segments/${segments.first().id}/embed"></iframe>
                """
                }
                writer.write """
                <span class='ridden'>Ridden by: ${riddenByXRiders} wotriders</span>
                <table class="table table-striped">
                <tr>
                    <th>Segment</th>
                    <th>Leader</th>
                    <th>Time</th>
                    <th>Gap</th>
                    <th>Chaser</th>
                </tr>
            """
                segments.each { segmentBest ->
                    writer.write """
                    <tr>
                        <td><a href="http://app.strava.com/segments/${segmentBest.id}/embed" target="embedme">${segmentBest.name}</a></td>
                        <td>${segmentBest.athlete}</td>
                        <td>${segmentBest.time} secs</td>
                        <td>${segmentBest.rupTime? segmentBest.rupTime - segmentBest.time + " sec(s)" : ""}</td>
                        <td>${segmentBest.rupAthlete? segmentBest.rupAthlete : ""}</td>
                    </tr>
                """
                }
                writer.write "</table>"
            }

            writer.write '''
    </div>

</body>
</html>
'''
        }
    }

}