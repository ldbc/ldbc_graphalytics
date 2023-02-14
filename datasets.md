# Graphalytics data sets

We host mirrors of our datasets on [Cloudflare R2](https://www.cloudflare.com/products/r2/), packaged using `zstd`.

For streaming decompression, use `curl`:

```bash
export DATASET_URL=...
curl --silent --fail ${DATASET_URL} | tar -xv --use-compress-program=unzstd
```

Dataset links:

* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/cit-Patents.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/com-friendster.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/datagen-7_5-fb.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/datagen-7_6-fb.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/datagen-7_7-zf.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/datagen-7_8-zf.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/datagen-7_9-fb.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/datagen-8_0-fb.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/datagen-8_1-fb.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/datagen-8_2-zf.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/datagen-8_3-zf.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/datagen-8_4-fb.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/datagen-8_5-fb.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/datagen-8_6-fb.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/datagen-8_7-zf.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/datagen-8_8-zf.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/datagen-8_9-fb.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/datagen-9_0-fb.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/datagen-9_1-fb.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/datagen-9_2-zf.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/datagen-9_3-zf.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/datagen-9_4-fb.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/datagen-sf10k-fb.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/datagen-sf3k-fb.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/dota-league.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/example-directed.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/example-undirected.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/graph500-22.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/graph500-23.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/graph500-24.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/graph500-25.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/graph500-26.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/graph500-27.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/graph500-28.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/graph500-29.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/graph500-30.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/kgs.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/twitter_mpi.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/wiki-Talk.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/test-bfs-directed.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/test-bfs-undirected.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/test-cdlp-directed.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/test-cdlp-undirected.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/test-pr-directed.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/test-pr-undirected.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/test-lcc-directed.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/test-lcc-undirected.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/test-wcc-directed.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/test-wcc-undirected.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/test-sssp-directed.tar.zst
* https://pub-383410a98aef4cb686f0c7601eddd25f.r2.dev/graphalytics/test-sssp-undirected.tar.zst
