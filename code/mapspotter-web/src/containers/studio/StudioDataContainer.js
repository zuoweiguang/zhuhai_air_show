import React, { Component,PropTypes} from 'react'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'

class StudioDataContainer extends Component {
    constructor(props, context) {
        super(props, context)
    }

    render() {
        return (
            <div></div>
        )
    }
}

StudioDataContainer.propTypes = {}

function mapStateToProps(state) {
    return {}
}

function mapDispatchToProps(dispatch) {
    return {}
}

export default connect(
    mapStateToProps,
    mapDispatchToProps
)(StudioDataContainer)
