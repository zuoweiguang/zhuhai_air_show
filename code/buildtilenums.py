#!/usr/bin/python
#coding=utf-8

__author__ = 'songhuixing'


for z in range(3, 17):
    f = open(r'/root/songhuixing/tiles/tile_{0}_EN'.format(z), 'wt')
    
    span = 2**z
    for x in xrange(span/2, span):
        for y in xrange(0, span/2):
            f.write("{0}_{1}_{2}\n".format(z, x, y))
            
    f.close()