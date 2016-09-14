import PointAnimation1 from './PointAnimation1'
import env from '../env'

export default class AnimationRender1 {
    constructor(map, options) {
        this.map = map
        this.options = options || {}
        this.painter = this.map.painter
        this.gl = this.painter.gl
        this.transform = this.painter.transform
        this.routes = null
        this.sourceTimeInterval = null
        this.animationTimeInterval = null
        this.animation = null
        this.init = this.init.bind(this)
        this.start = this.start.bind(this)
    }

    init() {
        let map = this.map
        this.map.addSource("vehicles", {
            "type": "geojson",
            "data": {
                "type": "FeatureCollection",
                "features": []
            }
        })

        let colors = ["#87cefa", "#42ff00", "#ffa100", "#ff0000", "#8e0e0b"]
        for (let j = 0; j < colors.length; j++) {
            let color = colors[j]
            this.map.addLayer({
                "id": "vehicles" + parseInt(j + 1),
                "type": "symbol",
                "source": "vehicles",
                "layout": {
                    "icon-image": "zoo-15"
                },
                "paint": {
                    "icon-opacity": 1,
                    "icon-color": color
                },
                "filter": ["==", "pointColor", color]
            })
        }
        map.on('moveend', function () {
            this.start()
        }.bind(this))
    }

    start() {
        let map = this.map
        let _routes = this.routes
        let _sourceTimeInterval = this.sourceTimeInterval
        let _animationTimeInterval = this.animationTimeInterval
        let _animation = this.animation
        _sourceTimeInterval = setInterval(()=> {
            querySourceData()
            console.log("querySourceData--" + new Date().getTime())
        }, 500)

        _animationTimeInterval = setInterval(()=> {
            startAnimation()
            console.log("startAnimation--" + new Date().getTime())
        }, 500)

        env.mapAnimationTimeInterval.push(_animationTimeInterval)
        env.mapAnimationTimeInterval.push(_sourceTimeInterval)

        const querySourceData = ()=> {
            _routes = map.querySourceFeatures('traffic', {
                sourceLayer: 'TrafficStatus'
            })
        }

        const startAnimation = ()=> {
            if (_routes && _routes.length > 0) {
                if (_animation) {
                    _animation.updateRoutes(_routes)
                    _animation.init()
                    _animation.start()
                } else {
                    let options = {routes: _routes}
                    _animation = new PointAnimation1(map, options)
                    _animation.init()
                    _animation.start()
                }

                console.log("initAnimation--" + new Date().getTime())
                if (_sourceTimeInterval) {
                    clearInterval(_sourceTimeInterval)
                }
                if (_animationTimeInterval) {
                    clearInterval(_animationTimeInterval)
                }
            }
        }
    }
}