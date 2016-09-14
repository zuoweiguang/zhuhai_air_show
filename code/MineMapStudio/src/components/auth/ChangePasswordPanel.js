import React, { Component, PropTypes } from 'react'
import {APP_ROOT_NAME,REGISTER_ROOT_NAME,FORGET_PASSWORD} from '../../config/appConfig'

export default class ChangePasswordPanel extends Component {
    constructor(props, context) {
        super(props, context)

    }



    render(){
        const {forgetpw,actions} = this.props
        const bodyHeight = document.body.clientHeight - 80
        const bodyStyle = {
            height: bodyHeight + 'px'
        }
        return(
            <div className="changepw-big-box"style={bodyStyle}>
                <div className="changepw-box">

                    <form className="changepw-form">
                        <div className="form-group">
                            <label>Password</label>
                            <input type="password" className="form-control" id="exampleInputPassword1" placeholder="Password"/>
                        </div>
                        <div className="form-group">
                            <label >Password</label>
                            <input type="password" className="form-control" id="exampleInputPassword1" placeholder="Password"/>
                        </div>
                        <div  className="btn-group forgetpwbutton-group" role="group">
                        <button type="button" className="btn btn-default changepwbutton" >设置新密码</button>
                        </div>
                    </form>
                </div>
            </div>

        )
    }
}
