import React, { Component, PropTypes } from 'react'
import { browserHistory } from 'react-router'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import {APP_ROOT_NAME} from '../../config/appConfig'
import authService from '../../middleware/service/authService'
import Base64Util from '../../utils/Base64Util'
import * as AuthAction from '../../actions/auth/AuthAction'

class LoginBodyContainer extends Component {
    constructor(props, context) {
        super(props, context)
        this.state = {error: false, errmsg: ""};
        this.handleSubmit = this.handleSubmit.bind(this)
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
                    password: Base64Util.encode(password) || '',
                    email: '',
                    token: userdata.username
                }
                this.setState({error: false, errmsg: "登录成功"})
                this.props.actions.userLoginAction(user)
                const { location } = this.props
                if (location.state && location.state.nextPathname) {
                    browserHistory.push(location.state.nextPathname)
                } else {
                    browserHistory.push(APP_ROOT_NAME)
                }
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
        const { user,actions} = this.props
        const bodyHeight = document.body.clientHeight - 80;
        const bodyStyle = {
            height: bodyHeight + 'px'
        }
        return (
            <div className="index-body" style={bodyStyle}>
                <center style={{paddingTop:'180px'}}>
                    <form className="form-horizontal " role="form" onSubmit={this.handleSubmit}
                          style={{width: '300px'}}>
                        <div className="form-group control-group">
                            <input type="text" ref="username" className="form-control input-xlarge" placeholder="用户名"
                                   name="username" defaultValue={user.username}/>
                        </div>
                        <div className="form-group control-group">
                            <input type="password" ref="password" className="form-control input-xlarge" placeholder="密码"
                                   name="password" defaultValue={Base64Util.decode(user.password)}/>
                        </div>
                        <div className="form-group">
                            <button type="submit" className="form-control btn  btn-primary btn-block">
                                登录
                            </button>
                        </div>
                        <p>{this.state.errmsg}</p>
                    </form>
                </center>
                <footer className="body-footer">
                    <small>Copyright©2016 北京四维图新科技股份有限公司</small>
                </footer>
            </div>
        )
    }
}

LoginBodyContainer.propTypes = {
    user: PropTypes.object.isRequired,
    actions: PropTypes.object.isRequired
}

function mapStateToProps(state) {
    return {
        user: state.user
    }
}

function mapDispatchToProps(dispatch) {
    return {
        actions: bindActionCreators(AuthAction, dispatch)
    }
}

export default connect(
    mapStateToProps,
    mapDispatchToProps
)(LoginBodyContainer)

