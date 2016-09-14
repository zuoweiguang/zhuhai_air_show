export default class CustomAnimation1 {
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
            "id": "vehicles1",
            "type": "circle",
            "source": "vehicles",
            "paint": {
                "circle-radius": 5,
                "circle-color": "#ff0000",
                "circle-opacity": 0.8
            },
            "filter":["==","circleColor","#ff0000"]
        })

        this.map.addLayer({
            "id": "vehicles2",
            "type": "circle",
            "source": "vehicles",
            "paint": {
                "circle-radius": 5,
                "circle-color": "#ffff00",
                "circle-opacity": 0.8
            },
            "filter":["==","circleColor","#ffff00"]
        })
    }

    start() {
        let map = this.map
        let quantity = 1000
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
            for (let i = 0; i < quantity; i++) {
                features[2 * i] = {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [116.46 + ((Math.random(1)) / 1000)*10, 39.92 + ((Math.random(1)) / 1000)*10]
                    },
                    "properties": {
                        "circleColor": "#ff0000"
                    }
                }
                features[2 * i + 1] = {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [116.46 + ((Math.random(1)) / 1000)*10, 39.92 + ((Math.random(1)) / 1000)*10]
                    },
                    "properties": {
                        "circleColor": "#ffff00"
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
            let previousPosition = marker.geometry.coordinates
            marker.properties.previousPosition = [previousPosition[0], previousPosition[1]]
            let currentPosition = marker.properties.nextPosition ? marker.properties.nextPosition : marker.geometry.coordinates
            marker.geometry.coordinates = [currentPosition[0], currentPosition[1]]
            marker.properties.nextPosition = updateCoordinates([currentPosition[0], currentPosition[1]])
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