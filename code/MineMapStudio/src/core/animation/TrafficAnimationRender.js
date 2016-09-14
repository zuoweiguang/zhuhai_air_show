import env from '../env'

export default class TrafficAnimationRender {
    constructor(map, options) {
        this.map = map
        this.options = options || {}
        this.animationMinZoom = 8
        this.animationMaxZoom = 19
        this.sourceStep = 0
        this.sourceObj = {
            "type": "vector",
            "tiles": ["http://minemap.navinfo.com/service/minemap/view/traffic/{z}/{x}/{y}"]
        }
        this.init = this.init.bind(this)
        this.getLayerStyle = this.getLayerStyle.bind(this)
        this.reloadSource = this.reloadSource.bind(this)
        this.start = this.start.bind(this)
    }

    init() {
        let map = this.map
        let animationMinZoom = this.animationMinZoom
        let animationMaxZoom = this.animationMaxZoom
        let sourceStep = this.sourceStep
        let sourceObj = this.sourceObj
        let that = this
        if (!map.getSource("animationSource_trafficStatus0")) {
            map.addSource("animationSource_trafficStatus0", sourceObj)
        }
        let layerStatus = [0, 1, 2, 3, 4, 5]

        layerStatus.map(status => {
                let layerStyle = that.getLayerStyle(status, 0)
                map.addLayer(layerStyle)
            }
        )

        let mapLayerIds = Object.keys(map.style._layers)
        let spiritLayers = []
        let brandLayers = []
        for (let i = 0; i < mapLayerIds.length; i++) {
            let id = mapLayerIds[i]
            if (id.startsWith("animationLayer_tmcSpiritPoint")) {
                spiritLayers.push(map.getLayer(id))
                map.removeLayer(id)
            } else if (id.startsWith("animationLayer_tmcBrand")) {
                brandLayers.push(map.getLayer(id))
                map.removeLayer(id)
            }
        }
        brandLayers.map(layer=> {
            map.addLayer(layer)
        })
        spiritLayers.map(layer=> {
            map.addLayer(layer)
        })
    }

    getLayerStyle(layerStutas, sourceStep) {
        let animationMinZoom = this.animationMinZoom
        let animationMaxZoom = this.animationMaxZoom
        let colors = ["#87cefa", "#42ff00", "#ffa100", "#ff0000", "#8e0e0b", "#87cefa"]
        let color = colors[layerStutas]
        return {
            "id": "animationLayer_trafficStatus" + 6 * sourceStep + layerStutas,
            "type": "line",
            "source": "animationSource_trafficStatus" + sourceStep,
            "source-layer": "TrafficStatus",
            "minzoom": animationMinZoom,
            "maxzoom": animationMaxZoom,
            "layout": {
                "line-cap": "round",
                "line-join": "round",
                "visibility": "visible",
                "line-round-limit": 2,
                "line-miter-limit": 0
            },
            "paint": {
                "line-color": color,
                "line-width": {
                    "base": 1.2,
                    "stops": [
                        [
                            1,
                            2
                        ],
                        [
                            12,
                            1.5
                        ],
                        [
                            20,
                            2
                        ]
                    ]
                },
                "line-blur": 1
            },
            "filter": ["==", "status", layerStutas]
        }
    }

    start() {
        let sourceStep = this.sourceStep
        let that = this
        sourceStep = 0
        env.trafficAnimation.timeoutSet.add(setTimeout(()=> {
            that.reloadSource()
            //env.trafficAnimation.timeIntervalSet.add(setInterval(()=> {
            //    that.reloadSource()
            //}, 60000))
        }, 400))
    }

    reloadSource() {
        let map = this.map
        let sourceStep = this.sourceStep++
        let sourceObj = this.sourceObj
        let that = this

        let layerIds = ["animationLayer_trafficStatus" + (sourceStep * 6 + 1), "animationLayer_trafficStatus" + (6 * sourceStep + 2), "animationLayer_trafficStatus" + (6 * sourceStep + 3), "animationLayer_trafficStatus" + (6 * sourceStep) + 4, "animationLayer_trafficStatus" + (6 * sourceStep + 5), "animationLayer_trafficStatus" + (6 * sourceStep + 6)]
        layerIds.map(layerId => {
                if (map.getLayer(layerId)) {
                    map.removeLayer(layerId)
                }
            }
        )
        let newSourceId = "animationSource_trafficStatus" + (sourceStep + 1)
        if (!map.getSource(newSourceId)) {
            map.addSource(newSourceId, sourceObj)
        }

        for (let i = 0; i < 6; i++) {
            let layerStyle = that.getLayerStyle(i, sourceStep + 1)
            map.addLayer(layerStyle)
        }


        if (this.sourceStep > 9) {
            this.sourceStep = 0
        }
    }
}