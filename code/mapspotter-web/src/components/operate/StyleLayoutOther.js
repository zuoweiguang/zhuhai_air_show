import React, { Component, PropTypes } from 'react'
import StyleContentItem from './StyleContentItem'
import env from '../../core/env'

export default class StyleLayoutOther extends Component {
    constructor(props, context) {
        super(props, context)
        this.handleStyleChange = this.handleStyleChange.bind(this)
    }

    handleStyleChange(id,value){
        env.map.setLayoutProperty(this.props.layerStyle.id, id, value)
        this.props.actions.solutionLayerLayoutModify(this.props.layerStyle,id,value)
    }

    render() {
        const { layerStyle} = this.props
        return <div className="content-body">
            <StyleContentItem handleStyleChange={this.handleStyleChange}
                contentItem={{title:"是否可见",type:"radio-group",id:"visibility",defaultValue:layerStyle.layout.visibility||"visible",
                inputItems:[{title:"是",value:"visible"},{title:"否",value:"none"}]}}/>
        </div>
    }
}

StyleLayoutOther.propTypes = {
    layerStyle: PropTypes.object.isRequired
}