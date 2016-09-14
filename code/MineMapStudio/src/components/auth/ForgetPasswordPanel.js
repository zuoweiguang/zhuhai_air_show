import React, { Component, PropTypes } from 'react'
import {browserHistory} from 'react-router'
import {APP_ROOT_NAME} from '../../config/appConfig'

export default class ForgetPasswordPanel extends Component {
    constructor(props, context) {
        super(props, context)
        this.handleSubmit = this.handleSubmit.bind(this)
    }
    handleSubmit(e){
    this.props.actions.ChangePasswordAction(false)

    }


render(){
    const {forgetpw,actions} = this.props
    const bodyHeight = document.body.clientHeight - 80;
    const bodyStyle = {
        height: bodyHeight + 'px'
    }
    return(
        <div className="forgetpw-big-box"style={bodyStyle}>
        <div className="forgetpw-box">

            <form className="forgetpw-form" role="form" >
                <div className="form-group">
                    <label  className="forgetpwtext">忘记密码</label>
                    <label className="forgetpwintro">温馨提示：请输入注册所用邮箱，找回密码！</label>
                    <input type="email" className="form-control forgetInputEmail1" placeholder="请输入注册邮箱"/>
                </div>
                <div  className="btn-group forgetpwbutton-group" role="group">
                <button type="button" className="btn btn-default forgetpwbutton" onClick={this.handleSubmit}>修改密码</button>
                 </div>
                </form>
        </div>
            </div>

    )
}
}