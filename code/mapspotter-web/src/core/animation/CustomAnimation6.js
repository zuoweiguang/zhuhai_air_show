export default class CustomAnimation6 {
    constructor(map, options) {
        this.map = map
        this.options = options || {}
        this.painter = this.map.painter
        this.gl = this.painter.gl
        this.transform = this.painter.transform
        this.route = {}
        this.routes = []
        this.trafficInterval = null
        this.init = this.init.bind(this)
        this.start = this.start.bind(this)
    }

    init() {

        this.route = {
            "type": "FeatureCollection",
            "features": []
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
                "line-join": "round"
            },
            "paint": {
                "line-width": 20,
                "line-color": "#ffff00"
            }
        })

        for (let i = 0; i < 8; i++) {
            this.map.addLayer({
                "id": "vehicles" + parseInt(i + 1),
                "type": "symbol",
                "source": "vehicles",
                "layout": {
                    "icon-image": "zoo-15",
                    "icon-rotate": parseInt(i * 45)
                },
                "paint": {
                    "icon-color": "#ff0000"
                },
                "filter": ["all", [">=", "iconRotate", parseInt(i * 45) - 15], ["<", "iconRotate", parseInt(i * 45) + 30]]
            })
        }
    }

    start() {
        let map = this.map
        let quantity = 20
        let delay = 1000
        let transitionStyle = 'linear'
        let routes = this.routes
        let route = this.route
        let trafficInterval = setInterval(function () {
            let trafficData = this.map.querySourceFeatures('traffic', {
                sourceLayer: 'TrafficStatus'
            })
            if (trafficData.length > 0) {
                route.features = []
                let lengthTraf = trafficData.length
                if (lengthTraf > 1000) {
                    lengthTraf = 1000
                }
                for (let i = 0; i < lengthTraf; i++) {
                    let _f = {
                        "type": "Feature",
                        "geometry": {
                            "type": "LineString",
                            "coordinates": lengthTraf[i].geometry.coordinates
                        },
                        "properties": lengthTraf[i].properties
                    }
                    route.features.push(_f)
                }

                this.routes = []
                for (let i = 0; i < lengthTraf; i++) {
                    let r = route.features[i]
                    let lineDistance = turf.lineDistance(r, 'meters')
                    this.routes[i] = []
                    for (let j = 0; j < 1000; j++) {
                        let segment = turf.along(r, j * lineDistance / 1000, 'meters')
                        this.routes[i].push(segment.geometry.coordinates)
                    }
                }

            }
        }, 500)

        setTimeout(function () {
            trafficInterval()
            loopData()
        }, 1000)

        function loopData() {
            if (routes.length > 0) {
                if (trafficInterval) {
                    clearInterval(trafficInterval)
                    trafficInterval = null
                }
                setTimeout(function () {
                    resetVehicles(quantity, delay, transitionStyle)
                    loop()
                }, 1000)
            }
        }

        map.on('move vehicles', function () {
            //loop()
        })

        function loop() {
            let data = moveMarkers()
            map.getSource('vehicles').setData(data)
            requestAnimationFrame(loop)
        }

        map.quantity = '50'
        map.delay = '1000'
        map.transitionStyle = 'linear'

        function resetVehicles() {
            if (quantity) {
                map.quantity = quantity
            } else {
                quantity = map.quantity
            }
            if (delay) {
                map.delay = delay
            } else {
                delay = map.delay
            }
            if (transitionStyle) {
                map.transitionStyle = transitionStyle
            } else {
                transitionStyle = map.transitionStyle
            }
            let features = []
            let len = routes.length
            for (let i = 0; i < quantity; i++) {
                let np = parseInt(Math.random() * len) + 1
                if (np >= len - 1) {
                    np = 1
                }
                let pp = np - 1
                if (pp < 0) {
                    pp = 0
                }
                let p = routes[pp]
                let n = routes[np]

                let angle = calulateAngle(p, n)

                features[i] = {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": n
                    },
                    "properties": {
                        "iconRotate": angle,
                        "previousPosition": pp,
                        "nextPosition": np,
                    }
                }
            }
            vehicles["data"]["features"] = features
            //map.getSource('vehicles').animate(delay, transitionStyle)
            map.getSource('vehicles').setData(vehicles.data)
        }

        let vehicles = {
            "type": "geojson",
            "data": {
                "type": "FeatureCollection",
                "features": []
            }
        }

        function moveMarkers() {
            for (let i = 0; i < vehicles.data.features.length; i++) {
                moveMarker(vehicles.data.features[i])
            }
            return vehicles.data
        }

        function moveMarker(marker) {
            let len = routes.length
            let np = marker.properties.nextPosition + 1
            if (np >= len - 1) {
                np = 1
            }
            let pp = np - 1
            if (pp < 0) {
                pp = 0
            }
            let p = routes[pp]
            let n = routes[np]
            let angle = calulateAngle(p, n)

            marker.geometry.coordinates = n
            marker.properties.previousPosition = pp
            marker.properties.nextPosition = np
            marker.properties.iconRotate = angle
        }

        function calulateAngle(s, e) {
            let start = map.project({lng: s[0], lat: s[1]})
            let end = map.project({lng: e[0], lat: e[1]})
            let diff_x = end.x - start.x
            let diff_y = end.y - start.y
            //let ag = 360 * Math.atan(diff_y / diff_x) / (2 * Math.PI)
            //if (ag < 0) {
            //    ag = ag + 360
            //}
            let ag = 0
            if (diff_y == 0 && diff_x > 0) {
                ag = 0
            } else if (diff_y > 0 && diff_x > 0) {
                ag = 45
            } else if (diff_y > 0 && diff_x == 0) {
                ag = 90
            } else if (diff_y > 0 && diff_x < 0) {
                ag = 135
            } else if (diff_y == 0 && diff_x < 0) {
                ag = 180
            } else if (diff_y < 0 && diff_x < 0) {
                ag = 225
            } else if (diff_y < 0 && diff_x == 0) {
                ag = 270
            } else if (diff_y < 0 && diff_x > 0) {
                ag = 315
            } else {
                ag = 0
            }

            ag = 360 * Math.atan(diff_y / diff_x) / (2 * Math.PI)

            ag = 180 * Math.atan2(diff_y, diff_x) / Math.PI
            if (ag < -15) {
                ag = ag + 360
            }
            if (ag >= 345) {
                ag = -15
            }
            return ag
        }

        function updateCoordinates(coordinates) {
            return [coordinates[0] + getRandomMove(), coordinates[1] + getRandomMove()]
        }

        function getRandomMove() {
            //return (Math.random() - 0.5) / 4
            return (Math.random(1)) / 10000
        }
    }


}