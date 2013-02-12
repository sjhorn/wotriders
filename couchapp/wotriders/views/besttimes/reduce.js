function(keys, values, rereduce) {
  if(!rereduce) {
     var arr = []
     keys.forEach(function(key, index) {
     	arr[index] = [values[index], key[0][2]]
     })
     return arr.reverse() //.slice(0,3)
  } else {
     var arr = []
     values.forEach(function(value) {
        arr.concat(value)
     })
     return arr //.slice(0,3)
  }
}