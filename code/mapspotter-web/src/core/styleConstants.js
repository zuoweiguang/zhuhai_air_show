export const satSource = {
    "sat": {
        "type": "raster",
        //"url" : "http://192.168.4.130:8080/demo2/sat.json",
        "tileSize": 256,
        "attribution": "",
        "autoscale": true,
        "bounds": [-180,
            -85,
            180,
            85],
        "cacheControl": "max-age=43200,s-maxage=604800",
        "center": [-43.88955327932,
            -12.590178885765,
            3],
        "created": 1358310600000,
        "description": "",
        "id": "mapbox.satellite",
        "mapbox_logo": true,
        "maxzoom": 19,
        "minzoom": 0,
        "modified": 1446150592060,
        "name": "Mapbox Satellite",
        "private": false,
        "scheme": "xyz",
        "tilejson": "2.0.0",
        "tiles": ["http://webst01.is.autonavi.com/appmaptile?style=6&x={x}&y={y}&z={z}"]
    }
}

export const roadSource = {
    "road": {
        "type": "vector",
        "tiles": ["http://192.168.4.218:8080/mapspotter/view/road/{z}/{x}/{y}"]
    }
}

export const adminSource = {
    "admin": {
        "type": "vector",
        "tiles": ["http://192.168.4.218:8080/mapspotter/view/admin/{z}/{x}/{y}"]
    }
}

export const backgroundSource = {
    "background": {
        "type": "vector",
        "tiles": ["http://192.168.4.218:8080/mapspotter/view/background/{z}/{x}/{y}"]
    }
}

export const poiSource = {
    "poi": {
        "type": "vector",
        "tiles": ["http://192.168.4.218:8080/mapspotter/view/poi/{z}/{x}/{y}"]
    }
}

export const constructionSource = {
    "construction": {
        "type": "vector",
        "tiles": ["http://192.168.4.218:8080/mapspotter/view/ms/{z}/{x}/{y}?type=Construction"]
    }
}

export const missingroadSource = {
    "missingroad": {
        "type": "vector",
        "tiles": ["http://192.168.4.218:8080/mapspotter/view/ms/{z}/{x}/{y}?type=Missingroad"]
    }
}

export const restricDetailSource = {
    "restricDetail": {
        "type": "vector",
        "tiles": ["http://192.168.4.218:8080/mapspotter/view/ms/{z}/{x}/{y}?type=RestricDetail"]
    }
}

export const originalTrailSource = {
    "originaltrail": {
        "type": "raster",
        "tileSize": 256,
        "tiles": ["http://119.29.86.160:8080/heatmap/show?source=sogou_0523_0605&x={x}&y={y}&z={z}"]
    }
}

export const backgroundLayer = {
    "id": "background",
    "type": "background",
    "paint": {
        "background-color": "#fafafa"
    },
    "interactive": false
}

export const satelliteLayer = {
    "id": "satellite",
    "type": "raster",
    "source": "sat",
    "source-layer": "mapbox_satellite_full",
    "layout": {
        "visibility": "visible"
    }
}

export const adminBoundaryLayer = {
    "id": "region_link",
    "type": "line",
    "source": "admin",
    "source-layer": "AdminBoundary",
    "layout": {
        "line-cap": "round",
        "line-join": "round"
    },
    "filter": ["in", "kind", 1, 2],
    "paint": {
        "line-color": "#00ff00",
        "line-width": {
            "base": 0.6,
            "stops": [[5, 0.6], [6, 0.8], [7, 1.9], [20, 4]]
        }
    }
}

export const luLayer = {
    "id": "lu",
    "type": "fill",
    "source": "background",
    "source-layer": "LU",
    "paint": {
        "fill-color": "#eeeeff"
    }
}

export const lcLayer = {
    "id": "lc",
    "type": "fill",
    "source": "background",
    "source-layer": "LC",
    "paint": {
        "fill-color": "#00ff00"
    }
}

export const cityModelLayer = {
    "id": "cityModel",
    "type": "fill",
    "source": "background",
    "source-layer": "CityModel",
    "paint": {
        "fill-color": "#eeeeff"
    }
}

export const railwayLayer = {
    "interactive": true,
    "layout": {
        "line-cap": "round",
        "line-join": "round"
    },
    "metadata": {
        "mapbox:group": "1444849345966.4436"
    },
    "type": "line",
    "source": "road",
    "id": "railway",
    "paint": {
        "line-color": "#ff0000",
        "line-width": {
            "base": 1.2,
            "stops": [[5, 0.6], [6, 0.8], [7, 1.9], [20, 17]]
        }
    },
    "source-layer": "RailWay"
}

export const railway2Layer = {
    "interactive": true,
    "layout": {
        "line-cap": "round",
        "line-join": "round"
    },
    "metadata": {
        "mapbox:group": "1444849345966.4436"
    },
    "type": "line",
    "source": "road",
    "id": "railway2",
    "paint": {
        "line-color": "#eeeeff",
        "line-width": {
            "base": 1.2,
            "stops": [[5, 0.3], [6, 0.5], [7, 1.3], [20, 10]]
        }
    },
    "source-layer": "RailWay"
}

export const roadLayer = {
    "interactive": true,
    "layout": {
        "line-cap": "round",
        "line-join": "round"
    },
    "metadata": {
        "mapbox:group": "1444849345966.4436"
    },
    "type": "line",
    "source": "road",
    "id": "road",
    "paint": {
        "line-color": "#808000",
        "line-width": {
            "base": 1.2,
            "stops": [[5, 0.6], [6, 0.8], [7, 1.9], [20, 10]]
        }
    },
    "source-layer": "Road"
}

export const road2Layer = {
    "id": "road2",
    "interactive": true,
    "layout": {
        "line-cap": "round",
        "line-join": "round"
    },
    "metadata": {
        "mapbox:group": "1444849345966.4436"
    },
    "type": "line",
    "source": "road",
    "paint": {
        "line-color": "#fff",
        "line-width": {
            "base": 1.2,
            "stops": [[13.5, 0], [14, 2.5], [20, 11.5]]
        }
    },
    "source-layer": "Road"
}

export const roadNameLayer = {
    "interactive": true,
    "layout": {
        "text-field": "{name}",
        "text-font": ["Open Sans Regular",
            "Arial Unicode MS Regular"],
        "text-size": {
            "base": 1,
            "stops": [[13, 12], [14, 13]]
        },
        "symbol-placement": "line"
    },
    "metadata": {
        "mapbox:group": "1456163609504.0715"
    },
    "type": "symbol",
    "source": "road",
    "id": "road_label",
    "paint": {
        //"text-color" : "#0000ff",
        "text-halo-width": 1.0,
        "text-halo-blur": 0.3
    },
    "source-layer": "Road"
}

export const poiLayer = {
    "id": "poi_pt",
    "type": "circle",
    "source": "poi",
    "source-layer": "Poi",
    "paint": {
        "circle-radius": 3,
        "circle-color": "#0000ff",
        "circle-opacity": 0.8
    }
}

export const originalTrailLayer = {
    "id": "originaltrail",
    "type": "raster",
    "source": "originaltrail",
    "minzoom": 7,
    "maxzoom": 16,
    "layout": {
        "visibility": "visible"
    }
}

export const defaultStyle = {
    "version": 8,
    "name": "Bright",
    "sources": {
        "sat": {
            "type": "raster",
            //"url" : "http://192.168.4.130:8080/demo2/sat.json",
            "tileSize": 256,
            "attribution": "",
            "autoscale": true,
            "bounds": [-180,
                -85,
                180,
                85],
            "cacheControl": "max-age=43200,s-maxage=604800",
            "center": [-43.88955327932,
                -12.590178885765,
                3],
            "created": 1358310600000,
            "description": "",
            "id": "mapbox.satellite",
            "mapbox_logo": true,
            "maxzoom": 19,
            "minzoom": 0,
            "modified": 1446150592060,
            "name": "Mapbox Satellite",
            "private": false,
            "scheme": "xyz",
            "tilejson": "2.0.0",
            "tiles": ["http://webrd01.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=7&x={x}&y={y}&z={z}", "http://webrd03.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=7&x={x}&y={y}&z={z}", "http://webrd04.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=7&x={x}&y={y}&z={z}", "http://webrd02.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=7&x={x}&y={y}&z={z}"]

            //卫星影像地址
            //http://webst01.is.autonavi.com/appmaptile?style=6&x={x}&y={y}&z={z}
        }

    },
    //"sprite" : "http://192.168.4.130:8080/demo2/sprite",
    //"glyphs": "mapbox://fonts/mapbox/{fontstack}/{range}.pbf",
    "layers": [
        {
            "id": "background",
            "type": "background",
            "paint": {
                "background-color": "#fafafa"
            },
            "interactive": true
        },
        {
            "id": "satellite",
            "type": "raster",
            "source": "sat",
            "source-layer": "mapbox_satellite_full",
            "layout": {
                "visibility": "visible"
            }
        }
    ]
}

export const simpleStyle = {
    "version": 8,
    "name": "Bright",
    "sources": {

        "road2": {
            "type": "vector",
            "tiles": [
                "http://192.168.4.130:8080/demo2/road/{z}/{x}/{y}.pbf"]

        },
        "admin": {
            "type": "vector",
            "tiles": ["http://192.168.4.218:8080/mapspotter/view/admin/{z}/{x}/{y}"]
        }
        ,
        "sat": {
            "type": "raster",
            //"url" : "http://192.168.4.130:8080/demo2/sat.json",
            "tileSize": 256,

            "attribution": "",
            "autoscale": true,
            "bounds": [-180,
                -85,
                180,
                85],
            "cacheControl": "max-age=43200,s-maxage=604800",
            "center": [0,
                0,
                3],
            "created": 1358310600000,
            "description": "",
            "id": "mapbox.satellite",
            "mapbox_logo": true,
            "maxzoom": 19,
            "minzoom": 0,
            "modified": 1446150592060,
            "name": "Mapbox Satellite",
            "private": false,
            "scheme": "xyz",
            "tilejson": "2.0.0",
            "tiles": ["http://webst01.is.autonavi.com/appmaptile?style=6&x={x}&y={y}&z={z}"]
        }
    },
    //"sprite" : "http://192.168.4.130:8080/demo2/sprite",
    "glyphs": "mapbox://fonts/mapbox/{fontstack}/{range}.pbf",
    "layers": [{
        "id": "background",
        "type": "background",
        "paint": {
            "background-color": "#fafafa"
        },
        "interactive": true
    }
        , {
            "id": "satellite",
            "type": "raster",
            "source": "sat",
            "source-layer": "mapbox_satellite_full",
            "layout": {
                "visibility": "visible"
            }
        }, {
            "id": "region_link",
            "type": "line",
            "source": "admin",
            "source-layer": "AdminBoundary",
            "layout": {
                "line-cap": "round",
                "line-join": "round"
            },
            "filter": ["in", "kind", 1, 2],
            "paint": {
                "line-color": "#00ff00",
                "line-width": {
                    "base": 0.6,
                    "stops": [[5, 0.6], [6, 0.8], [7, 1.9], [20, 4]]
                }
            }
        }
        , {
            "interactive": true,
            "layout": {
                "line-cap": "round",
                "line-join": "round"
            },
            "metadata": {
                "mapbox:group": "1444849345966.4436"
            },
            "type": "line",
            "source": "road2",
            "id": "road",
            "paint": {
                "line-color": "#ff0000"
                ,
                "line-width": {
                    "base": 1.2,
                    "stops": [[5, 0.6], [6, 0.8], [7, 1.9], [20, 17]]
                }
            },
            "source-layer": "road"
        }
        , {
            "interactive": true,
            "layout": {
                "line-cap": "round",
                "line-join": "round"
            },
            "metadata": {
                "mapbox:group": "1444849345966.4436"
            },
            "type": "line",
            "source": "road2",
            "id": "road2",
            "paint": {
                "line-color": "#eeeeff"
                ,
                "line-width": {
                    "base": 1.2,
                    "stops": [[5, 0.3], [6, 0.5], [7, 1.3], [20, 10]]
                }
            },
            "source-layer": "road"
        }
        , {
            "minzoom": 20,
            "interactive": true,
            "layout": {
                "text-field": "{name_cn}",
                //"text-font" : [ "Open Sans Regular",
                //    "Arial Unicode MS Regular" ],
                "text-size": {
                    "base": 1,
                    "stops": [[13, 12], [14, 13]]
                },
                "symbol-placement": "line"
            },
            "metadata": {
                "mapbox:group": "1456163609504.0715"
            },

            "type": "symbol",
            "source": "road2",
            "id": "road_label",
            "paint": {
                //"text-color" : "#0000ff",
                //"text-color" : "{mycolor}",
                "text-halo-width": 1.4,
                "text-halo-blur": 0.5
            },
            "source-layer": "road2"
        }
    ]
}

export const warehouseStyle = {
    "version": 8,
    "name": "Bright",
    "sources": {
        "road": {
            "type": "vector",
            "tiles": ["http://192.168.4.218:8080/mapspotter/view/road/{z}/{x}/{y}"]
        },
        "admin": {
            "type": "vector",
            "tiles": ["http://192.168.4.218:8080/mapspotter/view/admin/{z}/{x}/{y}"]
        },
        "background": {
            "type": "vector",
            "tiles": ["http://192.168.4.218:8080/mapspotter/view/background/{z}/{x}/{y}"]
        },
        "poi": {
            "type": "vector",
            "tiles": ["http://192.168.4.218:8080/mapspotter/view/poi/{z}/{x}/{y}"]
        }
    },
    //"sprite" : "http://192.168.4.130:8080/demo2/sprite",
    "glyphs": "mapbox://fonts/mapbox/{fontstack}/{range}.pbf",
    "layers": [{
        "id": "region_link",
        "type": "line",
        "source": "admin",
        "source-layer": "AdminBoundary",
        "layout": {
            "line-cap": "round",
            "line-join": "round"
        },
        "filter": ["in", "kind", 1, 2],
        "paint": {
            "line-color": "#000000",
            "line-width": {
                "base": 0.6,
                "stops": [[5, 0.6], [6, 0.8], [7, 1.9], [20, 4]]
            }
        }
    }, {
        "id": "lu",
        "type": "fill",
        "source": "background",
        "source-layer": "LU",
        "paint": {
            "fill-color": "#eeeeff"
        }
    }, {
        "id": "lc",
        "type": "fill",
        "source": "background",
        "source-layer": "LC",
        "paint": {
            "fill-color": "#00ff00"
        }
    },
        {
            "interactive": true,
            "layout": {
                "line-cap": "round",
                "line-join": "round"
            },
            "metadata": {
                "mapbox:group": "1444849345966.4436"
            },
            "type": "line",
            "source": "road",
            "id": "railway",
            "paint": {
                "line-color": "#ff0000",
                "line-width": {
                    "base": 1.2,
                    "stops": [[5, 0.6], [6, 0.8], [7, 1.9], [20, 17]]
                }
            },
            "source-layer": "RailWay"
        }, {
            "interactive": true,
            "layout": {
                "line-cap": "round",
                "line-join": "round"
            },
            "metadata": {
                "mapbox:group": "1444849345966.4436"
            },
            "type": "line",
            "source": "road",
            "id": "railway2",
            "paint": {
                "line-color": "#eeeeff",
                "line-width": {
                    "base": 1.2,
                    "stops": [[5, 0.3], [6, 0.5], [7, 1.3], [20, 10]]
                }
            },
            "source-layer": "RailWay"
        }, {
            "interactive": true,
            "layout": {
                "line-cap": "round",
                "line-join": "round"
            },
            "metadata": {
                "mapbox:group": "1444849345966.4436"
            },
            "type": "line",
            "source": "road",
            "id": "road",
            "paint": {
                "line-color": "#808000",
                "line-width": {
                    "base": 1.2,
                    "stops": [[5, 0.6], [6, 0.8], [7, 1.9], [20, 10]]
                }
            },
            "source-layer": "Road"
        }, {
            "interactive": true,
            "layout": {
                "text-field": "{name}",
                "text-font": ["Open Sans Regular",
                    "Arial Unicode MS Regular"],
                "text-size": {
                    "base": 1,
                    "stops": [[13, 12], [14, 13]]
                },
                "symbol-placement": "line"
            },
            "metadata": {
                "mapbox:group": "1456163609504.0715"
            },

            "type": "symbol",
            "source": "road",
            "id": "road_label",
            "paint": {
                //"text-color" : "#0000ff",
                "text-halo-width": 1.0,
                "text-halo-blur": 0.3
            },
            "source-layer": "Road"
        }, {
            "id": "poi_pt",
            "type": "circle",
            "source": "poi",
            "source-layer": "Poi",
            "paint": {
                "circle-radius": 3,
                "circle-color": "#0000ff",
                "circle-opacity": 0.8
            }
        }]
}

export const defaultStyleBackground = {
    "layout": {},
    "paint": {
        "background-color": "#9DD64F"
    }
}

export const defaultStyleCircle = {
    "layout": {},
    "paint": {
        "circle-radius": 8,
        "circle-color": "#ff0000",
        "circle-opacity": 0.8
    }
}

export const defaultStyleExtrusion = {
    "layout": {},
    "paint": {
        "extrusion-color": "#FFEFD5",
        "extrusion-antialias": true,
        "extrusion-lighting-anchor": "viewport",
        "extrusion-outline-color": "#909",
        "extrusion-opacity": 1,
        "extrusion-shadow-color": "#00f"
    }
}

export const defaultStyleFill = {
    "layout": {},
    "paint": {
        "fill-color": "#9ED9DA"
    }
}

export const defaultStyleLine = {
    "layout": {
        "line-cap": "round",
        "line-join": "round"
    },
    "paint": {
        "line-color": "#ff0000",
        "line-width": {
            "base": 1.2,
            "stops": [
                [
                    5,
                    0.1
                ],
                [
                    6,
                    0.2
                ],
                [
                    7,
                    1.5
                ],
                [
                    20,
                    18
                ]
            ]
        }
    }
}

export const defaultStyleRaster = {
    "layout": {},
    "paint": {}
}

export const defaultStyleSymbol = {
    "layout": {
        "icon-image": "marker-15"
    },
    "paint": {
        "icon-color": "#ff0000"
    }
}

export const defaultStyleText = {
    "layout": {
        "text-field": "{name}",
        "text-font": [
            "Open Sans Regular",
            "Arial Unicode MS Regular"
        ],
        "text-size": {
            "base": 1,
            "stops": [
                [
                    13,
                    12
                ],
                [
                    14,
                    13
                ]
            ]
        },
        "symbol-placement": "line"
    },
    "paint": {
        "text-halo-width": 1.0,
        "text-halo-blur": 0.3,
        "text-color": "#313A4B",
        "text-halo-color": "#FCFBFA"
    }
}