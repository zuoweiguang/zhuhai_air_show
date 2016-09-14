import React, { Component, PropTypes } from 'react'
import {Button,Glyphicon} from 'react-bootstrap'
import {Link,browserHistory} from 'react-router'
import Modal from 'react-modal'
import {APP_ROOT_NAME} from '../../config/appConfig'
import UserTool from '../../utils/UserTool'
import solutionService from '../../middleware/service/solutionService'
import warehouseService from '../../middleware/service/warehouseService'
import warehouseHandler from '../../middleware/handler/warehouseHandler'

export default class SolutionAddPanel extends Component {
    constructor(props, context) {
        super(props, context)

        this.state = {modalIsOpen: false}
        this.openModal = this.openModal.bind(this)
        this.closeModal = this.closeModal.bind(this)
        this.onTemplateClick = this.onTemplateClick.bind(this)
        this.solutionAdd = this.solutionAdd.bind(this)
        this.onOkClick = this.onOkClick.bind(this)
        this.linkToOperateWindow = this.linkToOperateWindow.bind(this)
    }

    solutionAdd(data) {
        solutionService.addSolution(data).then(response =>
            response.json()
        ).then(res => {
            if (res.errcode == 0 && res.data) {
                let solutionId = res.data.id
                warehouseService.getDefaultStyleInfo('background').then(response =>
                    response.json()
                ).then(res => {
                    if (res.errcode == 0 && res.data) {
                        return res.data
                    } else {
                        console.log(res)
                        this.linkToOperateWindow(solutionId)
                    }
                }).then(defaultStyle => {
                    return warehouseHandler.genSolutionBackGroundLayer(solutionId, defaultStyle)
                }).then(layer => solutionService.addLayer(layer).then(response => {
                        response.json()
                        this.linkToOperateWindow(solutionId)
                    }
                )).catch((e)=> {
                        console.log(e)
                        this.linkToOperateWindow(solutionId)
                    }
                )
            }
        }).catch((e)=>
            console.log(e)
        )
    }

    linkToOperateWindow(id) {
        browserHistory.push(`${APP_ROOT_NAME}operate/${id}`)
    }

    onOkClick(e) {
        e.preventDefault()
        let template = this.props.templates.filter(template =>
            template.selected === true
        )[0]
        let date = new Date()
        let createTime = date.getFullYear() + "-" + (date.getMonth() + 1) + "-" + date.getDate()
        let data = {
            name: this.refs.solutionNewName.value || template.name,
            desc: this.refs.solutionNewDesc.value || template.desc,
            icon: template.icon,
            createTime: createTime,
            lastUpdateTime: createTime,
            userID: UserTool.getLoginUserId(),
            template: template.id,
            center: [116.46, 39.92],
            zoom: 15,
            minZoom: 3,
            maxZoom: 19,
            bearing: 0,
            pitch: 60
        }

        this.solutionAdd(data)
        this.setState({modalIsOpen: false})
    }

    onTemplateClick(e) {
        this.props.solutionTemplateSelectAction(e.target.id)
    }

    openModal(e) {
        this.setState({modalIsOpen: true})
    }

    closeModal(e) {
        this.setState({modalIsOpen: false})
    }

    componentDidMount() {
        this.props.solutionTemplateListAction()
    }

    render() {
        const {templates,solutionAddAction,solutionTemplateSelectAction,solutionTemplateListAction} = this.props

        const templatePanels = templates.map((template, i) => {
            return <div className="col-sm-4 col-md-4" key={template.id}>
                <div className="template-box">
                    <div className="template-img">
                        <img height={120} src={template.icon} alt="Image"
                             className="img-icon"/>
                    </div>
                    <div className="template-cover" style={{display:template.selected?'block':'none'}}>
                        <center>
                            <div className="img-cover">
                                <div className="img-inner">
                                    <span className="glyphicon glyphicon-ok"></span>
                                </div>
                            </div>
                        </center>
                    </div>
                    <div className="template-info">
                        <div className="template-name">{template.name}</div>
                        <div className="template-desc">{template.desc}</div>
                    </div>
                    <div className="template-top" onClick={this.onTemplateClick} id={template.id}></div>
                </div>
            </div>
        })
        return <div className="col-sm-12 col-md-12">
            <div className="solution-state"><span className="label-text">Hi,&nbsp;&nbsp;</span><span
                className="label-name">{UserTool.getLoginUserName() || ""}</span>
            </div>
            <div className="solution-addpanel-container">
                <div className="btn btn-solution"
                     onClick={this.openModal}>
                    <span className="glyphicon glyphicon-plus-sign icon-solution"></span>&nbsp;&nbsp;<span>新增方案</span>
                </div>
            </div>
            <Modal className="modal-dialog modal-lg"
                   overlayClassName="modal-overlay-container"
                   isOpen={this.state.modalIsOpen}
                   onRequestClose={this.closeModal}>
                <div className="modal-content solution-modal-content">
                    <div className="modal-header">
                        <button type="button" className="close" onClick={this.closeModal}>
                            <span aria-hidden="true">&times;</span>
                            <span className="sr-only">Close</span>
                        </button>
                        <h4 className="modal-title">新增方案</h4>
                    </div>
                    <div className="modal-body">
                        <div className="modal-body-box">
                            <div className="input-box"><input ref="solutionNewName" type="text"
                                                              className="form-control" placeholder="名称"/></div>
                            <div className="input-box" style={{marginTop:'10px'}}><textarea ref="solutionNewDesc"
                                                                                            rows="2"
                                                                                            className="form-control"
                                                                                            placeholder="描述"/></div>
                            <div className="template-list">
                                {templatePanels}
                            </div>
                        </div>
                    </div>
                    <div className="modal-footer">
                        <button type="button" className="btn btn-default" onClick={this.closeModal}>取消</button>
                        <button type="button" className="btn btn-primary" onClick={this.onOkClick}>确定</button>
                    </div>
                </div>
            </Modal>
        </div>
    }
}