# -*- coding: utf-8 -*-
"""
Created on Thu Jan 07 11:06:43 2016

@author: SongHuiXing
"""
import math

class Bresenhamline:
    use_addvance = True
    
    @classmethod
    def getline(cls, x0, y0, x1, y1):
        if cls.use_addvance:
            return cls.getsimpleline(x0, y0, x1, y1)
        else:
            return cls.getadvanceline(x0, y0, x1, y1)
            
    @classmethod
    def getsimpleline(cls, x0, y0, x1, y1):
        dx = abs(x1 - x0)
        dy = abs(y1 - y0)
        sx = 1 if (x1 - x0) >0 else -1
        sy = 1 if (y1 - y0) >0 else -1
        
        isVertical = False
        if dy > dx:
            dx, dy = dy, dx
            isVertical = True
            
        eps = 2 * dy - dx
        
        linepixel = []
        loopx, loopy = x0, y0
        for i in range(0, dx):
            linepixel.append((loopx, loopy))
            
            if eps > 0:
                if isVertical:
                    loopx = loopx + sx
                else:
                    loopy = loopy + sy
                    
                eps = eps - 2*dx
            
            if isVertical:
                loopy = loopy + sy
            else:
                loopx = loopx + sx
            
            eps = eps + 2*dy
                    
                    
        return linepixel
    
    @classmethod
    def getadvanceline(cls, x0, y0, x1, y1):
        dx = abs(x1 - x0)
        dy = abs(y1 - y0)
        stepx = 1 if (x1 - x0) >0 else -1
        stepy = 1 if (y1 - y0) >0 else -1
        
        isVertical = False
        if dy > dx:
            dx, dy = dy, dx
            isVertical = True
            
        eps = 4 * dy
        
        ptsCollector = BresenhamlinePointCollection(x0, y0, x1, y1, stepx, stepy)
        
        startDirectPtCount = math.ceil((dx - 1)/2)
        
        totalLoopCount = math.ceil(startDirectPtCount / 2)
        startDirectPtCount = totalLoopCount * 2

        endDirectPtCount = dx - startDirectPtCount -1
        
        for i in range(totalLoopCount):
            if eps <= 0:
                eps = eps + 4*dy
            elif eps > 0 and eps <= 2*dx:
                eps = eps + 4*dy - 2*dx
            else:
                eps = eps + 4*dy - 4*dx
                
            if eps < 0:
                ptsCollector.collectdiag()
            elif eps >= 0 and eps < dx:
                ptsCollector.collecthigh(not isVertical)
            elif eps >= dx and eps < 2*dx:
                ptsCollector.collectlow(not isVertical)
            else:
                ptsCollector.collectbottom(not isVertical)
                
        lineCoords = ptsCollector.start_points
        
        endCoords = ptsCollector.end_points[0:endDirectPtCount+1]
        
        lineCoords.extend(endCoords.reverse())
        
        return lineCoords
        
    
    
class BresenhamlinePointCollection:
    def __init__(self, sloopx, sloopy, eloopx, eloopy, stepx, stepy):
        self.__startpts = []
        self.__endpts = []
        
        self.__sCursor_x = sloopx
        self.__sCursor_y = sloopy
        self.__eCursor_x = eloopx
        self.__eCursor_y = eloopy
        
        self.__stepx = stepx
        self.__stepy = stepy
        
    @property
    def start_points(self):
        return self.__startpts
        
    @property
    def end_points(self):
        return self.end_points
        
    def collectdiag(self):
        '''
         x......x......o
         .      .      .
         .      .      .
         x......o......x
         .      .      .
         .      .      .
         o......x......x
        '''
        self.__startpts.append((self.__sCursor_x + self.__stepx, 
                                self.__sCursor_y + self.__stepy))
        self.__startpts.append((self.__sCursor_x + 2*self.__stepx, 
                                self.__sCursor_y + 2*self.__stepy))
        self.__sCursor_x = self.__sCursor_x + 2*self.__stepx
        self.__sCursor_y = self.__sCursor_y + 2*self.__stepy
        
        self.__endpts.append((self.__eCursor_x - self.__stepx, 
                              self.__eCursor_y - self.__stepy))
        self.__endpts.append((self.__eCursor_x - 2*self.__stepx, 
                              self.__eCursor_y - 2*self.__stepy))
        self.__eCursor_x = self.__eCursor_x - 2*self.__stepx
        self.__eCursor_y = self.__eCursor_y - 2*self.__stepy
    
    def collecthigh(self, isHorizen):
        '''
         x......x......x
         .      .      .
         .      .      .
         x......o......o
         .      .      .
         .      .      .
         o......x......x
        '''
        if isHorizen:
            self.__startpts.append((self.__sCursor_x + self.__stepx, 
                                    self.__sCursor_y + self.__stepy))
            self.__startpts.append((self.__sCursor_x + 2*self.__stepx, 
                                    self.__sCursor_y + self.__stepy))
            self.__sCursor_x = self.__sCursor_x + 2*self.__stepx
            self.__sCursor_y = self.__sCursor_y + self.__stepy
            
            self.__endpts.append((self.__eCursor_x - self.__stepx, 
                                  self.__eCursor_y - self.__stepy))
            self.__endpts.append((self.__eCursor_x - 2*self.__stepx, 
                                  self.__eCursor_y - self.__stepy))
            self.__eCursor_x = self.__eCursor_x - 2*self.__stepx
            self.__eCursor_y = self.__eCursor_y - self.__stepy
        else:
            self.__startpts.append((self.__sCursor_x + self.__stepx, 
                                    self.__sCursor_y + self.__stepy))
            self.__startpts.append((self.__sCursor_x + self.__stepx, 
                                    self.__sCursor_y + 2*self.__stepy))
            self.__sCursor_x = self.__sCursor_x + self.__stepx
            self.__sCursor_y = self.__sCursor_y + 2*self.__stepy
            
            self.__endpts.append((self.__eCursor_x - self.__stepx, 
                                  self.__eCursor_y - self.__stepy))
            self.__endpts.append((self.__eCursor_x - self.__stepx, 
                                  self.__eCursor_y - 2*self.__stepy))
            self.__eCursor_x = self.__eCursor_x - self.__stepx
            self.__eCursor_y = self.__eCursor_y - 2*self.__stepy
    
    def collectlow(self, isHorizen):
        '''
         x......x......x
         .      .      .
         .      .      .
         x......x......o
         .      .      .
         .      .      .
         o......o......x
        '''
        if isHorizen:
            self.__startpts.append((self.__sCursor_x + self.__stepx, 
                                    self.__sCursor_y))
            self.__startpts.append((self.__sCursor_x + 2*self.__stepx, 
                                    self.__sCursor_y + self.__stepy))
            self.__sCursor_x = self.__sCursor_x + 2*self.__stepx
            self.__sCursor_y = self.__sCursor_y + self.__stepy
            
            self.__endpts.append((self.__eCursor_x - self.__stepx, 
                                  self.__eCursor_y))
            self.__endpts.append((self.__eCursor_x - 2*self.__stepx, 
                                  self.__eCursor_y - self.__stepy))
            self.__eCursor_x = self.__eCursor_x - 2*self.__stepx
            self.__eCursor_y = self.__eCursor_y - self.__stepy
        else:
            self.__startpts.append((self.__sCursor_x, 
                                    self.__sCursor_y + self.__stepy))
            self.__startpts.append((self.__sCursor_x + self.__stepx, 
                                    self.__sCursor_y + 2*self.__stepy))
            self.__sCursor_x = self.__sCursor_x + self.__stepx
            self.__sCursor_y = self.__sCursor_y + 2*self.__stepy
            
            self.__endpts.append((self.__eCursor_x, 
                                  self.__eCursor_y - self.__stepy))
            self.__endpts.append((self.__eCursor_x - self.__stepx, 
                                  self.__eCursor_y - 2*self.__stepy))
            self.__eCursor_x = self.__eCursor_x - self.__stepx
            self.__eCursor_y = self.__eCursor_y - 2*self.__stepy
    
    def collectbottom(self, isHorizen):
        '''
         x......x......x
         .      .      .
         .      .      .
         x......x......x
         .      .      .
         .      .      .
         o......o......o
        '''
        if isHorizen:
            self.__startpts.append((self.__sCursor_x + self.__stepx, 
                                    self.__sCursor_y))
            self.__startpts.append((self.__sCursor_x + 2*self.__stepx, 
                                    self.__sCursor_y))
            self.__sCursor_x = self.__sCursor_x + 2*self.__stepx
            
            self.__endpts.append((self.__eCursor_x - self.__stepx, 
                                  self.__eCursor_y))
            self.__endpts.append((self.__eCursor_x - 2*self.__stepx, 
                                  self.__eCursor_y))
            self.__eCursor_x = self.__eCursor_x - 2*self.__stepx
        else:
            self.__startpts.append((self.__sCursor_x, 
                                    self.__sCursor_y + self.__stepy))
            self.__startpts.append((self.__sCursor_x, 
                                    self.__sCursor_y + 2*self.__stepy))
            self.__sCursor_y = self.__sCursor_y + 2*self.__stepy
            
            self.__endpts.append((self.__eCursor_x, 
                                  self.__eCursor_y - self.__stepy))
            self.__endpts.append((self.__eCursor_x, 
                                  self.__eCursor_y - 2*self.__stepy))
            self.__eCursor_y = self.__eCursor_y - 2*self.__stepy