function(key, values, rereduce) {

    function flatten(array){
        var flat = [];
        for (var i = 0; i < array.length; i++){
            for(var j = 0; j < array[i].length; j++) {
                flat.push(array[i][j])
            }
        }
        return flat;
    }

   if(rereduce) {
      return Math.min.apply( Math, flatten(values) )
   } else {
      return Math.min.apply( Math, values )
   }
}