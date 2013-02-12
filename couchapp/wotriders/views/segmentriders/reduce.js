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
    function unique(arr){
       var u = {}, a = [];
       for(var i = 0, l = arr.length; i < l; ++i){
          if(u.hasOwnProperty(arr[i])) {
             continue;
          }
          a.push(arr[i]);
          u[arr[i]] = 1;
       }
       return a;
    }

    if(rereduce) {
        return unique(flatten(values))
    } else {
        return unique(values)
    }
}