import React, { Component, PropTypes } from 'react'
import StyleContentItem from './StyleContentItem'
import env from '../../core/env'

export default class StylePaintRaster extends Component {
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
                contentItem={{title:"不透明度(%)",type:"interpolate-percent",defaultValue:layerStyle.paint["raster-opacity"]||1,id:"raster-opacity"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"颜色旋转角度",type:"number",min:0,defaultValue:layerStyle.paint["raster-hue-rotate"]||0,id:"raster-hue-rotate"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"最低亮度(%)",type:"number",min:0,max:100,percent:true,defaultValue:layerStyle.paint["raster-brightness-min"]*100||0,id:"raster-brightness-min"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"最高亮度(%)",type:"number",min:0,max:100,percent:true,defaultValue:layerStyle.paint["raster-brightness-max"]*100||100,id:"raster-brightness-max"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"饱和度(%)",type:"number",min:0,max:100,percent:true,defaultValue:layerStyle.paint["raster-saturation"]*100||0,id:"raster-saturation"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"对比度(%)",type:"number",min:0,max:100,percent:true,defaultValue:layerStyle.paint["raster-contrast"]*100||0,id:"raster-contrast"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"图层褪去时间(ms)",type:"number",min:0,defaultValue:layerStyle.paint["raster-fade-duration"]||300,id:"raster-fade-duration"}}/>
        </div>
    }
}

StylePaintRaster.propTypes = {
    layerStyle: PropTypes.object.isRequired
}