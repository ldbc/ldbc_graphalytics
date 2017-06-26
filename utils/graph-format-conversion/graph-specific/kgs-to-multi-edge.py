#!/usr/bin/env python
#
# Copyright 2015 - 2017 Atlarge Research Team,
# operating at Technische Universiteit Delft
# and Vrije Universiteit Amsterdam, the Netherlands.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


import argparse

def main():
    argparser = argparse.ArgumentParser()
    argparser.add_argument("fin", help="input file (KGS_Edge_Basic)")
    argparser.add_argument("fout", help="output file")
    args = argparser.parse_args()

    with open(args.fin, "r") as fin, open(args.fout, "w") as fout:
        line = fin.readline()
        while line and (line.startswith("#") or line.startswith("RowID")):
            line = fin.readline()
        while line:
            parts = line.split(", ")
            src = int(parts[2])
            dst = int(parts[4])
            if (src < dst):
                edge = (src, dst)
            elif (src > dst):
                edge = (dst, src)
            else:
                raise Exception("Self loop detected")
            print(edge[0], edge[1], file=fout)
            line = fin.readline()

if __name__ == "__main__":
    main()
