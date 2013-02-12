function(doc) {
  if(doc.doc_type == "best") {
        doc.efforts.forEach(function(effort) {
  	     emit([doc.segment.id, doc.segment.name, effort.elapsedTime], effort.athlete.id);
        })
  }
}