import React, { Component, PropTypes } from 'react'
import StyleContentItem from './StyleContentItem'
import env from '../../core/env'
import operateHandler from '../../middleware/handler/operateHandler'
import {OPERATE_SOLUTION_LAYER_PAINT_MODIFY} from '../../constants/OperateActionTypes'

export default class StylePaintCircle extends Component {
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
                              contentItem={{title:"半径",type:"interpolate-number",min:0,defaultValue:layerStyle.paint["circle-radius"]||5,id:"circle-radius"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"颜色",type:"interpolate-color",defaultValue:layerStyle.paint["circle-color"]||"#000000",id:"circle-color"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"模糊(%)",type:"interpolate-percent",defaultValue:layerStyle.paint["circle-blur"]||0,id:"circle-blur"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"不透明度(%)",type:"interpolate-percent",defaultValue:layerStyle.paint["circle-opacity"]||1,id:"circle-opacity"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"偏移",type:"input-group",id:"circle-translate",inputItems:[{title:"X",value:layerStyle.paint["circle-translate"]?layerStyle.paint["circle-translate"][0]:0},{title:"Y",value:layerStyle.paint["circle-translate"]?layerStyle.paint["circle-translate"][1]:0}]}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"偏移锚点",type:"radio-group",id:"circle-translate-anchor",style:{marginTop:"30px"},defaultValue:layerStyle.paint["circle-translate-anchor"]||"map",
                inputItems:[{title:"正北",value:"map"},{title:"视野",value:"viewport"}]}}/>

        </div>
    }
}

StylePaintCircle.propTypes = {
    layerStyle: PropTypes.object.isRequired
}