#!/usr/bin/python
# -*- coding: utf-8 -*-

'''
some spatial methods
'''

__author__ = 'SongHuiXing'

import numpy as np
from mercator import MercatorCoord as mercator
from sdo import SdoGeometry
import shapely.geometry as shapeGeo
import shapely.affinity as Affinity

class SpatialUtil:
    TOR = 0.000001
    
    @staticmethod
    def getEnvelopeWithandHeight(env):
        lb = mercator.toMercator(env[0], env[1])
        rt = mercator.toMercator(env[2], env[3])
        
        width = rt[0] - lb[0]
        height = rt[1] - lb[1]
        
        return width, height
    
    @staticmethod
    def getDistance(x0, y0, x1, y1):
        return np.sqrt((x1 - x0)**2 + (y1 - y0)**2)
       
    @staticmethod
    def calc_angle(sdo_line1, d1, sdo_line2, d2):
        """
        计算两条link的夹角
        d1 -- line1是否是顺向
        d2 -- line2是否是顺向
        """
        g_line1 = SdoGeometry(sdo_line1)
        g_line2 = SdoGeometry(sdo_line2)

        linestring1 = shapeGeo.asShape(g_line1)
        linestring2 = shapeGeo.asShape(g_line2)

        if d1:
            s1 = linestring1.coords[0]
            e1 = linestring1.coords[-1]
        else:
            s1 = linestring1.coords[-1]
            e1 = linestring1.coords[0]

        l1 = shapeGeo.LineString([s1, e1])

        if d2:
            s2 = linestring2.coords[0]
            e2 = linestring2.coords[-1]
        else:
            s2 = linestring2.coords[-1]
            e2 = linestring2.coords[0]

        l2 = shapeGeo.LineString([s2, e2])

        if abs(e1[0] - s2[0]) > SpatialUtil.TOR or abs(e1[1] - s2[1]) > SpatialUtil.TOR:
            l1 = Affinity.translate(l1, -e1[0], -e1[1])
            l2 = Affinity.translate(l2, -s2[0], -s2[1])

        return SpatialUtil.vector_angle(l1.coords[0], l1.coords[-1], l2.coords[-1])

    @staticmethod
    def vector_angle(tip1, corner, tip2):
        vx = np.array([corner[0]-tip1[0], corner[1]-tip1[1]]) * 100000
        vy = np.array([tip2[0]-corner[0], tip2[1]-corner[1]]) * 100000

        lenX = np.sqrt(vx.dot(vx))
        lenY = np.sqrt(vy.dot(vy))

        cos = vx.dot(vy) / (lenX * lenY)

        try:
            cos = int(cos * 1000000) / 1000000.0

            if -1 > cos or cos > 1:
                return 90

            redians = np.arccos(cos)

            return abs(redians * 360 / 2 / np.pi)
        except:
            return 90
        
    @staticmethod
    def convertsdogeometry2json(sdogeoobj):
        sdoGeo = SdoGeometry(sdogeoobj)

        return sdoGeo.build_geojson_dict()

        # geotype = sdogeoobj.SDO_GTYPE
        #
        # dic = {}
        #
        # if geotype == 2001:
        #     dic['type'] = 'Point'
        #     pt = sdogeoobj.SDO_POINT
        #     dic['coordinates'] = [pt.X,pt.Y]
        # elif geotype == 2002:
        #     dic['type'] = 'LineString'
        #     coords = sdogeoobj.SDO_ORDINATES
        #     dic['coordinates'] = [[coords[2*i],coords[2*i+1]] for i in range(len(coords)/2)]
        #
        # return dic