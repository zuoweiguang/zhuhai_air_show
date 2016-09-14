/**
 * Created by hanyequn on 2016/8/23.
 */
import React, { Component, PropTypes } from 'react'
import * as AuthAction from '../../actions/auth/AuthAction'
import LoginPanel from '../../components/auth/LoginPanel'
import { connect } from 'react-redux'
import { bindActionCreators } from 'redux'
import '../../scss/auth/login.scss'
export default class LoginContainer extends Component{
    constructor(props, context) {
        super(props, context)
    }
    render() {
        const {user,actions,login} = this.props
        return(
            <LoginPanel actions={actions} user={user} login={login}/>
        )
    }



}


LoginContainer.propTypes = {
    user: PropTypes.object.isRequired,
    actions: PropTypes.object.isRequired,
    login: PropTypes.object.isRequired
}

function mapStateToProps(state) {
    return {
        user: state.user,
        login:state.login
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
)(LoginContainer)