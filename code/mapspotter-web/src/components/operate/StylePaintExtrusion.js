import React, { Component, PropTypes } from 'react'
import StyleContentItem from './StyleContentItem'
import env from '../../core/env'

export default class StylePaintExtrusion extends Component {
    constructor(props, context) {
        super(props, context)
        this.handleStyleChange = this.handleStyleChange.bind(this)
    }

    handleStyleChange(id, value) {
        env.map.setPaintProperty(this.props.layerStyle.id, id, value)
        this.props.actions.solutionLayerPaintModify(this.props.layerStyle,id,value)
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
                              contentItem={{title:"不透明度(%)",type:"interpolate-percent",min:0,max:100,defaultValue:layerStyle.paint["extrusion-opacity"]||1,id:"extrusion-opacity"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"阴影颜色",type:"interpolate-color",defaultValue:layerStyle.paint["extrusion-shadow-color"]||"#000000",id:"extrusion-shadow-color"}}/>
        </div>
    }
}

StylePaintExtrusion.propTypes = {
    layerStyle: PropTypes.object.isRequired
}