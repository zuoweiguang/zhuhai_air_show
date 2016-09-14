import React, { Component, PropTypes } from 'react'
import StyleContentItem from './StyleContentItem'
import env from '../../core/env'
import operateHandler from '../../middleware/handler/operateHandler'
import {OPERATE_SOLUTION_LAYER_LAYOUT_MODIFY} from '../../constants/OperateActionTypes'

export default class StyleLayoutSymbol extends Component {
    constructor(props, context) {
        super(props, context)
        this.handleStyleChange = this.handleStyleChange.bind(this)
    }

    handleStyleChange(id, value) {
        if (id == 'text-field') {
            value = new String(value || '')
        }
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
                              contentItem={{title:"图标",type:"icon",id:"icon-image",defaultValue:layerStyle.layout["icon-image"]}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"图标大小",type:"number",min:0,defaultValue:layerStyle.layout["icon-size"]||1,id:"icon-size"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"图标旋转角度",type:"number",defaultValue:layerStyle.layout["icon-rotate"]||0,id:"icon-rotate"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"图标补偿",type:"input-group",id:"icon-offset",
                inputItems:[{title:"右",value:layerStyle.layout["icon-offset"]?layerStyle.layout["icon-offset"][0]:0},
                {title:"下",value:layerStyle.layout["icon-offset"]?layerStyle.layout["icon-offset"][1]:0}]}}/>

            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"与文本匹配",type:"select",style:{marginTop:"30px"},id:"icon-text-fit",defaultValue:layerStyle.layout["icon-text-fit"]||"none",
                inputItems:[{title:"无",value:"none"},{title:"全部",value:"both"},
                {title:"宽度",value:"width"},{title:"高度",value:"height"}]}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"与文本填匹配",type:"text",defaultValue:"0,0,0,0",id:"icon-text-fit-padding"}}/>

            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"图标允许重叠",type:"radio-group",id:"icon-allow-overlap",defaultValue:layerStyle.layout["icon-allow-overlap"]||false,
                inputItems:[{title:"是",value:true},{title:"否",value:false}]}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"图标忽略压盖",type:"radio-group",id:"icon-ignore-placement",defaultValue:layerStyle.layout["icon-ignore-placement"]||false,
                inputItems:[{title:"是",value:true},{title:"否",value:false}]}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"图标掩盖文本显示",type:"radio-group",id:"icon-optional",defaultValue:layerStyle.layout["icon-optional"]||false,
                inputItems:[{title:"是",value:true},{title:"否",value:false}]}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"图标位置",type:"radio-group",id:"symbol-placement",defaultValue:layerStyle.layout["symbol-placement"]||"point",
                inputItems:[{title:"点",value:"point"},{title:"线",value:"line"}]}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"图标间隔(px)",type:"number",min:0,defaultValue:layerStyle.layout["symbol-spacing"]||250,id:"symbol-spacing"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"跨瓦片",type:"radio-group",id:"symbol-avoid-edges",defaultValue:layerStyle.layout["symbol-avoid-edges"]||false,
                inputItems:[{title:"是",value:true},{title:"否",value:false}]}}/>

            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"图标旋转锚点",type:"radio-group",id:"icon-rotation-alignment",defaultValue:layerStyle.layout["icon-rotation-alignment"]||"map",
                inputItems:[{title:"正北",value:"map"},{title:"视野",value:"vie1wport"}]}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"图标内边距",type:"number",defaultValue:layerStyle.layout["icon-padding"]||2,id:"icon-padding"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"图标保持直立",type:"radio-group",id:"icon-keep-upright",defaultValue:layerStyle.layout["icon-keep-upright"]||false,
                inputItems:[{title:"是",value:true},{title:"否",value:false}]}}/>

            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"字体",type:"select",id:"text-font",disabled:"disabled",defaultValue:layerStyle.layout["text-font"]||"Open Sans Regular",
                inputItems:[{title:"Open Sans Regular",value:"Open Sans Regular"},{title:"Arial Unicode MS Regular",value:"Arial Unicode MS Regular"}]}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"字体大小",type:"interpolate-number",min:0,defaultValue:layerStyle.layout["text-size"]||16,id:"text-size"}}/>



            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"文本值域",type:"text",id:"text-field",defaultValue:new String(layerStyle.layout["text-field"]||'')}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"文本间距",type:"number",min:0,defaultValue:layerStyle.layout["text-letter-spacing"]||0,id:"text-letter-spacing"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"文本行高",type:"number",min:0,defaultValue:layerStyle.layout["text-line-height"]||1.2,id:"text-line-height"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"文本最大宽度",type:"interpolate-number",min:0,defaultValue:layerStyle.layout["text-max-width"]||10,id:"text-max-width"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"文本对齐方式",type:"select",id:"text-justify",defaultValue:layerStyle.layout["text-justify"]||"center",
                inputItems:[{title:"左",value:"left"},{title:"中",value:"center"},{title:"右",value:"right"}]}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"文字旋转角度",type:"number",defaultValue:layerStyle.layout["text-rotate"]||0,id:"text-rotate"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"最大旋转角度",type:"number",min:0,defaultValue:layerStyle.layout["text-max-angle"]||45,id:"text-max-angle"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"文本内边距",type:"number",min:0,defaultValue:layerStyle.layout["text-padding"]||2,id:"text-padding"}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"文本锚点",type:"select",id:"text-anchor",defaultValue:layerStyle.layout["text-anchor"]||"center",
                inputItems:[{title:"中心",value:"center"},{title:"上",value:"top"},
                {title:"下",value:"bottom"},{title:"左",value:"left"},{title:"右",value:"right"},
                {title:"左上",value:"top-left"},{title:"右上",value:"top-right"},{title:"左下",value:"bottom-left"},{title:"右下",value:"bottom-right"}
                ]}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"文本偏移",type:"input-group",id:"text-offset",
                inputItems:[{title:"右",value:layerStyle.layout["text-offset"]?layerStyle.layout["text-offset"][0]:0},{title:"下",value:layerStyle.layout["text-offset"]?layerStyle.layout["text-offset"][1]:0}]}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"文本倾斜锚点",type:"radio-group",style:{marginTop:"30px"},id:"text-pitch-alignment",defaultValue:layerStyle.layout["text-pitch-alignment"]||"map",
                inputItems:[{title:"正北",value:"map"},{title:"视野",value:"viewport"}]}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"文本旋转锚点",type:"radio-group",id:"text-rotation-alignment",defaultValue:layerStyle.layout["text-rotation-alignment"]||"map",
                inputItems:[{title:"正北",value:"map"},{title:"视野",value:"viewport"}]}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"文本允许重叠",type:"radio-group",id:"text-allow-overlap",defaultValue:layerStyle.layout["text-allow-overlap"]||false,
                inputItems:[{title:"是",value:true},{title:"否",value:false}]}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"文本忽略压盖",type:"radio-group",id:"text-ignore-placement",defaultValue:layerStyle.layout["text-ignore-placement"]||false,
                inputItems:[{title:"是",value:true},{title:"否",value:false}]}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"文本压盖图标显示",type:"radio-group",id:"text-optional",defaultValue:layerStyle.layout["text-optional"]||false,
                inputItems:[{title:"是",value:true},{title:"否",value:false}]}}/>

            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"文本保持直立",type:"radio-group",id:"text-keep-upright",defaultValue:layerStyle.layout["text-transform"]||true,
                inputItems:[{title:"是",value:true},{title:"否",value:false}]}}/>
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"大小写转化",type:"select",id:"text-transform",defaultValue:layerStyle.layout["text-transform"]||"none",
                inputItems:[{title:"无变化",value:"none"},{title:"大写",value:"uppercase"},{title:"小写",value:"lowercase"}]}}/>

            <StyleContentItem handleStyleChange={this.handleStyleChange}
                              contentItem={{title:"是否可见",type:"radio-group",id:"visibility",defaultValue:layerStyle.layout.visibility||"visible",
                inputItems:[{title:"是",value:"visible"},{title:"否",value:"none"}]}}/>
        </div>
    }
}

StyleLayoutSymbol.propTypes = {
    layerStyle: PropTypes.object.isRequired
}