export default class CustomAnimation5 {
    constructor(map, options) {
        this.map = map
        this.options = options || {}
        this.painter = this.map.painter
        this.gl = this.painter.gl
        this.transform = this.painter.transform
        this.route = {}
        this.routes = []
        this.init = this.init.bind(this)
        this.start = this.start.bind(this)
        this.initVehicles = this.initVehicles.bind(this)
        this.loopData = this.loopData.bind(this)
    }

    init() {
        let ne = this.map.getBounds()._ne
        let sw = this.map.getBounds()._sw
        let distanceVer = ne.lat - sw.lat
        let distanceHor = ne.lng - sw.lng
        let p_es = [sw.lng + (distanceHor * 0.75), sw.lat + (distanceVer * 0.25)]
        let p_en = [sw.lng + (distanceHor * 0.75), sw.lat + (distanceVer * 0.75)]
        let p_wn = [sw.lng + (distanceHor * 0.25), sw.lat + (distanceVer * 0.75)]
        let p_ws = [sw.lng + (distanceHor * 0.25), sw.lat + (distanceVer * 0.25)]

        let es = this.map.project(p_es)
        let en = this.map.project(p_en)
        let wn = this.map.project(p_wn)
        let ws = this.map.project(p_ws)
        this.route = {
            "type": "FeatureCollection",
            "features": [{
                "type": "Feature",
                "geometry": {
                    "type": "LineString",
                    "coordinates": [p_es,
                        p_en,
                        p_wn,
                        p_ws,
                        p_es]
                }
            }]
        }
        this.map.addSource("testline", {
            "type": "geojson",
            "data": this.route
        })
        this.map.addSource("vehicles", {
            "type": "geojson",
            "data": {
                "type": "FeatureCollection",
                "features": []
            }
        })

        this.map.addLayer({
            "id": "testline",
            "type": "line",
            "source": "testline",
            "layout": {
                "line-cap": "round",
                "line-join": "round",
                "visibility": "none"
            },
            "paint": {
                "line-width": 20,
                "line-color": "#ffff00"
            }
        })

        let colors = ["#87cefa", "#42ff00", "#ffa100", "#ff0000", "#8e0e0b"]
        for (let i = 0; i < 10; i++) {
            let opacity = (parseInt(i + 1) / 10).toFixed(1)
            opacity = Number.parseFloat(opacity)
            for (let j = 0; j < colors.length; j++) {
                let color = colors[j]
                this.map.addLayer({
                    "id": "vehicles" + parseInt(i + 1) + "" + parseInt(j + 1),
                    "type": "line",
                    "source": "vehicles",
                    "layout": {
                        "line-cap": "round",
                        "line-join": "round"
                    },
                    "paint": {
                        "line-opacity": opacity,
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
                                    8
                                ]
                            ]
                        },
                        "line-color": color
                    },
                    "filter": ["all", ["==", "opacity", opacity], ["==", "lineColor", color]]
                })
            }
        }

        this.routes = []
        let lineDistance = turf.lineDistance(this.route.features[0], 'meters')
        for (var i = 0; i < 1000; i++) {
            let segment = turf.along(this.route.features[0], i * lineDistance / 1000, 'meters')
            this.routes.push(segment.geometry.coordinates)
        }
    }

    initVehicles() {
        let map = this.map
        let quantity = 50
        let routes = this.routes
        let features = []
        let len = routes.length
        let colors = ["#87cefa", "#42ff00", "#ffa100", "#ff0000", "#8e0e0b"]
        let colorLen = colors.length
        let vehicles = {
            "type": "geojson",
            "data": {
                "type": "FeatureCollection",
                "features": []
            }
        }
        for (let i = 0; i < quantity; i++) {
            let cloroIndex = parseInt(Math.random() * colorLen)
            let color = colors[cloroIndex]
            let p = parseInt(Math.random() * len) + 1
            for (let j = 0; j < 10; j++) {
                let endp = p + j
                if (endp > len - 1) {
                    endp = 1
                }
                let startp = endp - 1
                if (startp < 0) {
                    startp = 0
                }
                let start = routes[startp]
                let end = routes[endp]
                let opacity = (parseInt(j + 1) / 10).toFixed(1)
                opacity = Number.parseFloat(opacity)
                features[10 * i + j] = {
                    "type": "Feature",
                    "geometry": {
                        "type": "LineString",
                        "coordinates": [start, end]
                    },
                    "properties": {
                        "opacity": opacity,
                        "lineColor": color,
                        "startPosition": startp,
                        "endPosition": endp
                    }
                }
            }
        }
        vehicles["data"]["features"] = features
        map.getSource('vehicles').setData(vehicles.data)
        return vehicles
    }

    loopData(vehicles) {
        let routes = this.routes
        let map = this.map
        let len = routes.length
        let _vehicles = Object.assign({},vehicles)
        let features = _vehicles.data.features
        let lenFeatures = features.length

        const moveMarkers = ()=> {
            for (let i = 0; i < lenFeatures; i++) {
                moveMarker(features[i])
            }
        }

        const moveMarker = (marker) => {
            let endp = marker.properties.endPosition + 1
            if (endp > len - 1) {
                endp = 1
            }
            let startp = endp - 1
            if (startp < 0) {
                startp = 0
            }
            let start = routes[startp]
            let end = routes[endp]

            marker.geometry.coordinates = [start, end]
            marker.properties.startPosition = startp
            marker.properties.endPosition = endp
        }

        moveMarkers()
        map.getSource('vehicles').setData(_vehicles.data)
        return _vehicles
    }

    start() {
        let map = this.map
        let quantity = 20
        let routes = this.routes
        let vehicles = {}
        let that = this
        setTimeout(function () {
            startBefore()
            loop()
        }, 1000)

        const startBefore = ()=> {
            vehicles = that.initVehicles()
        }

        const loop = ()=> {
            vehicles = that.loopData(vehicles)
            requestAnimationFrame(loop)
        }
    }


}