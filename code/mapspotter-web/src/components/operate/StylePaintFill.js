import React, { Component, PropTypes } from 'react'
import StyleContentItem from './StyleContentItem'
import env from '../../core/env'

export default class StylePaintFill extends Component {
    constructor(props, context) {
        super(props, context)
        this.handleStyleChange = this.handleStyleChange.bind(this)
    }

    handleStyleChange(id,value){
        env.map.setPaintProperty(this.props.layerStyle.id, id, value)
        this.props.actions.solutionLayerPaintModify(this.props.layerStyle,id,value)
    }

    render() {
        const { layerStyle} = this.props
        return <div className="content-body">
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"平滑",type:"radio-group",id:"fill-antialias",defaultValue:layerStyle.paint["fill-antialias"]?"true":"false",
                inputItems:[{title:"是",value:"true"},{title:"否",value:"false"}]}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"不透明度(%)",type:"interpolate-percent",defaultValue:layerStyle.paint["fill-opacity"]||1,id:"fill-opacity"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"颜色",type:"interpolate-color",defaultValue:layerStyle.paint["fill-color"]||"#000000",id:"fill-color"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"轮廓颜色",type:"interpolate-color",defaultValue:layerStyle.paint["fill-outline-color"]||"#000000",id:"fill-outline-color"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"几何偏移",type:"input-group",id:"fill-translate",
                inputItems:[{title:"X",value:layerStyle.paint["fill-translate"]?layerStyle.paint["fill-translate"][0]:0},{title:"Y",value:layerStyle.paint["fill-translate"]?layerStyle.paint["fill-translate"][1]:0}]}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"偏移锚点",type:"radio-group",id:"fill-translate-anchor",style:{marginTop:"30px"},defaultValue:layerStyle.paint["fill-translate-anchor"]||"map",
                inputItems:[{title:"正北",value:"map"},{title:"视野",value:"viewport"}]}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"纹理",type:"icon",id:"fill-pattern",defaultValue:layerStyle.paint["fill-pattern"]}}/>
        </div>
    }
}

StylePaintFill.propTypes = {
    layerStyle: PropTypes.object.isRequired
}