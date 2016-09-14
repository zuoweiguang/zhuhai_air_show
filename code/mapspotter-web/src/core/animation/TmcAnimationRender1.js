import TmcSpiritAnimation from './TmcSpiritAnimation'
import TmcLineAnimation from './TmcLineAnimation'
import env from '../env'
import animationService from '../../middleware/service/animationService'

export default class TmcAnimationRender1 {
    constructor(map, options) {
        this.map = map
        this.options = options || {}
        this.routes = null
        this.linkRoutes = null
        this.sourceTimeInterval = null
        this.animationTimeInterval = null
        this.animationCriticalZoom = 15
        this.animation = new TmcSpiritAnimation(map, {routes: []})
        this.lineAnimation = new TmcLineAnimation(map, {routes: []})
        this.init = this.init.bind(this)
        this.start = this.start.bind(this)
    }

    init() {
        let map = this.map
        let animationCriticalZoom = this.animationCriticalZoom
        this.map.addSource("tmcSpiritAnimationPoint", {
            "type": "geojson",
            "data": {
                "type": "FeatureCollection",
                "features": []
            }
        })

        this.map.addSource("tmcSpiritAnimationLine", {
            "type": "geojson",
            "data": {
                "type": "FeatureCollection",
                "features": []
            }
        })

        let tmcStatuses = [5, 1, 2, 3, 4, 5]
        let colors = ["#87cefa", "#42ff00", "#ffa100", "#ff0000", "#8e0e0b", "#87cefa"]

        for (let j = 0; j < tmcStatuses.length; j++) {
            let status = tmcStatuses[j]
            let tmc = j
            let color = colors[j]
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

            this.map.addLayer({
                "id": "TmcSpiritAnimationPoint" + tmc,
                "type": "symbol",
                "source": "tmcSpiritAnimationPoint",
                "minzoom": animationCriticalZoom,
                "maxzoom": 17,
                "layout": {
                    "icon-image": "tmc-spirit-" + status
                },
                "paint": {},
                "filter": ["==", "tmc", tmc]
            })
        }
        map.on('moveend', function () {
            this.start()
        }.bind(this))
    }

    start() {
        let map = this.map
        let _routes = this.routes
        let _linkRoutes = this.linkRoutes
        let _sourceTimeInterval = this.sourceTimeInterval
        let _animationTimeInterval = this.animationTimeInterval
        let _animation = this.animation
        let _lineAnimation = this.lineAnimation
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
            if (zoom >= animationCriticalZoom && zoom <= 17) {
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
        }

        const queryLinkSourceData = ()=> {
            if (zoom >= 9 && zoom <= 17) {
                animationService.getRoadLinkSpirit(boundParam).then(response =>
                    response.json()
                ).then(res => {
                    if (res && res.length > 0) {
                        _linkRoutes = res
                        startLinkAnimation()
                    }
                }).catch((e)=>
                    console.log(e)
                )
            }
        }

        const startAnimation = ()=> {
            if (_animation) {
                _animation.endAnimate()
            }
            if (zoom >= animationCriticalZoom && zoom <= 17) {
                env.tmcAnimation.timeoutSet.add(setTimeout(()=> {
                    _animation.updateRoutes(_routes)
                    _animation.start()
                }, 200))
            }
        }

        const startLinkAnimation = ()=> {
            if (zoom >= 9 && zoom <= 17) {
                env.tmcAnimation.timeoutSet.add(setTimeout(()=> {
                    _lineAnimation.updateRoutes(_linkRoutes)
                    _lineAnimation.start()
                }, 200))
            }
        }

        env.tmcAnimation.timeoutSet.add(setTimeout(()=> {
            queryLinkSourceData()
            querySourceData()
        }, 400))
    }
}