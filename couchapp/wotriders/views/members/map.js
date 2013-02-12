function(doc) {
  if(doc.doc_type == "member") {
     emit(doc._id, doc);
  }
}