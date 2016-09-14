# -*- coding: utf-8 -*-
"""
Created on Mon Nov 23 16:47:34 2015

@author: SongHuiXing
"""
import math as ma


class MercatorCoord:
    originalR = 6378137
    originShift = ma.pi * originalR
    tileSize = 256
    initialResolution = 2 * ma.pi * originalR / tileSize;

    def __init__(self, level):
        self.__level = level
        
    def lonlat2pixel(self, lon, lat):
        mx, my = self.toMercator(lon, lat)
        return self.mercator2pixel(mx, my, self.__level)
        
    def meter2pixel(self, mx, my):
        return self.mercator2pixel(mx, my, self.__level)
    
    @classmethod
    def toMercator(cls, x, y):
        x = x * MercatorCoord.originShift / 180.0
        
        y = ma.log10(ma.tan((y + 90) * ma.pi / 360.0)) / (ma.pi / 180.0)
        y = y * MercatorCoord.originShift / 180.0
        
        return x, y

    @classmethod
    def fromMercator(cls, x, y):
        x = x / MercatorCoord.originShift * 180.0
        
        y = y / MercatorCoord.originShift * 180.0
        y = ma.atan(ma.exp(y * (ma.pi / 180.0))) * 360.0 / ma.pi -90
        
        return x, y
        
    @classmethod
    def mercator2pixel(cls, mx, my, level):
        res = cls.resolution(level)
        px = (mx + cls.originShift) / res
        py = (-my + cls.originShift) / res
        return (px, py)
        
    @classmethod
    def resolution(cls, level):
        return cls.initialResolution / ma.pow(2, level)