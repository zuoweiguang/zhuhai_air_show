import React, { Component, PropTypes } from 'react'
import LoginPanel from '../../components/auth/LoginPanel'
import {APP_ROOT_NAME} from '../../config/appConfig'
export default class RegisterPanel extends Component {
    constructor(props, context) {
        super(props, context)
        this.state = {err: false, errmsg: ""}
        this.handleChange = this.handleChange.bind(this)
        this.loginModelShow = this.loginModelShow.bind(this)
        this.registerHandleClick = this.registerHandleClick.bind(this)
    }

    registerHandleClick(e) {
        this.props.actions.RegisterBodyShowAction(false)

    }

    loginModelShow(e) {
        this.props.actions.loginModelShowAction(true)
    }

    handleChange(e) {
        this.setState({err: !this.state.err})
        switch (this.state.err) {
            case false:
                this.setState({errmsg: "用户名已注册"})
                this.refs.warninglogo.className = "glyphicon glyphicon-remove warn-err err-color"
                this.refs.usernamewarningtext.className = "username-warning-text err-color"
                break
            case true:
                this.setState({errmsg: "用户名正确"})
                this.refs.warninglogo.className = "glyphicon glyphicon-ok warn-ok ok-color"
                this.refs.usernamewarningtext.className = "username-warning-text ok-color"
                break
        }
        console.log(this.refs.warninglogo.className)
    }

    render() {
        const {user,actions,register} = this.props
        const bodyHeight = document.body.clientHeight - 80;
        const bodyStyle = {
            height: bodyHeight + 'px'
        }

        return (
            <div className="register-big-box" style={bodyStyle}>
                <div className="register-box">
                    <span className="register-nav-text">请填写以下信息，即可完成注册</span>
                    <form className="form-content register-form-content">
                        <div className="input-group reg-input-box">
                            <div className="input-group-addon">
                                <span className="glyphicon glyphicon-user"></span>
                            </div>
                            <input onBlur={this.handleChange} type="text" ref="regusername"
                                   className="form-control reg-input"
                                   placeholder="请输入用户名"/>
                        </div>
                        <div className="reg-username-warning">
                            <span ref="warninglogo"></span>
                            <span ref="usernamewarningtext">{this.state.errmsg}</span>
                        </div>

                        <div className="input-group reg-input-box">
                            <div className="input-group-addon">
                                <span className="glyphicon glyphicon-envelope"></span>
                            </div>
                            <input type="text" ref="regemail" className="form-control reg-input" placeholder="请输入邮箱地址"/>
                        </div>
                        <span className="reg-email-warning"></span>

                        <div className="input-group reg-input-box">
                            <div className="input-group-addon">
                                <span className="glyphicon glyphicon-lock"></span>
                            </div>
                            <input type="text" ref="regpassword" className="form-control reg-input"
                                   placeholder="设置密码（6-16位字符）"/>
                            <span className="glyphicon glyphicon-eye-open form-control-feedback" aria-hidden="true"></span>
                        </div>

                        <span className="reg-password-warning"></span>



                        <div className="input-group reg-input-box">
                            <div className="input-group-addon">
                                <span className="glyphicon glyphicon-lock"></span>
                            </div>
                            <input type="text" ref="regdoublepassword" className="form-control reg-input"
                                   placeholder="请再次输入密码（6-16位字符）"/>
                        </div>
                        <span className="reg-doublepassword-warning"></span>
                        <div className="btn-group register-button-box" role="group">
                            <button type="button" className="btn btn-default register-button"
                                    onClick={this.registerHandleClick}>现在注册
                            </button>
                        </div>
                        <div className="regtologin-box">

                            已有账号，请直接<a href="javascript:void(0);" className="regtologin"
                                       onClick={this.loginModelShow}>登录</a>
                        </div>
                    </form>
                </div>
            </div>
        )

    }

}
