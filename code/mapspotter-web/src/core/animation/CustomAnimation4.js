export default class CustomAnimation4 {
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

        this.routes = []
        let lineDistance = turf.lineDistance(this.route.features[0], 'meters')
        for (var i = 0; i < 1000; i++) {
            let segment = turf.along(this.route.features[0], i * lineDistance / 1000, 'meters')
            this.routes.push(segment.geometry.coordinates)
        }
    }

    start() {
        let map = this.map
        let quantity = 20
        let delay = 1000
        let transitionStyle = 'linear'
        let routes = this.routes
        setTimeout(function () {
            resetVehicles(quantity, delay, transitionStyle)
            loop()
        }, 1000)
        map.on('move vehicles', function () {
            //loop()
        })

        function loop() {
            /*
             if(map.intervalId) clearInterval(map.intervalId)
             map.intervalId = setInterval(
             function(){
             map.fire('move vehicles')
             }, delay
             )
             */
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