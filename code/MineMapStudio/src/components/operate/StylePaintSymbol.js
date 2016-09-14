import React, { Component, PropTypes } from 'react'
import StyleContentItem from './StyleContentItem'
import env from '../../core/env'
import operateHandler from '../../middleware/handler/operateHandler'
import {OPERATE_SOLUTION_LAYER_PAINT_MODIFY} from '../../constants/OperateActionTypes'

export default class StylePaintSymbol extends Component {
    constructor(props, context) {
        super(props, context)
        this.handleStyleChange = this.handleStyleChange.bind(this)
    }

    handleStyleChange(id,value){
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
        this.props.actions.solutionLayerPaintModify(this.props.layerStyle,id,value)
    }

    render() {
        const { layerStyle} = this.props
        return <div className="content-body">
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"图标不透明度(%)",type:"interpolate-percent",defaultValue:layerStyle.paint["icon-opacity"]||1,id:"icon-opacity"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"图标颜色",type:"interpolate-color",defaultValue:layerStyle.paint["icon-color"]||"#000000",id:"icon-color"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"图标边框颜色",type:"interpolate-color",defaultValue:layerStyle.paint["icon-halo-color"]||"#000000",id:"icon-halo-color"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"图标边框宽度",type:"interpolate-number",min:0,defaultValue:layerStyle.paint["icon-halo-width"]||0,id:"icon-halo-width"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"图标边框模糊(%)",type:"interpolate-percent",min:0,defaultValue:layerStyle.paint["icon-halo-blur"]||0,id:"icon-halo-blur"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"图标偏移",type:"input-group",id:"icon-translate",
                inputItems:[{title:"X",value:0},{title:"Y",value:0}]}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"图标偏移锚点",type:"radio-group",id:"icon-translate-anchor",style:{marginTop:"30px"},defaultValue:layerStyle.paint["icon-translate-anchor"]||"map",
                inputItems:[{title:"正北",value:"map"},{title:"视野",value:"viewport"}]}}/>

            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"文本不透明度(%)",type:"interpolate-percent",defaultValue:layerStyle.paint["text-opacity"]||1,id:"text-opacity"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"文本颜色",type:"interpolate-color",defaultValue:layerStyle.paint["text-color"]||"#000000",id:"text-color"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"文本边框颜色",type:"interpolate-color",defaultValue:layerStyle.paint["text-halo-color"]||"#000000",id:"text-halo-color"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"文本边框宽度",type:"interpolate-number",min:0,defaultValue:layerStyle.paint["text-halo-width"]||0,id:"text-halo-width"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"文本边框模糊(%)",type:"interpolate-percent",min:0,defaultValue:layerStyle.paint["text-halo-blur"]||0,id:"text-halo-blur"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"文本偏移",type:"input-group",id:"text-translate",
                inputItems:[{title:"X",value:layerStyle.paint["text-translate"]?layerStyle.paint["text-translate"][0]:0},{title:"Y",value:layerStyle.paint["text-translate"]?layerStyle.paint["text-translate"][1]:0}]}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"文本偏移锚点",type:"radio-group",id:"text-translate-anchor",style:{marginTop:"30px"},defaultValue:layerStyle.paint["text-translate-anchor"]||"map",
                inputItems:[{title:"正北",value:"map"},{title:"视野",value:"viewport"}]}}/>
        </div>
    }
}

StylePaintSymbol.propTypes = {
    layerStyle: PropTypes.object.isRequired
}