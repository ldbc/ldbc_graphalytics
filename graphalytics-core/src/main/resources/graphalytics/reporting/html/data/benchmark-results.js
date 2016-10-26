var data = {
  "id": "b8600984418",
  "system": {
    "platform": {
      "name": "Reference",
      "acronym": "ref",
      "version": "0.2-SNAPSHOT",
      "link": "http://github.com/ldbc/ldbc_graphalytics_platforms_reference"
    },
    "environment": {
      "name": "The Distributed ASCI Supercomputer",
      "acronym": "das5",
      "version": "5",
      "link": "http://www.cs.vu.nl/das5",
      "machines": [
        {
          "quantity": "20",
          "cpu": "Intel Xeon E5-2630 v3 (2.40GHz)",
          "memory": "? (64 GB)",
          "network": "IB(?) and GbE (?)",
          "storage": "HDD (2 * 4TB)"
        }
      ]
    },
    "tool": {
      "graphalytics-driver": {
        "name": "graphalytics-driver",
        "version": "0.2-SNAPSHOT",
        "link": "https://github.com/ldbc/ldbc_graphalytics_platforms_reference"
      },
      "graphalytics-core": {
        "name": "graphalytics-core",
        "version": "0.4-SNAPSHOT",
        "link": "https://github.com/ldbc/ldbc_graphalytics"
      }
    }
  },
  "benchmark": {
    "type": "standard:baseline",
    "name": "Standard Benchmark: Baseline",
    "target_scale": "S",
    "resources": {
      "memory": {
        "name": "memory",
        "baseline": "4",
        "scalability": "true"
      },
      "cpu-instance": {
        "name": "cpu-instance",
        "baseline": "1",
        "scalability": "true"
      }
    },
    "output": {
      "required": "true",
      "directory": "./output/"
    },
    "validation": {
      "required": "true",
      "directory": "/var/scratch/wlngai/graphalytics/vld/openg"
    }
  },
  "result": {
    "experiments": {
      "e492110": {
        "id": "e492110",
        "type": "std:baseline:BFS",
        "jobs": [
          "j495597"
        ]
      },
      "e723882": {
        "id": "e723882",
        "type": "std:baseline:WCC",
        "jobs": [
          "j714860"
        ]
      },
      "e688196": {
        "id": "e688196",
        "type": "std:baseline:PR",
        "jobs": []
      },
      "e493334": {
        "id": "e493334",
        "type": "std:baseline:CDLP",
        "jobs": []
      },
      "e524565": {
        "id": "e524565",
        "type": "std:baseline:LCC",
        "jobs": []
      },
      "e712504": {
        "id": "e712504",
        "type": "std:baseline:SSSP",
        "jobs": []
      }
    },
    "jobs": {
      "j495597": {
        "id": "j495597",
        "algorithm": "BFS",
        "dataset": "example-directed",
        "scale": "1",
        "repetition": "2",
        "runs": [
          "b840521",
          "b612131"
        ]
      },
      "j714860": {
        "id": "j714860",
        "algorithm": "WCC",
        "dataset": "example-directed",
        "scale": "1",
        "repetition": "2",
        "runs": [
          "b500201",
          "b826308"
        ]
      }
    },
    "runs": {
      "b840521": {
        "id": "b840521",
        "timestamp": "1477492375677",
        "success": "true",
        "makespan": "9",
        "processing_time": "unknown"
      },
      "b612131": {
        "id": "b612131",
        "timestamp": "1477492375687",
        "success": "true",
        "makespan": "0",
        "processing_time": "unknown"
      },
      "b826308": {
        "id": "b826308",
        "timestamp": "1477492375627",
        "success": "true",
        "makespan": "47",
        "processing_time": "unknown"
      },
      "b500201": {
        "id": "b500201",
        "timestamp": "1477492375689",
        "success": "true",
        "makespan": "0",
        "processing_time": "unknown"
      }
    }
  }
}