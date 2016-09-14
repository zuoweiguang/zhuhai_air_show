import React, { Component, PropTypes } from 'react'
import * as AuthAction from '../../actions/auth/AuthAction'
import '../../scss/index/register.scss'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import {APP_ROOT_NAME} from '../../config/appConfig'
import RegisterSuccessPanel from '../../components/index/RegisterSuccessPanel'
import RegisterPanel from '../../components/index/RegisterPanel'
export default class RegisterBodyContainer extends Component {
    constructor(props, context) {
        super(props, context)
    }
    render() {
        const {user,actions,register} = this.props
        return (
            <div>
               {register.registerBodyShow?<RegisterPanel actions={actions} user={user} register={register} />:<RegisterSuccessPanel actions={actions} user={user} />}
            </div>
        )
    }
}


RegisterBodyContainer.propTypes = {
    user: PropTypes.object.isRequired,
    actions: PropTypes.object.isRequired,
    register: PropTypes.object.isRequired

}

function mapStateToProps(state) {
    return {
        user: state.user,
        register:state.register

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
)(RegisterBodyContainer)
