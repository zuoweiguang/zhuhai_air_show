import React, { Component, PropTypes } from 'react'
import env from '../../core/env'
import AnimationTool from '../../utils/AnimationTool'
import animationRender from '../../core/animation/animationRender'

export default class LayerItem extends Component {
    constructor(props, context) {
        super(props, context)
        this.onLayerItemClick = this.onLayerItemClick.bind(this)
        this.onLayerVisibilityClick = this.onLayerVisibilityClick.bind(this)
        this.onLayerDelClick = this.onLayerDelClick.bind(this)
        this.handleDragStart = this.handleDragStart.bind(this)
    }

    onLayerItemClick(e) {
        const display = this.props.panelDisplayInfoAction('style')
        const layer = this.props.layer
        if (!layer.selected && layer.type != 'animation') {
            this.props.solutionLayerSelectedAction(layer.id, true)
            this.props.solutionLayerEditAction(layer.id)
            this.props.layerStyleInfoAction(layer)
            this.props.panelDisplayInfoAction('style', 'block')

        } else if (layer.type != 'animation') {
            this.props.solutionLayerSelectedAction(layer.id, false)
            this.props.panelDisplayInfoAction('style', 'none')
        }
    }

    onLayerVisibilityClick(e) {
        e.preventDefault()
        let layer = this.props.layer
        let visibility = layer.layout.visibility
        if (visibility == 'visible') {
            visibility = 'none'
        } else {
            visibility = 'visible'
        }
        this.props.solutionLayerVisibleAction(layer.id, layer.layout, visibility)
        if (env.map) {
            if (layer.type != 'animation') {
                env.map.setLayoutProperty(layer.id, 'visibility', visibility)
            } else {
                if (visibility == 'visible') {
                    let layerAnimationId = layer['source-layer']
                    if (layerAnimationId == 'TmcSpiritAnimation') {
                        animationRender.clearTmcSpiritAnimationLayer()
                        animationRender.addTmcSpiritAnimationLayer()
                    } else if (layerAnimationId == 'TmcBrandAnimation') {
                        animationRender.clearTmcBrandAnimationLayer()
                        animationRender.addTmcBrandAnimationLayer()
                    } else if (layerAnimationId == 'TrafficStatusAnimation') {
                        animationRender.clearTrafficAnimationLayer()
                        animationRender.addTrafficAnimationLayer()
                    }
                } else {
                    let layerAnimationId = layer['source-layer']
                    if (layerAnimationId == 'TmcSpiritAnimation') {
                        animationRender.clearTmcSpiritAnimationLayer()
                    } else if (layerAnimationId == 'TmcBrandAnimation') {
                        animationRender.clearTmcBrandAnimationLayer()
                    } else if (layerAnimationId == 'TrafficStatusAnimation') {
                        animationRender.clearTrafficAnimationLayer()
                    }
                }
            }
        }
    }

    onLayerDelClick(e) {
        e.preventDefault()
        let layer = this.props.layer
        let id = layer.id
        let layers = this.props.solution.layers
        this.props.solutionLayerDelAction(id)
        if (env.map && layer.type != 'animation') {
            env.map.removeLayer(id)
        } else {
            let layerAnimationId = layer['source-layer']
            if (layerAnimationId == 'TmcSpiritAnimation') {
                animationRender.clearTmcSpiritAnimationLayer()
            } else if (layerAnimationId == 'TmcBrandAnimation') {
                animationRender.clearTmcBrandAnimationLayer()
            } else if (layerAnimationId == 'TrafficStatusAnimation') {
                animationRender.clearTrafficAnimationLayer()
            }
        }
        let datas = []
        for (let i = 0; i < layers.length; i++) {
            if (layers[i].zindex > layer.zindex) {
                datas.push({id: layers[i].id, zindex: Number.parseInt(layers[i].zindex) - 1})
            }
        }
        this.props.solutionLayerEditZIndexAction(datas)
    }

    handleDragStart(e) {
        e.dataTransfer.setData("id", e.target.id)
        e.dataTransfer.setData("screenX", e.screenX)
        e.dataTransfer.setData("screenY", e.screenY)
        e.dataTransfer.setData("scrollTop", document.getElementById("layer-container-only-id").scrollTop)
    }

    render() {
        const { solution,solutionId,layer,solutionLayerAddAction,solutionLayerDelAction,solutionLayerEditAction,solutionLayerVisibleAction,solutionLayerEditZIndexAction,mapReorderLayers,layerItemId} = this.props
        const layerSelected = layer.selected ? 'layer-item selected' : 'layer-item '
        return <div id={layerItemId} draggable={layer.type != 'animation'?"true":"false"}
                    onDragStart={this.handleDragStart} className={layerSelected}
                    ref="layerItem">
            <div className="layer-note"></div>
            <div className="layer-info">
                <div className="layer-eye">
                    {
                        layer.layout.visibility == 'visible' ? <span className="glyphicon glyphicon-eye-open"
                                                                     title="隐藏"
                                                                     onClick={this.onLayerVisibilityClick}></span> :
                            <span className="glyphicon glyphicon-eye-close"
                                  title="显示" onClick={this.onLayerVisibilityClick}></span>
                    }
                </div>
                <div className="layer-label" onClick={this.onLayerItemClick}>{layer.name}</div>
            </div>
            <div className="layer-icon">
                <span className="glyphicon glyphicon-trash" title="删除"
                      onClick={this.onLayerDelClick}></span>
            </div>
        </div>
    }
}

LayerItem.propTypes = {
    layer: PropTypes.object.isRequired
}