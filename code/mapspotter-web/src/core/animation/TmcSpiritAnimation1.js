import AnimationTool from '../../utils/AnimationTool'
import env from '../env'

export default class TmcSpiritAnimation1 {
    constructor(map, options) {
        this.map = map
        this.options = options || {}
        this.routes = options.routes
        this.updateRoutes = this.updateRoutes.bind(this)
        this.start = this.start.bind(this)
        this.endAnimate = this.endAnimate.bind(this)
        this.initSpiritTracks = this.initSpiritTracks.bind(this)
        this.loopData = this.loopData.bind(this)
        this.animationTimeInterval = 100
    }

    updateRoutes(routes) {
        this.routes = routes
    }

    initSpiritTracks() {
        let map = this.map
        let _routes = this.routes
        let features = []
        let len = _routes.length
        let _tracks = {
            "type": "geojson",
            "data": {
                "type": "FeatureCollection",
                "features": []
            }
        }
        for (let j = 0; j < len; j++) {
            let _coords = _routes[j].coords
            let count = _coords.length
            let tmc = _routes[j].tmc || 1
            let current = _coords[0]
            features.push({
                "type": "Feature",
                "geometry": {
                    "type": "Point",
                    "coordinates": current
                },
                "properties": {
                    "tmc": tmc,
                    "position": 0,
                    "routeIndex": j
                }
            })
        }

        _tracks["data"]["features"] = features
        return _tracks
    }

    loopData(tracks) {
        let map = this.map
        let _routes = this.routes
        let _tracks = Object.assign({}, tracks)
        let features = _tracks.data.features
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
            let _route = _routes[j]
            if (_route) {
                let _coords = _route.coords
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
        }

        moveMarkers()
        return _tracks
    }

    start() {
        let map = this.map
        let that = this
        let _animationTimeInterval = this.animationTimeInterval

        let _tracks = this.initSpiritTracks()
        map.getSource('tmcSpiritAnimationPoint').setData(_tracks.data)

        env.tmcAnimation.timeIntervalSet.add(setInterval(()=> {
            _tracks = that.loopData(_tracks)
            map.getSource('tmcSpiritAnimationPoint').setData(_tracks.data)
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