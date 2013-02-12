function(keys, values, rereduce) { 
   var segments = {};
   if(!rereduce) {
      for(var i = 0; i < keys.length; i++) {
         var value = values[i];
         var athlete_id = value[0];
         if(!segments[athlete_id]) segments[athlete_id] = [];
         var membertimes = segments[athlete_id];
         membertimes.push(value[1]);
      }

      for(athlete_id in segments) {
           segments[athlete_id] = Math.min.apply( Math, segments[athlete_id] )
      } 
      
   } else {
      for(var i = 0; i < values.length; i++) {
         var value = values[i];
         for(var athlete_id in value) {
            if(!segments[athlete_id]) segments[athlete_id] = [];
            segments[athlete_id].push(value[athlete_id]);
         }
      }
      for(var athlete_id in segments) {
         segments[athlete_id] = Math.min.apply( Math, segments[athlete_id] )
      } 
   }
   return segments;
}