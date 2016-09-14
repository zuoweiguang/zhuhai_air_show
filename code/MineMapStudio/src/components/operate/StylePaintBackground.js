import React, { Component, PropTypes } from 'react'
import StyleContentItem from './StyleContentItem'
import env from '../../core/env'
import operateHandler from '../../middleware/handler/operateHandler'
import {OPERATE_SOLUTION_LAYER_PAINT_MODIFY} from '../../constants/OperateActionTypes'

export default class StylePaintBackground extends Component {
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
                contentItem={{title:"颜色",type:"interpolate-color",defaultValue:layerStyle.paint["background-color"]||"#000000",id:"background-color"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"纹理",type:"icon",id:"background-pattern",defaultValue:layerStyle.paint["background-pattern"]}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"不透明度(%)",type:"interpolate-percent",min:0,max:100,percent:true,defaultValue:layerStyle.paint["background-opacity"]||1,id:"background-opacity"}}/>
        </div>
    }
}

StylePaintBackground.propTypes = {
    layerStyle: PropTypes.object.isRequired
}