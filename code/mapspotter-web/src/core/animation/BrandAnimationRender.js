import env from '../env'
import animationService from '../../middleware/service/animationService'

export default class BrandAnimationRender {
    constructor(map, options) {
        this.map = map
        this.options = options || {}
        this.animationMinZoom = 13
        this.animationMaxZoom = 17
        this.init = this.init.bind(this)
        this.start = this.start.bind(this)
    }

    init() {
        let map = this.map
        let animationMinZoom = this.animationMinZoom
        let animationMaxZoom = this.animationMaxZoom
        if(!map.getSource("tmcBrandAnimation")){
            map.addSource("tmcBrandAnimation", {
                "type": "geojson",
                "data": {
                    "type": "FeatureCollection",
                    "features": []
                }
            })
        }

        let typeCodes = ["10501", "10502", "13701", "10702", "13401", "11101"]
        for (let j = 0; j < typeCodes.length; j++) {
            let typeCode = typeCodes[j]
            let image = j + 1
            this.map.addLayer({
                "id": "TmcBrandAnimation" + j,
                "type": "symbol",
                "source": "tmcBrandAnimation",
                "minzoom": animationMinZoom,
                "maxzoom": animationMaxZoom,
                "layout": {
                    "icon-image": "tmc-brand-" + image,
                    "icon-offset": [
                        0,
                        0
                    ]
                },
                "paint": {},
                "filter": ["==", "typeCode", typeCode]
            })
        }
        map.on('moveend', function () {
            let zoom = map.getZoom()
            if (zoom >= animationMinZoom && zoom <= animationMaxZoom) {
                this.start()
            }
        }.bind(this))
    }

    start() {
        try {
            env.brandAnimation.timeIntervalSet.forEach((v, k) => {
                if (v) {
                    clearInterval(v)
                }
            })
            env.brandAnimation.timeIntervalSet = new Set()
        } catch (e) {
            console.log(e)
        }
        let map = this.map
        let zoom = map.getZoom()
        let _routes = []
        let bounds = map.getBounds().toArray()
        let boundParam = {
            min_x: Math.min(bounds[0][0], bounds[1][0]),
            min_y: Math.min(bounds[0][1], bounds[1][1]),
            max_x: Math.max(bounds[0][0], bounds[1][0]),
            max_y: Math.max(bounds[0][1], bounds[1][1])
        }
        const querySourceData = ()=> {
            animationService.getRoadLinkBrand(boundParam).then(response =>
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
            if (zoom >= 8 && zoom <= 17) {
                env.brandAnimation.timeoutSet.add(setTimeout(()=> {
                    let _tracks = {
                        "type": "geojson",
                        "data": {
                            "type": "FeatureCollection",
                            "features": []
                        }
                    }
                    let features = []
                    if (_routes && _routes.length > 0) {
                        _routes.map(route=> {
                            features.push({
                                "type": "Feature",
                                "geometry": {
                                    "type": "Point",
                                    "coordinates": route.coord
                                },
                                "properties": {
                                    "typeCode": route['type_code']
                                }
                            })
                        })
                    }
                    _tracks["data"]["features"] = features
                    map.getSource('tmcBrandAnimation').setData(_tracks.data)
                }, 200))
            }
        }

        env.brandAnimation.timeoutSet.add(setTimeout(()=> {
            querySourceData()
            env.brandAnimation.timeIntervalSet.add(setInterval(()=> {
                querySourceData()
            }, 60000))
        }, 400))
    }

}