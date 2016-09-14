import React, { Component, PropTypes } from 'react'
import StyleContentItem from './StyleContentItem'
import env from '../../core/env'
import operateHandler from '../../middleware/handler/operateHandler'
import {OPERATE_SOLUTION_LAYER_PAINT_MODIFY} from '../../constants/OperateActionTypes'

export default class StylePaintExtrusion extends Component {
    constructor(props, context) {
        super(props, context)
        this.handleStyleChange = this.handleStyleChange.bind(this)
    }

    handleStyleChange(id, value) {
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
    }

    render() {
        const { layerStyle} = this.props
        return <div className="content-body">
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"颜色",type:"interpolate-color",defaultValue:layerStyle.paint["extrusion-color"]||"#000000",id:"extrusion-color"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange} key={"extrusion-antialias"}
                              contentItem={{title:"平滑",type:"radio-group",id:"extrusion-antialias",defaultValue:layerStyle.paint["extrusion-antialias"]?"true":"false",
                inputItems:[{title:"是",value:"true"},{title:"否",value:"false"}]}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"轮廓颜色",type:"interpolate-color",defaultValue:layerStyle.paint["extrusion-outline-color"]||"#000000",id:"extrusion-outline-color"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"不透明度(%)",type:"interpolate-percent",min:0,max:100,defaultValue:layerStyle.paint["extrusion-layer-opacity"]||1,id:"extrusion-layer-opacity"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"建筑物高度",type:"interpolate-number",defaultValue:layerStyle.paint["extrusion-height"]||3,id:"extrusion-height"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"建筑物最低高度",type:"interpolate-number",min:-2,defaultValue:layerStyle.paint["extrusion-min-height"],id:"extrusion-min-height"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"建筑物偏移",type:"input-group",id:"extrusion-translate",
                inputItems:[{title:"X",value:layerStyle.paint["extrusion-translate"]?layerStyle.paint["extrusion-translate"][0]:0},{title:"Y",value:layerStyle.paint["extrusion-translate"]?layerStyle.paint["extrusion-translate"][1]:0}]}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"建筑物偏移锚点",type:"radio-group",id:"extrusion-translate-anchor",style:{marginTop:"30px"},defaultValue:layerStyle.paint["extrusion-translate-anchor"]||"map",
                inputItems:[{title:"正北",value:"map"},{title:"视野",value:"viewport"}]}}/>
        </div>
    }
}

StylePaintExtrusion.propTypes = {
    layerStyle: PropTypes.object.isRequired
}