import React, { Component, PropTypes } from 'react'
import {Link} from 'react-router'
import StyleInfo from './StyleInfo'
import SourceInfo from './SourceInfo'

export default class StyleBox extends Component {
    constructor(props, context) {
        super(props, context)
        this.state = {isLayerNameEdit: false}
        this.onLayerNameEditClick = this.onLayerNameEditClick.bind(this)
        this.onLayerNameSaveClick = this.onLayerNameSaveClick.bind(this)
        this.onStyleInfoClick = this.onStyleInfoClick.bind(this)
        this.onDataInfoClick = this.onDataInfoClick.bind(this)
    }

    onLayerNameEditClick(e) {
        this.setState({isLayerNameEdit: true})
        e.preventDefault()
    }

    onLayerNameSaveClick(e) {
        e.preventDefault()
        let id = this.props.layerStyle.id
        let name = this.refs.layerName.value || this.props.layerStyle.name
        let data = {id: id, name: name}
        this.props.actions.solutionLayerEditNameAction(data)
        this.setState({isLayerNameEdit: false})
    }

    onStyleInfoClick(e) {
        e.preventDefault()
        this.refs.styleInfoBtn.style.backgroundColor = '#dfdfdf'
        this.refs.dataInfoBtn.style.backgroundColor = '#ffffff'
        this.refs.styleTabBody.style.display = 'block'
        this.refs.dataTabBody.style.display = 'none'
    }

    onDataInfoClick(e) {
        e.preventDefault()
        this.refs.styleInfoBtn.style.backgroundColor = '#ffffff'
        this.refs.dataInfoBtn.style.backgroundColor = '#dfdfdf'
        this.refs.styleTabBody.style.display = 'none'
        this.refs.dataTabBody.style.display = 'block'
    }

    render() {
        const { solution,layerStyle,layerSourceAttrs,actions,menuToggleAction,panelDisplayInfoAction} = this.props
        const tabContentHeight = (document.body.clientHeight - 88) + 'px'
        const tabContentStyle = {height: tabContentHeight}
        const tabHideContentStyle = {height: tabContentHeight, display: 'none'}

        const layerNameDiv = this.state.isLayerNameEdit ?
            <div className="header-label-input"><input ref="layerName" type="text"
                                                       className="form-control"
                                                       defaultValue={layerStyle.name}/></div> :
            <div className="header-label">{layerStyle.name}</div>
        const layerHeaderBtnGroup = this.state.isLayerNameEdit ? <div className="header-icon"><span
            className="glyphicon glyphicon-ok" onClick={this.onLayerNameSaveClick}
            title="保存"></span></div> :
            <div className="header-icon"><span className="glyphicon glyphicon-edit"
                                               title="编辑" onClick={this.onLayerNameEditClick}></span></div>

        return <div className="operate-style-box">
            <div className="operate-style-header">
                {layerNameDiv}
                {layerHeaderBtnGroup}
            </div>
            <div className="operate-style-body">
                <div className="btn-group btn-group-justified btn-group-xs"
                     style={{marginTop:'7px',marginBottom:'7px'}}>
                    <a type="button" className="btn btn-default"
                       style={{backgroundColor:'#dfdfdf',padding:'2px 5px',fontSize:'13px',color:'#666'}}
                       onClick={this.onStyleInfoClick} ref="styleInfoBtn">样式信息
                    </a>
                    <a type="button" className="btn btn-default"
                       style={{backgroundColor:'#ffffff',padding:'2px 5px',fontSize:'13px',color:'#666'}}
                       onClick={this.onDataInfoClick} ref="dataInfoBtn">数据信息
                    </a>
                </div>
                <div className="style-tab" ref="styleTabBody" style={{display:'block'}}>
                    <div className="style-tab-content" style={tabContentStyle}>
                        <StyleInfo solution={solution} layerStyle={layerStyle} actions={actions}/>
                    </div>
                </div>
                <div className="style-tab" ref="dataTabBody" style={{display:'none'}}>
                    <div className="style-tab-content" style={tabContentStyle}>
                        <SourceInfo solution={solution} layerStyle={layerStyle} layerSourceAttrs={layerSourceAttrs} actions={actions}/>
                    </div>
                </div>
            </div>
        </div>
    }
}

StyleBox.propTypes = {
    solution: PropTypes.object.isRequired,
    layerStyle: PropTypes.object.isRequired
}