function(doc) {
  if(doc.athlete_id) {
    doc.efforts.forEach(function(effort) {
      emit([doc.athlete_id, effort.segment.id], effort.effort.moving_time);
    })
  }
}