function(doc) {
  if(doc.athlete_id) {
        doc.efforts.forEach(function(effort) {
  	     emit([effort.segment.id, effort.segment.name], [[doc.athlete_id, doc.athlete_name], effort.effort.moving_time] );
        })
  }
}