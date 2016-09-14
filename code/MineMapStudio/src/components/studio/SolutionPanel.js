import React, { Component, PropTypes } from 'react'
import {Link} from 'react-router'
import {APP_ROOT_NAME} from '../../config/appConfig'
import solutionService from '../../middleware/service/solutionService'

export default class SolutionPanel extends Component {
    constructor(props, context) {
        super(props, context)
        this.state = {isEdit: false}
        this.imgMouseOver = this.imgMouseOver.bind(this)
        this.imgMouseOut = this.imgMouseOut.bind(this)
        this.onSolutionDelClick = this.onSolutionDelClick.bind(this)
        this.onSolutionEditClick = this.onSolutionEditClick.bind(this)
        this.onSolutionSaveClick = this.onSolutionSaveClick.bind(this)
        this.onSolutionCancelClick = this.onSolutionCancelClick.bind(this)
        this.onSolutionOperateClick = this.onSolutionOperateClick.bind(this)
        this.onSolutionCopyClick = this.onSolutionCopyClick.bind(this)
    }

    imgMouseOver(e) {
        this.refs.solutionCover.style.display = 'block'
    }

    imgMouseOut(e) {
        this.refs.solutionCover.style.display = 'none'
    }

    onSolutionDelClick(e) {
        e.preventDefault()
        let id = this.props.solution.id
        this.props.solutionDelAction(id)
    }

    onSolutionEditClick(e) {
        e.preventDefault()
        this.setState({isEdit: true})
    }

    onSolutionSaveClick(e) {
        e.preventDefault()
        let id = this.props.solution.id
        let name = this.refs.solutionName.value || this.props.solution.name
        let desc = this.refs.solutionDesc.value
        let date = new Date()
        let lastUpdateTime = date.getFullYear() + "-" + (date.getMonth() + 1) + "-" + date.getDate()
        let data = {id: id, name: name, desc: desc, lastUpdateTime: lastUpdateTime}
        this.props.solutionModifyAction(data)
        this.setState({isEdit: false})
    }

    onSolutionCancelClick(e) {
        e.preventDefault()
        this.setState({isEdit: false})
    }

    onSolutionOperateClick(e) {
        let id = this.props.solution.id
        let date = new Date()
        let lastUpdateTime = date.getFullYear() + "-" + (date.getMonth() + 1) + "-" + date.getDate()
        let data = {id: id, lastUpdateTime: lastUpdateTime}
        this.props.solutionModifyAction(data)
        this.props.solutionEditAction(id)
        let reg = /[_]{1}[d]{1}[i]{1}[s]{1}$/
        if(this.props.solution.name.match(reg)){
            this.props.displayInterfaceRouteAction("display")
        }else{
            this.props.displayInterfaceRouteAction("operate")
        }

    }

    onSolutionCopyClick(e) {
        e.preventDefault()
        const srcSolution = this.props.solution
        let date = new Date()
        let createTime = date.getFullYear() + "-" + (date.getMonth() + 1) + "-" + date.getDate()
        let lastUpdateTime = date.getFullYear() + "-" + (date.getMonth() + 1) + "-" + date.getDate()
        let copySolution = Object.assign({}, srcSolution, {
            name: srcSolution.name + '_copy',
            createTime: createTime,
            lastUpdateTime: lastUpdateTime
        })
        delete copySolution.id
        solutionService.addSolution(copySolution).then(response =>
            response.json()
        ).then(res => {
            if (res.errcode == 0 && res.data) {
                let soluID = res.data.id
                copySolution.id = soluID
                this.props.solutionCopyAction(srcSolution, copySolution)
            }
        }).catch((e)=>
            console.log(e)
        )
    }

    render() {
        const { solution,solutionListAction,solutionAddAction,solutionDelAction,solutionEditAction,solutionModifyAction,displayInterfaceRouteAction} = this.props
        const solutionBtnGroup = this.state.isEdit ? <div className="solution-icon"><span
            className="glyphicon glyphicon-ok" onClick={this.onSolutionSaveClick}
            title="保存"></span>&nbsp;&nbsp;<span
            className="glyphicon glyphicon-remove"
            title="取消" onClick={this.onSolutionCancelClick}></span></div> : <div className="solution-icon"><span
            className="glyphicon glyphicon-edit" onClick={this.onSolutionEditClick}
            title="编辑"></span>&nbsp;&nbsp;<span
            className="glyphicon glyphicon-trash"
            title="删除" onClick={this.onSolutionDelClick}></span></div>
        const solutionNameDiv = this.state.isEdit ?
            <div className="solution-header-input"><input ref="solutionName" type="text"
                                                          className="form-control"
                                                          defaultValue={solution.name}/></div> :
            <div className="solution-header-label">{solution.name}</div>
        const solutionDescDiv = this.state.isEdit ?
            <div className="solution-content-input"><textarea ref="solutionDesc" rows="2"
                                                              className="form-control"
                                                              defaultValue={solution.desc}/></div> :
            <div className="solution-content-more">{solution.desc || '点击右上角“编辑”按钮添加描述信息'}</div>
        return <div className="solution-panel-container">
            <div className="solution-cover" ref="solutionCover" onMouseOut={this.imgMouseOut}>
                <div className="solution-inner" onMouseOver={this.imgMouseOver}/>
                <div className="solution-action" onMouseOver={this.imgMouseOver}>
                    <Link
                        to={`${APP_ROOT_NAME}operate/${solution.id}`}
                        onClick={this.onSolutionOperateClick}><span>进入</span></Link>
                </div>
            </div>
            <div className="solution-panel">
                <img width={150} height={150} src={solution.icon||"app/images/studio/solution/1.png"} alt="Image"
                     className="img-circle solution-image" onMouseOver={this.imgMouseOver}/>
                <div className="solution-content-box">
                    <div className="solution-header">
                        {solutionNameDiv}
                        {solutionBtnGroup}
                    </div>
                    <div className="solution-date">
                        <span className="solution-title">创建时间：</span>
                        <span className="solution-content">{solution.createTime}</span>
                        <span>&nbsp;&nbsp;&nbsp;&nbsp;</span>
                        <span className="solution-title">更新时间：</span>
                        <span className="solution-content">{solution.lastUpdateTime}</span>
                    </div>
                    <div className="solution-desc">
                        {solutionDescDiv}
                    </div>
                    <div className="solution-btn">
                        <div className="solution-btn-detail">
                            <span onClick={this.onSolutionCopyClick}>复制</span> &nbsp;&nbsp;
                            <Link
                                to={`${APP_ROOT_NAME}operate/${solution.id}`}
                                onClick={this.onSolutionOperateClick}><span>进入>></span></Link>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    }
}

SolutionPanel.propTypes = {
    solution: PropTypes.object.isRequired
}