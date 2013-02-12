function(doc) {
  if(doc.athlete_id) {
        doc.efforts.forEach(function(effort) {
  	     emit(effort.segment.id, null);
        })
  }
}