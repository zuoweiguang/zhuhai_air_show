import React, { Component, PropTypes } from 'react'
import {Link} from 'react-router'
import LayerList from './LayerList'

export default class LayersBox extends Component {
    constructor(props, context) {
        super(props, context)
        this.state = {isSolutionNameEdit: false}
        this.onSolutionNameEditClick = this.onSolutionNameEditClick.bind(this)
        this.onSolutionNameSaveClick = this.onSolutionNameSaveClick.bind(this)
        this.onSolutionNameCancelClick = this.onSolutionNameCancelClick.bind(this)
        this.onLayerAddClick = this.onLayerAddClick.bind(this)
        this.onUploadClick = this.onUploadClick.bind(this)
        this.onLayerDuplicateClick = this.onLayerDuplicateClick.bind(this)
    }

    onSolutionNameEditClick(e) {
        this.setState({isSolutionNameEdit: true})
        e.preventDefault()
    }

    onSolutionNameSaveClick(e) {
        e.preventDefault()
        let id = this.props.solution.id
        let name = this.refs.solutionName.value || this.props.solution.name
        let date = new Date()
        let lastUpdateTime = date.getFullYear() + "-" + (date.getMonth() + 1) + "-" + date.getDate()
        let data = {id: id, name: name, lastUpdateTime: lastUpdateTime}
        this.props.actions.solutionNameModifyAction(data)
        this.setState({isSolutionNameEdit: false})
    }

    onSolutionNameCancelClick(e) {
        e.preventDefault()
        this.setState({isSolutionNameEdit: false})
    }

    onLayerAddClick(e) {
        e.preventDefault()
        this.props.panelDisplayInfoAction('warehouse', 'block')
    }

    onUploadClick(e) {
        e.preventDefault()
        //this.props.panelDisplayInfoAction('upload', 'block')
    }

    onLayerDuplicateClick(e) {
        e.preventDefault()
        const layerStyle = this.props.layerStyle
        if (layerStyle && layerStyle.id) {

            let layers = this.props.solution.layers
            let datas = []
            for (let i = 0; i < layers.length; i++) {
                if (layers[i].zindex > layerStyle.zindex) {
                    datas.push({id: layers[i].id, zindex: Number.parseInt(layers[i].zindex) + 1})
                }
            }
            this.props.actions.solutionLayerEditZIndexAction(datas)

            let newLayerStyle = Object.assign({}, layerStyle, {
                zindex: Number.parseInt(layerStyle.zindex) + 1,
                name: layerStyle.name + "_copy"
            })
            this.props.actions.solutionLayerDuplicateAction(newLayerStyle)

        } else {
            alert('请选择需要复制的图层！')
        }
    }

    render() {
        const { solution,layerStyle,actions,menuToggleAction,panelDisplayInfoAction,mapActions} = this.props
        const solutionNameDiv = this.state.isSolutionNameEdit ?
            <div className="header-label-input"><input ref="solutionName" type="text"
                                                       className="form-control"
                                                       defaultValue={solution.name}/></div> :
            <div className="header-label">{solution.name}</div>
        const solutionHeaderBtnGroup = this.state.isSolutionNameEdit ? <div className="header-icon"><span
            className="glyphicon glyphicon-ok" onClick={this.onSolutionNameSaveClick}
            title="保存"></span></div> :
            <div className="header-icon"><span className="glyphicon glyphicon-edit"
                                               title="编辑" onClick={this.onSolutionNameEditClick}></span></div>

        return <div className="operate-layer-box">
            <div className="operate-layer-header">
                {solutionNameDiv}
                {solutionHeaderBtnGroup}
            </div>
            <div className="operate-layer-info">
                <div className="header-label">我的图层</div>
                <div className="header-icon-box copy-layer-icon" title="复制图层" onClick={this.onLayerDuplicateClick}/>
                <div className="header-icon-box add-layer-icon" title="添加图层" onClick={this.onLayerAddClick}/>
                <div className="header-icon-box upload-icon" title="上传数据" onClick={this.onUploadClick}/>
            </div>
            <LayerList layers={solution.layers || []} solutionId={solution.id} actions={actions} solution={solution}
                       mapActions={mapActions}
                       menuToggleAction={menuToggleAction} panelDisplayInfoAction={panelDisplayInfoAction}/>
        </div>
    }
}

LayersBox.propTypes = {
    solution: PropTypes.object.isRequired
}