import React, { Component, PropTypes } from 'react'
import StyleLayoutLine from './StyleLayoutLine'
import StyleLayoutSymbol from './StyleLayoutSymbol'
import StyleLayoutOther from './StyleLayoutOther'
import StylePaintBackground from './StylePaintBackground'
import StylePaintCircle from './StylePaintCircle'
import StylePaintFill from './StylePaintFill'
import StylePaintLine from './StylePaintLine'
import StylePaintRaster from './StylePaintRaster'
import StylePaintSymbol from './StylePaintSymbol'
import StylePaintExtrusion from './StylePaintExtrusion'

export default class StyleInfo extends Component {
    constructor(props, context) {
        super(props, context)
    }

    render() {
        const { solution,layerStyle} = this.props
        let layoutBody
        let paintBody
        let typeName = ""
        switch (layerStyle.type) {
            case 'background':
                layoutBody = <StyleLayoutOther actions={this.props.actions} layerStyle={layerStyle}/>
                paintBody = <StylePaintBackground actions={this.props.actions} layerStyle={layerStyle}/>
                typeName = '背景'
                break;
            case 'fill':
                layoutBody = <StyleLayoutOther actions={this.props.actions} layerStyle={layerStyle}/>
                paintBody = <StylePaintFill actions={this.props.actions} layerStyle={layerStyle}/>
                typeName = '填充'
                break;
            case 'line':
                layoutBody = <StyleLayoutLine actions={this.props.actions} layerStyle={layerStyle}/>
                paintBody = <StylePaintLine actions={this.props.actions} layerStyle={layerStyle}/>
                typeName = '线'
                break;
            case 'symbol':
                layoutBody = <StyleLayoutSymbol actions={this.props.actions} layerStyle={layerStyle}/>
                paintBody = <StylePaintSymbol actions={this.props.actions} layerStyle={layerStyle}/>
                typeName = '标记'
                break;
            case 'raster':
                layoutBody = <StyleLayoutOther actions={this.props.actions} layerStyle={layerStyle}/>
                paintBody = <StylePaintRaster actions={this.props.actions} layerStyle={layerStyle}/>
                typeName = '栅格'
                break;
            case 'circle':
                layoutBody = <StyleLayoutOther actions={this.props.actions} layerStyle={layerStyle}/>
                paintBody = <StylePaintCircle actions={this.props.actions} layerStyle={layerStyle}/>
                typeName = '圆'
                break;
            case 'extrusion':
                layoutBody = <StyleLayoutOther actions={this.props.actions} layerStyle={layerStyle}/>
                paintBody = <StylePaintExtrusion actions={this.props.actions} layerStyle={layerStyle}/>
                typeName = '立体'
                break;
        }
        return <div>
            <div ref="contentLayout">
                <div className="content-header">布局-{typeName}</div>
                {layoutBody}
            </div>
            <div ref="contentPaint">
                <div className="content-header">绘制-{typeName}</div>
                {paintBody}
            </div>
        </div>
    }
}

StyleInfo.propTypes = {
    layerStyle: PropTypes.object.isRequired
}