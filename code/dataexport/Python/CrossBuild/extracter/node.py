# -*- coding: utf-8 -*-
"""
Created on Wed Jan 06 18:13:14 2016

@author: SongHuiXing
"""

from spatialutil import SpatialUtil as spatial

class CrossNode:
    def __init__(self, pid):
        self.PID = pid
        self.__connlinks = []

    def getinfos(self, cursor):
        cursor.execute("select GEOMETRY from RD_NODE where NODE_PID = :nodeid",
                       nodeid=self.PID)

        geo = cursor.fetchone()

        if geo is  None:
            return False

        self.__Geometry = geo[0]
            
        sql = "select LINK_PID, DIRECT from RD_LINK where {id} = :nodeid"

        #获取路口Node挂接Link及Link的通行方向 1 双方向 2 顺方向 3 逆方向
        cursor.execute(sql.format(id='S_NODE_PID'), nodeid=self.PID)
        self.__slinks = [(lid, direct if direct != 0 else 1) for lid, direct in cursor]

        cursor.execute(sql.format(id='E_NODE_PID'), nodeid=self.PID)
        self.__elinks = [(lid, direct if direct != 0 else 1) for lid, direct in cursor]

        return True

    def filterlinks(self, crosslinks):
        '''
        更新该路口点挂接的路口外道路
        lid：路口外Link的PID
        direct：Link相对于路口的通行方向 1 双方向 2 进入 3 退出
        '''
        
        #路口点作为起点挂接的link
        for lid,direct in self.__slinks:
            if lid in crosslinks:
                continue
            
            crossdir = direct
            if direct == 2:
                crossdir = 3
            elif direct == 3:
                crossdir = 2
            
            self.__connlinks.append((lid, crossdir, True))
 
        #路口点作为终点挂接的link
        self.__connlinks.extend((lid, direct, False) for lid, direct in self.__elinks if lid not in crosslinks)

        del self.__slinks
        del self.__elinks

    @property
    def ConnectLinks(self):
        return  self.__connlinks

    @property
    def X(self):
        if self.__Geometry is None:
            return 0.0
            
        return self.__Geometry.SDO_POINT.X

    @property
    def Y(self):
        if self.__Geometry is None:
            return 0.0
            
        return self.__Geometry.SDO_POINT.Y

    def getjsondict(self):
        dic = {'type':"Feature",
               'geometry':spatial.convertsdogeometry2json(self.__Geometry),
               'properties':{'PID':self.PID,
                             'ConnectLinks':[lid for lid, d, s in self.__connlinks]}
               }
        return dic