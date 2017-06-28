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

# -*- coding: utf-8 -*-
# pylint: disable=invalid-name
# © 2014 Mihai Capotă
"""Transform the output of the Graph 500 generator into text."""


from __future__ import division, print_function

import argparse
import struct


def main():
    argparser = argparse.ArgumentParser()

    argparser.add_argument("fin", help="input file")
    argparser.add_argument("fout", help="output file")
    args = argparser.parse_args()

    with open(args.fin, "rb") as fin, open(args.fout, "w") as fout:
        while True:
            edge = fin.read(16)
            if edge == "":
                break
            (v1, v2) = struct.unpack("qq", edge)
            print(v1, v2, file=fout)


if __name__ == "__main__":
    main()
