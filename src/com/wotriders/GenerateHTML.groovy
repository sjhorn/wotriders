package com.wotriders
import groovy.time.*
import groovy.json.JsonSlurper

class GenerateHTML {

    static void main(String[] args) {
        def index = 0;
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

        new File("segments").eachFile {
            if(it.name.startsWith('.')) return
            def segment = new JsonSlurper().parseText(it.text)
            def best = segment.efforts.min { it.elapsedTime }
            if (index == 0) {
                 writer.write """
                <iframe name="embedme" height='337' width='100%' frameborder='0' allowtransparency='true' scrolling='no' src="http://app.strava.com/segments/${segment.segment.id}/embed"></iframe>

                <table class="table table-striped">
                <tr>
                        <th>Segment</th>
                        <th>Leader</th>
                        <th>Time</th>
                </tr>
                """
            }
            index++;

            writer.write """
            <tr>
                <td><a href="http://app.strava.com/segments/${segment.segment.id}/embed" target="embedme">${segment.segment.name}</a></td>
                <td>${best.athlete.name}</td>
                <td>${best.elapsedTime} secs</td>
            </tr>
            """

        }
        writer.write '''
        </table>
        
    </div>

</body>
</html>
'''
        }
    }

}