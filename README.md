```commandline
$sbt docker:publishLocal
$docker run -d -p 9000:9000 --restart unless-stopped --name cities radusw/city-info:1.0

$docker logs cities --follow

$curl http://localhost:9000/api/London
```

Open the browser and go to http://localhost:9000 or http://localhost:9000/Bucharest
