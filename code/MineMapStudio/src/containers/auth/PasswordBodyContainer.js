import React, { Component, PropTypes } from 'react'
import * as AuthAction from '../../actions/auth/AuthAction'
import { connect } from 'react-redux'
import { bindActionCreators } from 'redux'
import '../../scss/auth/login.scss'
import ForgetPasswordPanel from '../../components/auth/ForgetPasswordPanel'
import ChangePasswordPanel from '../../components/auth/ChangePasswordPanel'
export default class PasswordBodyContainer extends Component {
    constructor(props, context) {
        super(props, context)

    }

    render() {
        const {forgetpw,actions} = this.props
        return (
            <div >
                {forgetpw.passwordstate?<ForgetPasswordPanel  actions={actions} forgetpw={forgetpw}/>:
                    <ChangePasswordPanel  actions={actions} forgetpw={forgetpw} />}
            </div>
        )
    }
}

PasswordBodyContainer.propTypes = {
    forgetpw: PropTypes.object.isRequired,
    actions: PropTypes.object.isRequired,
}

function mapStateToProps(state) {
    return {
        forgetpw:state.forgetpw
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
)(PasswordBodyContainer)