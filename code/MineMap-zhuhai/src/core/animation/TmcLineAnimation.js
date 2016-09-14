import AnimationTool from '../../utils/AnimationTool'
import env from '../env'

export default class TmcLineAnimation {
    constructor(map, options) {
        this.map = map
        this.options = options || {}
        this.routes = options.routes
        this.updateRoutes = this.updateRoutes.bind(this)
        this.initSpiritLineTracks = this.initSpiritLineTracks.bind(this)
        this.start = this.start.bind(this)
    }

    updateRoutes(routes) {
        this.routes = routes
    }

    initSpiritLineTracks() {
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
            let tmc = _routes[j].tmc || 1
            features.push({
                "type": "Feature",
                "geometry": {
                    "type": "LineString",
                    "coordinates": _coords
                },
                "properties": {
                    "tmc": tmc
                }
            })
        }

        _tracks["data"]["features"] = features
        return _tracks
    }

    start() {
        let map = this.map
        let _tracks = this.initSpiritLineTracks()
        map.getSource('animationSource_tmcSpiritLine').setData(_tracks.data)
    }
}