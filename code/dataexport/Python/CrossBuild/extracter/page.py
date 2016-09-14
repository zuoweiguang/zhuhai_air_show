# -*- coding: utf-8 -*-
"""
Created on Mon Nov 23 17:15:59 2015

@author: SongHuiXing
"""

import numpy as np
import math as ma
import bresenhamline as bresen
from mercator import MercatorCoord as mercator

class PageMatrix:
    '''
    页面栅格构建类
    构建类似于墨卡托瓦块的自定义范围的栅格图
    栅格坐标 x--从左到右 y--从上到下
    '''
    tilelevel = 16
    
    def __init__(self, mercator_env):
        self.__mercator_bound = mercator_env
    
        self.__mercatorutil = mercator(self.tilelevel)
        
        minpixel = self.__mercatorutil.meter2pixel(mercator_env[0][0], 
                                                       mercator_env[1][1])
        minpx = int(ma.floor(minpixel[0]))
        minpy = int(ma.floor(minpixel[1]))
                                               
        maxpixel = self.__mercatorutil.meter2pixel(mercator_env[1][0], 
                                                       mercator_env[0][1])
        maxpx = int(ma.floor(maxpixel[0]))
        maxpy = int(ma.floor(maxpixel[1]))
        
        self.__pixel_bound = [minpx, minpy, maxpx, maxpy]
        
        self.__pagewidth = maxpx - minpx + 1
        self.__pageheight = maxpy - minpy + 1
        
        self.__matrix = np.zeros((self.__pageheight, self.__pagewidth), dtype=np.uint8)
        
        self.__world_buffersize = (0, 0)
        
    def setbufsizeofmeter(self, width, height):
        self.__world_buffersize = (width, height)
        
    def colorregion(self, mercator_env, color=255):
        points = []
        
        points.append((mercator_env[0][0], mercator_env[0][1]))
        points.append((mercator_env[0][0], mercator_env[1][1]))
        points.append((mercator_env[1][0], mercator_env[1][1]))
        points.append((mercator_env[1][0], mercator_env[0][1]))
        
        self.__brushcolor4parallelo(color, *points)
            
    
    def colorline(self, mercator_pts, color=1):
        for i in range(len(mercator_pts) - 1):
            pts = mercator_pts[i]
            pte = mercator_pts[i+1]
            
            buf_s_left, buf_s_right = self.__getbufPoints4E(pte, pts)
            
            buf_e_left, buf_e_right = self.__getbufPoints4E(pts, pte)
            
            self.__brushcolor4parallelo(color, buf_e_left, buf_e_right, buf_s_left, buf_s_right)
            

    def __getbufPoints4E(self, point_s, point_e):
        '''
        获取终点方向上的两个buffer点
        '''
        if point_s[0] == point_e[0]:
            return self.__getverticalbufferpts(point_s, point_e)
            
        if point_s[1] == point_e[1]:
            return self.__gethorizenbufferpts(point_s, point_e)
 
        return self.__getnormalbufferpts(point_s, point_e)
                    
    def __gethorizenbufferpts(self, point_s, point_e):
        '''获取水平线的终点buffer'''
        bufx, bufy = self.__world_buffersize
        if point_e[0] > point_s[0]:
            newx = point_e[0]# + bufx
            return ((newx, point_e[1] + bufy), (newx, point_e[1] - bufy))
        else:
            newx = point_e[0]# - bufx
            return ((newx, point_e[1] - bufy), (newx, point_e[1] + bufy))
            
    def __getverticalbufferpts(self, point_s, point_e):
        '''获取竖直线的终点buffer'''
        bufx, bufy = self.__world_buffersize
        if point_e[1] > point_s[1]:
            newy = point_e[1]# + bufy
            return ((point_e[0] - bufx, newy), (point_e[0] + bufx, newy))
        else:
            newy = point_e[1]# - bufy
            return ((point_e[0] + bufx, newy), (point_e[0] - bufx, newy))
            
    def __getnormalbufferpts(self, point_s, point_e):
        '''获取倾斜线的终点buffer'''
        bufx, bufy = self.__world_buffersize
        panmx = np.matrix([[1, 0, 0],
                            [0, 1, 0],
                            [point_s[0], point_s[1], 1]])
                            
        inversePanmx = panmx.I
        
        tanTheta = (point_e[1] - point_s[1]) / (point_e[0] - point_s[0])
        angleTheta = ma.atan(tanTheta)
        rotatemx = np.matrix([[ma.cos(angleTheta), ma.sin(angleTheta), 0], 
                               [-1*ma.sin(angleTheta), ma.cos(angleTheta), 0],
                               [0, 0, 1]])
                               
        inverseRotatemx = rotatemx.I
        
        pointE = np.matrix([point_e[0], point_e[1], 1]) * inversePanmx
        pointE = pointE * inverseRotatemx
        
        newx=0.0; newyu = 0.0; newyd = 0.0
        if point_e[0] > point_s[0]:
            newx = pointE[0,0]# + bufx
            newyu = pointE[0,1] + bufy
            newyd = pointE[0,1] - bufy
        else:
            newx = pointE[0,0]# - bufx
            newyu = pointE[0,1] - bufy
            newyd = pointE[0,1] + bufy
            
        pointEU = np.matrix([newx, newyu, 1])
        point_e_up = pointEU * rotatemx * panmx
    
        pointED = np.matrix([newx, newyd, 1])
        point_e_down = pointED * rotatemx * panmx
        
        return ((point_e_up[0,0], point_e_up[0,1]), 
                (point_e_down[0,0], point_e_down[0,1]))
        
    def __brushcolor4parallelo(self, color, *points):
        '''给一个平行四边形上色'''
        ptcount = len(points)   
        
        raster_bound = {}
        for i in range(ptcount):
            pts = points[i]
            if i < ptcount -1:
                pte = points[i+1]
            else:
                pte = points[0]
                
            raster_line = self.__getrasterpts(pts, pte)
                                              
            for c, r in raster_line:
                if r not in raster_bound:
                    raster_bound[r] = []
                raster_bound[r].append(c)

        for row, columns in raster_bound.iteritems():
            if row >= self.__pageheight or row < 0: #画出图面外的不要
                continue
            
            mincol, maxcol = min(columns), max(columns)
            
            if mincol >= self.__pagewidth or maxcol < 0:
                continue
                
            mincol = mincol if mincol >= 0 else 0
            maxcol = maxcol if maxcol < self.__pagewidth else self.__pagewidth-1
            
            self.__matrix[row, mincol : maxcol+1] = color
        
    
    def __getrasterpts(self, mercator_s, mercator_e):
        '''获取从起点到终点的栅格坐标'''
        rasterrange = []
        middlerange = []
        
        rangestart = self.__mercator2page(*mercator_s)
        scol, srow = rangestart
        
        rangeend = self.__mercator2page(*mercator_e)
        ecol, erow = rangeend
            
        if scol != ecol: #非水平
            rasterrange.append(rangestart)
            
            middlerange = bresen.Bresenhamline.getline(scol, srow, ecol, erow)
                
            rasterrange.extend(middlerange)
            rasterrange.append(rangeend)
        else:
            step = 1 if erow > srow else -1
            rasterrange = [(scol, r) for r in range(srow, erow+step, step)]
        
        return rasterrange
    
    def __mercator2page(self, mx, my):
            pixel = self.__mercatorutil.meter2pixel(mx, my)
            pixelx = int(ma.floor(pixel[0]))
            pixely = int(ma.floor(pixel[1]))
            return (pixelx - self.__pixel_bound[0], pixely - self.__pixel_bound[1])
    
    @property
    def Matrix(self):
        return self.__matrix
    