# -*- coding: utf-8 -*-
"""
Created on Thu Feb 25 14:58:07 2016

@author: SongHuiXing
"""

import simplejson
import cross
import rdlink

class CustomJsonEncoder(simplejson.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, rdlink.RDLink):
            return rdlink.RDLink.convert2buildin(obj)
        elif isinstance(obj, cross.RDCross):
            return cross.RDCross.convert2buildin(obj)
        
        return simplejson.JSONEncoder.encode(self, obj)