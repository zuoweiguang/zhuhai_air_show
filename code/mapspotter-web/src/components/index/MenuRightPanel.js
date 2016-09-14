import React, { Component, PropTypes } from 'react'
import {Button,Glyphicon} from 'react-bootstrap'
import { browserHistory } from 'react-router'
import {APP_ROOT_NAME} from '../../config/appConfig'

export class MenuRightPanel extends Component {
    constructor(props, context) {
        super(props, context)
        this.handleLogout = this.handleLogout.bind(this);
    }

    handleLogout() {
        this.props.actions.userLogoutAction()
        browserHistory.push(APP_ROOT_NAME+"login")
    }

    render() {
        const { user,actions} = this.props
        const hidestyle = {
            display:'none'
        }
        const labelStyle = {
            color: '#cccccc',
            cursor: 'pointer'
        }
        return <div className="header-right-menu">
            <div style={hidestyle}>
                <button type="button" className="btn btn-info" onClick="onIndexUserRegisterClick()">注册</button>
                <button type="button" className="btn btn-default" onClick="onIndexUserLoginClick()">登录</button>
            </div>
            <div>
                <label style={labelStyle}><Glyphicon glyph="user"/>&nbsp;<span ref="curUserNameSpan">{user.username}</span>&nbsp;&nbsp;</label>
                <label style={labelStyle}><span className="glyphicon glyphicon-log-out" title="退出" onClick={this.handleLogout}></span></label>
            </div>
        </div>
    }
}
