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
        if(!map.getSource("animationSource_tmcSpiritPoint")){
            map.addSource("animationSource_tmcSpiritPoint", {
                "type": "vector",
                "tiles": ["http://minemap.navinfo.com/service130/vector-map/inrix3/{z}/{x}/{y}"]
            })
        }
        if(!map.getSource("animationSource_tmcSpiritLine")){

        }

        let tmcStatuses = [5, 1, 2, 3, 4, 5]
        let colors = ["#87cefa", "#42ff00", "#ffa100", "#ff0000", "#8e0e0b", "#87cefa"]

        for (let j = 0; j <= 5; j++) {
            let status = tmcStatuses[j]
            let tmc = j
            let color = colors[j]
            this.map.addLayer({
                "id": "animationLayer_tmcSpiritPoint" + tmc,
                "type": "symbol",
                "source": "animationSource_tmcSpiritPoint",
                "source-layer": "poi",
                "minzoom": animationCriticalZoom,
                "maxzoom": 19,
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
            if (zoom >= 8 && zoom <= 19) {
                env.tmcAnimation.timeoutSet.add(setTimeout(()=> {
                    _animation.start()
                }, 200))
            } else {
                _animation.endAnimate()
            }
        }

        const startLinkAnimation = ()=> {
            if (zoom >= 9 && zoom <= 19) {
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