<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
</head>
<body>

    <div class="container">

        <header>
            <img src="../images/motivated_by_strava.png" width="171" height="43">
            <h1>Wot Riders</h1>
            <p>30km/h average or bust</p>
            
        </header>
        <div class="profiles">
            <g:each var="member" in="${members}">
            <div>
                <a href="member?strava_id=${member.strava_id}"><img src="${member.avatar}"></a>
                <h3>${member.name}</h3>
                <span>${medals[member.strava_id][0]} gold, ${medals[member.strava_id][1]} silver, ${medals[member.strava_id][2]} bronze</span>
            </div>
            </g:each>
            <br>
        </div>
         <g:each var="riders" in="${riderCounts}">
         <span class="ridden">Ridden by: ${riders} wotriders</span>
         <table class="table table-striped">
                <tr>
                    <th style="width:35%; overflow:hidden">Segment</th>
                    <th>Leader</th>
                    <th>Time</th>
                    <th>Chaser/s</th>
                </tr>
                <g:each var="time" in="${besttimes}">
                    <g:if test="${ time.value.size() == riders}">
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
