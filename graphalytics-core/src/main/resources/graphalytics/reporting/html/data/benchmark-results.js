var results = {
  "id":"b2940223",

  "system": {
    "platform": {"name":"reference", "acronym": "ref", "version": "1.4.0",
      "link": "https://github.com/ldbc/ldbc_graphalytics_platforms_reference"},
    "environment": {
      "name":"DAS Supercomputer", "acronym":"das5", "version": "5", "link":"http://www.cs.vu.nl/das5/",
      "machines": [
        {
          "quantity": 20,
          "operating-system": "Centos",
          "cpu": {"name":"XEON", "cores":"16"},
          "memory": {"name":"?", "size":"40GB"},
          "network": {"name":"Infiniband", "throughput":"10GB/s"},
          "storage": {"name":"SSD", "volume":"20TB"},
          "accel": {}
        }
      ]
    },
    "benchmark": {
      "graphalytics-core": {
        "name": "graphalytics-core",
        "version": "1.1.0",
        "link": "https://github.com/ldbc/ldbc_graphalytics"
      },
      "graphalytics-platforms-reference": {
        "name": "graphalytics-platforms-reference",
        "version": "2.1.0",
        "link": "https://github.com/ldbc/ldbc_graphalytics_platforms_reference"
      }
    }
  },

  "configuration": {
    "target-scale": "L",
    "resources": {
      "cpu-instance": {"name": "cpu-instance", "baseline": 1, "scalability": true},
      "cpu-core": {"name": "cpu-core", "baseline": 32, "scalability": false},
      "memory": {"name": "memory", "baseline": 64, "scalability": true},
      "network": {"name": "network", "baseline": 10, "scalability": false}
    }
  },

  "result":{
    "experiments": {
      "e34252":{"id":"e34252", "type": "baseline-alg-bfs", "jobs":["j34252", "j75352", "j23552"]},
      "e75352":{"id":"e75352", "type": "baseline-alg-pr", "jobs":["j34252", "j75352", "j23552"]},
      "e23552":{"id":"e23552", "type": "baseline-alg-cdlp", "jobs":["j34252", "j75352", "j23552"]}
    },
    "jobs":{
      "j34252":{"id":"j34252", "algorithm":"bfs", "dataset":"D100", "scale":1,
        "repetition": 3, "runs":["r649352", "r124252", "r124252"]},
      "j75352":{"id":"j75352", "algorithm":"pr", "dataset":"D1000", "scale":1,
        "repetition": 3, "runs":["r649352", "r124252", "r124252"]},
      "j23552":{"id":"j23552",  "algorithm":"cdlp", "dataset":"G25", "scale":1,
        "repetition": 3, "runs":["r649352", "r124252", "r124252"]}
    },
    "runs":{
      "r649352":{"id":"r649352", "timestamp":1463310828849,
        "success":true, "makespan":23423422, "processing-time":2234},
      "r124252":{"id":"r124252", "timestamp":1463310324849,
        "success":true,  "makespan":2343422, "processing-time":234},
      "r643252":{"id":"r124252", "timestamp":1463310324849,
        "success":true,  "makespan":2343422, "processing-time":234}
    }
  }


}