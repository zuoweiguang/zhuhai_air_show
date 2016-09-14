import React, { Component, PropTypes } from 'react'
import Map from '../../core/Map'
import Style from '../../core/Style'
import env from '../../core/env'
import PitchCtrl from '../../core/PitchCtrl'
import animationRender from '../../core/animation/animationRender'
import * as styleConstants from '../../core/styleConstants'
import solutionHandler from '../../middleware/handler/solutionHandler'
import warehouseService from '../../middleware/service/warehouseService'
import FlyRouteAnimation from '../../core/animation/FlyRouteAnimation'


export default class MapBox extends Component {
    constructor(props, context) {
        super(props, context)
        this.flyRouteAnimation = null
    }

    componentDidMount() {
        const solution = this.props.solution
        let style = new Style()
        let styleValue = style.serialize()
        let map = new Map("mapBox", styleValue, solution.center, solution.zoom, solution.minZoom, solution.maxZoom, solution.bearing, solution.pitch)
        map.addControl(new mapboxgl.Navigation({position: 'top-right'}))
        //new PitchCtrl(map).init()
        env.map = map
        env.style = style

        this.flyRouteAnimation = new FlyRouteAnimation(env.map, this.props, 0.09)
        this.flyRouteAnimation.init();

        warehouseService.getUserSourceList().then(response =>
            response.json()
        ).then(res => {
            if (res.errcode == 0 && res.data && res.data.dataSources && res.data.dataSources.length > 0) {
                return res.data.dataSources
            }
        }).then(sources=> {
            sources.map(source => {
                    if (!map.getSource(source.source)) {
                        let obj = {type: source.type}
                        if (source.type == 'raster') {
                            obj.tileSize = source.tileSize
                            if (source.url && source.url.length > 0) {
                                obj.url = source.url
                            } else {
                                obj.tiles = source.tiles
                            }
                        } else {
                            obj.tiles = source.tiles
                        }
                        map.addSource(source.source, obj)
                    }
                }
            )
        }).then(
            ()=> {
                this.props.mapInitStateChangeAction(true, false)
            }
        ).catch((e)=>
            console.log(e)
        )
    }

    componentWillUnmount() {
        try {
            this.flyRouteAnimation.destory()
            this.flyRouteAnimation = null

            animationRender.clearTmcSpiritAnimationLayer()
            animationRender.clearTmcBrandAnimationLayer()
            animationRender.clearTrafficAnimationLayer()
        } catch (e) {
            console.log(e)
        }
    }

    componentWillReceiveProps(nextProps) {

        this.flyRouteAnimation.updateOptions(nextProps)

        if (nextProps.map.init.mapInit && !nextProps.map.init.mapLayerLoading && nextProps.solution.layers) {
            this.props.mapInitStateChangeAction(true, true)
            const solutionId = nextProps.solution.id
            let staticLayers = []
            let dynamicLayers = []
            nextProps.solution.layers.map(layer=> {
                if (layer.type == 'animation') {
                    dynamicLayers.push(layer)
                } else {
                    staticLayers.push(layer)
                }
            })
            let reorderLayers = staticLayers.sort((a, b)=> {
                return a.zindex - b.zindex
            })
            reorderLayers.map(layer => {
                    let layerStyle = solutionHandler.genMapStyleLayer(layer)
                    env.map.addLayer(layerStyle)
                }
            )

            dynamicLayers.map(layer=> {
                if (layer['source-layer'] == 'TmcSpiritAnimation') {
                    animationRender.addTmcSpiritAnimationLayer()
                } else if (layer['source-layer'] == 'TmcBrandAnimation') {
                    animationRender.addTmcBrandAnimationLayer()
                } else if (layer['source-layer'] == 'TrafficStatusAnimation') {
                    animationRender.addTrafficAnimationLayer()
                }
            })
        }

        if (nextProps.map.init.mapInit && nextProps.map.init.reorder && nextProps.solution.layers) {

            let mapLayerIds = Object.keys(env.map.style._layers)
            let tmcLayers = []

            for (let i = 0; i < mapLayerIds.length; i++) {
                if (mapLayerIds[i].startsWith("Tmc")) {
                    tmcLayers.push(env.map.getLayer(mapLayerIds[i]))
                }
                env.map.removeLayer(mapLayerIds[i])
            }
            let reorderLayers = nextProps.solution.layers.sort((a, b)=> {
                return a.zindex - b.zindex
            })

            reorderLayers.map(layer => {
                    let layerStyle = solutionHandler.genMapStyleLayer(layer)
                    if (layerStyle.type!="animation") {
                        env.map.addLayer(layerStyle)
                    }
                }
            )

            for(let i=0;i<tmcLayers.length;i++){
                env.map.addLayer(tmcLayers[i])
            }

            this.props.mapReorderLayers(false)
        }
    }

    render() {
        const {solution,actions,map,mapInitStateChangeAction,mapReorderLayers} = this.props
        return <div style={{height:'100%',width:'100%'}} id="mapBox">
        </div>
    }

}

MapBox.propTypes = {
    solution: PropTypes.object.isRequired,
    map: PropTypes.object.isRequired
}