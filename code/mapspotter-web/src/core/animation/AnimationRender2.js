import PointAnimation from './PointAnimation'
import env from '../env'
import animationService from '../../middleware/service/animationService'

export default class AnimationRender2 {
    constructor(map, options) {
        this.map = map
        this.options = options || {}
        this.routes = null
        this.sourceTimeInterval = null
        this.animationTimeInterval = null
        this.animationCriticalZoom = 14
        this.animation = new PointAnimation(map, {routes: []})
        this.init = this.init.bind(this)
        this.start = this.start.bind(this)
    }

    init() {
        let map = this.map
        let animationCriticalZoom = this.animationCriticalZoom
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
                "minzoom": animationCriticalZoom,
                "layout": {
                    "icon-image": "spirit-11"
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
        let zoom = map.getZoom()
        let animationCriticalZoom = this.animationCriticalZoom
        let bounds = map.getBounds().toArray()
        let boundParam = {
            min_x: Math.min(bounds[0][0], bounds[1][0]),
            min_y: Math.min(bounds[0][1], bounds[1][1]),
            max_x: Math.max(bounds[0][0], bounds[1][0]),
            max_y: Math.max(bounds[0][1], bounds[1][1])
        }
        const querySourceData = ()=> {
            animationService.getRoadSpirit(boundParam).then(response =>
                response.json()
            ).then(res => {
                if (res && res.length > 0) {
                    _routes = res
                    startAnimation()
                }
            }).catch((e)=>
                console.log(e)
            )
        }

        const startAnimation = ()=> {
            if (_animation) {
                _animation.endAnimate()
            }
            if (zoom >= animationCriticalZoom) {
                env.tmcAnimation.timeoutSet.add(setTimeout(()=> {
                    _animation.updateRoutes(_routes)
                    _animation.start()
                }, 200))
            }
            console.log("initAnimation--" + new Date().getTime())
        }

        env.tmcAnimation.timeoutSet.add(setTimeout(()=> {
            querySourceData()
        }, 400))
    }
}