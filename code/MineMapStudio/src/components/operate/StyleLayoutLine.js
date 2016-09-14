import React, { Component, PropTypes } from 'react'
import StyleContentItem from './StyleContentItem'
import env from '../../core/env'
import operateHandler from '../../middleware/handler/operateHandler'
import {OPERATE_SOLUTION_LAYER_LAYOUT_MODIFY} from '../../constants/OperateActionTypes'

export default class StyleLayoutLine extends Component {
    constructor(props, context) {
        super(props, context)
        this.handleStyleChange = this.handleStyleChange.bind(this)
    }

    handleStyleChange(id, value) {
        let oldValue = env.map.getLayoutProperty(this.props.layerStyle.id, id)
        let param = {
            oldValue: oldValue,
            newValue: value,
            layer: this.props.layerStyle,
            solution: {},
            key: id
        }
        operateHandler.addOperateRecord(OPERATE_SOLUTION_LAYER_LAYOUT_MODIFY, param)
        env.map.setLayoutProperty(this.props.layerStyle.id, id, value)
        this.props.actions.solutionLayerLayoutModify(this.props.layerStyle, id, value)
    }

    render() {
        const { layerStyle} = this.props
        return <div className="content-body">
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"末端样式",type:"select",id:"line-cap",defaultValue:layerStyle.layout["line-cap"]||"butt",
                inputItems:[{title:"对接",value:"butt"},{title:"圆角",value:"round"},{title:"直角",value:"square"}]}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"连接方式",type:"select",id:"line-join",defaultValue:layerStyle.layout["line-join"]||"bevel",
                inputItems:[{title:"斜角",value:"bevel"},{title:"圆角",value:"round"},{title:"直角",value:"miter"}]}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"斜切限制",type:"number",min:0,defaultValue:layerStyle.layout["line-miter-limit"]||2,
                id:"line-miter-limit"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"圆角限制",type:"number",min:0,defaultValue:layerStyle.layout["line-round-limit"]||1.05,
                id:"line-round-limit"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"是否可见",type:"radio-group",id:"visibility",defaultValue:layerStyle.layout.visibility||"visible",
                inputItems:[{title:"是",value:"visible"},{title:"否",value:"none"}]}}/>
        </div>
    }
}

StyleLayoutLine.propTypes = {
    layerStyle: PropTypes.object.isRequired
}