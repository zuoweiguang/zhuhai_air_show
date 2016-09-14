import TmcSpiritAnimation from './TmcSpiritAnimation'
import TmcLineAnimation from './TmcLineAnimation'
import env from '../env'
import animationService from '../../middleware/service/animationService'

export default class TmcAnimationRender {
    constructor(map, options) {
        this.map = map
        this.options = options || {}
        this.routes = null
        this.linkRoutes = null
        this.sourceTimeInterval = null
        this.animationTimeInterval = null
        this.animationCriticalZoom = 14
        this.animation = new TmcSpiritAnimation(map, {routes: []})
        this.lineAnimation = new TmcLineAnimation(map, {routes: []})
        this.init = this.init.bind(this)
        this.start = this.start.bind(this)
    }

    init() {
        let map = this.map
        let animationCriticalZoom = this.animationCriticalZoom
        if(!map.getSource("tmcSpiritAnimationPoint")){
            map.addSource("tmcSpiritAnimationPoint", {
                "type": "vector",
                "tiles": ["http://minemap.navinfo.com/service130/vector-map/inrix3/{z}/{x}/{y}"]
            })
        }
        if(!map.getSource("tmcSpiritAnimationLine")){

        }

        let tmcStatuses = [5, 1, 2, 3, 4, 5]
        let colors = ["#87cefa", "#42ff00", "#ffa100", "#ff0000", "#8e0e0b", "#87cefa"]

        for (let j = 0; j <= 5; j++) {
            let status = tmcStatuses[j]
            let tmc = j
            let color = colors[j]
            /*
             this.map.addLayer({
             "id": "TmcSpiritAnimationLine" + tmc,
             "type": "line",
             "source": "tmcSpiritAnimationLine",
             "minzoom": 9,
             "maxzoom": 17,
             "layout": {
             "line-cap": "round",
             "line-join": "round",
             "visibility": "visible",
             "line-round-limit": 2,
             "line-miter-limit": 0
             },
             "paint": {
             "line-color": color,
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
             15
             ]
             ]
             },
             "line-blur": 1
             },
             "filter": ["==", "tmc", tmc]
             })
             */

            this.map.addLayer({
                "id": "TmcSpiritAnimationPoint" + tmc,
                "type": "symbol",
                "source": "tmcSpiritAnimationPoint",
                "source-layer": "poi",
                "minzoom": animationCriticalZoom,
                "maxzoom": 17,
                "layout": {
                    "icon-image": "tmc-spirit-1" + status,
                    "icon-allow-overlap":true,
                    "symbol-avoid-edges":true,
                    "visibility": "visible"
                },
                "paint": {
                },
                "filter": ["all", ["==", "tmc", tmc], ["==", "pos", 1]]
            })
        }
    }

    start() {
        let map = this.map
        let _animation = this.animation
        let _lineAnimation = this.lineAnimation
        let zoom = map.getZoom()

        const startAnimation = ()=> {
            if (_animation) {
                _animation.endAnimate()
            }
            if (zoom >= 8 && zoom <= 17) {
                env.tmcAnimation.timeoutSet.add(setTimeout(()=> {
                    _animation.start()
                }, 200))
            } else {
                _animation.endAnimate()
            }
        }

        const startLinkAnimation = ()=> {
            if (zoom >= 9 && zoom <= 17) {
                env.tmcAnimation.timeoutSet.add(setTimeout(()=> {
                    _lineAnimation.updateRoutes([])
                    _lineAnimation.start()
                }, 200))
            }
        }

        env.tmcAnimation.timeoutSet.add(setTimeout(()=> {
            startAnimation()
        }, 500))
    }
}