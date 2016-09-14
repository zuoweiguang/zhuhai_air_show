import AnimationTool from '../../utils/AnimationTool'
import env from '../env'

export default class PointAnimation1 {
    constructor(map, options) {
        this.map = map
        this.options = options || {}
        this.painter = this.map.painter
        this.gl = this.painter.gl
        this.transform = this.painter.transform
        this.routes = options.routes
        this.tracks = []
        this.updateRoutes = this.updateRoutes.bind(this)
        this.init = this.init.bind(this)
        this.start = this.start.bind(this)
        this.initVehicles = this.initVehicles.bind(this)
        this.loopData = this.loopData.bind(this)
        this.distanceNum = 10
        this.trackNum = 2
        this.frameId = null
    }

    updateRoutes(routes) {
        this.routes = routes
    }

    init() {
        let _tracks = this.tracks = []
        let _routes = this.routes
        let _distanceNum = this.distanceNum
        let j = 0
        _routes.map(route=> {
            if (j < 1000) {
                let lineDistance = turf.lineDistance(route, 'meters')
                for (let i = 0; i < _distanceNum; i++) {
                    let segment = turf.along(route, i * lineDistance / _distanceNum, 'meters')
                    _tracks.push(segment.geometry.coordinates)
                }
            }
            j++
        })
        console.log("initAnimationOver--" + _tracks.length + "---" + new Date().getTime())
    }

    initVehicles() {
        let map = this.map
        let _distanceNum = this.distanceNum
        let _trackNum = this.trackNum
        let _tracks = this.tracks
        let features = []
        let len = _tracks.length / _distanceNum
        let colors = ["#87cefa", "#42ff00", "#ffa100", "#ff0000", "#8e0e0b"]
        let colorLen = colors.length
        let vehicles = {
            "type": "geojson",
            "data": {
                "type": "FeatureCollection",
                "features": []
            }
        }
        for (let j = 0; j < len; j++) {
            let cloroIndex = parseInt(Math.random() * colorLen)
            let color = colors[cloroIndex]
            let min = j * _distanceNum
            let max = (j + 1) * _distanceNum
            for (let i = 0; i < _trackNum; i++) {
                let p = parseInt(Math.random() * _distanceNum) + 1 + min
                if (p > max - 1) {
                    p = max - 1
                }
                if (p < min) {
                    p = min
                }
                let current = _tracks[p]
                features[j * _trackNum + i] = {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": current
                    },
                    "properties": {
                        "pointColor": color,
                        "position": p
                    }
                }
            }
        }

        vehicles["data"]["features"] = features
        map.getSource('vehicles').setData(vehicles.data)
        console.log("startAnimationOver--" + new Date().getTime())
        return vehicles
    }

    loopData(vehicles) {
        let _tracks = this.tracks
        let _distanceNum = this.distanceNum
        let map = this.map
        let _vehicles = Object.assign({}, vehicles)
        let features = _vehicles.data.features
        let lenFeatures = features.length

        const moveMarkers = ()=> {
            for (let i = 0; i < lenFeatures; i++) {
                moveMarker(features[i])
            }
        }

        const moveMarker = (marker) => {
            let i = marker.properties.position
            let j = parseInt(i / _distanceNum)
            let min = j * _distanceNum
            let max = (j + 1) * _distanceNum
            let p = i + 1
            if (p > max - 1) {
                p = min
            }
            if (p < min) {
                p = min
            }
            let current = _tracks[p]

            marker.geometry.coordinates = current
            marker.properties.position = p
        }

        moveMarkers()
        map.getSource('vehicles').setData(_vehicles.data)
        return _vehicles
    }

    start() {
        let map = this.map
        let vehicles = {}
        let that = this
        let _frameId = this.frameId

        if (_frameId) {
            AnimationTool.cancelFrame(_frameId)
        }

        setTimeout(function () {
            startBefore()
            loop()
        }, 100)

        const startBefore = ()=> {
            vehicles = that.initVehicles()
        }

        const loop = ()=> {
            env.mapAnimationTimeout.push(setTimeout(()=> {
                vehicles = that.loopData(vehicles)
                _frameId = AnimationTool.frame(loop)
                env.frameIdSet.add(_frameId)
                console.log("loopAnimationOver--" + new Date().getTime())
            }, 200))
        }
    }


}