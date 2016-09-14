import React, { Component, PropTypes } from 'react'
import StyleContentItem from './StyleContentItem'
import env from '../../core/env'
import operateHandler from '../../middleware/handler/operateHandler'
import {OPERATE_SOLUTION_LAYER_PAINT_MODIFY} from '../../constants/OperateActionTypes'

export default class StylePaintLine extends Component {
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
                              contentItem={{title:"颜色",type:"interpolate-color",defaultValue:layerStyle.paint["line-color"]||"#000000",id:"line-color"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"不透明度(%)",type:"interpolate-percent",min:0,max:100,percent:true,defaultValue:layerStyle.paint["line-opacity"]||1,id:"line-opacity"}}/>

            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"几何偏移",type:"input-group",id:"line-translate",
                inputItems:[{title:"X",value:layerStyle.paint["line-translate"]?layerStyle.paint["line-translate"][0]:0},{title:"Y",value:layerStyle.paint["line-translate"]?layerStyle.paint["line-translate"][1]:0}]}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"偏移锚点",type:"radio-group",id:"line-translate-anchor",style:{marginTop:"30px"},defaultValue:layerStyle.paint["line-translate-anchor"]||"map",
                inputItems:[{title:"正北",value:"map"},{title:"视野",value:"viewport"}]}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"宽度",type:"interpolate-number",min:0,defaultValue:layerStyle.paint["line-width"]||1,id:"line-width"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"偏移",type:"number",defaultValue:layerStyle.paint["line-offset"]||0,id:"line-offset"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"模糊(%)",type:"interpolate-percent",defaultValue:layerStyle.paint["line-blur"]||0,id:"line-blur"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"虚线",type:"text",id:"line-dasharray",defaultValue:layerStyle.paint["line-dasharray"]?"["+layerStyle.paint["line-dasharray"]+"]":"[0,0]"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"纹理",type:"icon",id:"line-pattern",defaultValue:layerStyle.paint["line-pattern"]}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"间隙宽度",type:"interpolate-number",min:0,defaultValue:layerStyle.paint["line-gap-width"]||0,id:"line-gap-width"}}/>
        </div>
    }
}

StylePaintLine.propTypes = {
    layerStyle: PropTypes.object.isRequired
}