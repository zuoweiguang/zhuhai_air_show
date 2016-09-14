import React, { Component, PropTypes } from 'react'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import '../../scss/studio/studioHeader.scss'
import * as MenuLinkActions from '../../actions/index/MenuLinkAction'
import * as AuthActions from '../../actions/auth/AuthAction'
import SolutionHeader from '../../components/studio/SolutionHeader'

class StudioHeaderContainer extends Component {
    constructor(props, context) {
        super(props, context)
    }

    render() {
        const { indexMenu,user,menuActions,userActions} = this.props
        return (
            <div>
                <div className="studio-page-header-container">
                    <SolutionHeader indexMenu={indexMenu} user={user} menuActions={menuActions}
                                    userActions={userActions}/>
                </div>
            </div>
        )
    }
}

StudioHeaderContainer.propTypes = {
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
)(StudioHeaderContainer)
