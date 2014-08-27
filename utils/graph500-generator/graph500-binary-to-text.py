#!/usr/bin/env python
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
