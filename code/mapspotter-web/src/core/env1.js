import solutionHandler from '../middleware/handler/solutionHandler'
const env = {
    map: null,
    style: null,
    mapAnimationTimeout: [],
    mapAnimationTimeInterval: [],
    step: {},
    reloadSource: function (sourceId, solution) {
        if (env.map) {
            let source = env.map.getSource(sourceId)
            if (source) {
                let t = env.step[sourceId]++
                //env.map.style._reloadSource(sourceId)
                let _layers = Object.values(env.map.style._layers)
                let layers = []
                setTimeout(function () {
                    solution.layers.map(layer => {
                            if (layer.source.startsWith(sourceId) && !layer.name.endsWith('_边框')) {
                                if (env.map.getLayer(layer.id)) {
                                    env.map.removeLayer(layer.id)
                                }
                            }
                        }
                    )
                }, 1000)
                let obj = {type: 'vector', tiles: source.tiles}
                let newSourceId = sourceId + t
                if (!env.map.getSource(newSourceId)) {
                    env.map.addSource(newSourceId, obj)
                }
                setTimeout(function () {
                    let layers = []
                    solution.layers.map(layer => {
                            if (layer.source.startsWith(sourceId) && !layer.name.endsWith('_边框')) {
                                layers.push(layer)
                            }
                        }
                    )
                    let reorderLayers = layers.sort((a, b)=> {
                        return a.zindex - b.zindex
                    })
                    reorderLayers.map(layer => {
                            if (layer.source.startsWith(sourceId) && !layer.name.endsWith('_边框')) {
                                let layerStyle = solutionHandler.genMapStyleLayer(Object.assign({}, layer, {
                                    id: layer.id + t,
                                    source: newSourceId
                                }))
                                env.map.addLayer(layerStyle)
                            }
                        }
                    )
                }, 500)

                if (env.step[sourceId] > 10) {
                    env.step[sourceId] = 0
                }
            }
        }
    },
    addAnimateTrafficSources: function () {
        env.map.addSource('traffic1', {
            "type": "geojson",
            "data": {
                "type": "FeatureCollection",
                "features": []
            }
        })
        env.map.addSource('traffic2', {
            "type": "geojson",
            "data": {
                "type": "FeatureCollection",
                "features": []
            }
        })
        env.map.addSource('traffic3', {
            "type": "geojson",
            "data": {
                "type": "FeatureCollection",
                "features": []
            }
        })
        env.map.addSource('traffic4', {
            "type": "geojson",
            "data": {
                "type": "FeatureCollection",
                "features": []
            }
        })
        env.map.addSource('traffic5', {
            "type": "geojson",
            "data": {
                "type": "FeatureCollection",
                "features": []
            }
        })
    },
    addAnimateTrafficLayers(){
        env.map.addLayer({
            "id": "traffic1",
            "source": "traffic1",
            "type": "line",
            "paint": {
                "line-color": "#42ff00",
                "line-width": {
                    "base": 1.2,
                    "stops": [
                        [
                            1,
                            2
                        ],
                        [
                            13,
                            2
                        ],
                        [
                            20,
                            15
                        ]
                    ]
                }
            }
        })
        env.map.addLayer({
            "id": "traffic2",
            "source": "traffic2",
            "type": "line",
            "paint": {
                "line-color": "#ffa100",
                "line-width": {
                    "base": 1.2,
                    "stops": [
                        [
                            1,
                            2
                        ],
                        [
                            13,
                            2
                        ],
                        [
                            20,
                            15
                        ]
                    ]
                }
            }
        })
        env.map.addLayer({
            "id": "traffic3",
            "source": "traffic3",
            "type": "line",
            "paint": {
                "line-color": "#ff0000",
                "line-width": {
                    "base": 1.2,
                    "stops": [
                        [
                            1,
                            2
                        ],
                        [
                            13,
                            2
                        ],
                        [
                            20,
                            15
                        ]
                    ]
                }
            }
        })
        env.map.addLayer({
            "id": "traffic4",
            "source": "traffic4",
            "type": "line",
            "paint": {
                "line-color": "#8e0e0b",
                "line-width": {
                    "base": 1.2,
                    "stops": [
                        [
                            1,
                            2
                        ],
                        [
                            13,
                            2
                        ],
                        [
                            20,
                            15
                        ]
                    ]
                }
            }
        })
        env.map.addLayer({
            "id": "traffic5",
            "source": "traffic5",
            "type": "line",
            "paint": {
                "line-color": "#87cefa",
                "line-width": {
                    "base": 1.2,
                    "stops": [
                        [
                            1,
                            2
                        ],
                        [
                            13,
                            2
                        ],
                        [
                            20,
                            15
                        ]
                    ]
                }
            }
        })
    },
    trafficLineStep: 0,
    trafficSourceFeatures: {
        'traffic1': [],
        'traffic2': [],
        'traffic3': [],
        'traffic4': [],
        'traffic5': []
    },
    animateTrafficLine: function () {
        env.mapAnimationTimeInterval.push(setInterval(()=> {
            env.trafficSourceFeatures.traffic1 = env.map.querySourceFeatures('traffic', {
                sourceLayer: 'TrafficStatus',
                filter: ["==", "status", 1]
            })
            env.trafficSourceFeatures.traffic2 = env.map.querySourceFeatures('traffic', {
                sourceLayer: 'TrafficStatus',
                filter: ["==", "status", 2]
            })
            env.trafficSourceFeatures.traffic3 = env.map.querySourceFeatures('traffic', {
                sourceLayer: 'TrafficStatus',
                filter: ["==", "status", 3]
            })
            env.trafficSourceFeatures.traffic4 = env.map.querySourceFeatures('traffic', {
                sourceLayer: 'TrafficStatus',
                filter: ["==", "status", 4]
            })
            env.trafficSourceFeatures.traffic5 = env.map.querySourceFeatures('traffic', {
                sourceLayer: 'TrafficStatus',
                filter: ["==", "status", 5]
            })

            for (let k = 1; k <= 5; k++) {
                let tk = 'traffic' + k + '' + k
                env.trafficSourceFeatures[tk] = []
                env.trafficSourceFeatures['traffic' + k].map(t=> {
                    let arc = []
                    /*
                    let lineDistance = turf.lineDistance(t, 'meters')
                    for (var i = 0; i < lineDistance; i++) {
                        var segment = turf.along(t, i / 100 * lineDistance, 'meters');
                        arc.push(segment.geometry.coordinates)
                    }*/
                    let lineDistance = t.geometry.coordinates
                    for (var i = 0; i < lineDistance.length; i++) {
                        arc.push(lineDistance[i])
                    }
                    let tt = Object.assign({}, {
                        type: "Feature",
                        geometry: {coordinates: arc, type: "LineString"},
                        properties: t.properties
                    })
                    env.trafficSourceFeatures[tk].push(tt)
                })
            }
            env.trafficLineStep = 1
            env.animateTrafficLineLoop()
        }, parseInt(8000)))

    },

    animateTrafficLineLoop: function () {
        let data = {
            "type": "FeatureCollection",
            "features": []
        }
        for (let k = 1; k <= 5; k++) {
            let tk = 'traffic' + k + '' + k
            let features = []
            env.trafficSourceFeatures[tk].map(t=> {
                let coods = t.geometry.coordinates
                let arc = []
                if (env.trafficLineStep < coods.length) {
                    arc = coods.slice(0, env.trafficLineStep)
                } else {
                    arc = coods
                }
                let tt = Object.assign({}, {
                    type: "Feature",
                    geometry: {coordinates: arc, type: "LineString"},
                    properties: t.properties
                })
                features.push(tt)
            })
            env.map.getSource('traffic' + k).setData(Object.assign({}, data, {features: features}))
        }
        env.trafficLineStep++
        requestAnimationFrame(env.animateTrafficLineLoop)
    }
}

export default env1