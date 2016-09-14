import React, { Component, PropTypes } from 'react'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import '../scss/index/header.css'
import MenusContainer from './index/MenusContainer'
import * as MenuLinkActions from '../actions/index/MenuLinkAction'
import * as AuthActions from '../actions/auth/AuthAction'

class HeaderContainer extends Component {
    constructor(props, context) {
        super(props, context)
    }

    render() {
        const { indexMenu,user,menuActions,userActions} = this.props
        const { indexMenuLinks,indexMenuRoute} = indexMenu
        return (
            <div className="page-header-container">
                <MenusContainer indexMenu={indexMenu} user={user} menuActions={menuActions} userActions={userActions}/>
            </div>
        )
    }
}

HeaderContainer.propTypes = {
    indexMenu: PropTypes.object.isRequired,
    menuActions: PropTypes.object.isRequired
}

function mapStateToProps(state) {
    return {
        indexMenu: state.indexMenu,
        user: state.user
    }
}

function mapDispatchToProps(dispatch) {
    return {
        menuActions: bindActionCreators(MenuLinkActions, dispatch),
        userActions: bindActionCreators(AuthActions, dispatch)
    }
}

export default connect(
    mapStateToProps,
    mapDispatchToProps
)(HeaderContainer)
