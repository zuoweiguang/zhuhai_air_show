import React, { Component,PropTypes} from 'react'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import DataList from '../../components/studio/DataList'
import * as StudioActions from '../../actions/studio/StudioAction'

class StudioDataContainer extends Component {
    constructor(props, context) {
        super(props, context)
    }

    render() {
        const {studioActions} = this.props
        return (
            <DataList studioActions={studioActions}/>
        )
    }
}

StudioDataContainer.propTypes = {studioActions: PropTypes.object.isRequired}

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
)(StudioDataContainer)
