# -*- coding: utf-8 -*-
"""
Created on Wed Jan 06 18:16:53 2016

@author: SongHuiXing
"""
from spatialutil import SpatialUtil as spatial
from link_sequence import LinkChain

class CrossOuterLink:
    STOP_FIND_ANGLE = 180
    STOP_FIND_COUNT = 4

    def __init__(self, pid, crossnodepid, direct, isstart):
        self.PID = pid
        self.CrossNodePID = crossnodepid
        self.__direct = direct #Link相对于路口的通行方向 1 双方向 2 进入 3 退出
        self.__isstartConnect = isstart #link是否是起点挂接在路口点上
        self.__restrictioninfos = {}
        self.__envelope = [0, 0, 0, 0]
        self.__linkchain= []
        self.__finalangle = 0

    def getattribute(self, cursor):
        cursor.execute("select GEOMETRY from RD_LINK where LINK_PID = :linkid",
                       linkid=self.PID)

        geo, = cursor.fetchone()

        self.__geometry = geo
        
        self.__updateenvelope()

        self.__getcrossrestriction(cursor)
        
        link_id, isstart = self.PID, self.__isstartConnect
        linkgeo = self.__geometry

        chain = LinkChain(link_id, linkgeo, self.__direct, isstart, cursor)

        self.__linkchain.extend(chain.get_chain())

    def __is_forbidden_car(self, vehicles):
        '''判断车辆类型是否是限制小型车'''
        
        if len(vehicles) == 0:
            return True
            
        flag_mask = 1 << 31
        car_mask = 1 | (1 << 8) | (1 << 11) | (1 << 12) | (1 << 24)
        
        for vehicle, in vehicles:
            vehicle_types = vehicle & car_mask
            flag = vehicle & flag_mask
            if vehicle & flag:          #允许通行
                if not vehicle_types:
                    return True
            elif vehicle_types:         #禁止通行
                return True
            
        return False
        
    def __getcrossrestriction(self, cursor):
        '''
        获取以该Link为进入Link的路口交限
        '''
        cursor.execute("select {fld} from {tbl} where {lfld} = :linkid and {nfld} = :nodeid".format(fld='PID',
                                                                                                    tbl='RD_RESTRICTION',
                                                                                                    lfld='IN_LINK_PID',
                                                                                                    nfld='NODE_PID'),
                       linkid=self.PID,
                       nodeid=self.CrossNodePID)

        resinformation = cursor.fetchall()

        sql_getdetail = '''select OUT_LINK_PID, RESTRIC_INFO, DETAIL_ID from {tbl} 
                            where {resfld} = :resid and RELATIONSHIP_TYPE = 1'''.format(tbl='RD_RESTRICTION_DETAIL',
                                                                            resfld='RESTRIC_PID')
        
        sql_getcondition = '''select VEHICLE from {tbl} 
                              where DETAIL_ID = :detailid and RES_OUT = 0'''.format(tbl='RD_RESTRICTION_CONDITION')
                                                                  
        for restricid, in resinformation:
            cursor.execute(sql_getdetail, resid=restricid)
            
            restric_details = cursor.fetchall()
            
            for lid, info, detail_id in restric_details:
                cursor.execute(sql_getcondition, detailid=detail_id)
                res_conditions = cursor.fetchall()
                
                if not self.__is_forbidden_car(res_conditions):
                    continue
                
                self.__restrictioninfos[lid] = info

    @property
    def Restrictions(self):
        return  self.__restrictioninfos
        
    @property
    def Direction(self):
        return self.__direct
        
    @property
    def IsStartConnect(self):
        return self.__isstartConnect
        
    def Coordinates(self):
        if self.__geometry is None:
            return
            
        ptcount = len(self.__geometry.SDO_ORDINATES) / 2
        for i in range(0, ptcount):
            yield (self.__geometry.SDO_ORDINATES[i*2], 
                   self.__geometry.SDO_ORDINATES[i*2 +1])
                   
    def __updateenvelope(self):
        for x, y in self.Coordinates():
            if 0 == self.__envelope[0]:
                self.__envelope[0] = self.__envelope[2] = x
                self.__envelope[1] = self.__envelope[3] = y
                continue
                
            if x < self.__envelope[0]:
                self.__envelope[0] = x
            elif x > self.__envelope[2]:
                self.__envelope[2] = x
                
            if y < self.__envelope[1]:
                self.__envelope[1] = y
            elif y > self.__envelope[3]:
                self.__envelope[3] = y
                
    @property
    def Envelope(self):
        return self.__envelope

    def getjsondict(self):
        dic = {'type':"Feature",
               'geometry':spatial.convertsdogeometry2json(self.__geometry),
               'properties':{'PID':self.PID,
                             'Chain':self.__linkchain}
               }
        return dic