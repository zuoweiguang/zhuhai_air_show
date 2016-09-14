import React, { Component,PropTypes} from 'react'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import SolutionList from '../../components/studio/SolutionList'
import * as SolutionActions from '../../actions/studio/SolutionAction'
import * as StudioActions from '../../actions/studio/StudioAction'

class StudioSolutionContainer extends Component {
    constructor(props, context) {
        super(props, context)
    }

    render() {
        const { solutions,templates,solutionActions,studioActions} = this.props
        return (
            <SolutionList solutions={solutions} templates={templates} actions={solutionActions}
                          studioActions={studioActions}/>
        )
    }
}

StudioSolutionContainer.propTypes = {
    solutions: PropTypes.array.isRequired,
    templates: PropTypes.array.isRequired,
    solutionActions: PropTypes.object.isRequired,
    studioActions: PropTypes.object.isRequired
}

function mapStateToProps(state) {
    return {
        solutions: state.solutions,
        templates: state.templates
    }
}

function mapDispatchToProps(dispatch) {
    return {
        solutionActions: bindActionCreators(SolutionActions, dispatch),
        studioActions: bindActionCreators(StudioActions, dispatch)
    }
}

export default connect(
    mapStateToProps,
    mapDispatchToProps
)(StudioSolutionContainer)
