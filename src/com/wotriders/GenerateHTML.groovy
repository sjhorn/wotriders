package com.wotriders
import groovy.time.*
import groovy.json.JsonSlurper

class GenerateHTML {

    static void main(String[] args) {
        new File("index.html").withWriter { writer ->
        writer.write '''
<!DOCTYPE html>
<html>
<head>
<link href="http://twitter.github.com/bootstrap/1.4.0/bootstrap.min.css" rel="stylesheet" media="screen">
<style>
        .alt { background-color: #eee }
</style>
</head>
<body>

<div style="float:left; width:420px; height:405px; overflow:auto">
<table>
<tr>
        <td>Segment</td>
        <td>Leader</td>
        <td>Time</td>
</tr>
'''
        int zebra = 0
        new File("segments").eachFile {
            
            def segment = new JsonSlurper().parseText(it.text)
            def best = segment.efforts.min { it.elapsedTime }
            writer.write """
<tr ${zebra % 2 == 0 ? '' :'class="alt"'}>
    <td><a href="http://app.strava.com/segments/${segment.segment.id}/embed" target="embedme">${segment.segment.name}</a></td>
    <td>${best.athlete.name}</td>
    <td>${best.elapsedTime} secs</td>
</tr>
"""
            zebra++
        }
        writer.write '''
</table>
</div>
<iframe name="embedme" height='405' width='590' frameborder='0' allowtransparency='true' scrolling='no' src=''></iframe>
</body>
</html>
'''
        }
    }

}
