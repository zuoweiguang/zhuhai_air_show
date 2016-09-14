export default class CustomAnimation2 {
    constructor(map, options) {
        this.map = map
        this.options = options || {}
        this.painter = this.map.painter
        this.gl = this.painter.gl
        this.transform = this.painter.transform
        this.init = this.init.bind(this)
        this.start = this.start.bind(this)
    }

    init() {
        this.map.addSource("vehicles", {
            "type": "geojson",
            "data": {
                "type": "FeatureCollection",
                "features": []
            }
        })

        this.map.addLayer({
            "id": "vehicles",
            "type": "line",
            "source": "vehicles",
            "layout": {
                "line-cap": "round",
                "line-join": "round"
            },
            "paint": {
                "line-opacity": 0.5,
                "line-width": 2,
                "line-color": "#ff0000"
            },
            "filter":["==","lineColor","#ff0000"]
        })

        this.map.addLayer({
            "id": "vehicles1",
            "type": "circle",
            "source": "vehicles",
            "paint": {
                "circle-radius": 2.2,
                "circle-color": "#ff0000",
                "circle-opacity": 0.6
            },
            "filter":["==","circleColor1","#ff0000"]
        })
        this.map.addLayer({
            "id": "vehicles2",
            "type": "circle",
            "source": "vehicles",
            "paint": {
                "circle-radius": 1.5,
                "circle-color": "#ff0000",
                "circle-opacity": 0.4
            },
            "filter":["==","circleColor2","#ff0000"]
        })
    }

    start() {
        let map = this.map
        let quantity = 2000
        let delay = 1000
        let transitionStyle = 'linear'
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
            moveMarkers()
            map.getSource('vehicles').setData(vehicles.data)
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
            for (let i = 0; i < quantity; i++) {
                let t1 = [116.46 + ((Math.random(1)) / 100), 39.92 + ((Math.random(1)) / 100)]
                let t2 = [116.46 + ((Math.random(1)) / 100), 39.92 + ((Math.random(1)) / 100)]
                features[3*i] = {
                    "type": "Feature",
                    "geometry": {
                        "type": "LineString",
                        "coordinates": [t1,t2]
                    },
                    "properties": {
                        "lineColor": "#ff0000"
                    }
                }

                features[3 * i+1] = {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": t1
                    },
                    "properties": {
                        "circleColor1": "#ff0000"
                    }
                }

                features[3 * i+2] = {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": t2
                    },
                    "properties": {
                        "circleColor2": "#ff0000"
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
            for (let i = 0; i < vehicles.data.features.length; i=i+3) {
                moveMarker(vehicles.data.features[i])
                let marker = vehicles.data.features[i]
                vehicles.data.features[i+1].geometry.coordinates = marker.geometry.coordinates[0]
                vehicles.data.features[i+2].geometry.coordinates = marker.geometry.coordinates[1]
            }
        }

        function moveMarker(marker) {
            let previousPosition = marker.geometry.coordinates[1]
            marker.properties.previousPosition = [previousPosition[0], previousPosition[1]]
            let currentPosition = marker.properties.nextPosition ? marker.properties.nextPosition : marker.geometry.coordinates[1]
            marker.properties.nextPosition = updateCoordinates([currentPosition[0], currentPosition[1]])
            marker.geometry.coordinates = [currentPosition, marker.properties.nextPosition]
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