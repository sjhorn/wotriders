<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
</head>
<body>

    <div class="container">
        <a href="/">
        <header>
            <img src="../images/motivated_by_strava.png" width="171" height="43">
            <h1>Wot Riders</h1>
            <p>30km/h average or bust</p>
            
        </header>
        </a>
         <g:each var="idx" in="${0..2}">
         <span class="ridden">${medal[idx]}</span>
         <table class="table table-striped">
                <tr>
                    <th style="width:35%; overflow:hidden">Segment</th>
                    <th>Leader</th>
                    <th>Time</th>
                    <th>Chaser/s</th>
                </tr>
                <g:each var="time" in="${besttimes}">
                    <g:if test="${time.value.size > idx && time.value[idx][0].toString() == strava_id}">
                    <tr>
                        <td><a href="http://app.strava.com/segments/${time.key[0]}" target="_blank">${time.key[1]}</a></td>
                        <td>${membersMap[time.value[0][0]]}</td>
                        <td>${time.value[0][1]} secs</td>
                        <td>
                            <g:if test="${time.value.size > 1}">
                            ${membersMap[time.value[1][0]]} (${time.value[1][1] - time.value[0][1]}s)
                            </g:if>
                            <g:if test="${time.value.size > 2}">
                            , ${membersMap[time.value[2][0]]} (${time.value[2][1] - time.value[0][1]}s)
                            </g:if>
                        </td>
                    </tr>
                    </g:if>
               </g:each>
         </table>
         </g:each>
    </div>

</body>
</html>
