import React, { Component,PropTypes} from 'react'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import HelpBox from '../../components/studio/HelpBox'
import * as StudioActions from '../../actions/studio/StudioAction'

class StudioHelpContainer extends Component {
    constructor(props, context) {
        super(props, context)
    }

    render() {
        const {studioActions} = this.props
        return (
            <HelpBox studioActions={studioActions}/>
        )
    }
}

StudioHelpContainer.propTypes = {studioActions: PropTypes.object.isRequired}

function mapStateToProps(state) {
    return {}
}

function mapDispatchToProps(dispatch) {
    return {
        studioActions: bindActionCreators(StudioActions, dispatch)
    }
}

export default connect(
    mapStateToProps,
    mapDispatchToProps
)(StudioHelpContainer)
