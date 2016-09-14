import AnimationTool from '../../utils/AnimationTool'
import env from '../env'

export default class PointAnimation {
    constructor(map, options) {
        this.map = map
        this.options = options || {}
        this.routes = options.routes
        this.tracks = []
        this.updateRoutes = this.updateRoutes.bind(this)
        this.init = this.init.bind(this)
        this.start = this.start.bind(this)
        this.endAnimate = this.endAnimate.bind(this)
        this.initVehicles = this.initVehicles.bind(this)
        this.loopData = this.loopData.bind(this)
        this.animationTimeInterval = 50
        this.animationNumInterval = 50
    }

    updateRoutes(routes) {
        this.routes = routes
    }

    init() {
        let _tracks = this.tracks
        let _routes = this.routes
        //_tracks = this.initVehicles()
    }

    initVehicles() {
        let map = this.map
        let _routes = this.routes
        let features = []
        let len = _routes.length
        let colors = ["#ffa100", "#ff0000", "#8e0e0b"]
        let colorLen = colors.length
        let vehicles = {
            "type": "geojson",
            "data": {
                "type": "FeatureCollection",
                "features": []
            }
        }
        let _numInterval = this.animationNumInterval
        let k = 0
        for (let j = 0; j < len; j++) {
            let _coords = _routes[j].coords
            let count = _coords.length
            let cloroIndex = parseInt(Math.random() * colorLen)
            //let cloroIndex = 1
            let color = colors[cloroIndex]
            let _trackNum = parseInt(count / _numInterval)
            for (let i = 0; i < _trackNum; i++) {
                let p = i * _numInterval
                if (p > count - 1) {
                    p = count - 1
                }
                if (p < 0) {
                    p = 0
                }
                let current = _coords[p]
                features[k] = {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": current
                    },
                    "properties": {
                        "pointColor": color,
                        "position": p,
                        "routeIndex": j
                    }
                }
                k++
            }
        }

        vehicles["data"]["features"] = features
        console.log("startAnimationOver--features-" + features.length + "----" + new Date().getTime())
        return vehicles
    }

    loopData(vehicles) {
        let map = this.map
        let _routes = this.routes
        let _vehicles = Object.assign({}, vehicles)
        let features = _vehicles.data.features
        if (!features) {
            return {
                "type": "geojson",
                "data": {
                    "type": "FeatureCollection",
                    "features": []
                }
            }
        }
        let lenFeatures = features.length

        const moveMarkers = ()=> {
            for (let i = 0; i < lenFeatures; i++) {
                moveMarker(features[i])
            }
        }

        const moveMarker = (marker) => {
            let i = marker.properties.position
            let j = marker.properties.routeIndex
            let _coords = _routes[j].coords
            let count = _coords.length
            let p = i + 1
            if (p > count - 1) {
                p = 0
            }
            if (p < 0) {
                p = 0
            }
            let current = _coords[p]
            marker.geometry.coordinates = current
            marker.properties.position = p
        }

        moveMarkers()
        return _vehicles
    }

    start() {
        let map = this.map
        let vehicles = this.tracks
        let that = this
        let _animationTimeInterval = this.animationTimeInterval

        vehicles = this.initVehicles()
        map.getSource('vehicles').setData(vehicles.data)

        env.tmcAnimation.timeIntervalSet.add(setInterval(()=> {
            vehicles = that.loopData(vehicles)
            map.getSource('vehicles').setData(vehicles.data)
        }, _animationTimeInterval))
    }

    endAnimate() {
        try {
            env.tmcAnimation.timeIntervalSet.forEach((v, k) => {
                if (v) {
                    clearInterval(v)
                }
            })
            env.tmcAnimation.timeIntervalSet = new Set()
        } catch (e) {
            console.log(e)
        }
    }
}