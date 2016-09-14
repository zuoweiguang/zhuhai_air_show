import React, { Component, PropTypes } from 'react'
import {Link} from 'react-router'
import {Media} from 'react-bootstrap'
import WarehouseSourceItem from './WarehouseSourceItem'
import warehouseService from '../../middleware/service/warehouseService'
import solutionService from '../../middleware/service/solutionService'
import warehouseHandler from '../../middleware/handler/warehouseHandler'
import solutionHandler from '../../middleware/handler/solutionHandler'
import env from '../../core/env'
import animationRender from '../../core/animation/animationRender'

export default class WarehouseBox extends Component {
    constructor(props, context) {
        super(props, context)
        this.onCancelClick = this.onCancelClick.bind(this)
        this.onConfirmClick = this.onConfirmClick.bind(this)
        this.genSolutionLayers = this.genSolutionLayers.bind(this)
    }

    onCancelClick(e) {
        this.props.panelDisplayInfoAction('warehouse', 'none')
    }

    genSolutionLayers() {
        var warehouse = this.props.warehouse
        var solution = this.props.solution
        let zindex = solution.layers.length || 1

        warehouse.map(source => {
            let sourceLayers = source["source-layers"]
            sourceLayers.map((sourceLayer, i) => {
                if (sourceLayer.checked) {
                    warehouseService.getDefaultStyleInfo(sourceLayer.type).then(response =>
                        response.json()
                    ).then(res => {
                        if (res.errcode == 0 && res.data) {
                            return res.data
                        } else {
                            console.log(res)
                        }
                    }).then(defaultStyle => {
                        return warehouseHandler.genSolutionLayer(solution, source, sourceLayer, defaultStyle, zindex++)
                    }).then(layer => solutionService.addLayer(layer).then(response =>
                            response.json()
                        ).then(res => {
                            if (res.errcode == 0 && res.data) {
                                layer = Object.assign({}, layer, {id: res.data.id})
                                this.props.actions.solutionLayerAddAction(layer)
                                if (layer.type != 'animation') {
                                    let layerStyle = solutionHandler.genMapStyleLayer(layer)
                                    env.map.addLayer(layerStyle)
                                } else {
                                    let delLayerId = false
                                    solution.layers.map(ly=> {
                                        if (ly.type == 'animation' && ly['source-layer'] == layer['source-layer']) {
                                            delLayerId = ly.id
                                            this.props.actions.solutionLayerDelAction(delLayerId)
                                        }
                                    })
                                    if (layer['source-layer'] == 'TmcSpiritAnimation') {
                                        animationRender.clearTmcSpiritAnimationLayer()
                                        animationRender.addTmcSpiritAnimationLayer()
                                    } else if (layer['source-layer'] == 'TmcBrandAnimation') {
                                        animationRender.clearTmcBrandAnimationLayer()
                                        animationRender.addTmcBrandAnimationLayer()
                                    } else if (layer['source-layer'] == 'TrafficStatusAnimation') {
                                        animationRender.clearTrafficAnimationLayer()
                                        animationRender.addTrafficAnimationLayer()
                                    }
                                }
                            } else {
                                console.log(res)
                            }
                        })
                    ).catch((e)=>
                        console.log(e)
                    )
                }
            })
        })
    }

    onConfirmClick(e) {
        e.preventDefault()
        this.props.panelDisplayInfoAction('warehouse', 'none')
        this.genSolutionLayers()
    }

    componentDidMount() {
        this.props.actions.warehouseSourcesAction(this.props.solution.id)
    }

    render() {

        const { solution,warehouse,actions} = this.props
        const warehouseItems = warehouse ? warehouse.map((source, i) => {
            return <WarehouseSourceItem solution={solution} key={source.source} actions={actions}
                                        warehouseSourceItem={source}/>
        }) : null
        return <div className="operate-warehouse-box">
            <div className="operate-warehouse-header clearfix">
                <div className="pull-left header-name">选择图层</div>
                <div className="btn-group pull-right" role="group">
                    <button className="btn btn-success btn-sm" style={{width:'80px'}} onClick={this.onConfirmClick}>确定
                    </button>
                    <button className="btn btn-default btn-sm" style={{width:'80px'}} onClick={this.onCancelClick}>
                        取消
                    </button>
                </div>
            </div>
            <div className="operate-warehouse-content">
                {warehouseItems}
            </div>
        </div>
    }
}

WarehouseBox.propTypes = {
    solution: PropTypes.object.isRequired,
    warehouse: PropTypes.arrayOf(PropTypes.object.isRequired).isRequired,
    actions: PropTypes.object.isRequired
}