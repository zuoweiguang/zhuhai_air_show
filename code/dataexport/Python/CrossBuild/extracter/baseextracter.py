# -*- coding: utf-8 -*-
"""
Created on Thu Feb 25 09:36:55 2016

@author: SongHuiXing
"""

class BaseExtracter:
    def __init__(self, orcl_conn):
        self.connection = orcl_conn
        
    def getrange(self):
        cursor = self.connection.cursor()

        cursor.execute("select MIN(PID), MAX(PID) from RD_Cross")

        res = cursor.fetchone()

        cursor.close()

        del cursor

        return res
        
    def extract(self, folder, minid=0, maxid=0):
        pass