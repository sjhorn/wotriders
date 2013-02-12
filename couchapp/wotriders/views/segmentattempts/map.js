function(doc) {
    if(doc.athlete_id) {
        doc.efforts.forEach(function(effort) {
	       emit([effort.segment.name.trim(),effort.segment.id], 1);
        })
    }
}