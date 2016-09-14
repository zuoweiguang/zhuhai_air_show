import React, { Component, PropTypes } from 'react'
import StyleContentItem from './StyleContentItem'
import SourceFilter from './SourceFilter'
import env from '../../core/env'
import * as styleConstants from '../../core/styleConstants'
import solutionService from '../../middleware/service/solutionService'
import solutionHandler from '../../middleware/handler/solutionHandler'
import operateHandler from '../../middleware/handler/operateHandler'
import {OPERATE_SOLUTION_LAYER_ZOOMRANGE_MODIFY,OPERATE_SOLUTION_LAYER_PAINT_MODIFY} from '../../constants/OperateActionTypes'

export default class SourceInfo extends Component {
    constructor(props, context) {
        super(props, context)
        this.handleStyleChange = this.handleStyleChange.bind(this)
        this.getDataTypeList = this.getDataTypeList.bind(this)
        this.getNewLayerStyleByType = this.getNewLayerStyleByType.bind(this)
    }

    handleStyleChange(id, value) {
        if (id == 'type') {
            const layerStyle = this.props.layerStyle
            let newLayerStyle = this.getNewLayerStyleByType(value, layerStyle)
            if (newLayerStyle) {
                try {
                    env.map.removeLayer(layerStyle.id)
                } catch (e) {
                    console.log(e)
                }
                delete newLayerStyle.id
                solutionService.addLayer(newLayerStyle).then(response =>
                    response.json()
                ).then(res => {
                    if (res.errcode == 0 && res.data) {
                        newLayerStyle = Object.assign({}, newLayerStyle, {id: res.data.id})
                        let newMapLayer = solutionHandler.genMapStyleLayer(newLayerStyle)
                        env.map.addLayer(newMapLayer)
                        this.props.actions.solutionLayerTypeModify(newLayerStyle, layerStyle)
                    } else {
                        console.log(res)
                    }
                }).catch((e)=>
                    console.log(e)
                )
            }
        } else if (id == 'extrusion-min-height') {
            let oldValue = env.map.getPaintProperty(this.props.layerStyle.id, id)
            let param = {
                oldValue: oldValue,
                newValue: value,
                layer: this.props.layerStyle,
                solution: {},
                key: id
            }
            operateHandler.addOperateRecord(OPERATE_SOLUTION_LAYER_PAINT_MODIFY, param)
            env.map.setPaintProperty(this.props.layerStyle.id, id, value)
            this.props.actions.solutionLayerPaintModify(this.props.layerStyle, id, value)
        } else if (id == 'minzoom') {
            const layerStyle = this.props.layerStyle
            const minzoom = Number.parseFloat(value)
            let param = {
                oldMinzoom: layerStyle.minzoom,
                oldMaxzoom: layerStyle.maxzoom,
                newMinzoom: minzoom,
                newMaxzoom: layerStyle.maxzoom,
                layer: this.props.layerStyle,
                solution: {}
            }
            operateHandler.addOperateRecord(OPERATE_SOLUTION_LAYER_ZOOMRANGE_MODIFY, param)
            env.map.setLayerZoomRange(layerStyle.id, minzoom, layerStyle.maxzoom)
            this.props.actions.solutionLayerZoomRangeModify(layerStyle, minzoom, layerStyle.maxzoom)
        } else if (id == 'maxzoom') {
            const layerStyle = this.props.layerStyle
            const maxzoom = Number.parseFloat(value)
            let param = {
                oldMinzoom: layerStyle.minzoom,
                oldMaxzoom: layerStyle.maxzoom,
                newMinzoom: layerStyle.minzoom,
                newMaxzoom: maxzoom,
                layer: this.props.layerStyle,
                solution: {}
            }
            operateHandler.addOperateRecord(OPERATE_SOLUTION_LAYER_ZOOMRANGE_MODIFY, param)
            env.map.setLayerZoomRange(layerStyle.id, layerStyle.minzoom, maxzoom)
            this.props.actions.solutionLayerZoomRangeModify(layerStyle, layerStyle.minzoom, maxzoom)
        }
    }

    getNewLayerStyleByType(newtype, layerStyle) {
        const datatype = layerStyle.datatype
        const sourceLayer = layerStyle['source-layer']
        let newLayerStyle = Object.assign({}, this.props.layerStyle)
        newLayerStyle.type = newtype
        const visibility = layerStyle.layout.visibility
        switch (datatype) {
            case 'line':
                if (sourceLayer == 'Road' || sourceLayer == 'RailWay') {
                    if (newtype == 'line') {
                        newLayerStyle = Object.assign({}, newLayerStyle, {
                            layout: Object.assign({}, styleConstants.defaultStyleLine.layout, {visibility: visibility}),
                            paint: Object.assign({}, styleConstants.defaultStyleLine.paint)
                        })
                    } else if (newtype == 'symbol') {
                        newLayerStyle = Object.assign({}, newLayerStyle, {
                            layout: Object.assign({}, styleConstants.defaultStyleText.layout, {visibility: visibility}),
                            paint: Object.assign({}, styleConstants.defaultStyleText.paint)
                        })
                    }
                    return newLayerStyle
                }
                return null
            case 'fill':
                return null
            case 'background':
                return null
            case 'symbol':
            case 'circle':
                if (newtype == 'symbol') {
                    newLayerStyle = Object.assign({}, newLayerStyle, {
                        layout: Object.assign({}, styleConstants.defaultStyleSymbol.layout, {visibility: visibility}),
                        paint: Object.assign({}, styleConstants.defaultStyleSymbol.paint)
                    })
                } else if (newtype == 'circle') {
                    newLayerStyle = Object.assign({}, newLayerStyle, {
                        layout: Object.assign({}, styleConstants.defaultStyleCircle.layout, {visibility: visibility}),
                        paint: Object.assign({}, styleConstants.defaultStyleCircle.paint)
                    })
                }
                return newLayerStyle
            case 'raster':
                return null
            default:
                return null
        }
    }

    getDataTypeList(sourceLayer, datatype) {
        switch (datatype) {
            case 'line':
                if (sourceLayer == 'Road' || sourceLayer == 'RailWay') {
                    return [{title: "线", value: "line"}, {title: "标记", value: "symbol"}]
                }
                return [{title: "线", value: "line"}]
            case 'fill':
                return [{title: "填充", value: "fill"}]
            case 'extrusion':
                return [{title: "立体", value: 0}, {title: "平面", value: -1.0}]
            case 'background':
                return [{title: "背景", value: "background"}]
            case 'symbol':
                return [{title: "标记", value: "symbol"}, {title: "圆", value: "circle"}]
            case 'circle':
                return [{title: "标记", value: "symbol"}, {title: "圆", value: "circle"}]
            case 'raster':
                return [{title: "栅格", value: "raster"}]
            default:
                return []
        }
    }

    render() {
        const {layerStyle,layerSourceAttrs,actions} = this.props
        const datatypes = this.getDataTypeList(layerStyle['source-layer'], layerStyle.datatype)
        return <div key={"source-"+layerStyle.name} ref="contentSource">
            <div>
                <div className="content-header">数据源</div>
                <div className="content-body">
                    <StyleContentItem handleStyleChange={this.handleStyleChange}
                                      contentItem={{title:"名称",id:"name",type:"text",defaultValue:layerStyle.name,disabled:"disabled"}}/>
                    <StyleContentItem handleStyleChange={this.handleStyleChange}
                                      contentItem={{title:"描述",id:"desc",type:"text",defaultValue:layerStyle.desc,disabled:"disabled"}}/>
                    <StyleContentItem handleStyleChange={this.handleStyleChange}
                                      contentItem={{id:layerStyle.type == "extrusion"?"extrusion-min-height":"type",title:"数据类型",type:"radio-group",
                                      defaultValue:layerStyle.type == "extrusion"?(layerStyle.paint["extrusion-min-height"]):(layerStyle.type),inputItems:datatypes}}/>
                </div>
            </div>
            <div ref="contentZoom">
                <div className="content-header">缩放比例</div>
                <div className="content-body">
                    <StyleContentItem handleStyleChange={this.handleStyleChange}
                                      contentItem={{title:"最小",id:"minzoom",type:"number",min:layerStyle.sourceminzoom||3,max:layerStyle.sourcemaxzoom||20,defaultValue:layerStyle.minzoom}}/>
                    <StyleContentItem handleStyleChange={this.handleStyleChange}
                                      contentItem={{title:"最大",id:"maxzoom",type:"number",min:layerStyle.sourceminzoom||3,max:layerStyle.sourcemaxzoom||20,defaultValue:layerStyle.maxzoom}}/>
                </div>
            </div>
            <div ref="contentFilter">
                <div className="content-header">数据过滤</div>
                <SourceFilter layerStyle={layerStyle} layerSourceAttrs={layerSourceAttrs} actions={actions}/>
            </div>
            <div ref="contentEvent">
            </div>
        </div>
    }
}

SourceInfo.propTypes = {
    layerStyle: PropTypes.object.isRequired
}