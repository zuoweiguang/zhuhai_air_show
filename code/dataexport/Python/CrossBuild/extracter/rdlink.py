# -*- coding: utf-8 -*-
"""
Created on Thu Feb 25 09:47:38 2016

@author: SongHuiXing
"""

import spatialutil as spatial

class RDLink:
    sql_get = "select {flds} from RD_LINK where {key_fld}=:key"
    target_fields = ','.join(['FUNCTION_CLASS','DIRECT','KIND',
                                'LANE_NUM','LANE_LEFT','LANE_RIGHT',
                                'MESH_ID','LENGTH','LEFT_REGION_ID',
                                'RIGHT_REGION_ID','GEOMETRY'])
                     
    sql_linklimit = "select (TYPE), LIMIT_DIR from RD_LINK_LIMIT where LINK_PID=:key"
    
    sql_speedlimit = '''select SPEED_TYPE, FROM_SPEED_LIMIT, TO_SPEED_LIMIT, SPEED_DEPENDENT
                        from RD_LINK_SPEEDLIMIT where LINK_PID=:key'''
    
    def __init__(self, pid):
        self.__link_pid = pid
        self.__link_limits = []
        self.__speed_limits = []
                                
    def build(self, cursor):
        all_flds = self.__getproperties(cursor)
        if all_flds is None:
            return False
        
        self.__geometry = all_flds[-1]
        self.__all_flds = all_flds[:-1]
            
        all_linklimits = self.__get_linklimits(cursor)
        if all_linklimits:
            for t, limitdir in all_linklimits:
                dic = {}
                dic['type'] = t
                dic['limit_dir'] = limitdir
                self.__link_limits.append(dic)
        
        all_speedlimits = self.__get_speedlimits(cursor)
        if all_speedlimits:
            for t, f_speed, t_speed, dependent in all_speedlimits:
                dic = {}
                dic['speed_type'] = t
                dic['from_speed_limit'] = f_speed
                dic['to_speed_limit'] = t_speed
                dic['speed_dependent'] = dependent
                self.__speed_limits.append(dic)
        
    def __getproperties(self, cursor):
        sql = RDLink.sql_get.format(flds=RDLink.target_fields)
        
        try:
            cursor.execute(sql, key=self.__link_pid)
        
            return cursor.fetchone()
        except:
            return None
            
    def __get_linklimits(self, cursor):
        return RDLink.__fetch_fields(cursor, self.sql_linklimit, self.__link_pid)
         
    def __get_speedlimits(self, cursor):
        return RDLink.__fetch_fields(cursor, self.sql_speedlimit, self.__link_pid)

    @property
    def pid(self):
        return self.__link_pid
        
    @property
    def all_prop(self):
        return self.__all_flds
        
    @property
    def link_limits(self):
        return self.__link_limits
    
    @property
    def speed_limits(self):
        return self.__speed_limits
    
    @staticmethod
    def __fetch_fields(cursor, sql, keyvalue):
        try:
            cursor.execute(sql, key=keyvalue)
        
            return cursor.fetchall()
        except:
            return None
            
    @staticmethod
    def convert2buildin(obj):
        if not isinstance(obj, RDLink):
            return obj
        
        dic = {'type':"Feature",
               'geometry':spatial.SpatialUtil.convertsdogeometry2json(obj.__geometry),
               'properties':{'link_pid':obj.pid,
                             'functionclass':obj.all_prop[0],
                             'direct':obj.all_prop[1],
                             'kind':obj.all_prop[2],
                             'lane_num':obj.all_prop[3],
                             'lane_left':obj.all_prop[4],
                             'lane_right':obj.all_prop[5],
                             'mesh_id':obj.all_prop[6],
                             'length':obj.all_prop[7],
                             'l_region':obj.all_prop[8],
                             'r_region':obj.all_prop[9],
                             'link_limit':obj.link_limits,
                             'link_speed_limit':obj.speed_limits}
                }

        return dic
    
    