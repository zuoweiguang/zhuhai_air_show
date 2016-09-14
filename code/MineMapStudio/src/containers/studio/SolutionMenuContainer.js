import React, { Component, PropTypes } from 'react'
import {Link} from 'react-router'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import * as studioActions from '../../actions/studio/StudioAction'
import SolutionMenuBox from '../../components/studio/SolutionMenuBox'

class SolutionMenuContainer extends Component {
    constructor(props, context) {
        super(props, context)
    }

    render() {
        const { studioMenus,studioActions} = this.props
        const menus = studioMenus.map(link =>
            <SolutionMenuBox key={link.id} link={link} {...studioActions}/>)
        return <div className="solution-body-menu">
            <div className="solution-menu-list">
                {menus}
            </div>
        </div>
    }
}

SolutionMenuContainer.propTypes = {
    studioMenus: PropTypes.array.isRequired,
    studioActions: PropTypes.object.isRequired
}

function mapStateToProps(state) {
    return {
        studioMenus: state.studioMenus,
    }
}

function mapDispatchToProps(dispatch) {
    return {
        studioActions: bindActionCreators(studioActions, dispatch)
    }
}

export default connect(
    mapStateToProps,
    mapDispatchToProps
)(SolutionMenuContainer)