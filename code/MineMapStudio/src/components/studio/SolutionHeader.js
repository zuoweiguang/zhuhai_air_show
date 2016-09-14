import React, { Component, PropTypes } from 'react'
import {Link,browserHistory} from 'react-router'
import {APP_ROOT_NAME} from '../../config/appConfig'
import {REGISTER_ROOT_NAME} from '../../config/appConfig'
import UserTool from '../../utils/UserTool'
import AuthUtil from '../../utils/AuthUtil'
import LoginPanel from '../auth/LoginPanel'

export default class SolutionHeader extends Component {
    constructor(props, context) {
        super(props, context)
        this.handleLogout = this.handleLogout.bind(this)
        this.openLoginModal = this.openLoginModal.bind(this)
    }

    handleLogout() {
        this.props.userActions.userLogoutAction()
        browserHistory.push(APP_ROOT_NAME +"studio")
    }

    openLoginModal(e) {
        this.props.userActions.loginModelShowAction(true)
    }

    render() {

        const { indexMenu,user,menuActions,userActions} = this.props
        const {indexMenuClickAction} = menuActions
        let popuTopStyleBool = true
        let userstate
        if (AuthUtil.isLogin()) {
            userstate = (
                <div className="userinfo-box">
                    <div>
                        <label className="userinfo-label"><span className="glyphicon glyphicon-user"></span>&nbsp;<span
                            ref="curUserNameSpan">{user.username}</span>&nbsp;&nbsp;</label>
                        <label className="userinfo-label"><span className="glyphicon glyphicon-log-out" title="退出"
                                                                onClick={this.handleLogout}></span></label>
                    </div>
                </div>

            )
        } else {
            userstate = (
                <div className="login-register-bar">
                <span className="login-navbar" onClick={this.openLoginModal}>登录</span>
            <span className="login-navbar-middle">/</span>
            <Link
                to={APP_ROOT_NAME+REGISTER_ROOT_NAME} title="注册">
                <span className="register-navbar" >注册</span>
                </Link>
                </div>
            )
        }
        return (
            <div className="studio-navbar-header">
                <Link
                    to={APP_ROOT_NAME} onClick={() => indexMenuClickAction('index')} title="返回首页">
                    <div className="back-box" style={{display:'none'}}>
                        <div className="icon-back"/>
                    </div>
                </Link>
                <div className="content-box">
                    <span className="content-info">欢迎来到MineMap工作台</span><span className="content-desc">&nbsp;&nbsp;&nbsp;&nbsp;内测&nbsp;v0.1</span>
                </div>
                {userstate}
            </div>

        )
    }
}

SolutionHeader.propTypes = {
    indexMenu: PropTypes.object.isRequired,
    user: PropTypes.object.isRequired,
    menuActions: PropTypes.object.isRequired,
    userActions: PropTypes.object.isRequired,
}

