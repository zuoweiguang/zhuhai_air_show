#!/usr/bin/python
# coding=utf-8

__author__ = 'SongHuiXing'

import numpy as np
#import scipy.sparse as sparse
from mercator import MercatorCoord as mercator
from page import PageMatrix as pgmatrix
from spatialutil import SpatialUtil as spatial
from link import CrossOuterLink
from node import CrossNode


class RDCross:
    areabufferfactor = 2 #factor = area buffer len / cross envelope len
    pagesizefactor = 7 #factor = page len / cross area len
    defaultenvelopelen = 0.00008 # 8m
    ratio = 1

    sql_get_node = "select NODE_PID from RD_CROSS_NODE where PID = :crossid"
    
    def __init__(self, pid):
        self.PID = pid
        self.__nodes = []
        self.__outlinks = []
        self.__envelope = [0, 0, 0, 0]
        self.__outterenvelope = []
        self.__crossarea = []
        self.__pageenvelope = []
        self.__pagebuffer = None
        
        # 0 2 0
        # 1 4 0
        # 3 1 0
        self.__restricinfomx = None #交限情况矩阵 0:无交限 1:禁直 2:禁左 3:禁右 4:禁调 5:同为退出线 6:同为进入线
        
        #进入该Link的方向 [[2 2 3]
        #退出该Link的方向 [3 3 2]]
        self.__throughlinkdirmx = None #从一条link到另一条link所需要的相对于画线方向的走行方向 2:顺 3:逆
        
    def __getnodes(self, cursor):
        cursor.execute(RDCross.sql_get_node, crossid=self.PID)

        allnodes = cursor.fetchall()
        
        for nodeid, in allnodes:
            node = CrossNode(nodeid)
            if not node.getinfos(cursor):
                del node
                continue

            self.__nodes.append(node)
         
    def __updateenvelope(self):
        
        for node in self.__nodes:
            x, y  = node.X, node.Y
            
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
                
        self.__outterenvelope = [coord for coord in self.__envelope]
        for link in self.__outlinks:
            minx, miny, maxx, maxy = link.Envelope
            if minx < self.__outterenvelope[0]:
                self.__outterenvelope[0] = minx
            if maxx > self.__outterenvelope[2]:
                self.__outterenvelope[2] = maxx
            if miny < self.__outterenvelope[1]:
                self.__outterenvelope[1] = miny
            if maxy > self.__outterenvelope[3]:
                self.__outterenvelope[3] = maxy
                
    def __extendfornewarea(self, area, factor):
        expandfactor = (factor -1) / 2.0
        
        arealen = area[2] - area[0]
        areawid = area[3] - area[1]
        
        newarea = [0, 0, 0, 0]
        newarea[0] = area[0] - arealen*expandfactor
        newarea[1] = area[1] - areawid*expandfactor
        newarea[2] = area[2] + arealen*expandfactor
        newarea[3] = area[3] + areawid*expandfactor
        
        return newarea
        
    def __calcbuffersize(self):
        crossnodeEnvelope = self.CrossEnvelope #路口范围
        
        self.__crossarea = self.__extendfornewarea(crossnodeEnvelope, RDCross.areabufferfactor)

        self.__pageenvelope = self.__extendfornewarea(self.__crossarea, RDCross.pagesizefactor)
        
    def __getlinks(self, cursor):
        cursor.execute("select LINK_PID from RD_CROSS_LINK where PID = :crossid",
                       crossid=self.PID)

        innerlinks = [lid for lid, in cursor]

        for node in self.__nodes:
            node.filterlinks(innerlinks)
            for outlinkid, direct, isstart in node.ConnectLinks:
                outlink = CrossOuterLink(outlinkid, node.PID, direct, isstart)
                outlink.getattribute(cursor)
                self.__outlinks.append(outlink)

    def __initrestrictionmatrix(self):
        masize = len(self.__outlinks)
        
        self.__restricinfomx = np.zeros((masize, masize), dtype=np.uint8)
        self.__throughlinkdirmx = np.zeros((2, masize), dtype=np.uint8)

        #is there a restriction out link which is not a cross out link??
        for i in range(len(self.__outlinks)): 
            link = self.__outlinks[i]
            self.__throughlinkdirmx[:,i] = [3, 2] if link.IsStartConnect else [2 ,3]
            direct = link.Direction   
            
            if direct==2:
                for j in range(len(self.__outlinks)):
                    line = self.__outlinks[j]
                    if 2 == line.Direction:
                        self.__restricinfomx[i,j] = 6   #同是进入线造成的理论交限
                    elif line.PID in link.Restrictions:
                        self.__restricinfomx[i,j] = link.Restrictions[line.PID]
                        
            elif direct==3:
                self.__restricinfomx[i,:] = 5   #退出线造成的理论交限
            else:
                self.__restricinfomx[i] = [link.Restrictions[l.PID] if l.PID in link.Restrictions else 0 \
                                            for l in self.__outlinks]

        return True

    def build(self, readonlycursor):
        self.__getnodes(readonlycursor)
        self.__getlinks(readonlycursor)
        self.__initrestrictionmatrix()
        
        self.__updateenvelope()
        self.__calcbuffersize()

    @property
    def Restrictions(self):
        return  self.__restricinfomx
    
    @property
    def CrossEnvelope(self):
        crossenv = [c for c in self.__envelope]
        halflen = RDCross.defaultenvelopelen / 2
        
        if crossenv[2] - crossenv[0] < RDCross.defaultenvelopelen:
            centx = (crossenv[0] + crossenv[2])/2
            crossenv[0] = centx - halflen
            crossenv[2] = centx + halflen
            
        if crossenv[3] - crossenv[1] < RDCross.defaultenvelopelen:
            centy = (crossenv[1] + crossenv[3])/2
            crossenv[1] = centy - halflen
            crossenv[3] = centy + halflen
            
        return crossenv

    def makebuffermatrix(self):
        ''' 
        创建路口缓冲 
        ①取得路口Envelope，如果len或width为0则采用默认路口envelope宽度
        ②倍化路口Envelope，得到路口的Area
        ③将Area扩展pagesizefactor倍，得到整个的PageEnvelope
        
        '''
        
        #世界坐标参数
        crossenv = self.CrossEnvelope
        
        #页面参数
        world_mercator_buffer = [mercator.toMercator(self.__pageenvelope[i], self.__pageenvelope[i+1]) for i in (0,2)]
        
        page = pgmatrix(world_mercator_buffer)
        
        mercator_crossenv = [mercator.toMercator(crossenv[i], crossenv[i+1]) for i in (0, 2)]
        buf_width = (mercator_crossenv[1][0] - mercator_crossenv[0][0]) * (RDCross.areabufferfactor -1)
        buf_height = mercator_crossenv[1][1] - mercator_crossenv[0][1] * (RDCross.areabufferfactor -1)
        page.setbufsizeofmeter(buf_width, buf_height)

        #刷路口外link颜色
        for i in range(len(self.__outlinks)):
            link = self.__outlinks[i]
            linepts = [mercator.toMercator(x, y) for (x, y) in link.Coordinates()]
            page.colorline(linepts, i+1)
            
        #刷路口颜色
        page.colorregion([mercator.toMercator(self.__crossarea[i], self.__crossarea[i+1]) for i in (0, 2)])
        
        self.__pagebuffer = page.Matrix
#        np.savetxt(r"e:\workspace\Python\data\crossmx\cross_{id}.txt".format(id=self.PID), 
#                   self.__pagebuffer, 
#                   fmt='%d', 
#                   delimiter=',', 
#                   newline=';')
        
        return self.__pagebuffer
    
#    @classmethod
#    def __getSCRPagebufferJson(cls, denseMatrix):
#        sparsemx = sparse.csr_matrix(denseMatrix)
#        dic = {'data':sparsemx.data.tolist(), 
#               'indices':sparsemx.indices.tolist(),
#               'indptr':sparsemx.indptr.tolist()}
#               
#        return dic
        
    def exportenvelopedatas(self):
        env = spatial.getEnvelopeWithandHeight(self.CrossEnvelope)
        area = spatial.getEnvelopeWithandHeight(self.__crossarea)
        page = spatial.getEnvelopeWithandHeight(self.__pageenvelope)
        alllink = spatial.getEnvelopeWithandHeight(self.__outterenvelope)
        
        return [self.PID, env[0], env[1], area[0], area[1], page[0], page[1], alllink[0], alllink[1]]

    @staticmethod
    def convert2buildin(obj):
        if isinstance(obj, RDCross):
            dic = {'PID':obj.PID,
                   'Nodes':[n.getjsondict() for n in obj.__nodes],
                   'Links':[l.getjsondict() for l in obj.__outlinks],
                   'Restrictions':obj.__restricinfomx.tolist(),
                   'LinkDirection':obj.__throughlinkdirmx.tolist(),
                   'Envelope':obj.__envelope,
                   'Linkenvelope':obj.__outterenvelope,
                   'CrossArea':obj.__crossarea,
                   'Wholepage':obj.__pageenvelope}#,
                   #'PageBuffer':obj.__pagebuffer.tolist(),
                   #'SparseMatrix':obj.__getSCRPagebufferJson(obj.__pagebuffer)}

            return dic
        else:
            return obj