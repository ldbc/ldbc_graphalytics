#!/bin.bash

for i in 1 2 3 4 5 6 7 8 9 10
do
  echo " The $i ************************************ iteration"
  ant run.test
  
  echo "sleep 10s"

done

echo "done"