import React, { Component, PropTypes } from 'react'
import authService from '../../middleware/service/authService'
import {Link,browserHistory} from 'react-router'
import Base64Util from '../../utils/Base64Util'
import CookieTool from '../../utils/CookieTool'
import {APP_ROOT_NAME,REGISTER_ROOT_NAME,FORGET_PASSWORD} from '../../config/appConfig'

export default class LoginPanel extends Component {
    constructor(props, context) {
        super(props, context)
        this.state = {error: false, errmsg: ""}
        this.closeModal = this.closeModal.bind(this)
        this.linkToRegister = this.linkToRegister.bind(this)
        this.handleSubmit = this.handleSubmit.bind(this)
        this.forgetpassword = this.forgetpassword.bind(this)
    }
    componentWillReceiveProps (){
        this.setState({error: false, errmsg: ""})
        this.refs.username.value=""
        this.refs.password.value=""
    }
    closeModal(e) {
        this.props.actions.loginModelShowAction(false)
    }

    linkToRegister(e) {
        browserHistory.push(`${APP_ROOT_NAME}register`)
        this.props.actions.loginModelShowAction(false)
    }
    forgetpassword(e){
        this.props.actions.loginModelShowAction(false)

    }
    handleSubmit(e) {

        e.preventDefault()
        const username = this.refs.username.value
        const password = this.refs.password.value
        authService.userLogin({username: username, password: password}).then(response =>
            response.json()
        ).then(res => {
            if (res.errcode == 0 && res.data) {
                let userdata = res.data.user
                let user = {
                    id: userdata.id,
                    username: userdata.username,
                    password: password || '',
                    email: '',
                    token: userdata.username,
                    autolchevalue:this.refs.rememberpasswordcheck.checked||false
                }
                this.setState({error: false, errmsg: "登录成功"})
                this.props.actions.userLoginAction(user)
                const { location } = this.props
                this.props.actions.loginModelShowAction(false)
                browserHistory.push(APP_ROOT_NAME)

            } else {
                console.log(res)
                this.setState({error: true, errmsg: '登录失败:' + res.errmsg})
            }
        }).catch((e)=> {
                console.log(e)
                this.setState({error: true, errmsg: '登录失败:' + e})
            }
        )
    }


    render() {
        const {user,actions,login} = this.props

        return (
            <div className="login-box">
                <div className="popup" style={{display:login.loginModalIsOpexn?'block':'none'}}>
                    <div className="top_nav">
                        <div >
                            <span>登录</span>
                            <a className="close" onClick={this.closeModal}></a>
                        </div>
                    </div>
                    <div className="login-err-box">{this.state.errmsg}</div>
                    <form className="form-content " role="form" onSubmit={this.handleSubmit}>
                        <div className="input-group login-input">
                            <div className="input-group-addon">
                                <span className="glyphicon glyphicon-user"></span>
                            </div>
                            <input type="text" ref="username" className="form-control login-form-control " placeholder="请输入用户名或邮箱地址"
                                   defaultValue={user.username}/>
                        </div>
                        <div className="input-group login-input">
                            <div className="input-group-addon">
                                <span className="glyphicon glyphicon-lock"></span>
                            </div>
                            <input type="password" ref="password" className="form-control login-form-control "
                                   placeholder="请输入密码" defaultValue={user.password}/>
                        </div>

                        <div className="automaticlogin-checkbox">
                            <input type="checkbox" name="automaticlogin" ref="rememberpasswordcheck"  defaultChecked={user.autolchevalue||false} /><span>下次自动登录</span>
                        </div>

                        <div className="btn-group login-button-box" role="group">
                            <button type="submit" className="btn btn-default login-button">登录</button>
                        </div>
                    </form>
                    <div className="forget-password-reg">

                        <Link
                            to={APP_ROOT_NAME+FORGET_PASSWORD} onClick={this.forgetpassword} >
                            <span className="forget-password" >忘记密码</span>
                        </Link>

                        <span className="login-reg" onClick={this.linkToRegister}>免费注册</span>
                    </div>
                </div>
                <div className="gray" style={{display:login.loginModalIsOpexn?'block':'none'}}></div>
            </div>
        )
    }
}
