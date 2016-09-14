#!/usr/bin/python
# -*- coding: utf-8 -*-

from operator import isSequenceType


class SdoGeometry:
    """This class converts the oracle SDO_GEOMETRY type to GeoJSON-like object and WKT String
    http://download.oracle.com/docs/cd/B19306_01/appdev.102/b14255/sdo_objrelschema.htm#i1004087
    http://geojson.org/geojson-spec.html 
    Usage: 
    g = SdoGeometry(sdo_geometry_object_from_oracle)
    print g.wkt()
    import simplejson
    print simplejson.dumps(g.__geo_interface__)
    from shapely.geometry import asShape
    shape = asShape(g)
    print shape.bounds
    .....  
    """

    def get_quadruplets(self):
        """This function returns 'quadruplets'.
        Those are basically SDO_ELEM_INFO triplets with ending coordinate added for later convenience"""

        elem_info_array = self.SDO_ELEM_INFO
        quadruplets = []
        if not elem_info_array:
            return quadruplets

        for i in range(0, len(elem_info_array) - 3, 3):
            quadruplets.append(elem_info_array[i:i + 4])

        # +[len(self.SDO_ORDINATES)+1] add where the next primitive geometry begins (1-based index)
        quadruplets.append(elem_info_array[-3:] + [len(self.SDO_ORDINATES) + 1])

        return quadruplets

    def build_geojson_dict(self):
        '''the implement of the geojson geo-interface'''

        geom_types_set = set()

        quadruplets = self.get_quadruplets()

        result_types = []

        result_coordinates = []

        if self.SDO_POINT:
            return {"type": "Point", "coordinates": [self.SDO_POINT.X, self.SDO_POINT.Y]}

        for g in quadruplets:
            coords = []
            if g[1] == 1:
                result_types.append("Point")
                geom_types_set.add("Point")
                coords.append(self.SDO_ORDINATES[int(g[0]) - 1])
                coords.append(self.SDO_ORDINATES[int(g[0])])
                result_coordinates.append(coords)
            elif g[1] == 2:  # linestring
                result_types.append("LineString")
                geom_types_set.add("LineString")
                for j in range(int(g[0]) - 1, int(g[3]) - 1, 2):
                    coords.append([self.SDO_ORDINATES[j], self.SDO_ORDINATES[j + 1]])
                result_coordinates.append(coords)
            elif g[1] == 1003:  # polygon
                result_types.append("Polygon")
                geom_types_set.add("Polygon")
                begin = int(g[0]) - 1
                if g[2] == 3:
                    coords.extend([[self.SDO_ORDINATES[begin], self.SDO_ORDINATES[begin + 1]],
                                   [self.SDO_ORDINATES[begin + 2], self.SDO_ORDINATES[begin + 1]],
                                   [self.SDO_ORDINATES[begin + 2], self.SDO_ORDINATES[begin + 3]],
                                   [self.SDO_ORDINATES[begin], self.SDO_ORDINATES[begin + 3]],
                                   [self.SDO_ORDINATES[begin], self.SDO_ORDINATES[begin + 1]]
                                   ])
                else:
                    for j in range(begin, int(g[3]) - 1, 2):
                        coords.append([self.SDO_ORDINATES[j], self.SDO_ORDINATES[j + 1]])
                result_coordinates.append([coords])
            elif (g[1] == 2003) and (result_types[-1] == "Polygon"):  # polygon hole.
                # according to specification hole can be specified before the main polygon. We don't account for this
                for j in range(int(g[0]) - 1, int(g[3]) - 1, 2):
                    coords.append([self.SDO_ORDINATES[j], self.SDO_ORDINATES[j + 1]])
                result_coordinates[-1].append(coords)

        if len(result_types) == 0:
            return None
        elif len(result_types) == 1:
            return {"type": result_types[0], "coordinates": result_coordinates[0]}
        elif len(geom_types_set) == 1:
            if "LineString" in geom_types_set:
                return {"type": "MultiLineString", "coordinates": result_coordinates}
            elif "Polygon" in geom_types_set:
                return {"type": "MultiPolygon", "coordinates": result_coordinates}
        else:
            geometries = []
            for i in range(0, len(result_types)):
                geometries.append({"type": result_types[i], "coordinates": result_coordinates[i]})  # untested
            return {"type": "GeometryCollection", "geometries": geometries}

    def __init__(self, *args):
        if len(args) == 1:
            sdo_geom = args[0]
            self.SDO_GTYPE = int(sdo_geom.SDO_GTYPE)
            self.SDO_SRID = sdo_geom.SDO_SRID
            self.SDO_POINT = sdo_geom.SDO_POINT
            self.SDO_ELEM_INFO = sdo_geom.SDO_ELEM_INFO
            self.SDO_ORDINATES = sdo_geom.SDO_ORDINATES
        else:
            self.SDO_GTYPE = args[0]
            self.SDO_SRID = args[1]
            self.SDO_POINT = args[2]
            self.SDO_ELEM_INFO = args[3]
            self.SDO_ORDINATES = args[4]
        self.__geo_interface__ = self.build_geojson_dict()

    def dimensions(self):
        return self.SDO_GTYPE / 1000

    def wkt(self):
        g = self.__geo_interface__

        if g["type"] == 'Point':
            res = "POINT (%s)"
        else:
            res = g["type"].upper() + " %s"

        try:
            import nicefloat
            def nice_format(x):
                try:
                    return nicefloat.nicefloat.str(x)
                except:
                    return "%.15s" % x
        except ImportError:
            def nice_format(x):
                return "%.15s" % x

        return res % geo_to_wkt_coords_nicefloat(g['coordinates'], nice_format)


def geo_to_wkt_coords_nicefloat(a, formator=str):
    if (type(a) == list) and (len(a) > 0) and not isSequenceType(a[0]):
        return " ".join([formator(k) for k in a])

    return "(" + ", ".join([geo_to_wkt_coords_nicefloat(k, formator) for k in a]) + ")"
