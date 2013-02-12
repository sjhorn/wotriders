function(doc) {
  if(doc.athlete_id) {
        doc.efforts.forEach(function(effort) {
  	     emit([effort.segment.id, doc.athlete_id, effort.effort.moving_time], effort.effort.moving_time);
        })
  }
}